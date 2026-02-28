# 🔧 Troubleshooting Guide

## Common Issues and Solutions

### 1. "OpenAI service not initialized"

**Symptom:** App crashes or shows error when trying to build website

**Solution:**
```bash
# Check if API key is set
cat local.properties | grep OPENAI_API_KEY

# If not set, add it:
echo "OPENAI_API_KEY=your-actual-api-key-here" >> local.properties

# Rebuild
./gradlew clean build
```

---

### 2. "Firebase upload failed: 404"

**Symptom:** Website builds but upload fails with 404 error

**Solution:**

1. **Check Firebase Storage bucket name:**
   ```kotlin
   // In FirebaseStorageService.kt
   private val bucketName = "lucifer-97501.firebasestorage.app"
   ```

2. **Verify Firebase Storage rules:**
   ```javascript
   rules_version = '2';
   service firebase.storage {
     match /b/{bucket}/o {
       match /websites/{websiteId}/{allPaths=**} {
         allow read: if true;
         allow write: if true;
       }
     }
   }
   ```

3. **Check Firebase console:**
   - Go to Firebase Console → Storage
   - Verify bucket exists
   - Check if files are being created

---

### 3. "Could not detect speech" (Always shows)

**Symptom:** Even when speaking clearly, app shows "Could not detect speech"

**Solution:**

1. **Check microphone permission:**
   ```bash
   adb shell dumpsys package com.monkey.lucifer | grep RECORD_AUDIO
   ```

2. **Test microphone:**
   - Open voice recorder on watch
   - Speak and verify it records

3. **Check logcat:**
   ```bash
   adb logcat | grep "OpenAI\|MediaRecorder\|HomeViewModel"
   ```

---

### 4. QR Code shows but website doesn't open

**Symptom:** QR code scans successfully but website shows 404 or doesn't load

**Solutions:**

**A. Check URL format:**
```bash
# Correct Firebase URL format:
https://firebasestorage.googleapis.com/v0/b/lucifer-97501.firebasestorage.app/o/websites%2F{id}%2Findex.html?alt=media

# Correct GitHub URL format:
https://{username}.github.io/{repo}/{projectId}/
```

**B. Check file upload:**
```bash
adb logcat | grep "FirebaseStorage\|GitHubService"
```

**C. Manually verify upload:**
- Firebase: Check Storage tab in Firebase Console
- GitHub: Check repository on GitHub.com

---

### 5. CSS/JS files not loading

**Symptom:** Website opens but styling is missing, no interactivity

**Solutions:**

**A. Check file paths in index.html:**
```html
<!-- CORRECT -->
<link rel="stylesheet" href="styles.css">
<script src="script.js"></script>

<!-- WRONG -->
<link rel="stylesheet" href="./folder/styles.css">
<script src="./folder/script.js"></script>
```

**B. Verify all files uploaded:**
```bash
# Check logcat for upload confirmations
adb logcat | grep "Uploaded.*html\|Uploaded.*css\|Uploaded.*js"
```

**C. Check AI response:**
```bash
# Look for file separator markers
adb logcat | grep "index.html ---\|styles.css ---\|script.js ---"
```

---

### 6. Watch screen keeps turning off

**Symptom:** Screen dims or turns off while using the app

**Solution:**

1. **Check WakeLock acquisition:**
   ```bash
   adb logcat | grep "WakeLock"
   ```

2. **Verify WakeLock permission in manifest:**
   ```xml
   <uses-permission android:name="android.permission.WAKE_LOCK" />
   ```

3. **Increase wake time:**
   ```kotlin
   // In HomeViewModel.kt
   wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes
   // Change to 30 minutes if needed:
   wakeLock?.acquire(30 * 60 * 1000L)
   ```

---

### 7. "You said: You" appears when silent

**Symptom:** Tap mic, say nothing, stop → shows "You said: You"

**Solution:** Already fixed! Check implementation:

```kotlin
// In HomeViewModel.kt stopRecordingAndProcess()
if (transcript.isBlank()) {
    _status.value = "Idle"
    _recognizedText.value = ""
    _error.value = "Could not detect speech"
    return@launch
}
```

If still happening, rebuild:
```bash
./gradlew clean assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

### 8. Website name not parsed correctly

**Symptom:** Says "Lucifer build website Falcon" but name shows as "My Website"

**Solutions:**

**A. Test regex patterns:**
```kotlin
val command = "Lucifer build website Falcon"
val result = parseWebsiteCommand(command)
// Should return "Falcon"
```

**B. Check logs:**
```bash
adb logcat | grep "Pattern.*matched\|parsed name"
```

**C. Supported patterns:**
```
✅ "website name is Falcon"
✅ "create website Falcon"
✅ "build a Falcon website"
✅ "make website called Falcon"
❌ "Falcon" (too short, needs context)
```

---

### 9. Build times out (takes too long)

**Symptom:** "Website generation timed out" error

**Solutions:**

**A. Increase timeout:**
```kotlin
// In AIService.kt
val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)  // Increase if needed
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()
```

**B. Check OpenAI API status:**
- Visit https://status.openai.com/

**C. Reduce max_tokens:**
```kotlin
// In AIService.kt generateWebsite()
put("max_tokens", 12000)  // Reduce to 8000 if timing out
put("temperature", 0.7)
```

---

### 10. GitHub upload fails but Firebase works

**Symptom:** Firebase URL works but GitHub upload shows error

**Solutions:**

**A. Check GitHub token:**
```bash
cat local.properties | grep GITHUB_TOKEN
# Should be a valid Personal Access Token
```

**B. Verify token permissions:**
- Go to GitHub → Settings → Developer Settings → Personal Access Tokens
- Ensure token has `repo` scope

**C. Check repository:**
```bash
# In GitHubService.kt
private val owner = "monkeyiscoding"
private val repo = "websites"
# Verify this repo exists and token has access
```

**D. Test manually:**
```bash
curl -H "Authorization: token YOUR_TOKEN" \
  https://api.github.com/repos/monkeyiscoding/websites
```

---

## Debug Commands

### View all logs
```bash
adb logcat | grep "lucifer\|HomeViewModel\|OpenAI\|Firebase\|GitHub"
```

### View specific errors
```bash
adb logcat | grep -i "error\|exception\|failed"
```

### View website generation
```bash
adb logcat | grep "WebsiteBuilder\|generateWebsite\|uploadWebsite"
```

### View file parsing
```bash
adb logcat | grep "parseGeneratedFiles\|filesMap\|index.html"
```

### Clear app data (fresh start)
```bash
adb shell pm clear com.monkey.lucifer
```

---

## Performance Optimization

### If website generation is slow:

1. **Use gpt-4o-mini instead of gpt-4o:**
   ```kotlin
   put("model", "gpt-4o-mini")  // Faster, cheaper
   ```

2. **Reduce complexity:**
   ```kotlin
   val details = WebsiteDetails(
       name = websiteName,
       description = "A simple website",  // Less complex
       additionalFeatures = listOf("basic design")  // Fewer features
   )
   ```

3. **Cache results:**
   - Generate once, store in database
   - Reuse for similar requests

---

## Testing Checklist

Before reporting a bug, try:

- [ ] Rebuild app: `./gradlew clean build`
- [ ] Reinstall: `adb install app/build/outputs/apk/debug/app-debug.apk`
- [ ] Clear data: `adb shell pm clear com.monkey.lucifer`
- [ ] Check logs: `adb logcat | grep HomeViewModel`
- [ ] Verify permissions: Audio recording, Wake Lock
- [ ] Test on different command: Try "create website Test123"
- [ ] Check API keys: OpenAI, Firebase, GitHub tokens
- [ ] Verify internet: Watch must have network access

---

## Getting Help

If issue persists:

1. **Collect logs:**
   ```bash
   adb logcat > lucifer_logs.txt
   # Reproduce issue
   # Ctrl+C to stop
   ```

2. **Check these files:**
   - `HomeViewModel.kt` - Main logic
   - `AIService.kt` - Website generation
   - `FirebaseStorageService.kt` - File upload
   - `build.gradle.kts` - Dependencies

3. **Provide details:**
   - Exact command spoken
   - Error message shown
   - Logcat output
   - Device model and OS version

---

## Known Limitations

1. **Voice Recognition:**
   - Works best with clear speech
   - Requires quiet environment
   - English language only

2. **Website Generation:**
   - Takes 15-30 seconds
   - Requires active internet
   - Limited by OpenAI API rate limits

3. **File Upload:**
   - Max file size: 10MB per file
   - Requires Firebase/GitHub to be configured
   - Network speed affects upload time

4. **Watch Hardware:**
   - Limited screen size (450x450 typical)
   - Battery drain during long operations
   - Storage space needed for APK

---

## Emergency Reset

If app is completely broken:

```bash
# 1. Clear all data
adb shell pm clear com.monkey.lucifer

# 2. Uninstall
adb uninstall com.monkey.lucifer

# 3. Clean build
cd /Users/ayush/StudioProjects/Lucifer2
./gradlew clean

# 4. Rebuild
./gradlew assembleDebug

# 5. Reinstall
adb install app/build/outputs/apk/debug/app-debug.apk

# 6. Test with simple command
# Say: "Lucifer create website Test"
```

---

**Last Updated:** February 28, 2026  
**For support:** Check `FINAL_IMPLEMENTATION_STATUS.md` for complete feature documentation

