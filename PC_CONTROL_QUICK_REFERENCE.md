# PC Control Voice Commands - Quick Reference

## 🎤 Command Format

### Basic Pattern:
```
[Action] [Target] on/in/at [PC Name]
```

### Examples:
- "Open notepad **on devil PC**"
- "Shutdown **my PC**"
- "Start chrome **at work computer**"

---

## 📝 Command Examples

### Opening Applications
```
✓ "Lucifer, open notepad on devil PC"
✓ "Lucifer, start calculator on my PC"
✓ "Lucifer, open paint on work computer"
✓ "Lucifer, launch chrome in devil PC"
✓ "Lucifer, open edge at my PC"
✓ "Lucifer, start task manager on devil PC"
```

### System Control
```
✓ "Lucifer, shutdown devil PC"
✓ "Lucifer, restart my PC"
✓ "Lucifer, lock work computer"
✓ "Lucifer, sleep devil PC"
✓ "Lucifer, logoff my PC"
```

### File Explorers
```
✓ "Lucifer, open downloads on devil PC"
✓ "Lucifer, show desktop on my PC"
✓ "Lucifer, open documents at work computer"
✓ "Lucifer, open pictures in devil PC"
```

### Web Browsing
```
✓ "Lucifer, open youtube on devil PC"
✓ "Lucifer, open google in my PC"
✓ "Lucifer, open gmail at devil PC"
```

### Network
```
✓ "Lucifer, wifi off on my PC"
✓ "Lucifer, wifi on in devil PC"
✓ "Lucifer, show IP on work computer"
```

---

## 🎯 PC Naming

### How Lucifer Identifies Your PC:

Lucifer matches PC names using:
1. **Nickname** (from Firestore) - Most flexible
2. **Device Name** - Windows computer name  
3. **Hostname** - Network hostname

### Examples from Your Firestore:
- **Nickname**: "MY PC" → Say: "my PC"
- **Device Name**: "DESKTOP-8T4V61D" → Say: "desktop"
- **Custom**: "devil PC" → Say: "devil PC"

### Tips:
- ✅ Use simple, memorable nicknames
- ✅ Case doesn't matter ("My PC" = "my pc")
- ✅ Partial matches work ("desktop" matches "DESKTOP-8T4V61D")
- ❌ Avoid special characters in nicknames

---

## 🔧 Supported PC Commands

### Applications (30+)
| Say This | Opens |
|----------|-------|
| notepad | Windows Notepad |
| calculator | Windows Calculator |
| paint | Microsoft Paint |
| chrome | Google Chrome |
| edge | Microsoft Edge |
| firefox | Mozilla Firefox |
| explorer | File Explorer |
| task manager | Task Manager |
| control panel | Control Panel |
| settings | Windows Settings |

### System Actions
| Say This | Does |
|----------|------|
| shutdown | Shuts down PC immediately |
| restart | Restarts PC immediately |
| sleep | Puts PC to sleep |
| lock | Locks the screen |
| logoff | Logs off current user |

### Files & Folders
| Say This | Opens |
|----------|-------|
| downloads | Downloads folder |
| documents | Documents folder |
| pictures | Pictures folder |
| desktop | Desktop folder |

### Websites
| Say This | Opens in Chrome |
|----------|----------------|
| youtube | YouTube.com |
| google | Google.com |
| gmail | Gmail.com |

---

## ⚠️ Common Mistakes

### ❌ Missing PC Name
```
Wrong: "Lucifer, open notepad"
Right: "Lucifer, open notepad on devil PC"
```

### ❌ Wrong PC Name
```
Wrong: "Lucifer, shutdown gaming rig"  (if not registered)
Right: "Lucifer, shutdown devil PC"    (must be registered)
```

### ❌ Unclear Command
```
Wrong: "Lucifer, do something on my PC"
Right: "Lucifer, open notepad on my PC"
```

---

## 💡 Pro Tips

### 1. Natural Language Works!
All these work:
- "Open notepad **on** devil PC"
- "Open notepad **in** devil PC"
- "Open notepad **at** devil PC"
- "Start notepad **on** devil PC"

### 2. Short & Clear
Best practice:
- ✅ "Open chrome on devil PC" (Clear, concise)
- ⚠️ "Hey Lucifer could you please maybe open chrome browser on my devil PC please" (Too verbose)

### 3. Wait for Confirmation
Lucifer will respond:
- ✅ "Command sent to devil PC, Sir. Executing: start notepad"
- ❌ "I couldn't find a PC named 'gaming rig', Sir."

### 4. One Command at a Time
Do this:
- ✅ "Open chrome on devil PC"
- ✅ [Wait for response]
- ✅ "Open notepad on devil PC"

Not this:
- ❌ "Open chrome and notepad and calculator on devil PC"

---

## 🚀 Quick Start Guide

### Step 1: Register Your PC
Make sure your PC is registered in Firestore with a nickname.

Example Firestore document:
```json
{
  "device_id": "0ad3bee0-6a32-4534-b158-0d044aa1cf64",
  "device_name": "DESKTOP-8T4V61D",
  "nickname": "devil PC",  ← Use this in voice commands
  "hostname": "DESKTOP-8T4V61D"
}
```

### Step 2: Start PC Agent
Run the monitoring agent on your PC to execute commands.

### Step 3: Test Simple Command
Say: **"Lucifer, open notepad on devil PC"**

### Step 4: Verify Execution
Check that:
- ✅ Lucifer responds with confirmation
- ✅ Command appears in Firestore
- ✅ Notepad opens on PC

---

## 📱 Voice Command Tips

### Speaking Clearly:
1. **Pause** before speaking
2. Speak at **normal pace**
3. **Enunciate** PC name clearly
4. Use **simple** command structure

### Example Rhythm:
```
"Lucifer, [pause] open notepad [pause] on devil PC"
```

### If Not Recognized:
1. Repeat more clearly
2. Use simpler words
3. Check PC name pronunciation
4. Try shorter commands

---

## 🎯 Most Common Commands

### Top 10 You'll Use Daily:

1. **"Open notepad on [PC]"**
2. **"Open chrome on [PC]"**
3. **"Lock [PC]"**
4. **"Shutdown [PC]"**
5. **"Open downloads on [PC]"**
6. **"Open youtube on [PC]"**
7. **"Restart [PC]"**
8. **"Open task manager on [PC]"**
9. **"Open calculator on [PC]"**
10. **"Sleep [PC]"**

---

## 🔮 Advanced Examples

### Scenario 1: Remote Work
```
Morning:
"Lucifer, wifi on in work computer"

During work:
"Lucifer, open chrome on work computer"
"Lucifer, open documents on work computer"

End of day:
"Lucifer, lock work computer"
```

### Scenario 2: Media Control
```
"Lucifer, open youtube on devil PC"
"Lucifer, volume up on devil PC"  (if configured)
```

### Scenario 3: System Maintenance
```
"Lucifer, open task manager on my PC"
"Lucifer, show IP on my PC"
"Lucifer, restart my PC"
```

---

## 🎉 Ready to Use!

Now you can control your PC just by talking to your watch! 

**Example conversation:**
```
You: "Lucifer, open notepad on devil PC"
Lucifer: "Command sent to devil PC, Sir. Executing: start notepad"

[Notepad opens on your PC]

You: "Lucifer, open youtube on devil PC"
Lucifer: "Command sent to devil PC, Sir. Executing: start chrome https://youtube.com"

[YouTube opens in Chrome]
```

**Enjoy your voice-controlled PC!** 🎤💻✨

