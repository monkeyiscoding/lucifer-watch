# Code Changes - File Path Fix

## Summary
Fixed HTML file references in generated websites to use correct simple filenames instead of folder-prefixed paths.

## File Modified
`app/src/main/java/com/monkey/lucifer/presentation/WebsiteBuilderViewModel.kt`

## Function Updated
`fixFileReferences(filesMap: MutableMap<String, String>): MutableMap<String, String>`

## Changes Made

### Before (Limited Pattern Matching)
```kotlin
private fun fixFileReferences(filesMap: MutableMap<String, String>): MutableMap<String, String> {
    // Only had ONE pattern per file type:
    val pattern = """href=["']\.[/\\][^/\\]*[/\\]\Q$cssFile\E["']"""
    // This missed many variations like:
    // - ./Name_files/styles.css (with underscores)
    // - ../folder/styles.css (with parent directory)
}
```

### After (Comprehensive Pattern Matching)
```kotlin
private fun fixFileReferences(filesMap: MutableMap<String, String>): MutableMap<String, String> {
    // Now handles MULTIPLE patterns per file type:
    val patterns = listOf(
        """href=["']\.[/\\][^/\\]*_files[/\\]\Q$cssFile\E["']""", // ./Name_files/styles.css
        """href=["']\.[/\\][^/\\]*[/\\]\Q$cssFile\E["']""",        // ./folder/styles.css
        """href=["']\.\./[^/\\]*[/\\]\Q$cssFile\E["']"""            // ../folder/styles.css
    )
    
    // Plus FINAL AGGRESSIVE FALLBACK:
    htmlContent = htmlContent.replace(
        Regex("""(href|src)=["']\.[/\\][^/\\]*[/\\]([^/\\]*\.(?:html|css|js|jpg|png|gif|svg|json))["']"""),
        "$1=\"$2\""
    )
}
```

## Key Improvements

### 1. Multiple Pattern Support
**CSS Files:**
```
Pattern 1: ./SomeName_files/styles.css → styles.css
Pattern 2: ./folder/styles.css → styles.css
Pattern 3: ../folder/styles.css → styles.css
```

**JavaScript Files:**
```
Pattern 1: ./SomeName_files/script.js → script.js
Pattern 2: ./folder/script.js → script.js
Pattern 3: ../folder/script.js → script.js
```

**Image Files:**
```
Pattern 1: ./folder_name/image.jpg → image.jpg
Pattern 2: ./folder/image.png → image.png
```

### 2. Final Fallback Regex
```kotlin
// Catches ANY remaining ./[anything]/filename pattern
Regex("""(href|src)=["']\.[/\\][^/\\]*[/\\]([^/\\]*\.(?:html|css|js|jpg|png|gif|svg|json))["']""")
// Replacement: $1="$2" (keeps href/src, removes folder)
```

### 3. Better Logging
```kotlin
Log.d(TAG, "✅ Fixed CSS reference for: $cssFile with pattern: $pattern")
Log.d(TAG, "⚠️ CSS already correct or no matches found for: $cssFile")
```

## Example Transformations

### Example 1: Sharma Murthy Website
**Before:**
```html
<link rel="stylesheet" href="./Sharma Murthy_files/styles.css">
<script src="./Sharma Murthy_files/script.js"></script>
```

**After:**
```html
<link rel="stylesheet" href="styles.css">
<script src="script.js"></script>
```

### Example 2: Generic Folder
**Before:**
```html
<link rel="stylesheet" href="./website_files/styles.css">
<script src="./website_files/script.js"></script>
```

**After:**
```html
<link rel="stylesheet" href="styles.css">
<script src="script.js"></script>
```

### Example 3: Parent Directory Reference
**Before:**
```html
<link rel="stylesheet" href="../assets/styles.css">
<script src="../assets/script.js"></script>
```

**After:**
```html
<link rel="stylesheet" href="styles.css">
<script src="script.js"></script>
```

## Firebase Storage Structure
```
Firebase Storage
└── websites/
    └── {projectId}/
        ├── index.html (references: styles.css, script.js)
        ├── styles.css
        └── script.js
```

All files are in the SAME folder, so references must be simple filenames.

## Process Flow

```
1. AI Generates Website Files
   ↓
2. Files Map Created with index.html, styles.css, script.js
   ↓
3. fixFileReferences(filesMap) Called
   ├─ Check for CSS files with folder prefixes
   ├─ Check for JS files with folder prefixes
   ├─ Check for image files with folder prefixes
   └─ Apply final fallback regex
   ↓
4. Fixed HTML with correct references
   ↓
5. uploadWebsiteFiles() to Firebase
   ↓
6. Website loads with CSS/JS working ✅
```

## Testing the Fix

### Check Logs
```
adb logcat | grep WebsiteBuilderViewModel
```

Look for:
```
D: Available files to fix: [styles.css, script.js, image.jpg]
D: ✅ Fixed CSS reference for: styles.css with pattern: ...
D: ✅ Fixed JS reference for: script.js with pattern: ...
```

### Check HTML Content
Download the HTML file from Firebase Storage and verify:
```html
<!-- Correct -->
<link rel="stylesheet" href="styles.css">
<script src="script.js"></script>
```

NOT:
```html
<!-- Wrong -->
<link rel="stylesheet" href="./Folder_files/styles.css">
<script src="./Folder_files/script.js"></script>
```

## Backward Compatibility
✅ This change is fully backward compatible:
- Still handles original patterns
- Adds new patterns without breaking existing functionality
- Fallback is safe and won't damage valid references
- Error handling unchanged

## Performance Impact
✅ Minimal performance impact:
- Only runs during website generation (not frequently)
- Uses simple string replacement operations
- Multiple patterns checked but all fail-fast

## Edge Cases Handled
✅ Correctly handles:
- Mixed quote types: `href='...'` and `href="..."`
- Both slashes and backslashes: `/` and `\`
- Multiple underscores in folder names: `./Some_Name_files/`
- Numbers in folder names: `./folder2/`
- Already correct references (skips them)
- Missing files (gracefully handles)

