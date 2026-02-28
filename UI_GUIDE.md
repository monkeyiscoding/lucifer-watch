# Lucifer AI Homepage - Visual Guide

## UI Layout

```
┌─────────────────────────────────┐
│                                 │
│      [Circular Logo Image]      │
│                                 │
│      Lucifer AI                 │
│      AI Assistant               │
│                                 │
│  ┌─────────────────────────┐   │
│  │   You said:             │   │
│  │   [Recognized Text]     │   │
│  └─────────────────────────┘   │
│                                 │
│         [Record Button]         │
│         (Large Circle)          │
│                                 │
│        [Clear Button]           │
│        (Small Circle)           │
│                                 │
└─────────────────────────────────┘
```

## Button States

### Record Button - Ready State
- Color: Red (#FF6B6B)
- Icon: Microphone
- Text: "Record"
- Clickable: Yes

### Record Button - Listening State
- Color: Dark Red (Error Color)
- Icon: Microphone Off
- Text: "Stop"
- Display: "Listening..." above
- Clickable: Yes

### Clear Button
- Appears when text is recognized
- Color: Dark Gray (#1A1A1A)
- Icon: Close (X)
- Size: 60dp

## Text Display Areas

### Status Messages (Center):
1. **"Press to Speak"** - Initial state (Gray text)
2. **"Listening..."** - During recording (Red text, bold)
3. **Recognized Text Card** - After recognition
   - Label: "You said:" (Teal text)
   - Text: Recognized speech (White text)
   - Background: Dark Gray (#1A1A1A)
4. **Error Messages** - If something goes wrong (Red text)

## Interaction Flow

```
Start
  ↓
Press Record Button
  ↓
Request Permission (if Android 12+)
  ↓
User Grants Permission
  ↓
Start Listening (Button changes to red, text shows "Listening...")
  ↓
User Speaks
  ↓
Speech Recognized
  ↓
Display Text in Card
  ↓
Show Clear Button
  ↓
User Can:
  A) Press Clear → Reset to initial state
  B) Press Record Again → Start new recording
```

## Color Palette

### Primary Colors:
- **Primary Red**: #FF6B6B (Record button, app highlights)
- **Teal Secondary**: #4ECDC4 (Accent text, descriptions)
- **Pure Black**: #000000 (Background)
- **Dark Gray**: #1A1A1A (Surface elements, cards)

### Text Colors:
- **White**: #FFFFFF (Main text)
- **Gray**: #888888 (Placeholder text like "Press to Speak")
- **Red Error**: #FF6B6B (Error messages)

## Responsive Design

The layout is optimized for:
- **Small Round Watches** (330x330dp)
- **Large Round Watches** (454x454dp)
- **Rectangular Watches** (280x280dp+)

Uses Compose's `fillMaxSize()` and responsive padding to adapt to screen sizes.

## Typography

| Element | Font Size | Weight |
|---------|-----------|--------|
| App Name | 24sp | Bold |
| Subtitle | 12sp | Regular |
| Status Text | 14sp | Regular |
| "Listening..." | 14sp | Bold |
| Recognized Text | 13sp | Medium |
| "You said:" | 12sp | Regular |

## Key Features

✅ **Dark Theme** - Pure black background reduces power consumption
✅ **Touch-Friendly** - Large 80dp buttons easy to tap on watches
✅ **Efficient** - Coil library handles image caching
✅ **Reactive** - StateFlow ensures smooth UI updates
✅ **Accessible** - Clear feedback on every action
✅ **Permissions** - Proper Android 12+ permission handling

## Implementation Details

### Image Loading
- Uses Coil library for lazy loading
- Circular clip for logo
- Crop content scale for proper sizing

### State Management
- HomeViewModel extends Android's ViewModel
- Uses Kotlin StateFlow for state observation
- Proper lifecycle management with Compose

### Speech Recognition
- Android's native SpeechRecognizer
- RecognitionListener for state updates
- Error handling with user-friendly messages

