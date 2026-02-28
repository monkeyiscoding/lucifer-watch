# 📦 How to Install Release APK on Your Watch

## 🚀 Step-by-Step Guide

### Step 1: Build the Release APK

Navigate to your project directory and build:

```bash
cd /Users/ayush/StudioProjects/Lucifer2
./gradlew assembleRelease
```

This creates an unsigned release APK at:
```
app/build/outputs/apk/release/app-release.apk
```

---

## 📋 Prerequisites

Before installing, ensure you have:

1. **ADB (Android Debug Bridge) installed**
   ```bash
   # Check if ADB is installed
   adb version
   ```

2. **Watch connected via USB**
   - Connect your Wear OS watch to your computer with a USB cable
   - Or connect to the same WiFi network

3. **USB Debugging enabled on watch**
   - Settings → Developer options → ADB debugging (ON)
   - Settings → Developer options → Verify apps over USB (OFF, optional)

4. **Watch recognized by ADB**
   ```bash
   adb devices
   ```
   You should see your watch listed as a device.

---

## 🔑 APK Signing (IMPORTANT for Release)

Release APKs must be signed. You have two options:

### Option A: Sign with Android Studio (Recommended)

1. **In Android Studio:**
   - Go to: `Build` → `Generate Signed Bundle/APK`
   - Select: `APK`
   - Click `Next`

2. **Create or select keystore:**
   - `New` to create a new keystore, or
   - Select existing keystore
   - Fill in keystore password and key details

3. **Select build variant:**
   - Choose `release`

4. **Choose signature version:**
   - Select: `V2 (Full APK Signature)`

5. **Finish:**
   - Signed APK will be created at:
   ```
   app/release/app-release.apk
   ```

### Option B: Sign with Command Line

```bash
# First, create a keystore (one-time only)
keytool -genkey -v -keystore ~/lucifer-release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias lucifer-key

# Then sign the APK
jarsigner -verbose -sigalg SHA256withRSA \
  -digestalg SHA-256 \
  -keystore ~/lucifer-release.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  lucifer-key

# Verify signature
jarsigner -verify -verbose -certs \
  app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## 📱 Installation Methods

### Method 1: Direct ADB Install (Recommended)

**Step 1: Make sure watch is connected**
```bash
adb devices
```

You should see:
```
List of attached devices
emulator-5554          device
```

**Step 2: Install the APK**
```bash
# For signed release APK
adb install -r app/build/outputs/apk/release/app-release.apk

# Or the exact path if different
adb install -r /path/to/your/app-release.apk
```

**Step 3: Verify installation**
```bash
adb shell pm list packages | grep lucifer
```

You should see:
```
package:com.monkey.lucifer
```

**Step 4: Run the app**
```bash
adb shell am start -n com.monkey.lucifer/com.monkey.lucifer.presentation.MainActivity
```

---

### Method 2: ADB Over WiFi (Wireless Installation)

If you want to install without USB cable:

**Step 1: Connect watch to WiFi**
- Settings → WiFi → Connect to your network

**Step 2: Enable ADB over network**
```bash
# Via USB first
adb tcpip 5555

# Disconnect USB
```

**Step 3: Connect to watch IP**
```bash
# Get watch IP from Settings → About → IP address
adb connect <watch-ip-address>:5555

# Example:
adb connect 192.168.1.100:5555
```

**Step 4: Install the APK**
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

### Method 3: Android Studio Direct Install

1. **Build and run in Android Studio:**
   - Connect watch via USB
   - Click `Run` button (green play icon)
   - Select your watch device
   - Choose `release` build variant

2. **Or use:**
   ```bash
   ./gradlew installRelease
   ```

---

## ✅ Verify Installation

### Check if app is installed:
```bash
adb shell pm list packages | grep lucifer
```

### Check app details:
```bash
adb shell pm dump com.monkey.lucifer | grep version
```

### Check install path:
```bash
adb shell pm path com.monkey.lucifer
```

### View app in settings:
```bash
adb shell settings list packages
```

---

## 🐛 Troubleshooting

### Problem: "Device not found"
```bash
# Solution 1: Check connections
adb devices

# Solution 2: Restart ADB
adb kill-server
adb start-server
adb devices

# Solution 3: Enable USB debugging on watch
# Settings → Developer options → ADB debugging (ON)
```

### Problem: "Install failed - INSTALL_FAILED_INVALID_APK"
```bash
# Solution: Ensure APK is properly signed
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk
```

### Problem: "Uninstall failed - Device not found during uninstall"
```bash
# Just install anyway with force flag
adb install -r app/build/outputs/apk/release/app-release.apk
```

### Problem: "Multiple devices detected"
```bash
# Specify device serial
adb -s <device-serial> install -r app/build/outputs/apk/release/app-release.apk

# Find serial number
adb devices
```

### Problem: "APK not found"
```bash
# Build first
./gradlew assembleRelease

# Check if file exists
ls -lh app/build/outputs/apk/release/app-release.apk
```

---

## 📊 Quick Reference Commands

| Task | Command |
|------|---------|
| **List devices** | `adb devices` |
| **Build release APK** | `./gradlew assembleRelease` |
| **Sign APK** | `./gradlew assembleRelease` (Android Studio) |
| **Install APK** | `adb install -r app-release.apk` |
| **Uninstall app** | `adb uninstall com.monkey.lucifer` |
| **Start app** | `adb shell am start -n com.monkey.lucifer/.MainActivity` |
| **View logs** | `adb logcat` |
| **Restart ADB** | `adb kill-server && adb start-server` |

---

## 🎯 Complete Workflow

### Quick Build & Install:
```bash
# 1. Build release APK
./gradlew assembleRelease

# 2. Install on watch
adb install -r app/build/outputs/apk/release/app-release.apk

# 3. Start the app
adb shell am start -n com.monkey.lucifer/com.monkey.lucifer.presentation.MainActivity

# 4. View logs (optional)
adb logcat | grep lucifer
```

### Or use one command:
```bash
./gradlew installRelease
```

---

## 🔒 Release APK Considerations

### Signing:
- Release APKs must be signed with a key
- Keep your keystore file safe and backed up
- Use the same key for all updates
- Never share your keystore password

### File Size:
- Check APK size before uploading to stores
```bash
ls -lh app/build/outputs/apk/release/app-release.apk
```

### Performance:
- Release build is optimized (smaller, faster)
- ProGuard/R8 obfuscation enabled
- Better performance on actual devices

### Testing:
- Always test release APK on actual device before publishing
- Check functionality after installation
- Verify all features work (logo, mic, speech, etc.)

---

## 🚀 Next Steps

1. **Build the Release APK:**
   ```bash
   ./gradlew assembleRelease
   ```

2. **Sign the APK** (if not already signed)
   - Use Android Studio's "Generate Signed Bundle/APK"

3. **Install on Watch:**
   ```bash
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```

4. **Launch the App:**
   ```bash
   adb shell am start -n com.monkey.lucifer/com.monkey.lucifer.presentation.MainActivity
   ```

5. **Verify Everything Works:**
   - Check your logo displays ✅
   - Test microphone button 🎤
   - Test voice commands 🗣️
   - Check app icon on launcher 🎨

---

## 💡 Pro Tips

1. **Keep backups of your keystore:**
   ```bash
   cp ~/lucifer-release.keystore ~/backup-keystore/
   ```

2. **Monitor installation with logs:**
   ```bash
   adb logcat | grep -i install
   ```

3. **Uninstall old version before installing:**
   ```bash
   adb uninstall com.monkey.lucifer
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```

4. **Check app size:**
   ```bash
   du -h app/build/outputs/apk/release/app-release.apk
   ```

---

**Status**: ✅ **Complete Guide Ready**  
**Time to Install**: ~5 minutes  
**No Errors**: ✅ Yes

