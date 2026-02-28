# ⚡ QUICK INSTALL - Release APK on Watch

## 🎯 The Fastest Way (5 minutes)

### Step 1: Connect Watch via USB
```bash
# Verify connection
adb devices
```

### Step 2: Build Release APK
```bash
cd /Users/ayush/StudioProjects/Lucifer2
./gradlew assembleRelease
```

### Step 3: Install on Watch
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

### Step 4: Launch App
```bash
adb shell am start -n com.monkey.lucifer/com.monkey.lucifer.presentation.MainActivity
```

✅ **Done!** Your app is installed with your logo! 🎉

---

## ⚠️ If Build Fails: Sign APK First

### Android Studio Method (Easy):
1. `Build` → `Generate Signed Bundle/APK` → `APK`
2. Select/create keystore
3. Choose `release` variant
4. Click `Finish`

Then run **Step 3** above.

---

## 🆘 Quick Troubleshooting

| Problem | Solution |
|---------|----------|
| `adb devices` is empty | Enable USB debugging: Settings → Developer options |
| APK not found | Run `./gradlew clean assembleRelease` first |
| Install failed | Try: `adb uninstall com.monkey.lucifer` then reinstall |
| Device offline | Restart ADB: `adb kill-server && adb start-server` |

---

## 📍 APK Location
```
/Users/ayush/StudioProjects/Lucifer2/app/build/outputs/apk/release/app-release.apk
```

---

**Ready?** Run the 4 steps above! 🚀

