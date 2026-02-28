# User Guide - New Settings Features

## Settings Button Location
**Home Screen → Top Right Corner**
- Look for the gear icon (⚙️)
- Tap to open Settings

## Settings Screen Overview

```
┌─────────────────────────────────────┐
│  ← Settings                         │
├─────────────────────────────────────┤
│                                     │
│  Real-Time Speak                  ◎ │  (red = enabled)
│  Speak AI response automatically    │
│                                     │
│  ─────────────────────────────────  │
│                                     │
│  Push-To-Talk                     ◯ │  (gray = disabled)
│  Hold mic button to record         │
│                                     │
│  ─────────────────────────────────  │
│                                     │
│  Push-To-Talk: Press and hold the  │
│  mic button to record. Release to  │
│  stop.                             │
│                                     │
└─────────────────────────────────────┘
```

## Option 1: Real-Time Speak

### What It Does
Controls whether the AI automatically reads responses aloud

### When Enabled (Default) ✓
```
User: "What's the weather?"
↓
Lucifer processes...
↓
Response text shows on screen
↓
Speaker plays: "The weather is..."
```

### When Disabled ✗
```
User: "What's the weather?"
↓
Lucifer processes...
↓
Response text shows on screen
↓
(No audio - user reads response)
```

### How to Toggle
1. Tap gear icon ⚙️
2. Tap "Real-Time Speak" card
3. Toggle switches (Red = On, Gray = Off)
4. Tap back arrow ← to return

---

## Option 2: Push-To-Talk

### What It Does
Changes how the microphone button works

### When Enabled (Push-To-Talk Mode)
```
Home Screen shows: "Hold to talk"

Press and HOLD mic button
     ↓
Lucifer listens (recording indicator)
     ↓
Release mic button
     ↓
Recording stops automatically
     ↓
Lucifer processes and responds
```

**Use Case:** Hands-free operation
- Perfect for when device is on wrist
- Natural push-to-talk experience
- Like a walkie-talkie interface

### When Disabled (Tap-To-Record Mode - Default)
```
Home Screen shows: "Tap to talk"

Tap mic button once
     ↓
Lucifer starts listening
     ↓
Tap mic button again
     ↓
Recording stops
     ↓
Lucifer processes and responds
```

**Use Case:** Standard interaction
- More intuitive for many users
- Tap to start, tap to stop
- Traditional microphone behavior

### How to Toggle
1. Tap gear icon ⚙️
2. Tap "Push-To-Talk" card
3. Toggle switches (Red = On, Gray = Off)
4. Tap back arrow ← to return

---

## Complete User Flow Example

### Scenario: Enable Push-To-Talk

**Step 1: Open Settings**
```
Home Screen
    ↓ (Tap gear icon)
Settings Screen
```

**Step 2: Enable Push-To-Talk**
```
Settings Screen
    ↓ (Tap "Push-To-Talk" card)
Toggle switches from gray to red
```

**Step 3: Return Home**
```
Settings Screen
    ↓ (Tap back arrow)
Home Screen
```

**Step 4: Use Push-To-Talk**
```
Home Screen now shows: "Hold to talk"
    ↓ (Press and hold mic button)
    ↓ (Release to stop)
Lucifer listens and responds
```

---

## Settings Persistence

✓ **Your settings are saved automatically**
- Close the app
- Reopen it
- Your settings are still there!

✓ **No manual save needed**
- Changes apply immediately
- Survives app restart
- Survives device restart

---

## Indicator Labels

The label below the mic button tells you the current mode:

| Mode | Label | Behavior |
|------|-------|----------|
| Push-To-Talk ON | "Hold to talk" | Press and hold to record |
| Push-To-Talk OFF | "Tap to talk" | Tap to start/stop |

---

## Mic Button Visual Feedback

### Recording State
- Button background: Light Red (#FF6B6B with transparency)
- Icon: Stop symbol ⏹️
- Label: "Lucifer is listening"

### Idle State
- Button background: Light White
- Icon: Mic symbol 🎤
- Label: "Lucifer is ready"

---

## Best Practices

### For Real-Time Speak
- **Enable if:** You like hearing responses
- **Disable if:** Quiet environment or prefer reading

### For Push-To-Talk
- **Enable if:** Device is on wrist (hands-free)
- **Disable if:** Device is on table (tap is easier)

---

## Troubleshooting

### Settings Not Saving?
- Settings are auto-saved when you tap them
- No "Save" button needed
- Just tap the toggle to change

### Push-To-Talk Not Working?
- Check if Push-To-Talk is enabled in settings
- Ensure you're pressing and holding (not just tapping)
- Try physical button if available

### No Audio Output?
- Check if Real-Time Speak is enabled
- Verify device volume is not muted
- Check device speaker settings

---

## Summary

| Feature | Default | Purpose |
|---------|---------|---------|
| Real-Time Speak | Enabled ✓ | Hear AI responses |
| Push-To-Talk | Disabled | Use tap mode |

**Want to change?** → Tap gear icon → Toggle option → Done! ✨

