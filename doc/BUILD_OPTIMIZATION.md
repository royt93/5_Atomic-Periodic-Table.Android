# APK/AAB Size Optimization Guide
**Last Updated**: 2025.10.13

## Summary of Optimizations Applied

### ✅ Build Configuration Optimizations

#### 1. **R8 Full Mode** (gradle.properties)
```properties
android.enableR8.fullMode=true
```
- Maximum code shrinking and obfuscation
- Dead code elimination
- Aggressive optimization

#### 2. **ProGuard Configuration** (app/build.gradle)
```gradle
proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
```
- Using `proguard-android-optimize.txt` instead of `proguard-android.txt`
- More aggressive optimization rules

#### 3. **Resource Shrinking** (app/build.gradle)
```gradle
release {
    minifyEnabled true
    shrinkResources true  // Automatically removes unused resources
}
```

#### 4. **ABI Splits** (app/build.gradle)
```gradle
splits {
    abi {
        enable true
        reset()
        include 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
        universalApk false
    }
}
```
**Benefits:**
- Reduces individual APK size by ~30-40%
- Google Play automatically serves the right APK for each device
- arm64-v8a: Modern 64-bit ARM devices (most common)
- armeabi-v7a: Older 32-bit ARM devices
- x86/x86_64: Intel-based Android devices (rare)

#### 5. **Packaging Options** (app/build.gradle)
```gradle
packagingOptions {
    resources {
        excludes += [
            'META-INF/DEPENDENCIES',
            'META-INF/LICENSE',
            'META-INF/*.kotlin_module',
            ...
        ]
    }
}
```
- Removes duplicate and unnecessary META-INF files

#### 6. **Native Debug Symbols** (app/build.gradle)
```gradle
ndk {
    debugSymbolLevel 'SYMBOL_TABLE'
}
```
- Smaller native debug symbols for crash reporting

#### 7. **Vector Drawable Support** (app/build.gradle)
```gradle
vectorDrawables.useSupportLibrary = true
```
- Use vector graphics instead of multiple PNG densities

#### 8. **Log Removal in Release** (proguard-rules.pro)
```proguard
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
```
- Removes all Log statements in release builds
- Reduces APK size by removing debug strings

### ⚡ Build Performance Optimizations

#### 9. **Gradle Parallel Execution** (gradle.properties)
```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
```

#### 10. **JVM Memory** (gradle.properties)
```properties
org.gradle.jvmargs=-Xmx4096m
```

---

## How to Build Optimized APK/AAB

### Option 1: Build AAB (Recommended for Google Play)
```bash
./gradlew :app:bundleProductionRelease
```
**Output location:**
```
app/build/outputs/bundle/productionRelease/
```

### Option 2: Build APK
```bash
./gradlew :app:assembleProductionRelease
```
**Output location:**
```
app/build/outputs/apk/productionRelease/
```

**Note:** Due to ABI splits, you'll get 4 separate APKs:
- `app-armeabi-v7a-productionRelease.apk`
- `app-arm64-v8a-productionRelease.apk`
- `app-x86-productionRelease.apk`
- `app-x86_64-productionRelease.apk`

---

## Expected APK Size Reduction

### Before Optimization (estimated):
- Universal APK: ~35-45 MB
- AAB: ~30-40 MB

### After Optimization (estimated):
- **arm64-v8a APK**: ~12-18 MB ⬇️ **60-70% smaller**
- **armeabi-v7a APK**: ~10-15 MB ⬇️ **60-70% smaller**
- **AAB**: ~20-30 MB ⬇️ **30-40% smaller**

---

## Additional Manual Optimizations (Optional)

### 1. **Language Resources** (app/build.gradle)
If your app only supports specific languages:
```gradle
defaultConfig {
    // Keep only English and Vietnamese
    resConfigs 'en', 'vi'
}
```

### 2. **Density Resources** (app/build.gradle)
If you want to optimize for specific screen densities:
```gradle
defaultConfig {
    // Keep only common densities
    resConfigs 'hdpi', 'xhdpi', 'xxhdpi', 'xxxhdpi'
}
```

### 3. **WebP Images**
Convert all PNG/JPG images to WebP format:
- Right-click image in Android Studio → Convert to WebP
- WebP provides 25-35% better compression than PNG/JPG
- Fully supported on Android 4.2.1+ (API 18+)

### 4. **Remove Unused Dependencies**
Review your `dependencies` block and remove any unused libraries.

---

## Build Verification

After building, verify your optimized APK:

```bash
# Analyze APK size breakdown
./gradlew :app:analyzeProductionReleaseBundle

# Or use Android Studio
# Build → Analyze APK → Select your APK file
```

---

## Troubleshooting

### Issue: App crashes after ProGuard optimization
**Solution:** Add keep rules for classes that use reflection:
```proguard
-keep class your.package.** { *; }
```

### Issue: Missing resources in release build
**Solution:** Disable shrinkResources temporarily:
```gradle
shrinkResources false
```

### Issue: R8 build is too slow
**Solution:** Reduce optimization passes in proguard-rules.pro:
```proguard
-optimizationpasses 3  # Reduce from 5 to 3
```

---

## Notes

1. **Test thoroughly** after enabling these optimizations
2. **Keep ProGuard mapping files** for crash reporting
3. **Use AAB format** for Google Play (automatic ABI optimization)
4. **Monitor crash reports** after release to catch ProGuard issues

---

## Build Variants

This project has 2 flavors × 2 build types = 4 variants:

| Flavor | Build Type | Output |
|--------|-----------|--------|
| dev | debug | Development build with test ads |
| dev | release | Optimized build with test ads |
| production | debug | Development build with real ads |
| production | release | **Final optimized build** |

**For production release:**
```bash
./gradlew :app:bundleProductionRelease
# or
./gradlew :app:assembleProductionRelease
```

---

## References

- [Android R8 Optimization](https://developer.android.com/studio/build/shrink-code)
- [ProGuard Rules](https://www.guardsquare.com/manual/configuration/usage)
- [APK Splits](https://developer.android.com/studio/build/configure-apk-splits)
- [Android App Bundle](https://developer.android.com/guide/app-bundle)
