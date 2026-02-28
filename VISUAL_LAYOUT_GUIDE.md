# Visual Layout Guide

## Home Screen Layout

### Default View (Push-To-Talk Disabled)
```
┌─────────────────────────────────────┐
│              [⚙️]                    │  Settings Button (36dp circle)
├─────────────────────────────────────┤
│                                     │
│    Lucifer is ready                 │
│                                     │  65% of screen
│    You said: [Previous text...]    │  (Scrollable)
│                                     │
│    AI: [Previous response...]      │
│                                     │
├─────────────────────────────────────┤
│                                     │
│              [🎤]                   │  56dp circle
│                                     │  35% of screen
│          Tap to talk                │
│                                     │
└─────────────────────────────────────┘
```

### Recording State View
```
┌─────────────────────────────────────┐
│              [⚙️]                    │
├─────────────────────────────────────┤
│                                     │
│    Lucifer is listening             │
│                                     │
│                                     │
├─────────────────────────────────────┤
│                                     │
│           [🔴⏹️🔴]                  │  Recording state (red bg)
│                                     │
│          Tap to talk                │
│                                     │
└─────────────────────────────────────┘
```

### Push-To-Talk Enabled View
```
┌─────────────────────────────────────┐
│              [⚙️]                    │
├─────────────────────────────────────┤
│                                     │
│    Lucifer is ready                 │
│                                     │
│    [Text content...]                │
│                                     │
├─────────────────────────────────────┤
│                                     │
│              [🎤]                   │  Same button
│                                     │
│         Hold to talk                │  Different label!
│                                     │
└─────────────────────────────────────┘
```

---

## Settings Screen Layout

### Full Settings Screen
```
┌─────────────────────────────────────┐
│  [←] Settings                       │  Header (40dp height)
├─────────────────────────────────────┤
│                                     │
│  ┌────────────────────────────────┐│
│  │ Real-Time Speak         [●|  ] ││  Toggle ON (Red #FF6B6B)
│  │ Speak AI response auto  Pad    ││
│  └────────────────────────────────┘│
│                                     │  12dp gap
│  ┌────────────────────────────────┐│
│  │ Push-To-Talk            [  |●] ││  Toggle OFF (Gray #4A4A4A)
│  │ Hold mic button to      Pad    ││
│  └────────────────────────────────┘│
│                                     │  12dp gap
│  Push-To-Talk: Press and hold the  │  Info text (10sp)
│  mic button to record. Release to  │  (70% opacity)
│  stop.                             │
│                                     │
└─────────────────────────────────────┘
```

### Settings Item Detail
```
┌────────────────────────────────────┐
│ ┌──────────────────────────────┐   │
│ │ Real-Time Speak     [Toggle] │   │  Dark gray bg
│ │ Speak AI response...         │   │  (8% opacity)
│ │                              │   │  Padding: 12dp
│ └──────────────────────────────┘   │
│                                    │
│ Settings Item Structure:           │
│ ├─ Title (12sp, Bold, White)      │
│ ├─ Description (10sp, Gray, 60%)  │
│ └─ Toggle (44×24dp, Red/Gray)     │
│                                    │
└────────────────────────────────────┘
```

### Toggle Switch States

#### Enabled (Real-Time Speak ON)
```
┌──────────────────────┐
│ Real-Time Speak      │
│ Speak AI response... │
│                      │
│        [●|  ]        │  Red background (#FF6B6B)
│                      │  White dot (animated)
└──────────────────────┘
```

#### Disabled (Push-To-Talk OFF)
```
┌──────────────────────┐
│ Push-To-Talk         │
│ Hold mic button...   │
│                      │
│        [  |●]        │  Gray background (#4A4A4A)
│                      │  White dot (animated)
└──────────────────────┘
```

### Animation Flow (Toggle Switch)
```
OFF → Tap → Animating → ON

[  |●]                [◐|●]               [●|  ]
gray                  transitioning        red
No movement          Smooth 220ms         Full movement
```

---

## Button States & Colors

### Settings Button (Top Right)
```
Normal State:
┌─────────────┐
│      ⚙️     │  36dp circle
│ White Icon  │  12% opacity background
└─────────────┘

Pressed State:
┌─────────────┐
│      ⚙️     │  36dp circle (slightly smaller)
│ White Icon  │  15% opacity background
└─────────────┘
```

### Mic Button (Center)

#### Idle State
```
┌─────────────┐
│      🎤     │  56dp circle
│ White Icon  │  12% opacity white bg
│   24dp      │  Scale: 1.0x
└─────────────┘
```

#### Recording State (PTT OFF)
```
┌─────────────┐
│      ⏹️     │  56dp circle
│ White Icon  │  18% opacity white bg
│   24dp      │  Scale: 1.08x (animated)
└─────────────┘

Label: "Tap to talk"
```

#### Recording State (PTT ON)
```
┌─────────────┐
│      🎤     │  56dp circle
│ White Icon  │  25% opacity RED bg
│   24dp      │  Scale: 1.08x (animated)
└─────────────┘

Label: "Hold to talk"
```

---

## Color Palette Reference

### Used in Settings Screen
```
┌─────────────────────────────────┐
│                                 │
│  Enabled Toggle (Real-Time)     │
│  Background: #FF6B6B            │
│  ████████████ Primary Red       │
│                                 │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│                                 │
│  Disabled Toggle (Push-To-Talk) │
│  Background: #4A4A4A            │
│  ████████████ Dark Gray         │
│                                 │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│                                 │
│  Text Color: #FFFFFF            │
│  ████████████ Pure White        │
│                                 │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│                                 │
│  Background: #000000            │
│  ████████████ Pure Black        │
│                                 │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│                                 │
│  Surface: #1A1A1A               │
│  ████████████ Dark Gray Surface │
│                                 │
└─────────────────────────────────┘
```

---

## Responsive Layout Breakdown

### Screen Space Distribution

#### Home Screen
```
Total Height = 100%

┌──────────────────┐
│   Settings Btn   │  ~5% (40dp)
├──────────────────┤
│                  │
│   Text Content   │  65% (Scrollable)
│   (65% weight)   │
│                  │
├──────────────────┤
│                  │
│   Mic Button     │  35% (Fixed)
│   (35% weight)   │
│   + Label        │
│                  │
└──────────────────┘
```

#### Settings Screen
```
Total Height = 100%

┌──────────────────┐
│   Back + Title   │  40dp (fixed)
├──────────────────┤
│                  │
│ Settings Items   │  60% (scrollable)
│ + Info Text      │
│                  │
└──────────────────┘
```

---

## Typography Hierarchy

### Home Screen
```
Primary:    "Lucifer is listening"       14sp, Bold, White
Secondary:  "You said: ..."              12sp, Regular, White
Tertiary:   "Tap to talk"                10sp, Regular, White 60%
```

### Settings Screen
```
Header:     "Settings"                   14sp, Bold, White
Title:      "Real-Time Speak"            12sp, Bold, White
Desc:       "Speak AI response..."       10sp, Regular, White 60%
Info:       "Push-To-Talk: Press..."     10sp, Regular, White 70%
```

---

## Touch Target Sizes

### Buttons
```
Settings Button:  36dp (minimum 48dp touch target)
Mic Button:       56dp (minimum 48dp touch target)
Setting Item:     Full width (easy to tap)
Toggle Area:      44×24dp (easy to tap)
Back Arrow:       36dp circle
```

### Spacing
```
Horizontal: 12dp margins
Vertical:   8-12dp gaps
Card Pad:   12dp internal padding
List Gap:   12dp between items
```

---

## Animation Specifications

### Settings Screen Entrance
```
Duration: 300ms (default Compose)
Type: Fade in
Scale: 0.95 → 1.0
Opacity: 0% → 100%
```

### Toggle Switch Animation
```
Duration: 220ms (custom tween)
Type: Smooth easing
Movement: Dot slides left/right
Color: Smooth background transition
```

### Mic Button Scale
```
Recording: 1.0x → 1.08x
Duration: 220ms
Easing: Smooth
Direction: Scale from center
```

### Text Animations
```
Status Text: Cross fade
Duration: 200ms (AnimatedContent)
Type: Smooth transition
```

---

## Accessibility Features

### Visual
- ✓ High contrast (white on black/red)
- ✓ Large touch targets (>48dp)
- ✓ Clear visual feedback
- ✓ Icon + text labels
- ✓ Color not only indicator

### Interaction
- ✓ Clear button states
- ✓ Smooth animations (not jarring)
- ✓ Responsive feedback
- ✓ No hidden functions
- ✓ Logical layout

### Semantic
- ✓ Descriptive labels
- ✓ Content descriptions for icons
- ✓ Clear heading hierarchy
- ✓ Proper grouping

