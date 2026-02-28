# Visual Guide - Silence Detection Zones

## Audio Amplitude Over Time

```
AMPLITUDE
    ^
    |     🔊 ZONE 1: SPEECH (>800)
 1500 |━━━┓             ┌━━━┓
    |     ┃ "Hello"  world ┃
 1000 |    ┃      ┌─────────┐ ┃
    |     ┃     ┌──┘ (decay) └─┘
  800 |━━━┛━━━━━━━━━━━━━━━━━━━━━━━
    |                      🔊 ZONE 3: DECAY (250-800)
  600 |──────────┐    ┌─────────────
    |           │    │
  400 |           └────┘
    |
  250 |━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    |                  🛑 ZONE 2: SILENCE (<250)
  100 |  ░░░░░░░░░░░░  (timer starts here)
    |
    └──────────────────────────────── (time)
      0ms  200ms  400ms  600ms  800ms
```

## Decision Tree

```
                    ┌─ Amplitude Reading
                    │
         ┌──────────┴──────────┐
         │                     │
      > 800?              < 250?
    [ZONE 1]          [ZONE 2]
         │                │
         ├─ Speech?        └─ After speech?
         │  │                 │
         │  ├─ Yes: Reset     ├─ Yes: Start timer
         │  │  Timer          │  ├─ Duration >= 150ms?
         │  │  Continue       │  │  ├─ Yes: STOP ✓
         │  │  Listening      │  │  └─ No: Keep waiting
         │  │
         │  └─ No: Mark      └─ No: Ignore
         │     speech=true       (before speech)
         │
         └─ [ZONE 3] (250-800)
            Reset timer
            (decay/noise)
```

## Timeline Example: "Create a website"

```
TIME  AMP   ZONE     EVENT                          ACTION
────────────────────────────────────────────────────────────
 0ms  30    [2]      Background hum                 Ignore (no speech yet)
10ms  40    [2]      Background hum                 Ignore
20ms  50    [2]      Background hum                 Ignore

100ms 900   [1]      "Cre..." speech starts        ✓ Mark speechDetected=true
110ms 1200  [1]      "-ate"                         ✓ Reset timer
120ms 800   [1]      "a" peaks                      ✓ Reset timer
130ms 600   [3]      "a" decay                      ✓ Reset timer (ZONE 3!)
140ms 1100  [1]      "web" speech resumes           ✓ Reset timer
150ms 950   [1]      "-site"                        ✓ Reset timer
160ms 200   [2]      ← SILENCE STARTS HERE         ✓ Timer START (now=160ms)
170ms 100   [2]      Silence continues             (elapsed: 10ms)
180ms 150   [2]      Silence continues             (elapsed: 20ms)
190ms 80    [2]      Silence continues             (elapsed: 30ms)
...
300ms 40    [2]      Silence continues             (elapsed: 140ms)
310ms 20    [2]      Silence continues             (elapsed: 150ms) ← ⏰
320ms 10    [2]      Silence confirmed             ✅ STOP & PROCESS
```

**Result**: Stop 320-160 = 160ms after silence starts!

---

## Amplitude Zones Explained

### ZONE 1: REAL SPEECH (> 800)
**Characteristics**:
- Loud vowel sounds
- Strong consonant attacks
- Clear voice projection
- Peaks in speech pattern

**Action**: Always reset silence timer
```
User is actively speaking
└─→ "I might be about to say more"
└─→ Don't count silence yet
```

### ZONE 3: DECAY/NOISE (250-800)
**Characteristics**:
- Tail of vowels fading
- Weak consonants (s, f, th)
- Room echo/reverb
- Breath between words
- Ambient noise

**Action**: Reset silence timer (not true silence!)
```
Could be the end of a sound
└─→ "Could be more speech coming"
└─→ Don't count silence yet
```

### ZONE 2: TRUE SILENCE (< 250)
**Characteristics**:
- Dead quiet
- No audible sound
- Complete speech pause
- Only background ambient

**Action**: Start 150ms confirmation timer
```
User has DEFINITELY stopped speaking
└─→ "Probably done talking"
└─→ Wait 150ms to be sure
└─→ Then process
```

---

## Why 150ms?

```
Human pause timing between words: 200-300ms
Minimum pause to detect sentence end: 150ms
Minimum pause for safety margin: 100ms

150ms = Sweet spot ✓
- Not too fast (avoids false stops)
- Not too slow (feels instant)
```

---

## Common Issues & Fixes

### Issue: Still stopping slowly
**Check**: Are you seeing "REAL SILENCE DETECTED"?
- If YES: Timer is working, might need lower threshold
- If NO: Amplitude not going below 250, increase sensitivity

### Issue: False stops (stops mid-sentence)
**Check**: Logs show noise reaching below 250?
- Likely: Very quiet speaker or noisy environment
- Fix: Increase SILENCE_THRESHOLD to 300-350

### Issue: Not stopping at all
**Check**: See "MAX DURATION REACHED"?
- Yes: Recording hits 60 second limit
- No: Speech detection not triggering (threshold too high)

---

## Testing with Real Audio

### Test 1: Normal Speech
```
Speak: "Hello world"
Expected amplitude pattern:
  Greeting: 1000+
  Decay:    400-600
  Pause:    < 250
  Process:  After 150ms silence
```

### Test 2: With Background Music
```
Speak: "Stop" (loud)
Background music: ~500 amplitude
Expected amplitude pattern:
  Your speech: 1000+
  Pause:       ~500 (music only, but > 250)
  Result:      No false stop (ZONE 3)
  Wait until:  Music stops or you wait for true silence
```

### Test 3: Noisy Environment  
```
Speak: "Quiet please"
Ambient noise: ~400
Expected amplitude pattern:
  Your speech: 800+
  Pause:       ~400 (noise, > 250)
  Result:      No false stop
  Real silence: When ambient < 250
```

---

## Flow Diagram

```
START RECORDING
       ↓
 ┌─────────────────┐
 │ Check Amplitude │
 └────────┬────────┘
          │
      ┌───┴───┐
      │       │
   >800?   <250?
    │       │
    ▼       ▼
  ZONE1   ZONE2
    │       │
    │    speechDetected?
    │       │
    │     YES
    │       │
    │    Start/Check
    │    150ms Timer
    │       │
    │       Duration>=150ms?
    │       │
    │      YES
    │       │
    │       ▼
    │     STOP ✅
    │
    └─────→ ZONE3
           Reset
           Timer

Legend:
  ZONE1 (>800)     = SPEECH
  ZONE2 (<250)     = SILENCE (starts timer)
  ZONE3 (250-800)  = DECAY (resets timer)
```

---

## Summary Table

| Amplitude | Zone | Status | Action |
|-----------|------|--------|--------|
| > 1500 | 1 | Strong speech | Reset timer, keep listening |
| 800-1500 | 1 | Normal speech | Reset timer, keep listening |
| 250-800 | 3 | Decay/noise | Reset timer, not real silence |
| < 250 | 2 | True silence | Start 150ms confirmation timer |
| 0-100 | 2 | Deep silence | Timer counting down... |

---

## Next Steps

1. Build and install
2. Run with logcat: `adb logcat | grep HomeViewModel`
3. Speak and watch zones detected
4. Should see "REAL SILENCE DETECTED" → "STOPPING"

**Good luck! 🎉**

