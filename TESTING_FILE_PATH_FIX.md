# Website File Path Fix - Testing Guide

## Overview
This fix addresses the issue where CSS and JavaScript files were not loading in generated websites because the HTML was referencing them with incorrect folder paths.

## Problem Summary
**Before Fix:**
```html
<!-- WRONG - Files not loading -->
<link rel="stylesheet" href="./Sharma Murthy_files/styles.css">
<script src="./Xiaomi_files/script.js"></script>
```

**After Fix:**
```html
<!-- CORRECT - Files load properly -->
<link rel="stylesheet" href="styles.css">
<script src="script.js"></script>
```

## Solution Details

### What Was Changed
**File: `WebsiteBuilderViewModel.kt`**
- Enhanced the `fixFileReferences()` function with:
  1. Multiple regex patterns to catch different folder naming styles
  2. Separate handlers for CSS, JS, and image files
  3. A final aggressive fallback regex that strips ANY folder prefix

### How It Works
1. **When website files are generated:**
   - AI creates index.html, styles.css, script.js
   - HTML may contain incorrect paths like `./Folder_Name_files/styles.css`

2. **Before upload:**
   - `fixFileReferences()` is called
   - Multiple patterns try to fix the references
   - Final fallback regex strips any remaining folder prefixes
   - All references are normalized to just filenames

3. **Upload to Firebase:**
   - All files uploaded to: `websites/{projectId}/`
   - HTML now has correct references: `href="styles.css"`

## Testing Steps

### Step 1: Generate a Website
1. Open the Lucifer app
2. Say: "Lucifer, create a website named [YourName]"
3. Wait for the website to be generated and uploaded

### Step 2: Verify File Upload
1. Check Firebase Storage Console
2. Navigate to: `lucifer-97501.firebasestorage.app`
3. Look for files in: `websites/{projectId}/`
   - `index.html`
   - `styles.css`
   - `script.js`

### Step 3: Check File References
1. Open Firebase Storage and view `index.html` content
2. Look for these patterns:
   ```html
   ✅ CORRECT: <link rel="stylesheet" href="styles.css">
   ✅ CORRECT: <script src="script.js"></script>
   ❌ WRONG: href="./Name_files/styles.css"
   ❌ WRONG: src="./folder/script.js"
   ```

### Step 4: Test in Browser
1. Get the website URL from the QR code
2. Open the URL in a browser
3. Verify:
   - Styling is applied (CSS working)
   - Interactive features work (JS working)
   - No console errors about failed file loads

### Step 5: Check Logcat
Monitor Android logcat for these messages:
```
D/WebsiteBuilderViewModel: Available files to fix: [styles.css, script.js]
D/WebsiteBuilderViewModel: ✅ Fixed CSS reference for: styles.css
D/WebsiteBuilderViewModel: ✅ Fixed JS reference for: script.js
D/WebsiteBuilderViewModel: Fixed file references in HTML
```

## Patterns Handled

The fix now correctly handles these patterns:
```
❌ ./SomeName_files/styles.css    → ✅ styles.css
❌ ./website_files/script.js       → ✅ script.js
❌ ./Name_files/image.jpg          → ✅ image.jpg
❌ ../folder/styles.css            → ✅ styles.css
❌ ./folder/script.js              → ✅ script.js
```

## Debugging Tips

If files still don't load after this fix:

1. **Check the HTML file content:**
   - Download from Firebase Storage
   - Search for `<link rel` and `<script src`
   - Verify they don't contain folder paths

2. **Check Browser Console:**
   - Right-click → Inspect → Console tab
   - Look for 404 errors on CSS/JS files
   - Note the paths it's trying to load

3. **Check Firebase Rules:**
   - Ensure Firebase Storage allows read access
   - Rules should allow: `allow read: if true;`

4. **Check Logs:**
   - Adb logcat | grep WebsiteBuilderViewModel
   - Look for "Fixed" messages
   - Check for "⚠️" warnings about unmatched patterns

## Expected Behavior After Fix

✅ When creating a website:
1. Files are generated correctly
2. File references are fixed before upload
3. All files upload to same folder
4. Website loads with CSS and JS working
5. Logcat shows successful fixes
6. Website displays styling and interactivity

## Files Modified
- `app/src/main/java/com/monkey/lucifer/presentation/WebsiteBuilderViewModel.kt`
  - Method: `fixFileReferences(filesMap: MutableMap<String, String>)`

## Rollback Instructions
If issues arise, the original `fixFileReferences()` is still functional but less comprehensive:
- It handles basic patterns but may miss some variations
- Always has fallback error handling

## Contact & Support
For issues or questions:
1. Check the detailed logs in logcat
2. Review the generated HTML in Firebase Storage
3. Verify the actual file references in the HTML content

