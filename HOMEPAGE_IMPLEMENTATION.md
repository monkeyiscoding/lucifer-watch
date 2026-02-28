# Lucifer AI Watch Application - Homepage Implementation

## Overview
A dark-themed AI assistant homepage for Wear OS (smartwatch) with speech recognition capabilities.

## Features Implemented

### 1. **Dark Theme (Black)**
- Primary color: Vibrant red (#FF6B6B)
- Secondary color: Teal (#4ECDC4)
- Background: Pure black (#000000)
- Surface: Dark gray (#1A1A1A)
- Applied through custom `LuciferTheme` in `Theme.kt`

### 2. **Speech Recognition UI**
- **Record Button**: Large circular button that toggles between:
  - Mic icon (when ready to record)
  - Mic-off icon (when actively listening)
- **Status Display**:
  - "Listening..." text when recording
  - Displays recognized speech in a styled card
  - Shows error messages if recognition fails
- **Clear Button**: Appears when text is recognized, clears the input

### 3. **Logo Integration**
- Displays circular logo from `assets/images/logo.jpeg`
- Loaded using Coil image library for efficient loading

### 4. **Permissions Handling**
- Requests `RECORD_AUDIO` permission on Android 12+
- Gracefully handles permission denials
- Added to `AndroidManifest.xml`

## Files Created/Modified

### New Files Created:
1. **`HomePage.kt`** - Main composable UI with:
   - Logo display
   - Record button
   - Speech recognition state management
   - Permission handling

2. **`HomeViewModel.kt`** - ViewModel managing:
   - Speech recognition lifecycle
   - Recognition results
   - Error handling
   - Uses Kotlin StateFlow for reactive state

### Modified Files:
1. **`MainActivity.kt`** - Updated to use new `HomePage` composable
2. **`Theme.kt`** - Implemented dark color scheme with custom colors
3. **`strings.xml`** - Added new string resources for UI text
4. **`strings.xml` (round)** - Updated for round watch displays
5. **`build.gradle.kts`** - Added dependencies:
   - `coil-compose` - Image loading library
   - `lifecycle-viewmodel-compose` - ViewModel integration
6. **`libs.versions.toml`** - Added version definitions
7. **`AndroidManifest.xml`** - Added `RECORD_AUDIO` permission

## Dependencies Added

```kotlin
// Image loading
implementation(libs.coil.compose)

// ViewModel support
implementation(libs.lifecycle.viewmodel.compose)
```

## How It Works

### Speech Recognition Flow:
1. User clicks the red circular record button
2. App requests `RECORD_AUDIO` permission (if needed)
3. Button changes to red with mic-off icon
4. "Listening..." text appears
5. App captures speech using Android's `SpeechRecognizer`
6. Recognized text is displayed in a styled card
7. User can clear the text with the clear button
8. User can record again to get new input

### UI Responsiveness:
- All state changes are reactive using Kotlin Coroutines `StateFlow`
- UI automatically updates when recognition state changes
- Smooth transitions between states

## Color Scheme

| Element | Color | Hex Code |
|---------|-------|----------|
| Background | Black | #000000 |
| Surface | Dark Gray | #1A1A1A |
| Primary (Button) | Vibrant Red | #FF6B6B |
| Secondary | Teal | #4ECDC4 |
| Text | White | #FFFFFF |
| Error State | Red | #FF6B6B |

## App Details

- **App Name**: Lucifer AI
- **Target Platform**: Wear OS (Android 12+)
- **Min SDK**: 30
- **Target SDK**: 36
- **Compose**: Enabled for modern UI development

## Next Steps

To further enhance the application, you could:
1. Add text-to-speech response capability
2. Integrate with an AI API (OpenAI, Google Gemini, etc.)
3. Add voice waveform visualization during recording
4. Implement voice command shortcuts
5. Add conversation history
6. Implement custom voice profiles

## Testing

To test the application:
1. Build and run on a Wear OS emulator or device
2. Click the record button to start speech recognition
3. Speak clearly into the device
4. See your speech displayed on screen
5. Click clear to reset and try again

