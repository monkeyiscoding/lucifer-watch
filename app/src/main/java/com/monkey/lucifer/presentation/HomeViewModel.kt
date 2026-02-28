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
    private var ttsService: TTSService? = null  // OpenAI TTS for better quality
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
            // Initialize OpenAI TTS service
            ttsService = TTSService(apiKey, context)
        }

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Default to English, but will be changed per response based on detected language
                tts?.language = Locale.US
                Log.d(TAG, "TTS initialized successfully")

                // Log available languages for debugging
                tts?.availableLanguages?.let { languages ->
                    val langList = languages.take(10).joinToString(", ") { it.displayName }
                    Log.d(TAG, "Available TTS languages (first 10): $langList")

                    // Check if Hindi is available
                    val hindiAvailable = languages.any {
                        it.language == "hi" || it.displayLanguage.contains("Hindi", ignoreCase = true)
                    }
                    Log.d(TAG, "Hindi TTS available: $hindiAvailable")

                    // If Hindi is not available, show helpful message
                    if (!hindiAvailable) {
                        Log.w(TAG, "⚠️ Hindi TTS not installed! Install from Watch Settings → Text-to-Speech")
                    }
                }

                // Log full list for debugging (can be disabled later)
                logAvailableTTSLanguages()
            } else {
                Log.e(TAG, "TTS initialization failed")
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

            // Set TTS language based on detected language
            val detectedLang = api.getDetectedLanguage()

            // Use OpenAI TTS for Hindi (better quality), fallback to system TTS for others
            if (detectedLang.lowercase() in listOf("hi", "hindi")) {
                Log.d(TAG, "🎤 Using OpenAI TTS for Hindi (Male voice)")
                ttsService?.speak(response, languageCode = "hi", isMaleVoice = true)
            } else {
                Log.d(TAG, "🎤 Using system TTS for $detectedLang")

                val locale = getLocaleForLanguage(detectedLang)

                tts?.let { ttsEngine ->
                    // Try primary locale first
                    var result = ttsEngine.setLanguage(locale)

                    // If Hindi failed, try alternative Hindi locales
                    if ((result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                        && detectedLang.lowercase() == "hindi") {

                        Log.w(TAG, "Hindi locale $locale not supported, trying alternatives...")

                        // Try Hindi without country code
                        result = ttsEngine.setLanguage(Locale("hi"))
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            // Try checking available languages for any Hindi variant
                            val hindiLocale = ttsEngine.availableLanguages?.firstOrNull {
                                it.language == "hi"
                            }

                            if (hindiLocale != null) {
                                result = ttsEngine.setLanguage(hindiLocale)
                                Log.d(TAG, "Found Hindi variant: $hindiLocale")
                            }
                        }
                    }

                    // Final fallback to English if all attempts failed
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(TAG, "Language $detectedLang not supported on this device")
                        _error.value = "TTS for $detectedLang not installed. Install from Watch Settings → Text-to-Speech"
                        ttsEngine.setLanguage(Locale.US)
                    } else {
                        Log.d(TAG, "✅ TTS language set to: $detectedLang ($locale)")
                    }
                }

                @Suppress("DEPRECATION")
                tts?.speak(response, TextToSpeech.QUEUE_FLUSH, null)
            }

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

    /**
     * Maps language codes from Whisper to Android Locale for TTS
     */
    private fun getLocaleForLanguage(languageCode: String): Locale {
        return when (languageCode.lowercase()) {
            "en", "english" -> Locale.US
            "hi", "hindi" -> Locale("hi", "IN")  // Hindi (India)
            "es", "spanish" -> Locale("es", "ES")  // Spanish
            "fr", "french" -> Locale.FRENCH
            "de", "german" -> Locale.GERMAN
            "it", "italian" -> Locale.ITALIAN
            "ja", "japanese" -> Locale.JAPANESE
            "ko", "korean" -> Locale.KOREAN
            "zh", "chinese" -> Locale.CHINESE
            "pt", "portuguese" -> Locale("pt", "BR")  // Portuguese (Brazil)
            "ru", "russian" -> Locale("ru", "RU")  // Russian
            "ar", "arabic" -> Locale("ar", "SA")  // Arabic
            "bn", "bengali" -> Locale("bn", "IN")  // Bengali
            "ta", "tamil" -> Locale("ta", "IN")  // Tamil
            "te", "telugu" -> Locale("te", "IN")  // Telugu
            "mr", "marathi" -> Locale("mr", "IN")  // Marathi
            "gu", "gujarati" -> Locale("gu", "IN")  // Gujarati
            "kn", "kannada" -> Locale("kn", "IN")  // Kannada
            "ml", "malayalam" -> Locale("ml", "IN")  // Malayalam
            "pa", "punjabi" -> Locale("pa", "IN")  // Punjabi
            "ur", "urdu" -> Locale("ur", "PK")  // Urdu
            "ne", "nepali" -> Locale("ne", "NP")  // Nepali
            else -> {
                Log.w(TAG, "Unknown language: $languageCode, falling back to English")
                Locale.US
            }
        }
    }

    /**
     * Check if a specific locale is supported by TTS
     */
    private fun isLocaleSupportedByTTS(locale: Locale): Boolean {
        val result = tts?.isLanguageAvailable(locale)
        return result == TextToSpeech.LANG_AVAILABLE ||
               result == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
               result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
    }

    /**
     * Diagnostic: Log all available TTS languages
     */
    fun logAvailableTTSLanguages() {
        tts?.availableLanguages?.let { languages ->
            Log.d(TAG, "═══════════════════════════════════")
            Log.d(TAG, "Available TTS Languages (${languages.size} total):")
            languages.sortedBy { it.displayName }.forEach { locale ->
                Log.d(TAG, "  - ${locale.displayName} (${locale.language}_${locale.country})")
            }
            Log.d(TAG, "═══════════════════════════════════")
        }
    }

    override fun onCleared() {
        silenceDetectionJob?.cancel()
        recorder?.release()
        tts?.shutdown()
        ttsService?.release()
        wakeLock?.release()
        super.onCleared()
    }
}
