# Memory Leak Analysis Report

**Project:** Atomic Periodic Table Android  
**Date:** 2026-01-17  
**Status:** ✅ Clean (Most issues already fixed)

---

## Executive Summary

Sau khi scan toàn bộ source code, **hầu hết các memory leak tiềm ẩn đã được fix**. Code đã được refactor với best practices để tránh memory leak. Dưới đây là phân tích chi tiết.

---

## 1. Handler Memory Leaks

### Trạng thái: ✅ ĐÃ FIX

Tất cả các Handler đều được cleanup đúng cách trong `onDestroy()`.

| File | Handler | Cleanup trong `onDestroy()` |
|------|---------|----------------------------|
| `MainAct.kt` | `initNameHandler`, `filterHandler` | ✅ Line 813-817 |
| `TableExt.kt` | `boilingHandler`, `meltingHandler`, `phaseHandler`, `yearHandler`, `electroHandler`, `groupsHandler`, `weightHandler`, `heatHandler`, `specificHandler`, `vapeHandler` | ✅ Line 542-561 |
| `SettingsAct.kt` | `themeChangeHandler` | ✅ Line 93 |
| `DictionaryAct.kt` | `updateButtonHandler`, `filterHandler`, `delayCloseHandler` | ✅ Line 326-330 |
| `ElectrodeAct.kt` | `filterHandler`, `delayCloseHandler` | ✅ Line 220-222 |
| `NuclideAct.kt` | `addViewsHandler` | ✅ Line 333 |
| `PHAct.kt` | `updateButtonHandler` | ✅ Line 196 |
| `EquationsAct.kt` | `filterHandler`, `delayCloseHandler` | ✅ Line 265-267 |
| `IonAct.kt` | `filterHandler`, `delayCloseHandler` | ✅ Line 254-256 |
| `IsotopesActExperimental.kt` | `filterHandler` | ✅ Line 403 |

---

## 2. ViewTreeObserver Listeners

### Trạng thái: ✅ ĐÃ FIX

Tất cả `OnScrollChangedListener` được register và unregister đúng cách.

| File | Register | Unregister |
|------|----------|------------|
| `MainAct.kt` | Line 210 | ✅ Line 820 |
| `SettingsAct.kt` | Line 216 | ✅ Line 88 |
| `LicensesAct.kt` | Line 100 | ✅ Line 184 |
| `SubmitAct.kt` | Line 105 | ✅ Line 218 |
| `UnitAct.kt` | Line 95 | ✅ Line 183 |
| `FavoritePageAct.kt` | Line 295 | ✅ Line 67 |
| `TableAct.kt` | Line 97 | ✅ Line 195 |

---

## 3. TextWatcher Listeners

### Trạng thái: ✅ ĐÃ FIX

Tất cả TextWatcher được store và cleanup đúng cách.

| File | Register | Unregister |
|------|----------|------------|
| `MainAct.kt` | Line 130 | ✅ Line 830 |
| `IsotopesActExperimental.kt` | Line 93 | ✅ Line 408 |
| `DictionaryAct.kt` | Line 222 | ✅ Line 335 |
| `EquationsAct.kt` | Line 183 | ✅ Line 272 |
| `ElectrodeAct.kt` | Line 165 | ✅ Line 227 |
| `IonAct.kt` | Line 204 | ✅ Line 261 |

---

## 4. SlidingUpPanelLayout Listeners

### Trạng thái: ✅ ĐÃ FIX

| File | Register | Unregister |
|------|----------|------------|
| `MainAct.kt` | Line 226 | ✅ Line 825 |
| `IsotopesActExperimental.kt` | Line 108 | ✅ Line 413 |

---

## 5. AdMobManager - Potential Memory Leaks

### Trạng thái: ⚠️ CẦN XEM XÉT

File: `AdMobManager.kt`

#### 5.1 CoroutineScope không có lifecycle management

```kotlin
// Line 122, 505
CoroutineScope(Dispatchers.Default).launch { ... }
```

**Vấn đề:** CoroutineScope được tạo mà không được cancel, có thể gây leak nếu Activity bị destroy.

**Đề xuất:** Sử dụng `lifecycleScope` hoặc `viewModelScope` thay vì tạo CoroutineScope mới.

#### 5.2 Handler trong AdMobManager

```kotlin
// Line 369, 376, 384, 395, 412, 421
Handler(Looper.getMainLooper()).postDelayed({ ... }, 1_000)
```

**Vấn đề:** Handler được tạo inline mà không lưu reference để cancel.

**Rủi ro:** Thấp trong trường hợp này vì AdMobManager là object singleton và các delay ngắn (1 giây).

#### 5.3 WeakReference đã được sử dụng đúng

```kotlin
// Line 77
private var currentActivity: WeakReference<Activity>? = null

// Line 167-168
fun setCurrentActivity(activity: Activity) {
    this.currentActivity = WeakReference(activity)
}
```

✅ Đây là best practice để tránh leak Activity reference.

---

## 6. Anonymous Object Classes (Inner Classes)

### Trạng thái: ⚠️ TIỀM ẨN RỦI RO NHỎ

Các anonymous object class giữ implicit reference đến outer class (Activity/Fragment).

| File | Line | Type | Rủi ro |
|------|------|------|--------|
| `NuclideAct.kt` | 116 | `SeekBar.OnSeekBarChangeListener` | Thấp - không có long-running task |
| `AdMobManager.kt` | 210, 264, 282, 406, 444 | Ad callbacks | Thấp - managed by SDK |
| `InfoExt.kt` | 435 | Picasso Callback | Thấp - short-lived |

**Ghi chú:** Các listener này có lifecycle ngắn hoặc được quản lý bởi framework/SDK, nên rủi ro thấp.

---

## 7. Picasso Image Loading

### Trạng thái: ✅ AN TOÀN

File: `InfoExt.kt`

Picasso được sử dụng đúng cách với:
- Placeholder và error images
- Callback để handle success/error
- OkHttp3Downloader với custom client

```kotlin
// Line 426-447
val picasso = Picasso.Builder(this)
    .downloader(OkHttp3Downloader(client))
    .build()

picasso.load(url)
    .placeholder(R.drawable.ic_launcher_background)
    .error(R.drawable.ic_launcher_background)
    .into(binding.elementImage, object : com.squareup.picasso.Callback { ... })
```

Picasso tự động quản lý lifecycle và cancel requests khi View bị detached.

---

## 8. RoyApp - Application Class

### Trạng thái: ⚠️ CẦN XEM XÉT

File: `RoyApp.kt`

```kotlin
// Line 42
CoroutineScope(Dispatchers.IO).launch { ... }
```

**Vấn đề:** CoroutineScope trong Application không được cancel.

**Rủi ro:** Thấp vì Application tồn tại suốt lifecycle của app.

---

## 9. Best Practices Đã Áp Dụng

### 9.1 View.postDelayed thay vì Handler

```kotlin
// ElementInfoAct.kt Line 221-223
// Memory leak fix: Use View.postDelayed instead of Handler
view.postDelayed({ ... }, delay)
```

```kotlin
// Utils.kt Line 41-43
// Use View.postDelayed instead of Handler to tie the callback to the View's lifecycle
view.postDelayed({ ... }, time)
```

```kotlin
// Anim.kt Line 17-19
// Use View.postDelayed instead of Handler to tie the callback to the View's lifecycle
view.postDelayed({ ... }, time)
```

### 9.2 Nullable Handler References

Tất cả Handler được khai báo nullable và cleanup:

```kotlin
private var handler: Handler? = null

// In onDestroy
handler?.removeCallbacksAndMessages(null)
handler = null
```

### 9.3 Listener References Stored

Tất cả listeners được lưu reference để cleanup:

```kotlin
private var scrollChangedListener: OnScrollChangedListener? = null

// Register
scrollChangedListener = object : OnScrollChangedListener { ... }
binding.scrollView.viewTreeObserver.addOnScrollChangedListener(scrollChangedListener)

// Unregister in onDestroy
scrollChangedListener?.let {
    binding.scrollView.viewTreeObserver.removeOnScrollChangedListener(it)
}
scrollChangedListener = null
```

---

## Recommendations

### Priority 1: Không cần hành động ngay

Code hiện tại đã được implement đúng best practices. Các potential issues có rủi ro thấp.

### Priority 2: Improvements (Optional)

1. **AdMobManager CoroutineScope**
   - Xem xét sử dụng `ProcessLifecycleOwner.get().lifecycleScope` thay vì tạo CoroutineScope mới

2. **SeekBar Listener trong NuclideAct**
   - Có thể store listener và set null trong onDestroy (minor improvement)

### Priority 3: Monitoring

- Sử dụng LeakCanary trong debug builds để phát hiện memory leak sớm
- Monitor memory usage trong Android Profiler

---

## Conclusion

**Đánh giá tổng thể: 🟢 TỐT**

Source code đã được refactor với memory leak prevention best practices:
- ✅ Handler được cleanup đúng cách
- ✅ Listeners được unregister trong onDestroy
- ✅ WeakReference được sử dụng cho Activity references
- ✅ View.postDelayed thay vì Handler ở một số nơi
- ⚠️ Một vài CoroutineScope cần xem xét (rủi ro thấp)

Không phát hiện memory leak nghiêm trọng nào.
