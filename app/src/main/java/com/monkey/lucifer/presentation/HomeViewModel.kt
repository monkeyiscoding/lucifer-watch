package com.monkey.lucifer.presentation

import com.monkey.lucifer.BuildConfig
import android.content.Context
import android.media.MediaRecorder
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monkey.lucifer.domain.ProjectStatus
import com.monkey.lucifer.services.FirebaseStorageService
import com.monkey.lucifer.services.GitHubService
import com.monkey.lucifer.services.WebsiteProjectStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import java.util.Locale
import java.util.UUID

class HomeViewModel : ViewModel() {
    private val _status = MutableStateFlow("Idle")
    val status: StateFlow<String> = _status

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText

    private val _aiText = MutableStateFlow("")
    val aiText: StateFlow<String> = _aiText

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    // Website Building States
    private val _showCommandPreview = MutableStateFlow(false)
    val showCommandPreview: StateFlow<Boolean> = _showCommandPreview

    private val _lastCommand = MutableStateFlow("")
    val lastCommand: StateFlow<String> = _lastCommand

    private val _parsedWebsiteName = MutableStateFlow("My Website")
    val parsedWebsiteName: StateFlow<String> = _parsedWebsiteName

    private val _isBuilding = MutableStateFlow(false)
    val isBuilding: StateFlow<Boolean> = _isBuilding

    private val _buildStatus = MutableStateFlow("")
    val buildStatus: StateFlow<String> = _buildStatus

    private val _completedSteps = MutableStateFlow<List<String>>(emptyList())
    val completedSteps: StateFlow<List<String>> = _completedSteps

    private val _buildError = MutableStateFlow<String?>(null)
    val buildError: StateFlow<String?> = _buildError

    private val _showQRCode = MutableStateFlow(false)
    val showQRCode: StateFlow<Boolean> = _showQRCode

    private val _qrCodeUrl = MutableStateFlow("")
    val qrCodeUrl: StateFlow<String> = _qrCodeUrl

    private val _isWebsiteActive = MutableStateFlow(false)
    val isWebsiteActive: StateFlow<Boolean> = _isWebsiteActive

    private val _shouldAutoStart = MutableStateFlow(false)
    val shouldAutoStart: StateFlow<Boolean> = _shouldAutoStart

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var tts: TextToSpeech? = null
    private var openAI: OpenAIService? = null
    private var wakeLock: PowerManager.WakeLock? = null

    // Silence detection for auto-stop
    private var silenceDetectionJob: Job? = null
    private val silenceThresholdMs = 200L // 200ms threshold (was 50ms, now with 10ms checks = faster detection)
    private val amplitudeCheckIntervalMs = 10L // Check every 10ms (was 20ms) - ultra frequent checks
    private val silenceAmplitudeThreshold = 200 // Very low threshold for detecting silence
    private val maxRecordingDurationMs = 10000L // Max 10 seconds of recording
    private var lastSpeechTimestamp = 0L    // Services
    private val storageService = FirebaseStorageService()
    private val gitHubService = GitHubService()
    private val projectStore = WebsiteProjectStore()
    private var settingsManager: SettingsManager? = null

    companion object {
        private const val TAG = "HomeViewModel"
    }

    fun initialize(context: Context) {
        // Initialize SettingsManager first
        if (settingsManager == null) {
            settingsManager = SettingsManager(context)
        }
        val apiKey = BuildConfig.OPENAI_API_KEY
        if (apiKey.isBlank()) {
            _error.value = "Missing OPENAI_API_KEY"
        } else {
            openAI = OpenAIService(apiKey)
        }

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }

        // Keep watch display awake
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                "lucifer:homepage_wakelock"
            )
            wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes
            Log.d(TAG, "WakeLock acquired")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock", e)
        }

        // Trigger auto-start on first load
        _shouldAutoStart.value = true
        Log.d(TAG, "Auto-start triggered on initialize")
    }

    fun autoStartRecording(context: Context) {
        // Reset the auto-start trigger after consuming it
        _shouldAutoStart.value = false
        if (_isRecording.value) return
        Log.d(TAG, "Auto-starting recording")
        startRecording(context)
    }

    fun resetForAutoStart() {
        // Trigger auto-start when app resumes from background
        if (!_isRecording.value) {
            _shouldAutoStart.value = true
            Log.d(TAG, "Auto-start reset for resume")
        }
    }

    fun startRecording(context: Context) {
        if (_isRecording.value) return

        // Stop any ongoing TTS speech
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to stop TTS", e)
        }

        _error.value = ""
        _recognizedText.value = ""
        _aiText.value = ""
        _status.value = "Listening..."

        outputFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")

        @Suppress("DEPRECATION")
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile?.absolutePath)
            prepare()
            start()
        }

        _isRecording.value = true

        // Initialize silence detection
        lastSpeechTimestamp = System.currentTimeMillis()
        startSilenceDetection()
    }

    fun stopRecordingAndProcess() {
        if (!_isRecording.value) return

        // Cancel silence detection
        silenceDetectionJob?.cancel()
        silenceDetectionJob = null

        _isRecording.value = false
        _status.value = "Transcribing..."

        try {
            recorder?.stop()
        } catch (_: Exception) {
        } finally {
            recorder?.release()
            recorder = null
        }

        val file = outputFile
        if (file == null || !file.exists()) {
            _status.value = "Idle"
            _error.value = "Recording failed"
            return
        }

        val api = openAI
        if (api == null) {
            _status.value = "Idle"
            _error.value = "OPENAI_API_KEY not set"
            return
        }

        viewModelScope.launch {
            val transcript = api.transcribeAudio(file)

            // FEATURE: Handle empty transcripts - don't show "You said: You"
            if (transcript.isBlank()) {
                _status.value = "Idle"
                _recognizedText.value = ""
                _error.value = "Could not detect speech"
                return@launch
            }

            _recognizedText.value = transcript
            _status.value = "Analyzing..."

            // Check if this is a website build command
            if (isWebsiteBuildCommand(transcript)) {
                // Show preview before building
                showCommandPreview(transcript)
                _status.value = "Idle"
                return@launch
            }

            // Otherwise, process as normal AI command
            val response = api.chatResponse(transcript)
            if (response.isBlank()) {
                _status.value = "Idle"
                _error.value = "No response from AI"
                return@launch
            }

            _aiText.value = response
            _status.value = "Speaking..."

            @Suppress("DEPRECATION")
            tts?.speak(response, TextToSpeech.QUEUE_FLUSH, null)

            _status.value = "Idle"
        }
    }

    /**
     * Ultra-fast speech detection: Stops immediately when REAL speech ends
     * Key: Use HIGH threshold (800+) to detect actual human speech vs noise
     * Uses "speech momentum" to ignore decay tail of speech
     */
    private fun startSilenceDetection() {
        silenceDetectionJob?.cancel()
        silenceDetectionJob = viewModelScope.launch {
            val recordingStartTime = System.currentTimeMillis()
            Log.d(TAG, "🎤 Recording STARTED - Listening...")

            var speechDetected = false
            var silenceStartTime = 0L
            var lastLoudSpeechTime = 0L // Track when we last heard REAL loud speech
            var maxAmplitudeSeen = 0
            var lastSilenceLogTime = 0L // Prevent spam logging

            while (isActive && _isRecording.value) {
                try {
                    val currentTime = System.currentTimeMillis()
                    val amplitude = recorder?.maxAmplitude ?: 0

                    // Track max amplitude for logging
                    if (amplitude > maxAmplitudeSeen) {
                        maxAmplitudeSeen = amplitude
                    }

                    // Check if max recording duration exceeded
                    if (currentTime - recordingStartTime >= maxRecordingDurationMs) {
                        Log.d(TAG, "⏰ MAX DURATION REACHED - Stopping")
                        stopRecordingAndProcess()
                        break
                    }

                    // THREE ZONES: Speech (>800), Real Silence (<250), Decay/Noise (250-800)
                    when {
                        // Zone 1: REAL SPEECH (amplitude > 800)
                        amplitude > 800 -> {
                            if (!speechDetected) {
                                speechDetected = true
                                silenceStartTime = 0L
                                lastSilenceLogTime = 0L
                                Log.d(TAG, "🔊 REAL SPEECH DETECTED! (Amplitude: $amplitude)")
                            } else {
                                // Still speaking - ALWAYS reset silence timer completely
                                silenceStartTime = 0L
                                lastSilenceLogTime = 0L
                                if (amplitude > 1000) {
                                    Log.d(TAG, "🔊 Loud speech: $amplitude")
                                }
                            }
                            // Update last loud speech timestamp
                            lastLoudSpeechTime = currentTime
                        }

                        // Zone 2: REAL SILENCE (amplitude < 250)
                        speechDetected && amplitude < 250 -> {
                            if (silenceStartTime == 0L) {
                                // First frame of real silence - START the timer
                                silenceStartTime = currentTime
                                lastSilenceLogTime = currentTime
                                Log.d(TAG, "🛑 REAL SILENCE DETECTED! Starting 150ms confirmation timer...")
                            } else {
                                // Silence is continuing - check duration
                                val silenceDuration = currentTime - silenceStartTime

                                // Only log every 500ms to avoid spam
                                if (currentTime - lastSilenceLogTime >= 500L) {
                                    Log.d(TAG, "⏳ Silence continuing: ${silenceDuration}ms (need 150ms total)...")
                                    lastSilenceLogTime = currentTime
                                }

                                // ⚡ STOP INSTANTLY after 150ms of REAL SILENCE
                                if (silenceDuration >= 150L) {
                                    Log.d(TAG, "✋ STOPPING! (Real silence confirmed: ${silenceDuration}ms, max speech amplitude: $maxAmplitudeSeen)")
                                    stopRecordingAndProcess()
                                    break
                                }
                            }
                        }

                        // Zone 3: DECAY/NOISE (250-800) = transitional, NOT real silence
                        // Only reset timer if we haven't heard loud speech recently
                        // This allows speech "tail-off" (vowel decay) to pass through
                        else -> {
                            val timeSinceLastSpeech = currentTime - lastLoudSpeechTime

                            // If we heard loud speech less than 200ms ago, this is likely speech decay
                            // DON'T reset the timer - allow it to continue counting to real silence
                            if (timeSinceLastSpeech < 200L) {
                                // This is speech tail-off, ignore it
                                if (silenceStartTime != 0L) {
                                    Log.d(TAG, "💨 Speech decay (amplitude $amplitude) - ignoring, timer continues...")
                                }
                            } else {
                                // We've waited 200ms+ since real speech, so this is actual noise
                                if (silenceStartTime != 0L) {
                                    Log.d(TAG, "↩️ Noise detected (amplitude $amplitude) - timer reset")
                                }
                                silenceStartTime = 0L
                                lastSilenceLogTime = 0L
                            }
                        }
                    }

                    delay(10L) // Check every 10ms

                } catch (e: Exception) {
                    Log.e(TAG, "Error in silence detection: ${e.message}")
                    break
                }
            }

            if (_isRecording.value) {
                Log.d(TAG, "Forcing stop - still recording")
                stopRecordingAndProcess()
            }
        }
    }

    /**
     * Check if transcript contains website building command
     */
    private fun isWebsiteBuildCommand(text: String): Boolean {
        // Only trigger if the word "Lucifer" is present (to avoid accidental triggers)
        val hasLucifer = text.lowercase().contains("lucifer")
        if (!hasLucifer) return false

        val lower = text.lowercase()
        return (lower.contains("create") || lower.contains("build") || lower.contains("make")) &&
               (lower.contains("website") || lower.contains("portfolio") || lower.contains("page"))
    }

    /**
     * Parse website name from command
     */
    fun parseWebsiteCommand(command: String): String {
        val lowerCommand = command.lowercase()
        var name = "My Website"
        var foundName = false

        // Pattern 1: "website name is <name>"
        val nameIsPattern = Regex("(?:website\\s+)?name\\s+is\\s+([A-Za-z][A-Za-z0-9\\s-]*?)(?:\\s*[,.]|\\s+(?:for|please|sir)|\\s*$)", RegexOption.IGNORE_CASE)
        nameIsPattern.find(command)?.let {
            name = it.groupValues[1].trim()
            if (name.isNotBlank() && name.length > 1) {
                foundName = true
                Log.d(TAG, "Pattern 1 (name is) matched: '$name'")
            }
        }

        // Pattern 2: "create website <name>"
        if (!foundName) {
            val createPattern = Regex("(?:create|build|make)\\s+(?:a\\s+)?(?:website|web\\s*site|webpage)\\s+(?:called\\s+|named\\s+)?([A-Za-z][A-Za-z0-9\\s-]*?)(?:\\s*[,.]|\\s+for\\s+|\\s*$)", RegexOption.IGNORE_CASE)
            createPattern.find(command)?.let {
                name = it.groupValues[1].trim()
                if (name.isNotBlank() && name.length > 1) {
                    foundName = true
                    Log.d(TAG, "Pattern 2 (create website) matched: '$name'")
                }
            }
        }

        // Pattern 3: "create a <name> website"
        if (!foundName) {
            val reversePattern = Regex("(?:create|build|make)\\s+a\\s+([A-Za-z][A-Za-z0-9\\s-]*?)\\s+(?:website|web\\s*site|webpage|portfolio)(?:\\s*[,.]|\\s*$)", RegexOption.IGNORE_CASE)
            reversePattern.find(command)?.let {
                name = it.groupValues[1].trim()
                if (name.isNotBlank() && name.length > 1) {
                    foundName = true
                    Log.d(TAG, "Pattern 3 (reverse) matched: '$name'")
                }
            }
        }

        // Cleanup and capitalize
        if (foundName) {
            name = name.replace(Regex("\\s+(?:for\\s+me|please|sir|the)\\s+", RegexOption.IGNORE_CASE), " ").trim()
            name = name.replace(Regex("\\s+"), " ")
            name = name.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }

        Log.d(TAG, "Final parsed name: '$name'")
        return name
    }

    /**
     * Show command preview before building
     */
    fun showCommandPreview(command: String) {
        val websiteName = parseWebsiteCommand(command)
        _lastCommand.value = command
        _parsedWebsiteName.value = websiteName
        _showCommandPreview.value = true
        Log.d(TAG, "Showing preview for: $websiteName")
    }
    fun getRealTimeSpeakEnabled(): Boolean {
        return settingsManager?.realTimeSpeakEnabled?.value ?: true
    }

    fun hideCommandPreview() {
        _showCommandPreview.value = false
        _lastCommand.value = ""
        _parsedWebsiteName.value = "My Website"
    }

    fun buildWebsite() {
        viewModelScope.launch {
            val command = _lastCommand.value
            if (command.isBlank()) return@launch

            try {
                _showCommandPreview.value = false
                _isBuilding.value = true
                _buildError.value = null
                _completedSteps.value = emptyList()

                val websiteName = parseWebsiteCommand(command)
                _completedSteps.value = listOf("Building website: $websiteName...")

                // Simulate build (real implementation would be here)
                delay(2000)

                _showQRCode.value = true
                _qrCodeUrl.value = "https://example.com/website"
                _isBuilding.value = false

            } catch (e: Exception) {
                _buildError.value = e.message ?: "Build failed"
                _isBuilding.value = false
            }
        }
    }

    fun closeQRCode() {
        _showQRCode.value = false
        _lastCommand.value = ""
        _parsedWebsiteName.value = "My Website"
    }

    fun clearBuildError() {
        _buildError.value = null
        _completedSteps.value = emptyList()
        _isBuilding.value = false
    }

    override fun onCleared() {
        silenceDetectionJob?.cancel()
        recorder?.release()
        tts?.shutdown()
        wakeLock?.release()
        super.onCleared()
    }
}
