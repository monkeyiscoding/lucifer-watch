# 🚀 QUICK TEST GUIDE - Website File Path Fix

## STEP 1: Build & Install
```bash
# In Android Studio: 
# Click Run (green play button)
# OR use terminal:
./gradlew installDebug
```

---

## STEP 2: Create Test Website

### Voice Command:
```
"Lucifer, create a portfolio website. The website name is Test Website."
```

### Expected Response:
- Shows command preview
- Click "Send" button
- Shows "Creating project..." (0-100%)
- Shows QR code when complete

---

## STEP 3: Check Logcat (While Website is Being Created)

```bash
adb logcat | grep -E "WebsiteBuilder|Fixed"
```

### Expected Output:
```
D/WebsiteBuilder: Available files to fix: [styles.css, script.js]
D/WebsiteBuilder: ✅ Fixed CSS reference for: styles.css with pattern: ...
D/WebsiteBuilder: ✅ Fixed JS reference for: script.js with pattern: ...
D/WebsiteBuilder: Fixed file references in HTML
D/WebsiteBuilder: Sample of fixed HTML: ...
```

---

## STEP 4: Verify on Firebase

### 4.1 Open Firebase Console
- Go to: https://console.firebase.google.com
- Select project: `lucifer-97501`
- Click "Storage" in left menu

### 4.2 Find Website Files
```
Storage browser/
  └── websites/
      └── {some-uuid}/
          ├── index.html
          ├── styles.css
          └── script.js
```

### 4.3 Download index.html
- Click on `index.html`
- Click "Download" 
- Open in text editor

### 4.4 Verify File References
Search for `href=` and `src=`:

✅ **CORRECT:**
```html
<link rel="stylesheet" href="styles.css">
<script src="script.js"></script>
```

❌ **WRONG (should not see this):**
```html
<link rel="stylesheet" href="./Test Website_files/styles.css">
<script src="./folder/script.js"></script>
```

---

## STEP 5: Test Website in Browser

### 5.1 Get Website URL
Two ways:

**Option A:** Scan QR code on watch

**Option B:** Get direct URL from Firebase:
- Click `index.html` in Storage
- Copy "Download URL" 
- Example: `https://firebasestorage.googleapis.com/v0/b/lucifer-97501.firebasestorage.app/o/websites%2F...`

### 5.2 Open in Browser
- Chrome (recommended)
- Safari
- Firefox

### 5.3 Verify Styling Works
Website should have:
- ✅ Colors (CSS loaded)
- ✅ Proper layout (CSS loaded)
- ✅ Fonts/spacing (CSS loaded)

If website looks plain/unstyled → CSS NOT loading

### 5.4 Verify JavaScript Works
- Try interactive features (buttons, forms, etc.)
- Check browser console for errors

### 5.5 Check Browser Console
Press **F12** (or right-click → Inspect)

Go to **Console** tab:

✅ **GOOD:** No errors

❌ **BAD:** 
```
Failed to load resource: net::ERR_FILE_NOT_FOUND
.../Test Website_files/styles.css - 404
```

Go to **Network** tab and refresh:

✅ **GOOD:** All files show `200 OK`:
```
index.html - 200 OK
styles.css - 200 OK
script.js  - 200 OK
```

❌ **BAD:** 
```
styles.css - 404 Not Found
```

---

## STEP 6: Verify in Different Scenarios

### Test Case 1: Simple Website
```
"Lucifer, create a website named Simple Test"
```
Expected: 3 files (HTML, CSS, JS) all load correctly

### Test Case 2: Complex Name with Spaces
```
"Lucifer, create a website named John Doe Portfolio 2024"
```
Expected: Works despite complex name

### Test Case 3: Special Characters
```
"Lucifer, create a website named Tech & Design Co."
```
Expected: Works even with special chars

---

## 🚨 TROUBLESHOOTING

### Problem: CSS/JS Still Not Loading

#### Check 1: Logcat Messages
```bash
adb logcat | grep "Fixed.*reference"
```

If you see:
```
⚠️ CSS already correct or no matches found
⚠️ JS already correct or no matches found
```

This means AI already generated correct paths (rare but possible)

#### Check 2: Download HTML and Inspect
```bash
# Get URL from Firebase Storage
curl "https://firebasestorage.googleapis.com/.../index.html" -o test.html

# Check references
grep -E 'href=|src=' test.html
```

Should show:
```html
href="styles.css"
src="script.js"
```

NOT:
```html
href="./folder/styles.css"
src="./Name_files/script.js"
```

#### Check 3: Firebase Storage Rules
In Firebase Console → Storage → Rules:

```javascript
service firebase.storage {
  match /b/{bucket}/o {
    match /websites/{allPaths=**} {
      allow read: if true;  // ← Must be true for public access
    }
  }
}
```

#### Check 4: File Actually Uploaded
- Verify all 3 files in Firebase Storage
- Check file sizes (should be > 0 bytes)
- Click on each file to see metadata

---

## ✅ SUCCESS INDICATORS

### On Watch:
- ✅ QR code displays
- ✅ "Website ready" message
- ✅ No error messages

### In Logcat:
- ✅ "Fixed CSS reference" message
- ✅ "Fixed JS reference" message
- ✅ "Fixed file references in HTML" message

### In Firebase:
- ✅ 3 files uploaded (index.html, styles.css, script.js)
- ✅ index.html contains `href="styles.css"` (no folder prefix)
- ✅ index.html contains `src="script.js"` (no folder prefix)

### In Browser:
- ✅ Website displays with colors/styling
- ✅ JavaScript features work
- ✅ Console shows no 404 errors
- ✅ Network tab shows all files loaded (200 OK)

---

## 📝 QUICK COMMANDS REFERENCE

```bash
# Install app
./gradlew installDebug

# Watch logs
adb logcat | grep WebsiteBuilder

# Watch for fixes
adb logcat | grep "Fixed.*reference"

# Download website from Firebase
# (Replace URL with actual URL from Firebase Console)
curl "https://firebasestorage.googleapis.com/..." -o index.html

# Check file references in HTML
grep -E 'href=|src=' index.html

# Clear app data (start fresh)
adb shell pm clear com.monkey.lucifer
```

---

## 🎯 ONE-MINUTE TEST

Fastest way to verify fix is working:

1. **Say:** "Lucifer, create a website named Quick Test"
2. **Wait** for QR code to appear
3. **Open** browser console (F12)
4. **Scan** QR code
5. **Check** Console tab for 404 errors

**If NO 404 errors** → ✅ Fix is working!  
**If 404 errors** → ❌ See troubleshooting section

---

**Good luck with testing! 🚀**

