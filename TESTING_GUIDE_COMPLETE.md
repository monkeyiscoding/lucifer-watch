# 🧪 Complete Testing Guide - Website Builder v2.0

## 📱 How to Test on Your Watch

### Setup
1. Build the app: `./gradlew assembleDebug`
2. Deploy to watch: `adb install app/build/outputs/apk/debug/app-debug.apk`
3. Open the Lucifer app on your watch
4. You should see: "Lucifer is ready" with mic icon

---

## ✅ Test Case 1: Website Name Extraction

### Command 1A: "Name is" Pattern
```
📢 Voice: "Lucifer, the website name is Lucifer"

Expected:
✅ Preview shows: "Website Name: Lucifer"
✅ Build button available
✅ After build: HTML has <title>Lucifer</title>
✅ Firestore: { "name": "Lucifer" }

Check Logs:
adb logcat | grep "Final extracted website name"
Should show: "Final extracted website name: 'Lucifer' (found: true)"
```

### Command 1B: "Create website" Pattern
```
📢 Voice: "Create website Phoenix"

Expected:
✅ Preview shows: "Website Name: Phoenix"
✅ After build: HTML title is "Phoenix"
✅ Firestore: { "name": "Phoenix" }

Check Logs:
Should show: "Pattern 2 (create website) matched: 'Phoenix'"
```

### Command 1C: "Create a [name] website" Pattern
```
📢 Voice: "Build a Starlight portfolio"

Expected:
✅ Preview shows: "Website Name: Starlight"
✅ After build: HTML title is "Starlight"

Check Logs:
Should show: "Pattern 3 (reverse style) matched: 'Starlight'"
```

### Command 1D: Simple Pattern
```
📢 Voice: "Mockingjay website"

Expected:
✅ Preview shows: "Website Name: Mockingjay"
✅ After build: HTML title is "Mockingjay"

Check Logs:
Should show: "Pattern 4 (simple style) matched: 'Mockingjay'"
```

### Command 1E: No Name Given (Default)
```
📢 Voice: "Create a website for me"

Expected:
✅ Preview shows: "Website Name: My Website"
✅ After build: HTML title is "My Website"
✅ Firestore: { "name": "My Website" }

Check Logs:
Should show: "Name validation failed, using default"
```

---

## ✅ Test Case 2: Multi-File Website Generation

### Check Firebase Storage
```bash
# Via Firebase Console
1. Go to: https://console.firebase.google.com
2. Project: lucifer-97501
3. Storage → Storage browser
4. Look for: websites/ folder
5. Should see files:
   - index.html ✅
   - styles.css ✅
   - script.js ✅
```

### Check File Contents
```
index.html:
✅ Has <link rel="stylesheet" href="styles.css">
✅ Has <script src="script.js"></script>
✅ Has <title>YOUR_NAME</title>

styles.css:
✅ Contains CSS for layout
✅ Has media queries for responsive design

script.js:
✅ Contains interactive functionality
✅ Properly formatted JavaScript
```

---

## ✅ Test Case 3: Command Preview Screen

### Test Preview Display
```
📢 Voice: "Create Artemon website for me"

Expected:
1. Preview Screen Shows:
   ✅ "Your Command: Create Artemon website for me"
   ✅ "Website Name: Artemon"
   ✅ Cancel button (top-left area)
   ✅ Build button (bottom area)
   
2. Can Scroll:
   ✅ Text scrollable if command is long
   ✅ Build button always visible
   
3. User Actions:
   ✅ Tap Cancel → Returns to home screen
   ✅ Tap Build → Shows building screen
```

### Test with Long Command
```
📢 Voice: "Lucifer I want you to create a beautiful professional portfolio website for me. The name should be MyAwesomePortfolio and include modern design with smooth animations"

Expected:
✅ Preview scrollable
✅ Can see full command when scrolling
✅ Build button stays visible at bottom
✅ Name extracted: "My Awesome Portfolio" OR "MyAwesomePortfolio"
```

---

## ✅ Test Case 4: Building Progress Screen

### Check Progress Messages
```
During Build, you should see (in order):
1. ✅ "Started analyzing your requirements"
2. ✅ "Identified: Lucifer"
3. ✅ "Creating project: Lucifer"
4. ✅ "Project structure ready"
5. ✅ "Generating website files..."
6. ✅ "Website files generated (3 files: index.html, styles.css, script.js)"
7. ✅ "CSS styling included..."
8. ✅ "Mobile and desktop styles applied"
9. ✅ "JavaScript interactivity included..."
10. ✅ "Interactive elements ready"
11. ✅ "Uploading website files to Firebase Storage..."
12. ✅ "Website uploaded successfully (3 files)"
13. ✅ "Generating QR code..."
14. ✅ "QR code generated"
15. ✅ "Saving project metadata..."
16. ✅ "✅ Website ready, Sir!"
```

### Check Logs
```bash
adb logcat | grep "Website files generated"
Should show: "Website files generated (3 files: index.html, styles.css, script.js)"
```

---

## ✅ Test Case 5: QR Code Completion Screen

### Check Display
```
Expected Screen:
┌──────────────────────────┐
│ Website is ready, sir!   │
│                          │
│     ┌────────────┐       │
│     │   QR CODE  │       │
│     │            │       │
│     └────────────┘       │
│                          │
│    [ Close Button ]      │
└──────────────────────────┘

Checklist:
✅ Black background (no gradient)
✅ Message is green color
✅ QR code centered
✅ QR code readable (scan with phone)
✅ Close button at bottom
✅ No extra information shown
✅ No URLs shown
✅ No metadata displayed
```

### Test QR Code
```
1. Tap Close button
   ✅ Returns to home screen
   
2. Scan QR with phone camera
   ✅ Opens website URL
   ✅ Website displays with correct name
   ✅ Styling loads (colors, fonts)
   ✅ JavaScript works (if interactive elements)
```

---

## ✅ Test Case 6: Firestore Database Check

### Check Saved Data
```bash
# Via Firebase Console
1. Go to: https://console.firebase.google.com
2. Project: lucifer-97501
3. Firestore Database
4. Collection: WebsiteProjects
5. Should see documents with:

{
  "id": "uuid-string",
  "name": "Lucifer",                    ✅ YOUR WEBSITE NAME
  "description": "A professional portfolio website",
  "created_at": 1739800793218,
  "storage_path": "websites/uuid/index.html",
  "firebase_url": "https://firebasestorage.googleapis.com/...",
  "status": "COMPLETE"
}
```

### Verify Name Saved Correctly
```
For each test command, verify in Firestore:
- Command "Lucifer website" → name = "Lucifer" ✅
- Command "Phoenix portfolio" → name = "Phoenix" ✅
- Command "Starlight" → name = "Starlight" ✅
- Command "no name given" → name = "My Website" ✅
```

---

## ✅ Test Case 7: Empty Transcript Handling

### Test Silent Recording
```
1. Tap Mic button
2. Don't say anything
3. Tap Stop button after 2-3 seconds

Expected:
✅ No "You said:" message shown
✅ Status shows: "Lucifer is ready"
✅ No error messages
✅ No text displayed
✅ Clean UI
```

### Test Very Short Speech
```
1. Tap Mic button
2. Say just: "Hmm" or "Uh"
3. Tap Stop button

Expected:
✅ Might show: "You said: Hmm"
✅ Not recognized as website command
✅ No build started
✅ Returns to normal state
```

---

## ✅ Test Case 8: Watch Display Stays Awake

### Test Display
```
1. Open Lucifer app
2. Don't touch watch for 2 minutes
3. Watch screen should stay ON ✅

4. Check during recording:
   - Tap mic to start recording
   - Say something slowly
   - Watch should stay awake ✅

5. Check during building:
   - Say build command
   - Let it build for full time
   - Watch should stay awake ✅

If display turns off:
❌ WakeLock not properly acquired
```

### Check Logs
```bash
adb logcat | grep "WakeLock"
Should show related messages about acquisition
```

---

## ✅ Test Case 9: Error Handling

### Test Network Error
```
1. Disconnect watch from internet
2. Say build command
3. Expected:
   ✅ Shows error message clearly
   ✅ Returns to home screen safely
   ✅ No app crash
```

### Test Timeout
```
1. Say build command
2. If internet is slow, may timeout
3. Expected:
   ✅ Shows: "⚠️ Timeout, retrying... (Attempt 2)"
   ✅ Automatically retries
   ✅ Either completes or shows error
```

---

## 📊 Verification Checklist

### Before Committing to Production

#### Code Quality
- [ ] No compilation errors
- [ ] No runtime crashes
- [ ] All regex patterns tested
- [ ] Proper logging in place
- [ ] Error handling works

#### Feature Testing
- [ ] Website name extraction works for all patterns
- [ ] Multi-file generation confirmed
- [ ] Preview screen shows and works
- [ ] QR code screen clean and simple
- [ ] Names saved to Firestore correctly
- [ ] Watch stays awake while app open
- [ ] Empty transcripts handled
- [ ] Scrollable text areas work

#### User Experience
- [ ] Screen layout is clear and readable
- [ ] Text sizes appropriate for watch
- [ ] Buttons are easy to tap
- [ ] Messages are understandable
- [ ] Colors are readable
- [ ] Transitions are smooth

#### Firebase Integration
- [ ] Files upload to correct location
- [ ] Files have correct names
- [ ] Metadata saves to Firestore
- [ ] QR codes work and open correct URL
- [ ] Website displays with correct name

---

## 🐛 Debugging Commands

### View All Logs
```bash
adb logcat
```

### Filter for Website Builder
```bash
adb logcat | grep "WebsiteBuilder"
```

### Filter for AIService
```bash
adb logcat | grep "OpenAI"
```

### Filter for Firebase
```bash
adb logcat | grep "Firebase"
```

### Clear Logs
```bash
adb logcat -c
```

### Save Logs to File
```bash
adb logcat > logs.txt
```

---

## ✅ Final Verification

Run this complete flow:
```
1. Open app
   ✅ Shows "Lucifer is ready"

2. Tap mic
   ✅ Shows "Lucifer is listening"

3. Say: "Create website Lucifer"
   ✅ Shows transcription

4. Preview appears
   ✅ Shows "Website Name: Lucifer"
   ✅ Shows your command
   ✅ Shows Cancel and Build buttons

5. Tap Build
   ✅ Shows building screen with progress

6. Wait for completion (30-40 seconds)
   ✅ Shows all progress messages
   ✅ No errors shown

7. QR code screen appears
   ✅ Shows "Website is ready, sir!"
   ✅ Shows QR code centered
   ✅ Shows Close button

8. Scan QR with phone
   ✅ Opens website
   ✅ Website title is "Lucifer"
   ✅ Website is styled
   ✅ Website is responsive

9. Tap Close
   ✅ Returns to home
   ✅ Shows "Lucifer is ready"

10. Check Firestore
    ✅ Document exists
    ✅ name = "Lucifer"
    ✅ status = "COMPLETE"

11. Check Firebase Storage
    ✅ index.html exists
    ✅ styles.css exists
    ✅ script.js exists

✅✅✅ ALL TESTS PASSED! ✅✅✅
```

---

## 📞 Common Issues & Solutions

### Issue: "Website name not extracted"
**Solution:**
- Check the exact voice command format
- Review logs: `adb logcat | grep "Final extracted"`
- Verify regex patterns are working
- Try a different command format

### Issue: "Build hangs on generating HTML"
**Solution:**
- Check internet connection
- Look for timeout error in logs
- May be retrying automatically
- Give it more time (can take 20-30 seconds)

### Issue: "QR code not visible"
**Solution:**
- Check if QR code generated successfully
- Look at logs for QR generation
- Try building again
- Ensure display is bright enough

### Issue: "Files not in Firebase Storage"
**Solution:**
- Check Firebase API key is correct
- Verify bucket name in code
- Check upload logs for errors
- Ensure Firestore rules allow writing

### Issue: "Watch display turned off"
**Solution:**
- WakeLock not acquired properly
- Check for exceptions in initialization
- Verify PowerManager access
- Restart app and try again

---

## 🎉 Success Indicators

You'll know everything is working when:

1. ✅ Every website command extracts the correct name
2. ✅ Preview shows before building starts
3. ✅ Building completes successfully in 30-40 seconds
4. ✅ QR code screen is clean with just 3 elements
5. ✅ QR code opens a working, styled website
6. ✅ Website name appears in HTML title and page
7. ✅ Firestore document has correct name
8. ✅ Firebase Storage has 3 files per website
9. ✅ Watch stays awake during entire process
10. ✅ Empty transcripts don't show unwanted messages

---

**Testing Date:** February 18, 2026
**Last Updated:** February 18, 2026
**Status:** Ready for Testing ✅

