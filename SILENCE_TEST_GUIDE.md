# Quick Test Guide - Silence Detection

## What Changed
The app now stops recording **immediately after you finish speaking** (150ms silence confirmation).

Before: 4-5 seconds delay ❌
After: 150ms delay ✅

## How to Test

### Test 1: Single Word
```
1. Open the app
2. Say: "hello"
3. Wait for silence
4. Expected: Stops immediately, processes "hello"
```

### Test 2: Sentence
```
1. Open the app
2. Say: "create a website called my portfolio"
3. Wait for silence
4. Expected: Stops after you finish, shows preview
```

### Test 3: Watch the Logs
```
adb logcat | grep HomeViewModel
```

You should see this pattern:
```
🎤 Recording STARTED - Listening...
🔊 REAL SPEECH DETECTED! (Amplitude: 1500)
🔊 Loud speech: 2000
↩️ Decay/Noise detected (amplitude 567) - timer reset
🛑 REAL SILENCE DETECTED! Starting 150ms confirmation timer...
✋ STOPPING! (Real silence confirmed: 150ms, max speech amplitude: 2000)
```

### Test 4: Background Noise
- Speak normally, not loudly
- The 150ms timer only starts when amplitude < 250
- Background noise > 250 won't trigger false stops

## Key Indicators

✓ **Working**:
- Stops 150-300ms after you finish speaking
- Logs show three zones clearly
- No 4-5 second delays

✗ **Not Working**:
- Still delayed 2+ seconds
- Logs don't show "REAL SILENCE DETECTED"
- Frequent false stops from noise

## Build & Test

```bash
# Build
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Watch logs
adb logcat | grep HomeViewModel
```

## If It's Still Not Working

1. Check logs - which zone is being detected?
2. Try speaking louder (should go to Zone 1: SPEECH)
3. Try in a quiet room (silence should be Zone 2: <250 amplitude)
4. If still slow, check if `silenceDetectionJob` is running (look for "Recording STARTED")

## Parameters to Tune (if needed)

In `HomeViewModel.kt`, lines ~300:

```kotlin
const val SPEECH_THRESHOLD = 800        // Increase if ambient noise > 800
const val SILENCE_THRESHOLD = 250       // Decrease if false stops, increase if sluggish
const val SILENCE_CONFIRMATION_MS = 150 // Decrease for faster stop (100ms), increase for stability (200ms)
```

---

**Good luck! The fix is deployed - you should see instant stopping now! 🚀**

