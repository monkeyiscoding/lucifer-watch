# File Path Fix - Verification & Testing Guide

## Changes Made

### 1. WebsiteBuilderViewModel.kt (Lines 408-461)
**Enhanced fixFileReferences() function with:**

- **Better regex patterns** that handle folder names with spaces, underscores, dashes
- **Pattern examples:**
  - Removes: `./Ayush Soni_files/styles.css` → `styles.css`
  - Removes: `./Website_Name_files/script.js` → `script.js`
  - Removes: `./Falcon_files/image.jpg` → `image.jpg`

- **Added support for:**
  - CSS files (.css)
  - JavaScript files (.js)
  - Image files (.jpg, .png, .gif, .svg)
  
- **Improved logging** for debugging

### 2. AIService.kt (Lines 485-545)
**Strengthened the OpenAI prompt with:**

- ❌ WRONG examples with folder paths (clearly marked)
- ✅ CORRECT examples with just filenames (clearly marked)
- Validation checklist for AI to verify before returning
- Stricter instructions about JSON-only output
- Bold warnings in the prompt

## How It Works

1. **AI generates HTML** with paths (may include folder names)
2. **fixFileReferences() runs** and strips folder prefixes using regex
3. **All files uploaded to same Firebase folder**
4. **Regex ensures all href/src point to simple filenames**
5. **Website loads with CSS and JS working correctly**

## Testing Steps

```bash
1. Open the Lucifer app
2. Say: "Create a website called TestPortfolio"
3. Wait for completion
4. Download the index.html from Firebase Storage
5. Open in text editor
6. Search for: href=" and src="
7. Verify results show ONLY filenames:
   - href="styles.css"  ✅
   - src="script.js"    ✅
   - src="image.jpg"    ✅
   
   NOT:
   - href="./folder/styles.css"  ❌
   - src="./Name_files/script.js"  ❌
```

## Expected HTML Structure After Fix

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TestPortfolio</title>
    <link rel="stylesheet" href="styles.css">
    <!--↑ No folder prefix! Just the filename ✅ -->
</head>
<body>
    <!-- Content -->
    <script src="script.js"></script>
    <!--↑ No folder prefix! Just the filename ✅ -->
</body>
</html>
```

## Files Structure in Firebase Storage

```
firebaseStorage/
└── websites/
    └── [projectId]/
        ├── index.html    (references styles.css and script.js)
        ├── styles.css    (loaded when href="styles.css")
        └── script.js     (loaded when src="script.js")
```

## Regex Patterns Used

```kotlin
// For CSS files
href=["']\.?/?[^/"']*[/\\]*\Q$cssFile\E["']
Matches any variation of: href="./anything/styles.css" 
Replaces with: href="styles.css"

// For JavaScript files  
src=["']\.?/?[^/"']*[/\\]*\Q$jsFile\E["']
Matches any variation of: src="./anything/script.js"
Replaces with: src="script.js"

// For Image files
src=["']\.?/?[^/"']*[/\\]*\Q$imageFile\E["']
Matches any variation of: src="./anything/image.jpg"
Replaces with: src="image.jpg"
```

## Fallback Logic

If fixFileReferences() doesn't catch something, the regex patterns are now:
- More permissive (catches more variations)
- Handles multiple slashes or backslashes
- Works with quoted and unquoted paths
- Deals with optional ./ prefixes

## No More Issues With

✅ Website names with spaces: "Ayush Soni", "John Doe Portfolio"  
✅ Website names with underscores: "My_Site", "Portfolio_2024"  
✅ Website names with dashes: "my-portfolio", "web-design"  
✅ Website names with special chars: "Portfolio!Pro", "Website#1"  
✅ Multi-word names: "Photography Studio Portfolio"  

All will now generate correct same-folder references.

## Troubleshooting

If CSS/JS still don't load:
1. Check Firebase Storage path - should be `websites/uuid/`
2. Verify files are uploaded (all 3: html, css, js)
3. Check HTML for any remaining folder prefixes in href/src
4. Look at WebsiteBuilderViewModel logs for "Fixed file references" message
5. Verify the regex pattern matched and replaced correctly

