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

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var tts: TextToSpeech? = null
    private var openAI: OpenAIService? = null
    private var wakeLock: PowerManager.WakeLock? = null

    // Services
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
    }

    fun stopRecordingAndProcess() {
        if (!_isRecording.value) return

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

    /**
     * Hide command preview
     */
    fun hideCommandPreview() {
        _showCommandPreview.value = false
        _lastCommand.value = ""
    }


    /**
     * Build website after user confirms
     */
    fun buildWebsite() {
        val command = _lastCommand.value
        if (command.isBlank()) return

        viewModelScope.launch {
            try {
                _showCommandPreview.value = false
                _isBuilding.value = true
                _buildError.value = null
                _completedSteps.value = emptyList()

                val websiteName = parseWebsiteCommand(command)
                Log.d(TAG, "Starting website build: $websiteName")

                addStep("Analyzing requirements...")
                kotlinx.coroutines.delay(500)

                addStep("Generating HTML, CSS, and JavaScript files...")

                val api = openAI
                if (api == null) {
                    throw Exception("OpenAI service not initialized")
                }

                // Generate website with parsed details
                val details = com.monkey.lucifer.domain.WebsiteDetails(
                    name = websiteName,
                    description = "A professional responsive website",
                    additionalFeatures = listOf("premium design", "mobile responsive", "smooth animations")
                )
                val generatedCode = api.generateWebsite(details)
                Log.d(TAG, "Generated code (${generatedCode.length} chars)")

                addStep("Files generated successfully")
                kotlinx.coroutines.delay(500)

                // Upload to GitHub (ONLY)
                addStep("Uploading to GitHub...")
                val projectId = UUID.randomUUID().toString()

                // Parse generated files
                val filesMap = parseGeneratedFiles(generatedCode)

                // Upload ONLY to GitHub (skip Firebase)
                val gitResult = gitHubService.uploadWebsite(projectId, websiteName, filesMap)
                if (gitResult.isFailure) {
                    throw gitResult.exceptionOrNull() ?: Exception("GitHub upload failed")
                }

                val finalUrl = gitResult.getOrNull()!!
                addStep("GitHub upload successful")
                Log.d(TAG, "GitHub URL: $finalUrl")

                // Validate final URL
                if (finalUrl.isBlank()) {
                    throw Exception("Unable to generate website URL. GitHub upload failed.")
                }

                // Save to Firestore with website name
                addStep("Saving project metadata...")
                val websiteProject = com.monkey.lucifer.domain.WebsiteProject(
                    id = projectId,
                    name = websiteName,
                    description = "A professional website",
                    htmlContent = filesMap["index.html"] ?: "",
                    firebaseStorageUrl = null,  // No Firebase URL
                    githubUrl = finalUrl,
                    status = ProjectStatus.COMPLETE
                )
                projectStore.saveProject(websiteProject)

                addStep("✅ Website ready, sir!")
                Log.d(TAG, "Setting QR code URL: $finalUrl")
                _qrCodeUrl.value = finalUrl
                Log.d(TAG, "Setting showQRCode to true")
                _showQRCode.value = true
                Log.d(TAG, "Setting isBuilding to false")
                _isBuilding.value = false

                Log.d(TAG, "Build complete. Website: $websiteName. URL: $finalUrl. QR URL: ${_qrCodeUrl.value}. Show QR: ${_showQRCode.value}")

            } catch (e: Exception) {
                Log.e(TAG, "Build failed", e)
                val errorMsg = when (e) {
                    is java.net.SocketTimeoutException -> "Website generation timed out"
                    else -> e.message ?: "Build failed"
                }
                _buildError.value = errorMsg
                addStep("❌ Error: $errorMsg")
                _isBuilding.value = false
            }
        }
    }

    private fun addStep(step: String) {
        _completedSteps.value = _completedSteps.value + step
        _buildStatus.value = step
    }

    private fun parseGeneratedFiles(generatedCode: String): Map<String, String> {
        val filesMap = mutableMapOf<String, String>()

        // Try parsing separator format first
        val indexMarker = "--- index.html ---"
        val cssMarker = "--- styles.css ---"
        val jsMarker = "--- script.js ---"

        val indexStart = generatedCode.indexOf(indexMarker)
        val cssStart = generatedCode.indexOf(cssMarker)
        val jsStart = generatedCode.indexOf(jsMarker)

        if (indexStart != -1 && cssStart != -1 && jsStart != -1) {
            val indexContent = generatedCode.substring(indexStart + indexMarker.length, cssStart).trim()
            val cssContent = generatedCode.substring(cssStart + cssMarker.length, jsStart).trim()
            val jsContent = generatedCode.substring(jsStart + jsMarker.length).trim()

            if (indexContent.isNotBlank()) filesMap["index.html"] = indexContent
            if (cssContent.isNotBlank()) filesMap["styles.css"] = cssContent
            if (jsContent.isNotBlank()) filesMap["script.js"] = jsContent
        } else {
            // Fallback: try JSON parsing
            try {
                val json = org.json.JSONObject(generatedCode)
                if (json.has("files")) {
                    val filesArray = json.optJSONArray("files")
                    if (filesArray != null) {
                        for (i in 0 until filesArray.length()) {
                            val fileObj = filesArray.getJSONObject(i)
                            val path = fileObj.optString("path")
                            val content = fileObj.optString("content")
                            if (path.isNotBlank() && content.isNotBlank()) {
                                filesMap[path] = content
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not parse generated files", e)
            }
        }

        if (filesMap.isEmpty()) {
            // Create a default page
            filesMap["index.html"] = generatedCode.takeIf { it.startsWith("<!") } ?: "<html><body>Website content</body></html>"
        }

        return filesMap
    }

    fun closeQRCode() {
        _showQRCode.value = false
        clear()
    }

    fun clearBuildError() {
        _buildError.value = null
        _completedSteps.value = emptyList()
        clear()
    }

    fun clear() {
        _status.value = "Idle"
        _recognizedText.value = ""
        _aiText.value = ""
        _error.value = ""
        _showCommandPreview.value = false
        _showQRCode.value = false
    }

    fun getRealTimeSpeakEnabled(): Boolean {
        return settingsManager?.realTimeSpeakEnabled?.value ?: true
    }

    override fun onCleared() {
        recorder?.release()
        tts?.shutdown()
        wakeLock?.release()
        super.onCleared()
    }
}
