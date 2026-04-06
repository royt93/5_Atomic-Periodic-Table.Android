# Memory Leak Audit - Atomic Periodic Table Android

> Cập nhật lần cuối: 2026-04-06 | Phiên 2 (Full Audit)
> Build Status: ✅ BUILD SUCCESSFUL (Debug + Release, 3 rounds)

---

## Tóm tắt Trạng thái

| Khu vực | Trạng thái | Ghi chú |
|---|---|---|
| Handlers (Activities) | ✅ Đã xử lý | Tất cả Handler đều được lưu trữ và cleanup trong onDestroy() |
| TextWatchers | ✅ Đã xử lý | Remove listener trong onDestroy() |
| ViewTreeObserver Listeners | ✅ Đã xử lý | Remove listener trong onDestroy() |
| SlidingPanel Listeners | ✅ Đã xử lý | Remove listener trong onDestroy() |
| AdMob Banner (AdView) | ✅ Đã xử lý | destroy() gọi trong onDestroy() |
| Activity reference in Singleton | ✅ Đã FIX (Phiên 2) | MainAct clear AdMobManager.interstitialListener trong onDestroy() |
| Handler trong for-loop | ✅ Đã FIX (Phiên 2) | DictionaryAct: Handler tạo N lần/keystroke → di chuyển ra ngoài loop |
| SettingsAct inline Handler | ✅ Đã FIX (Phiên 2) | showLanguageConfirmDialog dùng themeChangeHandler thay vì tạo mới |
| AdMobManager CoroutineScope | ✅ Đã FIX (Phiên 2) | splashScreenJob được track và có thể cancel |
| AdMobManager.initSplashScreen | ✅ Đã FIX (Phiên 2) | Job được cancel trước khi tạo Job mới |
| SplashAct animations | ✅ Đã FIX (Phiên 1) | Cancel animation trong onDestroy() |
| Context leaks (WeakReference) | ✅ OK | AdMobManager dùng WeakReference<Activity> |

---

## Bug Đã Tìm Thấy Và Fix (Phiên 2)

### 🔴 Bug Nghiêm Trọng 1: Activity Reference Leak trong Singleton

**File:** `MainAct.kt`  
**Vấn đề:**
```kotlin
// onCreate() - Set listener của singleton trỏ vào Activity
AdMobManager.interstitialListener = this  // this = Activity

// onDestroy() trước khi fix - KHÔNG clear listener
override fun onDestroy() {
    // ... handlers cleanup
    // ❌ THIẾU: Không clear AdMobManager.interstitialListener
    super.onDestroy()
}
```
AdMobManager là **singleton** (object). Nếu Activity bị destroy (xoay màn hình, back stack), singleton vẫn giữ strong reference tới Activity đã bị destroy → **Memory Leak**.

**Fix:**
```kotlin
override fun onDestroy() {
    // ... handlers cleanup

    // ✅ FIX: Clear singleton reference để tránh Activity leak
    if (AdMobManager.interstitialListener === this) {
        AdMobManager.interstitialListener = null
    }
    super.onDestroy()
}
```

---

### 🔴 Bug Nghiêm Trọng 2: Handler Tạo Trong For-Loop (N Handlers/Keystroke)

**File:** `DictionaryAct.kt` - hàm `filter()`  
**Vấn đề:** Mỗi lần user gõ 1 ký tự, hàm `filter()` được gọi, và bên trong có vòng `for` lặp qua từng item. Với danh sách 800+ items, mỗi keystroke tạo ra **800+ Handler objects** và post **800+ callbacks**:

```kotlin
// ❌ BUG: Handler TẠO TRONG FOR LOOP → N handlers/keystroke
for (item in list) {
    // check filter ...
    filterHandler = Handler(Looper.getMainLooper())  // ← Tạo Handler N lần!
    filterHandler?.postDelayed({
        // update UI...
    }, 10)
    // update adapter...
}
```

Điều này gây ra:
- **Memory pressure**: 800+ Handler instances được tạo/huỷ mỗi keystroke
- **UI flicker**: Adapter reset 800+ lần thay vì 1 lần
- **Potential leak**: filterHandler chỉ giữ reference tới handler CUỐI CÙNG, 799 handlers trước không được cancel

**Fix:**
```kotlin
// ✅ FIX: Chỉ tạo 1 Handler, NGOÀI for-loop
for (item in list) {
    // check filter, add to filteredList...
}
// Handler tạo SAU khi lọc xong toàn bộ
filterHandler?.removeCallbacksAndMessages(null)
filterHandler = Handler(Looper.getMainLooper())
filterHandler?.postDelayed({
    // update UI...
}, 10)
// Update adapter 1 lần duy nhất
mAdapter.filterList(filteredList)
recyclerView.adapter = ...
```

---

### 🟡 Bug Trung Bình 3: Inline Handler Không Được Storage/Cancel

**File:** `SettingsAct.kt` - hàm `showLanguageConfirmDialog()`  
**Vấn đề:**
```kotlin
// ❌ Tạo Handler inline, không save reference → không cancel được
Handler(Looper.getMainLooper()).postDelayed({
    Utils.fadeInAnim(binding.confirmDialog.root, 300)
}, 320)
```
Nếu Activity bị destroy trong 320ms delay, callback vẫn chạy → NPE hoặc leak.

**Fix:**
```kotlin
// ✅ Reuse themeChangeHandler đã có sẵn trong class
themeChangeHandler?.removeCallbacksAndMessages(null)  // cancel previous
themeChangeHandler = Handler(Looper.getMainLooper())
themeChangeHandler?.postDelayed({
    Utils.fadeInAnim(binding.confirmDialog.root, 300)
}, 320)
```
themeChangeHandler đã được cleanup trong `onDestroy()` của SettingsAct.

---

### 🟡 Bug Trung Bình 4: Coroutine Không Được Track/Cancel

**File:** `AdMobManager.kt` - hàm `initSplashScreen()`  
**Vấn đề:**
```kotlin
// ❌ Coroutine tạo không được track
CoroutineScope(Dispatchers.Default).launch {
    // collect events MÃIMÃI nếu không bị cancel
    EventBus.eventFlow.collectLatest { value ->
        // Nested coroutine không được track
        CoroutineScope(Dispatchers.Main).launch {
            loadAppOpenAd(...)
        }
    }
}
```
Nếu `initSplashScreen()` được gọi nhiều lần (edge case), nhiều coroutine thu thập EventBus cùng lúc.

**Fix:**
```kotlin
// ✅ Track Job để có thể cancel
private var splashScreenJob: Job? = null  // Track tại class level

// Trong hàm:
splashScreenJob?.cancel()  // Cancel job cũ trước
splashScreenJob = CoroutineScope(Dispatchers.Default).launch {
    EventBus.eventFlow.collectLatest { value ->
        CoroutineScope(Dispatchers.Main).launch {
            loadAppOpenAd(
                onAdLoaded = { result ->
                    splashScreenJob = null  // Clear sau khi hoàn thành
                    // ...
                }
            )
        }
    }
}
```

---

## Những Gì Đã Đúng (Không Cần Fix)

### ✅ Handler Cleanup Pattern (Áp dụng đúng toàn bộ codebase)
```kotlin
// Pattern đúng, áp dụng trong: MainAct, NuclideAct, PHAct, IonAct, EquationsAct, ElectrodeAct, IsotopesActExperimental, TableExt
private var handler: Handler? = null

// Khởi tạo:
handler = Handler(Looper.getMainLooper())
handler?.postDelayed({ ... }, delay)

// Cleanup onDestroy():
handler?.removeCallbacksAndMessages(null)
handler = null
```

### ✅ TextWatcher Cleanup (Đúng trong tất cả Activity có search)
```kotlin
// Lưu reference:
private var textWatcher: TextWatcher? = null
textWatcher = object : TextWatcher { ... }
binding.editText.addTextChangedListener(textWatcher)

// Cleanup:
textWatcher?.let { binding.editText.removeTextChangedListener(it) }
textWatcher = null
```

### ✅ ViewTreeObserver.OnScrollChangedListener (Đúng)
```kotlin
// Pattern trong: SettingsAct, FavoritePageAct, LicensesAct, SubmitAct, UnitAct
private var scrollChangedListener: ViewTreeObserver.OnScrollChangedListener? = null

// Cleanup:
scrollChangedListener?.let {
    binding.scrollView.viewTreeObserver.removeOnScrollChangedListener(it)
}
scrollChangedListener = null
```

### ✅ SlidingUpPanel Listener
```kotlin
// Pattern trong: MainAct, IsotopesActExperimental
private var panelSlideListener: SlidingUpPanelLayout.PanelSlideListener? = null

// Cleanup:
panelSlideListener?.let {
    binding.slidingLayout.removePanelSlideListener(it)
}
panelSlideListener = null
```

### ✅ AdView (Banner) Lifecycle
```kotlin
// onResume: adView?.resume()
// onPause: adView?.pause()
// onDestroy: adView?.destroy()
```

### ✅ WeakReference cho Activity trong Singleton
AdMobManager dùng `WeakReference<Activity>` cho `currentActivity`, ngăn singleton giữ strong reference.

### ✅ OnBackPressedCallback
Tất cả Activity dùng `OnBackPressedDispatcher` thay vì deprecated `onBackPressed()`. Callback tự động cleanup khi Activity bị destroy.

---

## Các Cảnh Báo Deprecation (Không Phải Memory Leak)

Những cảnh báo này xuất hiện trong build logs nhưng KHÔNG phải memory leak — chỉ là deprecated API:

| File | API Deprecated | Tác động |
|---|---|---|
| `CalculatorAct.kt:51` | `onBackPressed()` | Functional nhưng nên migrate |
| `SplashAct.kt:144` | `overridePendingTransition()` | Functional nhưng nên migrate |
| `AdMobManager.kt:658-659` | `activeNetworkInfo`, `isConnected` | Functional trên API < 23 |
| `LocaleHelper.kt:33` | `Locale(String)` constructor | Functional |

---

## Khuyến Nghị Tương Lai (Priority 3 - Không Khẩn Cấp)

1. **LeakCanary**: Thêm vào `build.gradle` (debug only):
   ```kotlin
   debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
   ```

2. **CalculatorAct**: Migrate từ `onBackPressed()` sang `OnBackPressedDispatcher`

3. **AdMobManager CoroutineScopes**: Cân nhắc dùng `ProcessLifecycleOwner.get().lifecycleScope` thay vì `CoroutineScope(Dispatchers.Default)` để lifecycle-aware hơn

4. **DictionaryPref trong loop**: `DictionaryPref(this)` được tạo cho mỗi item trong `DictionaryAct.filter()`. Tối ưu: tạo 1 lần bên ngoài loop.

---

## Kết Quả Kiểm Tra Build

| Round | Loại Build | Kết Quả |
|---|---|---|
| 1 | `assembleDebug` (incremental) | ✅ BUILD SUCCESSFUL (1m 1s) |
| 2 | `assembleDebug` (cached) | ✅ BUILD SUCCESSFUL (4s) |
| 3 | `clean assembleRelease` | ✅ BUILD SUCCESSFUL (3m 16s) |

Không có lỗi biên dịch. Chỉ có cảnh báo deprecation không ảnh hưởng đến runtime.
