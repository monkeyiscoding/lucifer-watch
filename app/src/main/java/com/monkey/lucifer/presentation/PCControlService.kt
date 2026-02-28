package com.monkey.lucifer.presentation

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class PCDevice(
    val deviceId: String,
    val deviceName: String,
    val nickname: String,
    val hostname: String
)

class PCControlService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val firebaseApiKey = "AIzaSyDER86nAMW9YjbJupojXcAzj5J0gLVij-o"
    private val firebaseProjectId = "lucifer-97501"
    private val firestoreBase = "https://firestore.googleapis.com/v1"

    companion object {
        // Common CMD commands mapping
        private val COMMAND_MAPPINGS = mapOf(
            // Applications
            "notepad" to "start notepad",
            "calculator" to "start calc",
            "paint" to "start mspaint",
            "chrome" to "start chrome",
            "edge" to "start msedge",
            "firefox" to "start firefox",
            "explorer" to "explorer",
            "file explorer" to "explorer",
            "windows explorer" to "explorer",
            "task manager" to "taskmgr",
            "control panel" to "control",
            "settings" to "start ms-settings:",
            "command prompt" to "cmd",
            "cmd" to "cmd",
            "powershell" to "powershell",
            "terminal" to "cmd",

            // System commands
            "shutdown" to "shutdown /s /t 0",
            "restart" to "shutdown /r /t 0",
            "sleep" to "rundll32.exe powrprof.dll,SetSuspendState 0,1,0",
            "lock" to "rundll32.exe user32.dll,LockWorkStation",
            "logoff" to "shutdown /l",

            // Media commands
            "volume up" to "nircmd.exe changesysvolume 2000",
            "volume down" to "nircmd.exe changesysvolume -2000",
            "mute" to "nircmd.exe mutesysvolume 1",
            "unmute" to "nircmd.exe mutesysvolume 0",

            // File operations
            "open downloads" to "start shell:downloads",
            "open documents" to "start shell:mydocuments",
            "open pictures" to "start shell:mypictures",
            "open desktop" to "start shell:desktop",

            // Popular websites (direct mappings)
            "youtube" to "start chrome https://youtube.com",
            "google" to "start chrome https://google.com",
            "gmail" to "start chrome https://gmail.com",
            "facebook" to "start chrome https://facebook.com",
            "twitter" to "start chrome https://twitter.com",
            "instagram" to "start chrome https://instagram.com",
            "linkedin" to "start chrome https://linkedin.com",
            "reddit" to "start chrome https://reddit.com",
            "amazon" to "start chrome https://amazon.com",
            "netflix" to "start chrome https://netflix.com",
            "spotify" to "start chrome https://open.spotify.com",
            "github" to "start chrome https://github.com",
            "stackoverflow" to "start chrome https://stackoverflow.com",
            "wikipedia" to "start chrome https://wikipedia.org",
            "twitch" to "start chrome https://twitch.tv",
            "discord" to "start chrome https://discord.com",
            "whatsapp" to "start chrome https://web.whatsapp.com",
            "telegram" to "start chrome https://web.telegram.org",

            // Network commands
            "wifi off" to "netsh interface set interface Wi-Fi disabled",
            "wifi on" to "netsh interface set interface Wi-Fi enabled",
            "show ip" to "ipconfig",

            // Screen commands
            "screenshot" to "snippingtool /clip",
            "brightness up" to "powershell (Get-WmiObject -Namespace root/WMI -Class WmiMonitorBrightnessMethods).WmiSetBrightness(1,100)",
            "brightness down" to "powershell (Get-WmiObject -Namespace root/WMI -Class WmiMonitorBrightnessMethods).WmiSetBrightness(1,50)",
        )
    }

    /**
     * Fetch all registered PCs from Firestore
     */
    suspend fun getAllDevices(): List<PCDevice> = withContext(Dispatchers.IO) {
        try {
            val url = "$firestoreBase/projects/$firebaseProjectId/databases/(default)/documents/Devices?key=$firebaseApiKey"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("PCControl", "Failed to fetch devices: ${response.code}")
                    return@withContext emptyList()
                }

                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                val documents = jsonResponse.optJSONArray("documents") ?: JSONArray()

                val devices = mutableListOf<PCDevice>()
                for (i in 0 until documents.length()) {
                    val doc = documents.getJSONObject(i)
                    val fields = doc.optJSONObject("fields") ?: continue

                    val deviceId = fields.optJSONObject("device_id")?.optString("stringValue") ?: continue
                    val deviceName = fields.optJSONObject("device_name")?.optString("stringValue") ?: ""
                    val nickname = fields.optJSONObject("nickname")?.optString("stringValue") ?: ""
                    val hostname = fields.optJSONObject("hostname")?.optString("stringValue") ?: ""

                    devices.add(PCDevice(deviceId, deviceName, nickname, hostname))
                }

                Log.d("PCControl", "Found ${devices.size} devices")
                devices
            }
        } catch (e: Exception) {
            Log.e("PCControl", "Error fetching devices", e)
            emptyList()
        }
    }

    /**
     * Find a PC by nickname (case-insensitive and flexible matching)
     */
    suspend fun findDeviceByNickname(nickname: String): PCDevice? {
        val devices = getAllDevices()

        // Normalize the search term
        val normalizedSearch = nickname.lowercase().trim()
            .replace(Regex("\\s+"), "") // Remove all spaces for comparison

        Log.d("PCControlService", "Searching for device with nickname: '$nickname' (normalized: '$normalizedSearch')")
        Log.d("PCControlService", "Available devices: ${devices.map { "${it.nickname} (${it.deviceId})" }}")

        return devices.find {
            val normalizedNickname = it.nickname.lowercase().replace(Regex("\\s+"), "")
            val normalizedDeviceName = it.deviceName.lowercase().replace(Regex("\\s+"), "")
            val normalizedHostname = it.hostname.lowercase().replace(Regex("\\s+"), "")

            // Try exact match first
            it.nickname.equals(nickname, ignoreCase = true) ||
            it.deviceName.equals(nickname, ignoreCase = true) ||
            it.hostname.equals(nickname, ignoreCase = true) ||
            // Then try normalized (no spaces) match
            normalizedNickname == normalizedSearch ||
            normalizedDeviceName == normalizedSearch ||
            normalizedHostname == normalizedSearch
        }
    }

    /**
     * Parse user command and generate CMD command
     */
    fun parsePCCommand(userInput: String): Pair<String?, String?> {
        val input = userInput.lowercase()

        // Extract PC nickname from the end of the sentence
        // Look for common PC name patterns: "my pc", "devil pc", "work computer", etc.
        // Only capture 1-3 words after the preposition to avoid capturing too much
        // The (?:...) around the preposition means don't include it in the capture
        val pcNamePattern = Regex("(?:on|in|at|to|from)\\s+((?:[\\w.-]+\\s+){0,2}[\\w.-]+)\\s*$")
        val pcMatch = pcNamePattern.find(input)
        // Group 1 contains only the PC name, NOT the preposition
        var pcNickname = pcMatch?.groupValues?.get(1)?.trim()?.trim(',', '.', '!', '?', ':', ';')

        // Extra safety: Remove prepositions if they somehow got captured
        pcNickname = pcNickname?.let { name ->
            var cleaned = name
            listOf("from", "on", "in", "at", "to").forEach { prep ->
                if (cleaned.startsWith("$prep ")) {
                    cleaned = cleaned.substring(prep.length + 1).trim()
                }
            }
            // Also handle concatenated words like "mypc" -> "my pc" for better matching
            if (cleaned == "mypc") cleaned = "my pc"
            cleaned
        }

        Log.d("PCControlService", "Input: $input")
        Log.d("PCControlService", "Regex matched: ${pcMatch?.value}")
        Log.d("PCControlService", "Group 1 (raw): ${pcMatch?.groupValues?.get(1)}")
        Log.d("PCControlService", "PC nickname (final): $pcNickname")

        // Remove the PC part from the command to avoid mis-parsing
        val commandPart = if (pcMatch != null) {
            input.substring(0, pcMatch.range.first).trim()
        } else {
            input
        }

        val cleanedCommandPart = commandPart
            .replace(Regex("^lucifer\\s*,?\\s*"), "")
            .replace(Regex("^please\\s+"), "")
            .trim()

        var cmdCommand: String? = null

        // If the user already provided a URL or domain, open it directly
        extractUrlFromText(cleanedCommandPart)?.let { url ->
            cmdCommand = "start chrome $url"
        }

        // Check for direct command mappings first (exact matches)
        if (cmdCommand == null) {
            // Try exact match
            COMMAND_MAPPINGS[cleanedCommandPart]?.let {
                cmdCommand = it
                return@let
            }

            // Try with common prefixes removed
            for ((key, value) in COMMAND_MAPPINGS) {
                if (cleanedCommandPart == key ||
                    cleanedCommandPart.startsWith("$key ") ||
                    cleanedCommandPart.endsWith(" $key") ||
                    cleanedCommandPart.contains(" $key ")) {
                    cmdCommand = value
                    break
                }
            }
        }

        // Handle website/URL patterns with explicit keywords
        if (cmdCommand == null && (cleanedCommandPart.contains("website") || cleanedCommandPart.contains("site") || cleanedCommandPart.contains("web page") || cleanedCommandPart.contains("webpage"))) {
            // Extract website name before the website/site keyword
            val websitePattern = Regex("(?:open|start|go to|launch)\\s+([\\w\\s]+?)\\s+(?:website|site|web page|webpage)")
            val websiteMatch = websitePattern.find(cleanedCommandPart)
            if (websiteMatch != null) {
                val siteName = websiteMatch.groupValues[1].trim()
                val url = convertToURL(siteName)
                cmdCommand = "start chrome $url"
            }
        }

        // Handle "open [something]" pattern
        if (cmdCommand == null && cleanedCommandPart.startsWith("open ")) {
            val openPattern = Regex("^open\\s+(.+)")
            val openMatch = openPattern.find(cleanedCommandPart)
            openMatch?.groupValues?.get(1)?.trim()?.let { target ->
                val cleanTarget = target
                    .replace(" website", "")
                    .replace(" site", "")
                    .replace(" web page", "")
                    .replace(" webpage", "")
                    .trim()

                if (shouldTreatAsWebsite(target, cleanTarget)) {
                    val url = convertToURL(cleanTarget)
                    cmdCommand = "start chrome $url"
                } else {
                    // Normalize the app name and check mappings
                    val normalizedApp = normalizeAppName(cleanTarget)
                    if (COMMAND_MAPPINGS.containsKey(normalizedApp)) {
                        cmdCommand = COMMAND_MAPPINGS[normalizedApp]
                    } else {
                        // For unknown apps, use 'start [appname]' - Windows will find it
                        cmdCommand = "start ${cleanTarget.lowercase().replace(" ", "")}"
                    }
                }
            }
        }

        // Handle "start [app]" pattern
        if (cmdCommand == null && cleanedCommandPart.startsWith("start ")) {
            val startPattern = Regex("^start\\s+(.+)")
            val startMatch = startPattern.find(cleanedCommandPart)
            startMatch?.groupValues?.get(1)?.trim()?.let { target ->
                val cleanTarget = target
                    .replace(" website", "")
                    .replace(" site", "")
                    .replace(" web page", "")
                    .replace(" webpage", "")
                    .trim()

                if (shouldTreatAsWebsite(target, cleanTarget)) {
                    val url = convertToURL(cleanTarget)
                    cmdCommand = "start chrome $url"
                } else {
                    // For unknown apps, normalize and try
                    cmdCommand = "start ${cleanTarget.lowercase().replace(" ", "")}"
                }
            }
        }

        // Handle "go to [website]" pattern
        if (cmdCommand == null && cleanedCommandPart.startsWith("go to ")) {
            val goToPattern = Regex("^go to\\s+(.+)")
            val goToMatch = goToPattern.find(cleanedCommandPart)
            goToMatch?.groupValues?.get(1)?.trim()?.let { destination ->
                val cleanDest = destination
                    .replace(" website", "")
                    .replace(" site", "")
                    .replace(" web page", "")
                    .replace(" webpage", "")
                    .trim()
                val url = convertToURL(cleanDest)
                cmdCommand = "start chrome $url"
            }
        }

        // Handle "launch [something]" pattern
        if (cmdCommand == null && cleanedCommandPart.startsWith("launch ")) {
            val launchPattern = Regex("^launch\\s+(.+)")
            val launchMatch = launchPattern.find(cleanedCommandPart)
            launchMatch?.groupValues?.get(1)?.trim()?.let { target ->
                val cleanTarget = target
                    .replace(" website", "")
                    .replace(" site", "")
                    .replace(" web page", "")
                    .replace(" webpage", "")
                    .trim()

                if (shouldTreatAsWebsite(target, cleanTarget)) {
                    val url = convertToURL(cleanTarget)
                    cmdCommand = "start chrome $url"
                } else {
                    // For unknown apps, normalize and try
                    cmdCommand = "start ${cleanTarget.lowercase().replace(" ", "")}"
                }
            }
        }

        // Handle "run [command]" pattern
        if (cmdCommand == null && cleanedCommandPart.startsWith("run ")) {
            val runPattern = Regex("^run\\s+(.+)")
            val runMatch = runPattern.find(cleanedCommandPart)
            cmdCommand = runMatch?.groupValues?.get(1)?.trim()
        }

        // Handle system commands (shutdown, restart, lock, etc.)
        if (cmdCommand == null) {
            when {
                cleanedCommandPart.contains("shutdown") -> cmdCommand = "shutdown /s /t 0"
                cleanedCommandPart.contains("restart") || cleanedCommandPart.contains("reboot") -> cmdCommand = "shutdown /r /t 0"
                cleanedCommandPart.contains("lock") -> cmdCommand = "rundll32.exe user32.dll,LockWorkStation"
                cleanedCommandPart.contains("sleep") -> cmdCommand = "rundll32.exe powrprof.dll,SetSuspendState 0,1,0"
                cleanedCommandPart.contains("logoff") || cleanedCommandPart.contains("log off") -> cmdCommand = "shutdown /l"
            }
        }

        return Pair(pcNickname, cmdCommand)
    }

    /**
     * Check if a term is likely a website name
     */
    private fun isLikelyWebsite(term: String): Boolean {
        val websites = listOf(
            "facebook", "twitter", "instagram", "linkedin", "reddit", "tiktok",
            "amazon", "ebay", "netflix", "spotify", "github", "stackoverflow",
            "wikipedia", "reddit", "twitch", "discord", "whatsapp", "telegram"
        )
        return websites.any { term.contains(it) }
    }

    /**
     * Convert website name to full URL
     */
    private fun convertToURL(siteName: String): String {
        val cleanName = siteName.lowercase().trim()

        // Special cases with specific URLs
        val urlMappings = mapOf(
            "facebook" to "https://facebook.com",
            "fb" to "https://facebook.com",
            "twitter" to "https://twitter.com",
            "x" to "https://twitter.com",
            "instagram" to "https://instagram.com",
            "insta" to "https://instagram.com",
            "linkedin" to "https://linkedin.com",
            "reddit" to "https://reddit.com",
            "youtube" to "https://youtube.com",
            "gmail" to "https://gmail.com",
            "google" to "https://google.com",
            "amazon" to "https://amazon.com",
            "netflix" to "https://netflix.com",
            "spotify" to "https://spotify.com",
            "github" to "https://github.com",
            "stackoverflow" to "https://stackoverflow.com",
            "stack overflow" to "https://stackoverflow.com",
            "wikipedia" to "https://wikipedia.org",
            "wiki" to "https://wikipedia.org",
            "twitch" to "https://twitch.tv",
            "discord" to "https://discord.com",
            "whatsapp" to "https://web.whatsapp.com",
            "telegram" to "https://web.telegram.org",
            "tiktok" to "https://tiktok.com",
            "pinterest" to "https://pinterest.com",
            "ebay" to "https://ebay.com"
        )

        // Check if we have a mapping
        urlMappings[cleanName]?.let { return it }

        // If no mapping, assume it's a domain name and add .com
        return if (cleanName.contains(".")) {
            "https://$cleanName"
        } else {
            "https://$cleanName.com"
        }
    }

    /**
     * Send command to PC via Firestore
     */ /**
     * Send command or query to PC via Firestore
     * Returns the document ID if successful
     */
    suspend fun sendCommandToPC(deviceId: String, command: String, isQuery: Boolean = false): String? = withContext(Dispatchers.IO) {
        try {
            val url = "$firestoreBase/projects/$firebaseProjectId/databases/(default)/documents/Devices/$deviceId/Commands?key=$firebaseApiKey"

            val document = JSONObject().apply {
                put("fields", JSONObject().apply {
                    put("command", JSONObject().put("stringValue", command))
                    put("executed", JSONObject().put("booleanValue", false))
                    put("status", JSONObject().put("stringValue", "pending"))
                    put("output", JSONObject().put("stringValue", ""))
                    put("return_code", JSONObject().put("integerValue", "0"))
                    put("success", JSONObject().put("booleanValue", false))
                    put("is_query", JSONObject().put("booleanValue", isQuery)) // Mark as query
                })
            }

            Log.d("PCControl", "Sending ${if (isQuery) "query" else "command"} to device $deviceId: $command")

            val request = Request.Builder()
                .url(url)
                .post(document.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val success = response.isSuccessful
                Log.d("PCControl", "${if (isQuery) "Query" else "Command"} sent: ${response.code}")

                if (success) {
                    // Extract document ID from response
                    val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                    val docName = jsonResponse.optString("name", "")
                    val docId = docName.substringAfterLast("/")
                    Log.d("PCControl", "Created document ID: $docId")
                    return@withContext docId
                } else {
                    Log.e("PCControl", "Error: ${response.body?.string()}")
                    return@withContext null
                }
            }
        } catch (e: Exception) {
            Log.e("PCControl", "Error sending ${if (isQuery) "query" else "command"}", e)
            null
        }
    }

    /**
     * Wait for query result from PC (poll specific command document)
     */
    suspend fun waitForQueryResult(deviceId: String, commandId: String, timeout: Long = 30000): String? = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val pollInterval = 2000L // Poll every 2 seconds

            Log.d("PCControl", "Waiting for query result from device: $deviceId, command: $commandId")

            while (System.currentTimeMillis() - startTime < timeout) {
                // Get specific command document
                val url = "$firestoreBase/projects/$firebaseProjectId/databases/(default)/documents/Devices/$deviceId/Commands/$commandId?key=$firebaseApiKey"

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val docJson = JSONObject(response.body?.string() ?: "{}")
                        val fields = docJson.optJSONObject("fields")

                        if (fields != null) {
                            val isQuery = fields.optJSONObject("is_query")?.optBoolean("booleanValue") ?: false
                            val executed = fields.optJSONObject("executed")?.optBoolean("booleanValue") ?: false
                            val output = fields.optJSONObject("output")?.optString("stringValue") ?: ""
                            val status = fields.optJSONObject("status")?.optString("stringValue") ?: ""

                            Log.d("PCControl", "Poll: executed=$executed, isQuery=$isQuery, status=$status, output='$output'")

                            if (isQuery && executed && status == "completed") {
                                Log.d("PCControl", "Query result received: $output")
                                return@withContext output
                            }
                        }
                    } else {
                        Log.w("PCControl", "Failed to fetch command: ${response.code}")
                    }
                }

                // Wait before next poll
                kotlinx.coroutines.delay(pollInterval)
            }

            Log.w("PCControl", "Query result timeout after ${timeout}ms")
            null
        } catch (e: Exception) {
            Log.e("PCControl", "Error waiting for query result", e)
            null
        }
    }

    /**
     * Process user voice command and execute on PC
     */
    @Suppress("UnusedPrivateMember", "unused")
    suspend fun processVoiceCommand(userInput: String): String {
        try {
            val (pcNickname, cmdCommand) = parsePCCommand(userInput)

            if (cmdCommand == null) {
                return "I couldn't identify a valid command in that request, Sir."
            }

            if (pcNickname == null) {
                return "Please specify which PC you want to control, Sir."
            }

            // Find the PC
            val device = findDeviceByNickname(pcNickname)
            if (device == null) {
                return "I couldn't find a PC named '$pcNickname', Sir."
            }

            // Send command
            val commandId = sendCommandToPC(device.deviceId, cmdCommand)

            return if (commandId != null) {
                "Command sent to ${device.nickname}, Sir. Executing: $cmdCommand"
            } else {
                "Failed to send command to ${device.nickname}, Sir."
            }
        } catch (e: Exception) {
            Log.e("PCControl", "Error processing voice command", e)
            return "An error occurred while processing your command, Sir."
        }
    }

    private fun shouldTreatAsWebsite(originalTarget: String, cleanTarget: String): Boolean {
        return originalTarget.contains("website") ||
            originalTarget.contains("site") ||
            originalTarget.contains("web page") ||
            originalTarget.contains("webpage") ||
            looksLikeDomain(cleanTarget) ||
            isLikelyWebsite(cleanTarget)
    }

    private fun extractUrlFromText(text: String): String? {
        val urlWithScheme = Regex("https?://[^\\s]+", RegexOption.IGNORE_CASE).find(text)
        if (urlWithScheme != null) {
            return urlWithScheme.value
        }

        val wwwUrl = Regex("www\\.[^\\s]+", RegexOption.IGNORE_CASE).find(text)
        if (wwwUrl != null) {
            return "https://${wwwUrl.value}"
        }

        val domainMatch = Regex("\\b[a-z0-9-]+(\\.[a-z0-9-]+)+\\b", RegexOption.IGNORE_CASE).find(text)
        return domainMatch?.value?.let { "https://$it" }
    }

    private fun looksLikeDomain(value: String): Boolean {
        return Regex("^[a-z0-9-]+(\\.[a-z0-9-]+)+$", RegexOption.IGNORE_CASE).matches(value)
    }

    /**
     * Normalize application names to match CMD mappings
     */
    private fun normalizeAppName(appName: String): String {
        val normalized = appName.lowercase().trim()

        // Common variations
        return when {
            normalized.contains("file explorer") || normalized.contains("windows explorer") -> "file explorer"
            normalized.contains("task manager") -> "task manager"
            normalized.contains("control panel") -> "control panel"
            normalized.contains("command prompt") || normalized == "cmd" -> "command prompt"
            normalized == "powershell" || normalized == "power shell" -> "powershell"
            normalized == "calc" || normalized == "calculator" -> "calculator"
            normalized == "notepad" -> "notepad"
            normalized == "paint" -> "paint"
            normalized == "chrome" || normalized == "google chrome" -> "chrome"
            normalized == "edge" || normalized == "microsoft edge" -> "edge"
            normalized == "firefox" || normalized == "mozilla firefox" -> "firefox"
            else -> normalized
        }
    }
}


