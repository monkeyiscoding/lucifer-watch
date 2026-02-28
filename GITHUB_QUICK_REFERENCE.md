# GitHub Integration - Quick Reference Guide

## What Was Just Implemented ✅

Your website builder now integrates with GitHub! Every website generated will be automatically uploaded to:

```
https://github.com/monkeyiscoding/lucifer-websites
```

---

## How It Works

### Step-by-Step Flow

1. **User says:** "Lucifer, create a website. The website name is MyPortfolio"

2. **App generates:**
   - `index.html` - Main website file
   - `styles.css` - Styling
   - `script.js` - Interactivity

3. **App uploads to:**
   - ✅ **Firebase Storage** (backup hosting)
   - ✅ **GitHub Repository** (primary hosting)

4. **User gets QR code** pointing to:
   ```
   https://monkeyiscoding.github.io/lucifer-websites/websites/<projectId>/index.html
   ```

5. **Metadata saved to Firestore** with both URLs:
   ```json
   {
     "name": "MyPortfolio",
     "firebase_url": "...",
     "github_url": "https://monkeyiscoding.github.io/lucifer-websites/...",
     "status": "COMPLETE"
   }
   ```

---

## Credentials

| Item | Value |
|------|-------|
| **GitHub Account** | monkeyiscoding |
| **Repository** | lucifer-websites |
| **Access Token** | YOUR_GITHUB_TOKEN_HERE |
| **Repository URL** | https://github.com/monkeyiscoding/lucifer-websites |
| **GitHub Pages** | https://monkeyiscoding.github.io/lucifer-websites |

---

## Repository Structure

```
lucifer-websites/ (One repo for ALL websites)
├── README.md
└── websites/
    ├── abc123def456/ (Project ID 1)
    │   ├── index.html
    │   ├── styles.css
    │   └── script.js
    ├── xyz789uvw012/ (Project ID 2)
    │   ├── index.html
    │   ├── styles.css
    │   └── script.js
    └── ... (more projects)
```

---

## Files Created/Modified

### New Files
- ✅ `GitHubService.kt` - Handles all GitHub operations

### Modified Files
- ✅ `WebsiteBuilderViewModel.kt` - Calls GitHub upload
- ✅ `WebsiteProject.kt` - Added `githubUrl` field
- ✅ `WebsiteProjectStore.kt` - Saves GitHub URL to Firestore

---

## Key Features

✅ **Automatic Repository Creation**
- Creates the repo on first website build
- No manual setup needed

✅ **Organized File Structure**
- Each website in its own folder
- Easy to browse and manage

✅ **GitHub Pages Hosting**
- Websites publicly accessible
- Free hosting

✅ **Fallback Mechanism**
- If GitHub fails → Uses Firebase instead
- Websites always work

✅ **QR Code Generation**
- Points to GitHub Pages URL
- Shorter, cleaner URLs

✅ **Complete Metadata**
- Both URLs saved in Firestore
- Can switch between GitHub/Firebase

---

## Testing Checklist

After building the app:

- [ ] Create a website via voice command
- [ ] Check GitHub repository for the files
- [ ] Verify files appear in correct folder structure
- [ ] Open the GitHub Pages URL in browser
- [ ] Check Firestore for both `firebase_url` and `github_url`
- [ ] Verify QR code points to GitHub URL

---

## Error Handling

### If GitHub Upload Fails
- ✅ Firebase upload still completes
- ✅ QR code uses Firebase URL
- ✅ Both URLs saved (Firebase = functional)
- ✅ User sees warning but build succeeds

### If Repository Creation Fails
- ✅ Error is logged
- ✅ Subsequent uploads try again
- ✅ Firebase remains as fallback

---

## Future Enhancements

- [ ] Update existing websites
- [ ] Delete websites from GitHub
- [ ] List all projects in GitHub
- [ ] Add GitHub Actions for backups
- [ ] Custom domain support
- [ ] Website version history

---

## Need to Update Credentials Later?

If you need to change the token or username, edit `GitHubService.kt`:

```kotlin
class GitHubService(
    private val token: String = "YOUR_NEW_TOKEN_HERE",
    private val username: String = "YOUR_USERNAME_HERE",
    private val repoName: String = "lucifer-websites"
) {
```

---

## Troubleshooting

**Q: Can I see my websites on GitHub?**
A: Yes! Go to: https://github.com/monkeyiscoding/lucifer-websites

**Q: What if GitHub is down?**
A: Firebase takes over. QR code will point to Firebase instead.

**Q: How are websites accessed?**
A: Via GitHub Pages:
```
https://monkeyiscoding.github.io/lucifer-websites/websites/{projectId}/index.html
```

**Q: Can I change the repository name?**
A: Yes, modify `repoName` in `GitHubService.kt` class definition.

---

## Summary

🎉 **GitHub Integration Complete!**

Your websites are now:
- Generated with AI
- Stored on Firebase (backup)
- Stored on GitHub (primary)
- Accessible via GitHub Pages
- Tracked in Firestore with both URLs

Ready to build and test! 🚀

