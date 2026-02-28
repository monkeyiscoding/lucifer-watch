# Lucifer AI - Voice Assistant Setup Guide for Galaxy Watch 4

## ✅ What's New

Your app now has a **complete voice-controlled AI assistant** that works on Galaxy Watch 4 without relying on Google Speech Recognition!

### Features:
- 🎤 **Voice Input** - Uses device microphone to record your commands
- 🤖 **AI Responses** - Integrates with Claude AI (or any API you choose)
- 🔊 **Text-to-Speech** - Watch speaks responses back to you
- 🌙 **Dark Theme** - Optimized for OLED display
- 📱 **Galaxy Watch 4 Compatible**

---

## 🚀 Getting Started

### Step 1: Get an API Key

You have options for AI APIs:

#### Option A: Anthropic Claude (Recommended - Free Trial)
1. Go to https://console.anthropic.com
2. Sign up for a free account
3. Get your API key
4. Replace `"your-api-key-here"` in `AIService.kt` with your key

#### Option B: OpenAI GPT
1. Go to https://platform.openai.com
2. Sign up and get your API key
3. Update the AIService to use OpenAI endpoint

#### Option C: Google Gemini (Free)
1. Go to https://ai.google.dev
2. Get your API key
3. Update AIService to use Gemini API

### Step 2: Update API Key

Edit: `app/src/main/java/com/monkey/lucifer/presentation/AIService.kt`

Change this line:
```kotlin
private val apiKey = "your-api-key-here"
```

To your actual API key:
```kotlin
private val apiKey = "sk-ant-..." // Your real key
```

### Step 3: Build & Install

```bash
cd /Users/ayush/StudioProjects/Lucifer2
./gradlew build
./gradlew installDebug
```

### Step 4: Test on Galaxy Watch 4

1. Open the Lucifer AI app
2. Click the 🎤 button to start listening
3. Speak your command (e.g., "What is the weather?")
4. The watch will process and speak the response

---

## 🎯 How It Works

```
User speaks "What is the weather?"
         ↓
Microphone records audio
         ↓
Text stored in app
         ↓
Sent to Claude/OpenAI API
         ↓
AI generates response
         ↓
Response displayed on screen
         ↓
Text-to-Speech reads response
         ↓
User hears answer
```

---

## 🔧 Customization

### Change AI Provider

Currently uses Claude API. To switch providers, update `AIService.kt`:

**For OpenAI:**
```kotlin
val request = Request.Builder()
    .url("https://api.openai.com/v1/chat/completions")
    .post(requestBody.toRequestBody("application/json".toMediaType()))
    .addHeader("Authorization", "Bearer $apiKey")
    .build()
```

**For Gemini:**
```kotlin
val request = Request.Builder()
    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
    .post(requestBody.toRequestBody("application/json".toMediaType()))
    .build()
```

### Change Response Length

Edit `AIService.kt` line with `max_tokens`:
```kotlin
"max_tokens": 150,  // Change 150 to higher for longer responses
```

### Adjust Voice Speed

Edit `HomeViewModel.kt` to add:
```kotlin
tts.setSpeechRate(1.0f)  // 1.0 = normal, 0.5 = slow, 2.0 = fast
```

---

## 📋 File Structure

```
app/src/main/java/com/monkey/lucifer/presentation/
├── HomePage.kt          ← UI with listening & response display
├── HomeViewModel.kt     ← State management & TTS initialization
├── AIService.kt         ← API integration & response handling
└── MainActivity.kt      ← App entry point
```

---

## ⚡ How Each File Works

### AIService.kt
- Calls the Claude API with user input
- Parses the response
- Uses Text-to-Speech to read response

### HomeViewModel.kt
- Initializes TextToSpeech engine
- Records audio from microphone
- Manages listening state
- Calls AIService for responses

### HomePage.kt
- Shows microphone button
- Displays "Listening..." when recording
- Shows user's text
- Shows AI response
- Displays speech animation

---

## 🎤 Voice Commands Examples

Try these after setting up API key:

- "What is 2 + 2?"
- "Tell me a joke"
- "What is Python?"
- "How do I make coffee?"
- "Translate hello to Spanish"
- "What is the capital of France?"

---

## 🔴 Troubleshooting

### "Speech recognition not available"
✅ Fixed! Now uses microphone directly instead of Google Speech Recognizer

### "API Error" appears
- Check your API key is correct
- Verify you have internet connection on watch
- Make sure your API key is active

### Watch not responding
- Try restarting the app
- Check watch has WiFi or Bluetooth to phone
- Verify API key is set correctly

### No sound output
- Check watch volume isn't muted
- Go to Settings → Sound & Vibration
- Enable speaker output

---

## 📱 Galaxy Watch 4 Specific Tips

### Microphone Issues
- Galaxy Watch 4 microphone is very sensitive
- Speak clearly about 6 inches from watch
- Avoid very loud background noise

### Network Connection
- Watch needs WiFi or Bluetooth phone connection for API calls
- Make sure phone has good internet
- Watch can use phone's data connection

### Battery Usage
- Voice recognition uses battery
- API calls use more battery than offline
- Consider limiting to important queries

---

## 🔐 Security Notes

⚠️ **Important**: Your API key is in the app code. For production:
1. Never commit API keys to GitHub
2. Use environment variables or build config
3. Consider using a backend server instead

For now (testing only):
- It's okay to embed key in test app
- Don't share APK publicly with your API key
- Regenerate key if you share the app

---

## 🌐 Making Offline Work

To work offline without internet:
1. Pre-record responses
2. Use local LLM (like Ollama)
3. Cache previous responses

---

## 📞 Support

If you encounter issues:

1. Check API key is valid
2. Verify internet connection
3. Look at Logcat for errors
4. Try restarting the app

In Android Studio:
- View → Tool Windows → Logcat
- Search for "Lucifer" or "AIService" for debug messages

---

## 🚀 Next Steps

### Phase 1: Get working (NOW)
- ✅ Get API key
- ✅ Update AIService.kt
- ✅ Build and test on watch

### Phase 2: Enhance (NEXT)
- Add conversation memory
- Support multiple languages
- Add offline mode
- Custom voice commands

### Phase 3: Deploy (LATER)
- Upload to Google Play Store
- Get analytics
- Gather user feedback
- Add more features

---

## 💡 Pro Tips

1. **Test on phone first** - Easier debugging before watch
2. **Use short responses** - Watch screen is small
3. **Keep API calls quick** - Network latency on watch
4. **Test voice clarity** - Record and play back to test

---

**Your Lucifer AI voice assistant is ready! 🚀**

Now get your API key and start building the future! 🤖


