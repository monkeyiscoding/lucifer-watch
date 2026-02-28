# File Path Fix - Complete Solution

## Problem
When generating websites with multiple files (HTML, CSS, JS), the AI was generating file references with folder paths like:
```html
<link rel="stylesheet" href="./Sharma Murthy_files/styles.css">
<script src="./Falcon_files/script.js"></script>
```

But when uploading to Firebase, all files are placed in the same flat directory:
```
websites/
└── projectId/
    ├── index.html
    ├── styles.css
    └── script.js
```

This caused CSS and JS files not to load because the browser couldn't find them at the incorrect paths.

## Solution

### Part 1: Improved Prompt (AIService.kt)
The AI generation prompt already includes clear instructions to use only filenames:
```
⚠️ CRITICAL FILE PATH REQUIREMENTS - FOLLOW EXACTLY:
- All files (index.html, styles.css, script.js) will be stored in the SAME Firebase folder
- Use ONLY the filename in href and src attributes - NO folder prefixes whatsoever
- CORRECT: href="styles.css" ✅
- CORRECT: src="script.js" ✅
```

### Part 2: Enhanced Path Fixing (WebsiteBuilderViewModel.kt)
The `fixFileReferences()` function now includes:

1. **Multiple Pattern Matching** - Catches various folder naming styles:
   - `./Name_files/styles.css` → `styles.css`
   - `./folder/styles.css` → `styles.css`
   - `../folder/styles.css` → `styles.css`

2. **Separate Handlers** for:
   - CSS files (href attributes)
   - JavaScript files (src attributes)
   - Image files (src attributes)

3. **Final Aggressive Fallback**:
   ```kotlin
   htmlContent = htmlContent.replace(
       Regex("""(href|src)=["']\.[/\\][^/\\]*[/\\]([^/\\]*\.(?:html|css|js|jpg|png|gif|svg|json))["']"""),
       "$1=\"$2\""
   )
   ```
   This catches ANY remaining `./folder/filename` pattern and strips it.

## How It Works

When files are generated and before upload:
1. HTML file is parsed from the AI response
2. `fixFileReferences()` is called with the files map
3. For each CSS file found:
   - Tries multiple patterns to find and fix references
   - Replaces `href="./anything/styles.css"` with `href="styles.css"`
4. Same process for JS and image files
5. Final fallback regex strips any remaining folder prefixes
6. All files are uploaded to Firebase with correct references

## Upload Process

```
Firebase Storage Structure:
https://firebasestorage.googleapis.com/v0/b/lucifer-97501.firebasestorage.app/o/
└── websites/
    └── [projectId]/
        ├── index.html (references: styles.css, script.js) ✅
        ├── styles.css
        └── script.js
```

## Testing the Fix

After rebuilding:
1. Create a website with voice command
2. Check Firebase Storage to see files are uploaded correctly
3. Verify HTML file references use simple filenames:
   - `<link rel="stylesheet" href="styles.css">`
   - `<script src="script.js"></script>`
4. Website should load with CSS and JavaScript working properly

## Files Modified
- `/app/src/main/java/com/monkey/lucifer/presentation/WebsiteBuilderViewModel.kt`
  - Updated `fixFileReferences()` function with enhanced pattern matching

## Logging
The fix includes detailed logging:
```
D/WebsiteBuilderViewModel: ✅ Fixed CSS reference for: styles.css with pattern: ...
D/WebsiteBuilderViewModel: ✅ Fixed JS reference for: script.js with pattern: ...
D/WebsiteBuilderViewModel: Fixed file references in HTML
```

Check logcat to verify the fix is working correctly.

