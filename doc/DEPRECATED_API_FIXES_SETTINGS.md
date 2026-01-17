# Deprecated API Fixes - Settings Activities
**Date**: 2025.10.13
**Updated by**: Claude Code

---

## 📋 Tóm Tắt Thay Đổi

Đã cập nhật **4 Settings Activities** để loại bỏ các deprecated APIs và sử dụng modern Android APIs:

1. ✅ **LicensesAct.kt** - License information display
2. ✅ **OrderAct.kt** - Element ordering settings with drag & drop
3. ✅ **SubmitAct.kt** - Bug/issue submission form
4. ✅ **UnitAct.kt** - Temperature unit selection

---

## 🔄 Chi Tiết Thay Đổi Cho Từng Class

### 1. LicensesAct.kt

#### Deprecated APIs Đã Fix:
- ❌ `systemUiVisibility` → ✅ `WindowInsetsControllerCompat`
- ❌ `SYSTEM_UI_FLAG_*` → ✅ Modern insets APIs
- ❌ `onBackPressed()` → ✅ `OnBackPressedDispatcher`

#### Logic Đặc Biệt:
```kotlin
// ✅ Bảo tồn logic: Check license info panel visibility trước khi back
onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        if (binding.lInc.root.visibility == View.VISIBLE) {
            // Panel đang hiện -> ẩn panel thay vì finish activity
            hideInfoPanel()
        } else {
            // Panel đã ẩn -> finish activity
            finish()
        }
    }
})
```

**Features:**
- Hiển thị license information cho Wikipedia, Sothree libraries
- Scroll-based title bar animation (compact/expanded)
- License panel với fade in/out animation

---

### 2. OrderAct.kt

#### Deprecated APIs Đã Fix:
- ❌ `systemUiVisibility` → ✅ `WindowInsetsControllerCompat`
- ❌ `SYSTEM_UI_FLAG_*` → ✅ Modern insets APIs
- ❌ `onBackPressed()` → ✅ `OnBackPressedDispatcher`

#### Components:
```kotlin
// ✅ RecyclerView với Drag & Drop functionality
val mList = binding.ordRecycler
mList.layoutManager = LinearLayoutManager(this)
mList.adapter = mAdapter
mList.dragListener = onItemDragListener
```

**Features:**
- Drag & Drop RecyclerView để sắp xếp elements
- OnItemDragListener để handle drag events
- Simple back navigation

---

### 3. SubmitAct.kt

#### Deprecated APIs Đã Fix:
- ❌ `systemUiVisibility` → ✅ `WindowInsetsControllerCompat`
- ❌ `SYSTEM_UI_FLAG_*` → ✅ Modern insets APIs
- ❌ `onBackPressed()` → ✅ `OnBackPressedDispatcher`

#### Logic Đặc Biệt:
```kotlin
// ✅ Bảo tồn logic: Check dropdown visibility trước khi back
onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        if (binding.dropIssue.root.visibility == View.VISIBLE) {
            // Dropdown đang hiện -> ẩn dropdown thay vì finish activity
            Utils.fadeOutAnim(binding.background, 150)
            Utils.fadeOutAnim(binding.dropIssue.root, 150)
        } else {
            // Dropdown đã ẩn -> finish activity
            finish()
        }
    }
})
```

**Features:**
- Issue type dropdown (Data Issue, Bug, Question)
- Email intent với pre-filled subject & body
- Scroll-based title bar animation
- Fade animations cho dropdown

---

### 4. UnitAct.kt

#### Deprecated APIs Đã Fix:
- ❌ `systemUiVisibility` → ✅ `WindowInsetsControllerCompat`
- ❌ `SYSTEM_UI_FLAG_*` → ✅ Modern insets APIs
- ❌ `onBackPressed()` → ✅ `OnBackPressedDispatcher`

#### Temperature Units Logic:
```kotlin
// ✅ Logic chọn đơn vị nhiệt độ: Kelvin, Celsius, Fahrenheit
private fun tempUnits() {
    val tempPreference = TemperatureUnits(this)
    val tempPrefValue = tempPreference.getValue()

    // Load saved preference và update UI
    when (tempPrefValue) {
        "kelvin" -> setActiveButton(binding.kelvinBtn)
        "celsius" -> setActiveButton(binding.celsiusBtn)
        "fahrenheit" -> setActiveButton(binding.fahrenheitbtn)
    }

    // Save preference khi user chọn
    binding.kelvinBtn.setOnClickListener {
        tempPreference.setValue("kelvin")
        setActiveButton(binding.kelvinBtn)
    }
    // ... tương tự cho celsius và fahrenheit
}
```

**Features:**
- Temperature unit selection (Kelvin, Celsius, Fahrenheit)
- Chip buttons với active/outline states
- Scroll-based title bar animation
- SharedPreferences integration

---

## 🎯 Pattern Chung Cho Tất Cả Classes

### 1. Edge-to-Edge Setup

```kotlin
// Bật chế độ edge-to-edge
WindowCompat.setDecorFitsSystemWindows(window, false)

// Lấy controller
val windowInsetsController = WindowCompat.getInsetsController(window, view)

// Ẩn navigation bar
windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())

// Set behavior
windowInsetsController.systemBarsBehavior =
    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
```

### 2. Back Press Handler

```kotlin
// Đăng ký callback
onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        // Custom logic here (optional)
        finish()
    }
})

// UI button click
binding.backBtn.setOnClickListener {
    onBackPressedDispatcher.onBackPressed()
}
```

### 3. Scroll-based Title Animation

Tất cả activities (trừ OrderAct) đều có title bar animation:

```kotlin
binding.scrollView.viewTreeObserver
    .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
        override fun onScrollChanged() {
            if (binding.scrollView.scrollY > 150) {
                // Hiện compact title bar
                binding.titleCompact.visibility = View.VISIBLE
                binding.titleExpanded.visibility = View.INVISIBLE
                binding.titleBar.elevation = resources.getDimension(R.dimen.one_elevation)
            } else {
                // Hiện expanded title bar
                binding.titleCompact.visibility = View.INVISIBLE
                binding.titleExpanded.visibility = View.VISIBLE
                binding.titleBar.elevation = resources.getDimension(R.dimen.zero_elevation)
            }
        }
    })
```

---

## 📦 New Imports Added

Tất cả 4 classes đều thêm:

```kotlin
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
```

---

## ✅ Bảo Tồn Logic Gốc 100%

### LicensesAct:
- ✅ License info panel show/hide logic
- ✅ Fade in/out animations
- ✅ Wikipedia & Sothree license buttons
- ✅ Scroll-based title animation

### OrderAct:
- ✅ Drag & Drop RecyclerView functionality
- ✅ OnItemDragListener callbacks
- ✅ Linear layout manager

### SubmitAct:
- ✅ Dropdown show/hide logic
- ✅ Issue type selection (Data Issue, Bug, Question)
- ✅ Email intent với pre-filled data
- ✅ Fade animations
- ✅ Scroll-based title animation

### UnitAct:
- ✅ Temperature unit selection logic
- ✅ Chip button active/outline states
- ✅ SharedPreferences integration
- ✅ Scroll-based title animation

---

## 🚀 Benefits

### 1. Modern APIs
- ✅ No deprecation warnings
- ✅ Support predictive back gesture (Android 13+)
- ✅ Better gesture navigation support
- ✅ Type-safe APIs

### 2. User Experience
- 🎯 Smooth animations
- 🎯 Edge-to-edge design
- 🎯 Predictive back preview
- 🎯 Better system bar behavior

### 3. Code Quality
- 📖 Vietnamese comments giải thích rõ ràng
- 📖 Organized code structure
- 📖 Backward compatible (minSdk 23)
- 📖 Future-proof implementation

---

## 🔍 Testing Checklist

### LicensesAct:
- [ ] Open license panel → Check fade animation
- [ ] Press back khi panel mở → Panel ẩn, không finish activity
- [ ] Press back khi panel đóng → Activity finish
- [ ] Scroll để kiểm tra title animation

### OrderAct:
- [ ] Drag & drop items → Check reordering works
- [ ] Press back → Activity finish
- [ ] Check edge-to-edge layout

### SubmitAct:
- [ ] Open dropdown → Check fade animation
- [ ] Press back khi dropdown mở → Dropdown ẩn, không finish activity
- [ ] Press back khi dropdown đóng → Activity finish
- [ ] Select issue type → Check email intent
- [ ] Scroll để kiểm tra title animation

### UnitAct:
- [ ] Select Kelvin → Check button state changes
- [ ] Select Celsius → Check button state changes
- [ ] Select Fahrenheit → Check button state changes
- [ ] Press back → Activity finish & preference saved
- [ ] Scroll để kiểm tra title animation
- [ ] Reopen → Check selected unit is restored

---

## 📊 Summary

| Class | Deprecated APIs Fixed | Special Logic | Comments Added |
|-------|----------------------|---------------|----------------|
| LicensesAct | 3 | License panel back handling | ✅ Tiếng Việt |
| OrderAct | 3 | Standard back handling | ✅ Tiếng Việt |
| SubmitAct | 3 | Dropdown back handling | ✅ Tiếng Việt |
| UnitAct | 3 | Standard back handling | ✅ Tiếng Việt |
| **Total** | **12 APIs** | **2 special cases** | **✅ Full** |

---

## 🔗 Related Files

- `AboutAct.kt` - ✅ Already fixed
- `FavoritePageAct.kt` - ✅ Already fixed
- `LicensesAct.kt` - ✅ Fixed in this update
- `OrderAct.kt` - ✅ Fixed in this update
- `SubmitAct.kt` - ✅ Fixed in this update
- `UnitAct.kt` - ✅ Fixed in this update

---

**✅ All 4 Settings Activities optimized successfully!**

**Total: 6/6 Activities in settings package are now using modern APIs! 🎉**
