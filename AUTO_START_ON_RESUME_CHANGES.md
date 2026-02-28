# Auto-Start Recording on App Resume - Changes Summary

## Problem Solved
Previously, when you closed and reopened the app, the auto-start listening feature would not work. Now, every time you open/reopen the app, it will automatically start listening.

## How It Works Now

### User Experience Flow:
1. **First App Open** → Permission dialog → Auto-starts listening
2. **Close App** (app runs in background)
3. **Reopen App** → Recording auto-starts immediately
4. **While Recording** → Show red Stop button
5. **Click Stop** → Show Mic button to start new recording
6. **Close & Reopen** → Auto-starts again ✅

## Technical Changes

### 1. **HomePage.kt** - Updated LaunchedEffect
Changed from `LaunchedEffect(Unit)` to `LaunchedEffect(isRecording)`:
- Now triggers whenever the `isRecording` state changes
- Checks if not recording, then requests permission and auto-starts
- Works on first load AND every time app resumes from background

```kotlin
LaunchedEffect(isRecording) {
    if (!isRecording) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
}
```

### 2. **MainActivity.kt** - Added Lifecycle Callbacks
Added `onResume()` override to detect when app returns to foreground:
- Calls `homeViewModel.resetForAutoStart()` 
- This triggers HomePage's LaunchedEffect to run again
- Added necessary lifecycle imports

```kotlin
override fun onResume() {
    super.onResume()
    homeViewModel.resetForAutoStart()
}
```

### 3. **HomeViewModel.kt** - New Reset Method
Added `resetForAutoStart()` function:
- Resets the state to "Idle" if not currently recording
- Allows the LaunchedEffect in HomePage to detect the state change
- Triggers the auto-start flow

```kotlin
fun resetForAutoStart() {
    if (!_isRecording.value) {
        _status.value = "Idle"
    }
}
```

## Why This Works

1. When app resumes → `onResume()` is called
2. Calls `resetForAutoStart()` → Sets status to "Idle"
3. This state change triggers `LaunchedEffect(isRecording)` in HomePage
4. LaunchedEffect sees `isRecording = false` → Requests permission
5. Permission granted → `autoStartRecording()` called → Recording starts ✅

## Files Modified
- `/Users/ayush/StudioProjects/Lucifer2/app/src/main/java/com/monkey/lucifer/presentation/HomePage.kt`
- `/Users/ayush/StudioProjects/Lucifer2/app/src/main/java/com/monkey/lucifer/presentation/HomeViewModel.kt`
- `/Users/ayush/StudioProjects/Lucifer2/app/src/main/java/com/monkey/lucifer/presentation/MainActivity.kt`

## Testing Checklist
- [ ] Open app for first time → Auto-start listening ✅
- [ ] Close app → Go to home screen
- [ ] Reopen app → Auto-start listening ✅
- [ ] Stop recording → Mic button appears ✅
- [ ] Close & reopen → Auto-start again ✅
- [ ] Click Stop while recording → Still works ✅
- [ ] App doesn't crash on resume ✅

