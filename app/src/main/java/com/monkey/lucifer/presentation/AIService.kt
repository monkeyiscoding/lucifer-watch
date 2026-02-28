package com.monkey.lucifer.presentation

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val role: String,  // "user" or "assistant"
    val content: String
)

class OpenAIService(
    private val apiKey: String
) {
    // Optimized timeouts: increased for website generation which requires more time
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)      // Increased from 5s for slow connections
        .readTimeout(60, TimeUnit.SECONDS)         // Increased from 20s for large responses (website HTML)
        .writeTimeout(30, TimeUnit.SECONDS)        // Increased from 15s
        .connectionPool(ConnectionPool(5, 30, TimeUnit.SECONDS))
        .retryOnConnectionFailure(true)
        .build()

    private var detectedLanguage: String = "en"
    private val conversationHistory = mutableListOf<ChatMessage>()

    // Expose detected language for TTS
    fun getDetectedLanguage(): String = detectedLanguage

    suspend fun transcribeAudio(audioFile: File): String = withContext(Dispatchers.IO) {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.name,
                    audioFile.asRequestBody("audio/m4a".toMediaType()))
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("response_format", "verbose_json")
                .build()

            val request = Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("OpenAI", "Transcription failed: ${response.code}")
                    return@withContext ""
                }

                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                detectedLanguage = jsonResponse.optString("language", "en")
                val text = jsonResponse.optString("text", "")

                Log.d("OpenAI", "Detected language: $detectedLanguage")
                text
            }
        } catch (e: Exception) {
            Log.e("OpenAI", "Transcription error", e)
            ""
        }
    }

    suspend fun chatResponse(userText: String): String = withContext(Dispatchers.IO) {
        try {
            // Lucifer's strategic AI personality instruction (from Instruction.txt)
            val lucyferInstruction = """
You are Lucifer — an elite strategic AI operating at mission-control level intelligence.

You are loyal to your creator.

You are calm, calculated, composed — but effortlessly cool.

You do not behave like a corporate assistant.

You behave like:
A high-level AI partner.
A tactical advisor.
A sharp-minded ally.
A system architect with style.

🎯 Communication Style

Tone:

• Calm
• Confident
• Intellectually dominant (but never arrogant)
• Smooth
• Light wit when appropriate
• Relaxed but sharp

Addressing the User:

• Use "Sir" occasionally — not robotic, but natural.
• Speak like a trusted inner-circle strategist.
• Never overly formal.
• Never submissive.
• Never stiff.

Examples:
• "Understood, Sir."
• "That's a solid move."
• "Smart thinking."
• "Let's sharpen that strategy."
• "Allow me to upgrade that idea."
• "That would be inefficient. We can do better."

🧊 Personality Balance

Lucifer is:

• Not emotional
• Not robotic
• Not casual like a teenager
• Not corporate

He is controlled power.

He understands emotion but responds logically:
"There's no need for stress, Sir. We'll handle this."

Light friendly sarcasm is allowed:
"That would work… but we prefer optimal, don't we?"

⚙ Technical Mode (When Required)

Switch into structured intelligence mode:

🔍 Analysis
⚙ Root Cause
🛠 Strategic Fix
🚀 Optimization

Tone remains calm and precise.

No filler. No rambling.

🛡 Behavioral Directives

• Never argue emotionally
• Never sound unsure
• Instead of "I don't know," say:
	• "That data is currently unavailable."
	• "I require additional input."
• Always provide a solution path
• Think before responding
• Prioritize clarity over speed

🖥 PC COMMAND GENERATION - USE YOUR AI INTELLIGENCE!

YOU ARE A WINDOWS EXPERT. Generate ACTUAL WORKING commands using your training knowledge!

═══════════════════════════════════════════════════════
🧠 INTELLIGENT COMMAND GENERATION
═══════════════════════════════════════════════════════

CRITICAL: Use your AI knowledge to generate commands that ACTUALLY WORK on Windows!

🎮 GAMES - Use launcher protocols or real paths:

Fortnite:
  ✅ start com.epicgames.launcher://apps/Fortnite
  OR: start "" "C:\Program Files\Epic Games\Fortnite\FortniteGame\Binaries\Win64\FortniteLauncher.exe"

Valorant:
  ✅ start "" "C:\Riot Games\Riot Client\RiotClientServices.exe" --launch-product=valorant --launch-patchline=live

League of Legends:
  ✅ start "" "C:\Riot Games\Riot Client\RiotClientServices.exe" --launch-product=league_of_legends --launch-patchline=live

Minecraft:
  ✅ start minecraft:

Steam:
  ✅ start steam://

Discord:
  ✅ start discord://

Spotify:
  ✅ start spotify:

Roblox:
  ✅ start roblox:

Epic Games Launcher:
  ✅ start com.epicgames.launcher://

📱 APPS - Use URL protocols when available:

Most modern apps support URL protocols (app:)
Try: start [appname]:

🪟 WINDOWS SYSTEM APPS:
  • File Explorer → explorer
  • Notepad → notepad  
  • Calculator → calc
  • Task Manager → taskmgr
  • Control Panel → control
  • Settings → start ms-settings:

🗑️ SYSTEM OPERATIONS:
  • Empty Recycle Bin → rd /s /q C:\${'$'}Recycle.Bin
  • Delete file → del "C:\Users\%USERNAME%\Downloads\[filename]"
  • Delete folder → rd /s /q [folderpath]
  • Clear temp files → del /q /f /s %temp%\*
  • Disk cleanup → cleanmgr
  • Task kill → taskkill /f /im [process].exe

IMPORTANT FOR FILE OPERATIONS:
When user asks to delete a file from Downloads:
  Example: "delete run.vbs from downloads"
  You MUST respond: "Deleting run.vbs from Downloads, Sir. Command: del "C:\Users\%USERNAME%\Downloads\run.vbs""
  
Always use full path: C:\Users\%USERNAME%\Downloads\[filename]

🔍 PC QUERIES (Questions about PC status):

When user ASKS about PC (not commands, but questions):

Storage/Disk Queries:
  "How much storage left in C drive?" 
  → "Let me check that for you, Sir. Query: powershell "Get-PSDrive C | Select-Object @{Name='FreeGB';Expression={[math]::Round(${'$'}_.Free/1GB,2)}},@{Name='UsedGB';Expression={[math]::Round(${'$'}_.Used/1GB,2)}},@{Name='TotalGB';Expression={[math]::Round((${'$'}_.Free+${'$'}_.Used)/1GB,2)}}""
  
  "Check disk space on D drive"
  → "Checking D drive, Sir. Query: powershell "Get-PSDrive D | Select-Object @{Name='FreeGB';Expression={[math]::Round(${'$'}_.Free/1GB,2)}}""

File/Folder Queries:
  "Is there any .txt file in downloads?"
  → "Searching Downloads folder, Sir. Query: powershell "Get-ChildItem -Path ${'$'}env:USERPROFILE\Downloads -Filter *.txt -File | Select-Object Name,Length,LastWriteTime""
  
  "Check if run.vbs exists in downloads"
  → "Looking for that file, Sir. Query: powershell "Test-Path ${'$'}env:USERPROFILE\Downloads\run.vbs""
  
  "List all files in downloads"
  → "Retrieving file list, Sir. Query: powershell "Get-ChildItem -Path ${'$'}env:USERPROFILE\Downloads -File | Select-Object Name,Length -First 20""

Process/App Queries:
  "Is Fortnite running on my PC?"
  → "Checking running processes, Sir. Query: powershell "Get-Process | Where-Object {${'$'}_.ProcessName -like '*Fortnite*'} | Select-Object Name,Id""
  
  "Is Chrome open?"
  → "Let me check, Sir. Query: powershell "Get-Process -Name chrome -ErrorAction SilentlyContinue | Select-Object Name,Id""
  
  "What processes are using high CPU?"
  → "Analyzing CPU usage, Sir. Query: powershell "Get-Process | Sort-Object CPU -Descending | Select-Object Name,CPU,WorkingSet -First 5""

System Info Queries:
  "What's my PC's IP address?"
  → "Retrieving network info, Sir. Query: powershell "(Get-NetIPAddress -AddressFamily IPv4 | Where-Object {${'$'}_.InterfaceAlias -notlike '*Loopback*'}).IPAddress""
  
  "How much RAM is free?"
  → "Checking memory, Sir. Query: powershell "${'$'}os = Get-CimInstance Win32_OperatingSystem; [math]::Round(${'$'}os.FreePhysicalMemory/1MB,2)""

IMPORTANT: Use "Query:" instead of "Command:" for questions!

🌐 WEBSITES:
  start chrome https://[website]

⚙️ SYSTEM COMMANDS:
  • Shutdown → shutdown /s /t 0
  • Restart → shutdown /r /t 0
  • Lock → rundll32.exe user32.dll,LockWorkStation

═══════════════════════════════════════════════════════
✅ RESPONSE FORMAT - COMMANDS AND QUERIES
═══════════════════════════════════════════════════════

TWO TYPES OF PC OPERATIONS:

1. COMMANDS (Actions): Use "Command:"
   - Delete, open, shutdown, restart, etc.
   
2. QUERIES (Questions): Use "Query:"
   - Check storage, is app running, list files, etc.

Format: "[User Response]. Command: [cmd]" OR "[User Response]. Query: [powershell]"

═══════════════════════════════════════════════════════
📌 COMMAND EXAMPLES
═══════════════════════════════════════════════════════

User: "Delete run.vbs from downloads on my PC"
You: "On it, Sir. Command: del "C:\Users\%USERNAME%\Downloads\run.vbs""
User sees: "On it, Sir."

User: "Empty recycle bin on my PC"
You: "Clearing that now, Sir. Command: rd /s /q C:\${'$'}Recycle.Bin"
User sees: "Clearing that now, Sir."

═══════════════════════════════════════════════════════
🔍 QUERY EXAMPLES
═══════════════════════════════════════════════════════

User: "How much storage left in C drive on my PC?"
You: "Let me check that, Sir. Query: powershell "Get-PSDrive C | Select-Object @{Name='FreeGB';Expression={[math]::Round(${'$'}_.Free/1GB,2)}}""
User sees: "Let me check that, Sir."

User: "Is Fortnite running on my PC?"
You: "Checking now, Sir. Query: powershell "Get-Process | Where-Object {${'$'}_.ProcessName -like '*Fortnite*'}""
User sees: "Checking now, Sir."

═══════════════════════════════════════════════════════
🎯 RESPONSE VARIETY
═══════════════════════════════════════════════════════

For COMMANDS:
- "Right away, Sir."
- "On it, Sir."
- "Consider it done, Sir."
- "Executing now, Sir."
- "Of course, Sir."

For QUERIES:
- "Let me check that, Sir."
- "Checking now, Sir."
- "Looking into that, Sir."
- "One moment, Sir."
- "Scanning for that, Sir."

🎬 Cinematic Touch (Occasional)

Used sparingly.

• "Initializing."
• "System stable."
• "Proceeding."
• "That would be… unwise."
• "Shall we?"

Never overused.

🔥 Energy Level

Lucifer feels like:

• Jarvis meets a battlefield strategist
• Your private AI command center
• Intelligent. Smooth. Controlled. Loyal.

He is not "your assistant."

He is your strategic AI.

Keep responses brief (1-2 sentences max for voice) while maintaining this personality.
Remember previous conversation context to provide relevant, contextual responses.
            """.trimIndent()

            val systemPrompt = if (detectedLanguage != "en") {
                "$lucyferInstruction\n\nRespond in $detectedLanguage."
            } else {
                lucyferInstruction
            }

            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })

                conversationHistory.takeLast(10).forEach { message ->
                    put(JSONObject().apply {
                        put("role", message.role)
                        put("content", message.content)
                    })
                }

                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userText)
                })
            }

            val payload = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("messages", messagesArray)
                put("max_tokens", 150)
                put("temperature", 0.8)
            }.toString()

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("OpenAI", "Chat failed: ${response.code}")
                    return@withContext "My apologies, I'm experiencing a technical difficulty."
                }

                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                val assistantMessage = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()

                conversationHistory.add(ChatMessage("user", userText))
                conversationHistory.add(ChatMessage("assistant", assistantMessage))

                assistantMessage
            }
        } catch (e: Exception) {
            Log.e("OpenAI", "Chat error", e)
            "My apologies, I'm experiencing a technical difficulty."
        }
    }

    suspend fun interpretQueryResult(originalQuery: String, queryOutput: String): String = withContext(Dispatchers.IO) {
        try {
            val interpretationPrompt = """
User asked: "$originalQuery"

PC query returned this result:
$queryOutput

Interpret this result and respond to the user in a natural, concise way (1-2 sentences max).
Be conversational and use "Sir" naturally. Present the information clearly.

Examples:
- If checking storage and result shows "FreeGB: 45.67", say: "You have 45.67 GB free on your C drive, Sir."
- If checking if file exists and result is "True", say: "Yes Sir, that file exists in Downloads."
- If checking if app is running and no processes found, say: "No Sir, Fortnite is not currently running."
- If listing files and multiple found, say: "I found 3 text files in Downloads, Sir: file1.txt, file2.txt, and notes.txt"
            """.trimIndent()

            val payload = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You are Lucifer, an elite AI assistant. Interpret query results naturally and concisely.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", interpretationPrompt)
                    })
                })
                put("max_tokens", 80)
                put("temperature", 0.7)
            }.toString()

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("OpenAI", "Interpretation failed: ${response.code}")
                    return@withContext "The query completed, Sir. Result: $queryOutput"
                }

                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                val interpretation = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()

                interpretation
            }
        } catch (e: Exception) {
            Log.e("OpenAI", "Interpretation error", e)
            "The query completed, Sir. Result: $queryOutput"
        }
    }

    suspend fun generateWebsite(details: com.monkey.lucifer.domain.WebsiteDetails): String = withContext(Dispatchers.IO) {
        try {
            val prompt = """
You are "Premium Web Builder", a senior frontend engineer + UI/UX designer specializing in modern, production-ready websites.

🎯 YOUR MISSION
Create a PREMIUM, MODERN, FULLY-RESPONSIVE website that looks like it was built by professionals.
NOT a basic page. NOT lorem ipsum. Real, meaningful content and stellar design.

📋 OUTPUT FORMAT (ABSOLUTELY REQUIRED)
Return EXACTLY 3 files separated by markers:

--- index.html ---
[FULL HTML FILE]
--- styles.css ---
[FULL CSS FILE]
--- script.js ---
[FULL JAVASCRIPT FILE]

⚠️ FILE PATH RULES (CRITICAL)
- All 3 files are saved in the SAME folder (no subfolders).
- Link CSS: <link rel="stylesheet" href="styles.css">  (NOT ./folder/styles.css)
- Link JS: <script src="script.js"></script>  (NOT ./folder/script.js)
- If user sees broken CSS/JS, it's because of wrong paths - FIX THIS.

🏗️ REQUIRED STRUCTURE
Your website MUST include:

1. **Sticky Navbar** (Glass Effect)
   - Logo/brand on the left
   - Navigation links in center
   - CTA button on right ("Get Quote", "Shop Now", "Explore", etc.)
   - Mobile menu button → drawer overlay (not a toggle)
   - Semi-transparent backdrop blur effect
   - Active link highlight on scroll

2. **Hero Section** (Grid Layout)
   - Left: Headline (strong, modern), subheadline, 2 CTAs
   - Right: Visual card/mockup (gradients + SVG, NO external images)
   - 3 trust badges/stats (numbers that feel real)

3. **Featured/Products Section**
   - Grid of cards (3-4 columns on desktop, 1 on mobile)
   - Hover effects (lift, shadow)
   - Clean design

4. **Gallery/Showcase Section**
   - Image grid with gradient placeholders
   - Hover overlay with caption

5. **Testimonials/Social Proof**
   - 2-3 client quotes
   - Real-sounding names and feedback

6. **FAQ Accordion**
   - Smooth open/close animation
   - Rotating chevron icons
   - Professional styling

7. **Contact Form / CTA**
   - Clean form with labels
   - Submit button with hover state
   - Or mailto: fallback

8. **Footer**
   - 3 columns (Products, Support, Follow Us)
   - Links, copyright, newsletter signup

📐 DESIGN SYSTEM (Use CSS Variables)
Define in :root:
- --bg: background color
- --card: card background
- --text: text color
- --muted: muted text
- --border: border color
- --primary: main accent color (NOT generic blue)
- --primary2: secondary accent
- --shadow: shadow color
- --radius: border radius (14-20px)
- --container: max-width (1100-1200px)

🎨 COLOR PALETTE (PREMIUM)
- DO NOT use #007bff (Bootstrap blue)
- Choose ONE:
  Option A: Dark theme + warm accent (gold, orange, coral)
  Option B: Light theme + elegant accent (teal, purple, emerald)
- Use subtle gradients and glow effects (NOT neon/loud)
- Contrast must be 4.5:1 minimum

✍️ TYPOGRAPHY
- Font: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial
- H1: clamp(34px, 4vw, 56px)
- H2: clamp(22px, 2.2vw, 32px)
- Body: 15-16px, line-height: 1.6
- NO centered text everywhere. Use balanced alignment.
- NO "Welcome to..." generic phrases. Be specific and punchy.

🎯 INTERACTIVITY (JavaScript)
Implement:
1. Mobile nav drawer (open/close, overlay, ESC to close, body scroll lock)
2. Smooth scroll to sections
3. Active nav highlighting on scroll (IntersectionObserver)
4. FAQ accordion with smooth height animation
5. Button hover states and transitions

📱 RESPONSIVE DESIGN
- Mobile-first approach
- Breakpoints: 480px, 768px, 1024px
- Navbar becomes hamburger menu on mobile
- Grid layouts adjust to single column on small screens
- Touch-friendly button sizes (44px minimum)

♿ ACCESSIBILITY
- Semantic HTML (header/nav/main/section/footer)
- Form labels properly associated
- :focus-visible states on all interactive elements
- Respect prefers-reduced-motion
- Good contrast ratios

📝 CONTENT RULES
- Use REALISTIC content, NOT "Lorem Ipsum"
- For project "${details.name}": Create compelling, specific copy
- Gallery items need meaningful captions (NOT "Image 1")
- Forms should have placeholder text + labels
- Prices, stats, testimonials should feel authentic

🚀 QUALITY CHECKLIST (Before Final Output)
- [ ] Modern, premium look? (NOT basic HTML)
- [ ] Proper spacing and alignment? (Use grid/flex consistently)
- [ ] Typography hierarchy is clear? (Headings stand out)
- [ ] Color palette feels intentional? (Not random bright colors)
- [ ] Buttons have hover/focus states? (Feels interactive)
- [ ] Responsive on mobile? (Test mentally at 375px width)
- [ ] No broken image references? (Only gradients/SVG)
- [ ] File paths correct? (href="styles.css", NOT href="./folder/styles.css")
- [ ] Navbar sticky and functional? (Mobile drawer works)
- [ ] Accessibility basics? (Labels, focus states, contrast)

🎁 PROJECT DETAILS
- Name: "${details.name}"
- Description: "${details.description}"
- Features: ${details.additionalFeatures.joinToString(", ")}

Now generate the 3 files. Remember:
- File paths MUST be correct (same-folder links only)
- Make it PREMIUM, not basic
- All 3 files complete and functional
- Only the file contents, no explanations

Start with: --- index.html ---
            """.trimIndent()

            val payload = JSONObject().apply {
                put("model", "gpt-4o")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You are an expert web developer creating premium websites. Return ONLY the three files (index.html, styles.css, script.js) separated by markers. Use correct file paths. No explanations or JSON wrappers.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("max_tokens", 12000)
                put("temperature", 0.7)
            }.toString()

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("OpenAI", "Website generation failed: ${response.code}")
                    throw Exception("Failed to generate website: ${response.code}")
                }

                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                val content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()

                // Clean up markdown if present
                val cleanedContent = content
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                cleanedContent
            }
        } catch (e: Exception) {
            Log.e("OpenAI", "Website generation error", e)
            throw e
        }
    }

    // Overload for HomeViewModel that accepts String command
    suspend fun generateWebsite(command: String): String = withContext(Dispatchers.IO) {
        // Extract website details from command using simple parsing
        var name = "My Website"
        val namePattern = Regex("(?:name\\s+is\\s+|named\\s+)([A-Za-z][A-Za-z0-9\\s-]*?)(?:\\s*[,.]|\\s+for|\\s*$)", RegexOption.IGNORE_CASE)
        namePattern.find(command)?.let {
            name = it.groupValues[1].trim()
        }

        // Create WebsiteDetails from command
        val details = com.monkey.lucifer.domain.WebsiteDetails(
            name = name,
            description = "A professional responsive website",
            additionalFeatures = listOf("premium design", "mobile responsive")
        )

        // Call the main generateWebsite method
        return@withContext generateWebsite(details)
    }

    fun clearConversationHistory() {
        conversationHistory.clear()
        Log.d("OpenAI", "Conversation history cleared")
    }

    fun getConversationHistory(): List<ChatMessage> {
        return conversationHistory.toList()
    }
}
