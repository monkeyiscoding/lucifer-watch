package com.monkey.lucifer.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monkey.lucifer.domain.BuildStep
import com.monkey.lucifer.domain.ProjectStatus
import com.monkey.lucifer.domain.WebsiteDetails
import com.monkey.lucifer.domain.WebsiteProject
import com.monkey.lucifer.services.FirebaseStorageService
import com.monkey.lucifer.services.GitHubService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class WebsiteBuilderViewModel(
    private val aiService: OpenAIService,
    private val storageService: FirebaseStorageService = FirebaseStorageService(),
    private val gitHubService: GitHubService = GitHubService(),
    private val projectStore: com.monkey.lucifer.services.WebsiteProjectStore = com.monkey.lucifer.services.WebsiteProjectStore()
) : ViewModel() {

    private val _isBuilding = MutableStateFlow(false)
    val isBuilding: StateFlow<Boolean> = _isBuilding

    private val _currentStep = MutableStateFlow<BuildStep>(BuildStep.Analyzing)
    val currentStep: StateFlow<BuildStep> = _currentStep

    private val _currentProject = MutableStateFlow<WebsiteProject?>(null)
    val currentProject: StateFlow<WebsiteProject?> = _currentProject

    private val _completedSteps = MutableStateFlow<List<String>>(emptyList())
    val completedSteps: StateFlow<List<String>> = _completedSteps

    private val _showStopDialog = MutableStateFlow(false)
    val showStopDialog: StateFlow<Boolean> = _showStopDialog

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // For command preview screen
    private val _showCommandPreview = MutableStateFlow(false)
    val showCommandPreview: StateFlow<Boolean> = _showCommandPreview

    private val _lastCommand = MutableStateFlow("")
    val lastCommand: StateFlow<String> = _lastCommand

    private val _parsedWebsiteName = MutableStateFlow("My Website")
    val parsedWebsiteName: StateFlow<String> = _parsedWebsiteName

    companion object {
        private const val TAG = "WebsiteBuilder"
    }

    /**
     * Parse user command to extract website details
     */
    fun parseWebsiteCommand(command: String): WebsiteDetails {
        val lowerCommand = command.lowercase()

        // Extract name from various patterns
        var name = "My Website"
        var foundName = false

        // Pattern 1: "website name is <name>" or "name is <name>" (highest priority)
        // This pattern catches: "The website name is Lucifer" or "name is Lucifer"
        val nameIsPattern = Regex("(?:website\\s+)?name\\s+is\\s+([A-Za-z][A-Za-l0-9\\s-]*?)(?:\\s*[,.]|\\s+(?:for|please|sir)|\\s*$)", RegexOption.IGNORE_CASE)
        nameIsPattern.find(command)?.let {
            name = it.groupValues[1].trim()
            if (name.isNotBlank() && name.length > 1) {
                foundName = true
                Log.d(TAG, "Pattern 1 (name is) matched: '$name'")
            }
        }

        // Pattern 2: "create website <name>" or "build website <name>"
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

        // Pattern 3: "create a <name> website/portfolio" style
        if (!foundName) {
            val reversePattern = Regex("(?:create|build|make)\\s+a\\s+([A-Za-z][A-Za-z0-9\\s-]*?)\\s+(?:website|web\\s*site|webpage|portfolio)(?:\\s*[,.]|\\s*$)", RegexOption.IGNORE_CASE)
            reversePattern.find(command)?.let {
                name = it.groupValues[1].trim()
                if (name.isNotBlank() && name.length > 1) {
                    foundName = true
                    Log.d(TAG, "Pattern 3 (reverse style) matched: '$name'")
                }
            }
        }

        // Pattern 4: Just "<name> website" without "create"
        if (!foundName) {
            val simplePattern = Regex("([A-Za-z][A-Za-z0-9\\s-]*?)\\s+(?:website|web\\s*site|webpage|portfolio)(?:\\s*[,.]|\\s*$)", RegexOption.IGNORE_CASE)
            simplePattern.find(command)?.let {
                name = it.groupValues[1].trim()
                if (name.isNotBlank() && name.length > 1) {
                    foundName = true
                    Log.d(TAG, "Pattern 4 (simple style) matched: '$name'")
                }
            }
        }

        // Clean up the name: remove common particles and extra spaces
        if (foundName) {
            name = name.replace(Regex("\\s+(?:for\\s+me|please|sir|the)\\s+", RegexOption.IGNORE_CASE), " ").trim()
            name = name.replace(Regex("\\s+"), " ")
        }

        // Final validation: ensure name is not empty or too generic
        if (name.isBlank() || name.length < 2 || name.lowercase() == "my website") {
            name = "My Website"
            Log.d(TAG, "Name validation failed, using default")
        } else {
            // Capitalize first letter of each word
            name = name.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }

        Log.d(TAG, "Final extracted website name: '$name' (found: $foundName)")

        // Extract description and features
        val features = mutableListOf<String>()
        if (lowerCommand.contains("portfolio")) features.add("portfolio sections")
        if (lowerCommand.contains("contact")) features.add("contact form")
        if (lowerCommand.contains("gallery")) features.add("image gallery")
        if (lowerCommand.contains("blog")) features.add("blog layout")
        if (lowerCommand.contains("dark")) features.add("dark theme")
        if (lowerCommand.contains("animation")) features.add("smooth animations")

        val description = when {
            lowerCommand.contains("portfolio") -> "A professional portfolio website"
            lowerCommand.contains("business") -> "A modern business website"
            lowerCommand.contains("blog") -> "A clean blog website"
            lowerCommand.contains("landing") -> "An engaging landing page"
            else -> "A professional responsive website"
        }

        return WebsiteDetails(
            name = name,
            description = description,
            additionalFeatures = features
        )
    }

    /**
     * Build website from voice command
     */
    fun buildWebsite(command: String) {
        if (_isBuilding.value) {
            Log.w(TAG, "Build already in progress")
            return
        }

        viewModelScope.launch {
            try {
                _isBuilding.value = true
                _errorMessage.value = null
                _completedSteps.value = emptyList()

                // Step 1: Analyze requirements
                _currentStep.value = BuildStep.Analyzing
                addCompletedStep("Started analyzing your requirements")
                delay(800)

                val details = parseWebsiteCommand(command)
                Log.d(TAG, "Parsed details: $details")
                addCompletedStep("Identified: ${details.name}")
                delay(500)

                // Step 2: Create project structure
                _currentStep.value = BuildStep.CreatingProject
                val projectId = UUID.randomUUID().toString()
                addCompletedStep("Creating project: ${details.name}")
                delay(1000)

                val project = WebsiteProject(
                    id = projectId,
                    name = details.name,
                    description = details.description,
                    htmlContent = "",
                    status = ProjectStatus.CREATING
                )
                _currentProject.value = project
                addCompletedStep("Project structure ready")

                // Step 3: Generate HTML
                _currentStep.value = BuildStep.GeneratingHTML
                addCompletedStep("Generating website files...")

                var htmlContent: String? = null
                var filesMap = mutableMapOf<String, String>()
                var retryCount = 0
                val maxRetries = 3

                while (htmlContent == null && retryCount <= maxRetries) {
                    try {
                        val jsonResponse = aiService.generateWebsite(details)
                        Log.d(TAG, "Generated website JSON (${jsonResponse.length} chars)")

                        filesMap.clear()

                        // First try separator-based format
                        val separatorFiles = parseSeparatorOutput(jsonResponse)
                        if (separatorFiles.isNotEmpty()) {
                            filesMap.putAll(separatorFiles)
                        } else {
                            // Prefer new schema: { project_name, tech_stack, files: [ {path, content} ] }
                            val rootJson = org.json.JSONObject(jsonResponse)
                            if (rootJson.has("files")) {
                                val filesArray = rootJson.optJSONArray("files")
                                if (filesArray != null) {
                                    for (i in 0 until filesArray.length()) {
                                        val fileObj = filesArray.getJSONObject(i)
                                        val path = fileObj.optString("path")
                                        val content = fileObj.optString("content")
                                        if (path.isNotBlank() && content.isNotBlank()) {
                                            filesMap[path] = content
                                            Log.d(TAG, "Parsed file: $path (${content.length} chars)")
                                        }
                                    }
                                }
                            } else {
                                // Legacy fallback: flat JSON map of filename -> content
                                for (key in rootJson.keys()) {
                                    filesMap[key] = rootJson.getString(key)
                                    Log.d(TAG, "Parsed file: $key (${rootJson.getString(key).length} chars)")
                                }
                            }
                        }

                        if (filesMap.isEmpty()) {
                            throw Exception("No files generated in response")
                        }

                        // Fix file paths in HTML - ensure CSS and JS references use correct relative paths
                        filesMap = fixFileReferences(filesMap)

                        // Use index.html as the main content for validation and logging
                        htmlContent = filesMap["index.html"] ?: filesMap.values.first()

                        val validationError = validateGeneratedWebsite(htmlContent)
                        if (validationError != null) {
                            retryCount++
                            htmlContent = null
                            addCompletedStep("⚠️ Quality check failed, regenerating... ($validationError)")
                            Log.w(TAG, "Quality check failed: $validationError")
                            if (retryCount <= maxRetries) {
                                delay(1200)
                                continue
                            }
                            throw Exception("Website quality check failed: $validationError")
                        }

                        addCompletedStep("Website files generated (${filesMap.size} files: ${filesMap.keys.joinToString(", ")})")
                    } catch (e: java.net.SocketTimeoutException) {
                        retryCount++
                        if (retryCount <= maxRetries) {
                            addCompletedStep("⚠️ Timeout, retrying... (Attempt ${retryCount + 1})")
                            delay(2000) // Wait before retry
                        } else {
                            throw e
                        }
                    }
                }

                delay(500)

                // Step 4: Styling
                _currentStep.value = BuildStep.StylingWebsite
                addCompletedStep("CSS styling included...")
                delay(500)
                addCompletedStep("Mobile and desktop styles applied")

                // Step 5: Adding interactivity
                _currentStep.value = BuildStep.AddingInteractivity
                addCompletedStep("JavaScript interactivity included...")
                delay(500)
                addCompletedStep("Interactive elements ready")

                // Update project with content
                _currentProject.value = project.copy(htmlContent = htmlContent!!)

                // Step 6: Upload to GitHub (ONLY)
                _currentStep.value = BuildStep.UploadingToCloud
                addCompletedStep("Uploading website files to GitHub...")

                val gitHubUploadResult = gitHubService.uploadWebsite(projectId, details.name, filesMap)

                if (gitHubUploadResult.isFailure) {
                    throw gitHubUploadResult.exceptionOrNull() ?: Exception("GitHub upload failed")
                }

                val gitHubUrl = gitHubUploadResult.getOrNull()!!
                Log.d(TAG, "GitHub URL: $gitHubUrl")
                addCompletedStep("GitHub upload successful (${filesMap.size} files)")

                // Step 7: Generate QR code (DISABLED - QRCodeGenerator not available)
                // _currentStep.value = BuildStep.GeneratingQR
                // addCompletedStep("Generating QR code...")
                // val qrBitmap = withContext(Dispatchers.Default) {
                //     QRCodeGenerator.generateQRCodeForDarkTheme(gitHubUrl, 512)
                // }
                // addCompletedStep("QR code generated")

                // Step 8: Save metadata to Firestore
                addCompletedStep("Saving project metadata...")
                val finalProject = project.copy(
                    htmlContent = htmlContent!!,
                    firebaseStorageUrl = null,  // No Firebase URL
                    storagePath = null,  // No storage path
                    githubUrl = gitHubUrl,
                    qrCodeBitmap = null, // QR generation disabled
                    status = ProjectStatus.COMPLETE
                )
                val saved = projectStore.saveProject(finalProject)
                if (!saved) {
                    addCompletedStep("⚠️ Metadata save failed")
                }

                // Step 9: Complete
                _currentStep.value = BuildStep.Complete
                _currentProject.value = finalProject

                addCompletedStep("✅ Website ready, Sir!")
                Log.d(TAG, "Build complete: ${finalProject.name}")

            } catch (e: Exception) {
                Log.e(TAG, "Build failed", e)
                val errorMsg = when (e) {
                    is java.net.SocketTimeoutException -> "Website generation timed out. Please check your internet connection and try again, Sir."
                    else -> e.message ?: "Build failed"
                }
                _errorMessage.value = errorMsg
                _currentProject.value = _currentProject.value?.copy(status = ProjectStatus.FAILED)
                addCompletedStep("❌ Build failed: $errorMsg")
            } finally {
                _isBuilding.value = false
            }
        }
    }

    /**
     * Show command preview screen
     */
    fun showCommandPreview(command: String) {
        val details = parseWebsiteCommand(command)
        _lastCommand.value = command
        _parsedWebsiteName.value = details.name
        _showCommandPreview.value = true
        Log.d(TAG, "Showing command preview for: ${details.name}")
    }

    /**
     * Hide command preview screen
     */
    fun hideCommandPreview() {
        _showCommandPreview.value = false
        _lastCommand.value = ""
        _parsedWebsiteName.value = "My Website"
    }

    /**
     * Build website from preview (after user confirms)
     */
    fun buildWebsiteFromPreview() {
        _showCommandPreview.value = false
        val command = _lastCommand.value
        if (command.isNotBlank()) {
            buildWebsite(command)
        }
    }

    /**
     * Show stop confirmation dialog
     */
    fun showStopDialog() {
        _showStopDialog.value = true
    }

    /**
     * Hide stop confirmation dialog
     */
    fun dismissStopDialog() {
        _showStopDialog.value = false
    }

    /**
     * Stop building process and delete all data
     */
    fun stopBuilding() {
        viewModelScope.launch {
            try {
                _showStopDialog.value = false

                val project = _currentProject.value
                if (project != null && project.firebaseStorageUrl != null) {
                    // Delete from Firebase if already uploaded
                    storageService.deleteWebsite(project.id)
                }
                if (project != null) {
                    projectStore.deleteProject(project.id)
                }

                _currentProject.value = project?.copy(status = ProjectStatus.CANCELLED)
                addCompletedStep("❌ Build cancelled by user")
                _isBuilding.value = false

                Log.d(TAG, "Build cancelled and data deleted")
            } catch (e: Exception) {
                Log.e(TAG, "Error during cancellation", e)
            }
        }
    }

    /**
     * Clear current project
     */
    fun clearProject() {
        _currentProject.value = null
        _completedSteps.value = emptyList()
        _errorMessage.value = null
        _currentStep.value = BuildStep.Analyzing
    }

    private fun addCompletedStep(step: String) {
        _completedSteps.value = _completedSteps.value + step
    }

    /**
     * Fix HTML file references to load CSS/JS from the same folder as index.html
     */
    private fun fixFileReferences(filesMap: MutableMap<String, String>): MutableMap<String, String> {
        try {
            if (!filesMap.containsKey("index.html")) {
                return filesMap
            }

            // Normalize file keys so any AI-provided folder paths are stripped.
            val normalizedFiles = mutableMapOf<String, String>()
            for ((rawName, content) in filesMap) {
                val normalizedName = rawName.substringAfterLast('/').substringAfterLast('\\')
                if (normalizedName != rawName) {
                    Log.d(TAG, "Normalized file name: $rawName -> $normalizedName")
                }
                normalizedFiles[normalizedName] = content
            }

            var htmlContent = normalizedFiles["index.html"]!!
            val fixedFiles = mutableMapOf<String, String>()

            fixedFiles["index.html"] = htmlContent

            // Process CSS files
            for ((fileName, content) in normalizedFiles) {
                if (fileName.endsWith(".css") && fileName != "index.html") {
                    fixedFiles[fileName] = content

                    val patterns = listOf(
                        """href=["']\.[/\\][^/\\'"]*_files[/\\]$fileName["']""",
                        """href=["']\.[/\\][^/\\'"]*[/\\]$fileName["']""",
                        """href=["']\.\./[^'\\"]*[/\\]$fileName["']""",
                        """href=["']$fileName["']"""
                    )

                    val replacement = "href=\"$fileName\""

                    for (pattern in patterns) {
                        val newContent = htmlContent.replace(Regex(pattern), replacement)
                        if (newContent != htmlContent) {
                            htmlContent = newContent
                            break
                        }
                    }
                }
            }

            // Process JS files
            for ((fileName, content) in normalizedFiles) {
                if (fileName.endsWith(".js") && fileName != "index.html") {
                    fixedFiles[fileName] = content

                    val patterns = listOf(
                        """src=["']\.[/\\][^/\\'"]*_files[/\\]$fileName["']""",
                        """src=["']\.[/\\][^/\\'"]*[/\\]$fileName["']""",
                        """src=["']\.\./[^'\\"]*[/\\]$fileName["']""",
                        """src=["']$fileName["']"""
                    )

                    val replacement = "src=\"$fileName\""

                    for (pattern in patterns) {
                        val newContent = htmlContent.replace(Regex(pattern), replacement)
                        if (newContent != htmlContent) {
                            htmlContent = newContent
                            break
                        }
                    }
                }
            }

            // Process image files
            for ((fileName, content) in normalizedFiles) {
                if ((fileName.endsWith(".jpg") || fileName.endsWith(".png") ||
                     fileName.endsWith(".gif") || fileName.endsWith(".svg") ||
                     fileName.endsWith(".jpeg") || fileName.endsWith(".webp")) &&
                    fileName != "index.html") {

                    fixedFiles[fileName] = content

                    val patterns = listOf(
                        """src=["']\.[/\\][^/\\'"]*_files[/\\]$fileName["']""",
                        """src=["']\.[/\\][^/\\'"]*[/\\]$fileName["']""",
                        """src=["']\.\./[^'\\"]*[/\\]$fileName["']""",
                        """src=["']$fileName["']"""
                    )

                    val replacement = "src=\"$fileName\""

                    for (pattern in patterns) {
                        val newContent = htmlContent.replace(Regex(pattern), replacement)
                        if (newContent != htmlContent) {
                            htmlContent = newContent
                            break
                        }
                    }
                }
            }

            // Final fallback: strip any remaining ./<anything>_files/ prefixes
            htmlContent = htmlContent.replace(Regex("""(href|src)=["']\./[^"']*_files/([^"']+)["']"""), "$1=\"$2\"")

            for ((fileName, content) in normalizedFiles) {
                if (!fixedFiles.containsKey(fileName) && fileName != "index.html") {
                    fixedFiles[fileName] = content
                }
            }

            fixedFiles["index.html"] = htmlContent
            return fixedFiles
        } catch (e: Exception) {
            Log.e(TAG, "Error fixing file references", e)
            return filesMap
        }
    }

    private fun parseSeparatorOutput(raw: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val indexMarker = "--- index.html ---"
        val cssMarker = "--- styles.css ---"
        val jsMarker = "--- script.js ---"

        val indexStart = raw.indexOf(indexMarker)
        val cssStart = raw.indexOf(cssMarker)
        val jsStart = raw.indexOf(jsMarker)

        if (indexStart == -1 || cssStart == -1 || jsStart == -1) {
            return emptyMap()
        }

        val indexContent = raw.substring(indexStart + indexMarker.length, cssStart).trim()
        val cssContent = raw.substring(cssStart + cssMarker.length, jsStart).trim()
        val jsContent = raw.substring(jsStart + jsMarker.length).trim()

        if (indexContent.isNotBlank()) result["index.html"] = indexContent
        if (cssContent.isNotBlank()) result["styles.css"] = cssContent
        if (jsContent.isNotBlank()) result["script.js"] = jsContent

        return result
    }

    private fun validateGeneratedWebsite(html: String): String? {
        val lower = html.lowercase()
        val sectionCount = Regex("<section\\b").findAll(lower).count()
        if (sectionCount < 6) {
            return "Missing sections ($sectionCount found)"
        }
        if (!lower.contains("<nav") || !lower.contains("navbar")) {
            return "Missing sticky navbar"
        }
        if (!lower.contains("hero")) {
            return "Missing hero section"
        }
        if (!lower.contains("testimonials") && !lower.contains("social proof")) {
            return "Missing testimonials"
        }
        if (!lower.contains("faq") || !lower.contains("accordion")) {
            return "Missing FAQ accordion"
        }
        if (!lower.contains("contact") || !lower.contains("<form")) {
            return "Missing contact form"
        }
        if (!lower.contains("styles.css") || !lower.contains("script.js")) {
            return "Missing CSS/JS links"
        }
        if (lower.contains("_files/")) {
            return "Invalid asset paths"
        }
        val wordCount = Regex("\\b\\w+\\b").findAll(lower).count()
        if (wordCount < 350) {
            return "Content too short"
        }
        return null
    }
}
