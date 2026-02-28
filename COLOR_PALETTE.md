# Lucifer AI - Color Palette & Design Reference Card

## 🎨 Official Color Palette

### Dark Theme Colors

```
┌──────────────────────────────────────────────────────────────┐
│                     PRIMARY COLOR                             │
│                                                                │
│                    🔴 VIBRANT RED                             │
│                   Hex: #FF6B6B                                │
│                   RGB: (255, 107, 107)                        │
│                   Usage: Record button, highlights            │
│                                                                │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                   SECONDARY COLOR                             │
│                                                                │
│                    🟦 COOL TEAL                               │
│                   Hex: #4ECDC4                                │
│                   RGB: (78, 205, 196)                         │
│                   Usage: Accents, descriptions                │
│                                                                │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                  BACKGROUND COLOR                             │
│                                                                │
│                    ⬛ PURE BLACK                              │
│                   Hex: #000000                                │
│                   RGB: (0, 0, 0)                              │
│                   Usage: Main background                      │
│                                                                │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                   SURFACE COLOR                               │
│                                                                │
│                   ⬛ DARK GRAY                                │
│                   Hex: #1A1A1A                                │
│                   RGB: (26, 26, 26)                           │
│                   Usage: Cards, buttons, surfaces             │
│                                                                │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                    TEXT COLOR                                 │
│                                                                │
│                    ⚪ PURE WHITE                              │
│                   Hex: #FFFFFF                                │
│                   RGB: (255, 255, 255)                        │
│                   Usage: Main text                            │
│                                                                │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                   ERROR COLOR                                 │
│                                                                │
│                    🔴 BRIGHT RED                              │
│                   Hex: #FF6B6B                                │
│                   RGB: (255, 107, 107)                        │
│                   Usage: Error messages, alerts               │
│                                                                │
└──────────────────────────────────────────────────────────────┘
```

## 📏 Component Sizes

### Buttons

**Record Button (Primary)**
```
Size: 80dp × 80dp
Shape: Circle
Color: #FF6B6B (Red)
Icon: Microphone (40dp)
Padding: Centered

State: Listening
Color: #FF5252 (Dark Red)
Icon: Microphone Off (40dp)
```

**Clear Button (Secondary)**
```
Size: 60dp × 60dp
Shape: Circle
Color: #1A1A1A (Dark Gray)
Icon: Close/X (30dp)
Padding: Centered
Visibility: Shows when text recognized
```

### Logo

```
Size: 80dp × 80dp
Shape: Circle (Clipped)
Source: assets/images/logo.jpeg
Scale: Crop (ContentScale.Crop)
Border: None
```

### Text Areas

**App Name**
```
Font Size: 24sp
Weight: Bold
Color: #FF6B6B (Red)
Alignment: Center
```

**Subtitle**
```
Font Size: 12sp
Weight: Regular
Color: #4ECDC4 (Teal)
Alignment: Center
```

**Status Text**
```
Font Size: 14sp
Weight: Regular/Bold
Color: Varies by status
Alignment: Center
```

**Recognized Text Card**
```
Font Size: 13sp
Weight: Medium
Color: #FFFFFF (White)
Background: #1A1A1A (Dark Gray)
Padding: 12dp
Border Radius: 8dp
Label: "You said:" (12sp, #4ECDC4)
```

**Error Messages**
```
Font Size: 12sp
Weight: Regular
Color: #FF6B6B (Red)
Alignment: Center
```

**Placeholder Text**
```
Font Size: 14sp
Weight: Regular
Color: #888888 (Gray)
Alignment: Center
Text: "Press to Speak"
```

## 🎯 Layout Spacing

### Vertical Spacing
```
Logo
  ↓ 16dp
App Name
  ↓ 8dp
Subtitle
  ↓ 32dp
Status/Text Display Area
  ↓ 32dp
Record Button
  ↓ 24dp
Clear Button (when visible)
```

### Horizontal Padding
```
Screen Edge
  ← 16dp →
Content (fill width)
  ← 16dp →
Screen Edge
```

## 💫 Visual Effects

### Record Button States

**Ready State**
```
Background: #FF6B6B (Solid Red)
Icon: Microphone (White)
Shadow: Subtle
Interaction: Clickable
```

**Listening State**
```
Background: #FF5252 (Dark Red)
Icon: Microphone Off (White)
Shadow: Subtle
Feedback: Status text changes
```

**Pressed State**
```
Background: #FF5252 (Darker)
Opacity: 0.8
Ripple: Yes (Material Design)
```

### Text Card Animation
```
Appears: Fade in
Content: Animated
Duration: ~200ms
Curve: Ease-in-out
```

## 🎨 Contrast Ratios

| Text | Background | Ratio | WCAG Level |
|------|-----------|-------|-----------|
| White (#FFF) | Black (#000) | 21:1 | AAA |
| White (#FFF) | Dark Gray (#1A1A1A) | 18:1 | AAA |
| Red (#FF6B6B) | Black (#000) | 5.4:1 | AA |
| Teal (#4ECDC4) | Black (#000) | 8:1 | AAA |
| Gray (#888888) | Black (#000) | 5.6:1 | AA |

All color combinations meet or exceed WCAG AA accessibility standards.

## 📱 Responsive Breakpoints

### Small Round (330×330)
```
Logo: 80dp
Padding: 12dp
Button: 80dp
Font: Base (no scaling)
```

### Large Round (454×454)
```
Logo: 80dp
Padding: 16dp
Button: 80dp
Font: Base (no scaling)
```

### Rectangular (280+×400+)
```
Logo: 80dp
Padding: 16dp
Button: 80dp
Font: Base (no scaling)
Width Constraint: fillMaxWidth with padding
```

## 🔧 Theme Configuration

Location: `/app/src/main/java/com/monkey/lucifer/presentation/theme/Theme.kt`

```kotlin
private val DarkColorScheme = Colors(
    primary = Color(0xFFFF6B6B),              // Red
    primaryVariant = Color(0xFFFF5252),       // Dark Red
    secondary = Color(0xFF4ECDC4),            // Teal
    secondaryVariant = Color(0xFF45B7AA),     // Dark Teal
    background = Color(0xFF000000),           // Black
    surface = Color(0xFF1A1A1A),              // Dark Gray
    error = Color(0xFFFF6B6B),                // Red
    onBackground = Color(0xFFFFFFFF),         // White
    onSurface = Color(0xFFFFFFFF),            // White
    onPrimary = Color(0xFF000000),            // Black
    onSecondary = Color(0xFF000000),          // Black
    onError = Color(0xFF000000)               // Black
)
```

## 🌙 Dark Mode Optimization

- **Pure Black Background**: Reduces power consumption on OLED displays
- **High Contrast**: Ensures readability in bright sunlight
- **Warm Accents**: Red and teal are eye-friendly in low light
- **Reduced Blue Light**: No bright blue colors to avoid eye strain

## ✨ Accessibility Features

- ✅ High contrast ratios (all text meets WCAG AA+)
- ✅ Large touch targets (80dp buttons)
- ✅ Clear visual feedback for all states
- ✅ Descriptive text labels
- ✅ Proper icon usage with descriptions
- ✅ No reliance on color alone for information

---

**Design System Version**: 1.0
**Last Updated**: February 2026
**Platform**: Wear OS 12+

