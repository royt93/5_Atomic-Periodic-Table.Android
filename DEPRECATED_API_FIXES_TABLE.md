# Deprecated API Fixes - Table Activities
**Date**: 2025.10.13
**Updated by**: Claude Code

---

## 📋 Tóm Tắt Thay Đổi

Đã cập nhật **6 Table Activities** để loại bỏ các deprecated APIs và sử dụng modern Android APIs:

1. ✅ **DictionaryAct.kt** - Chemistry/Physics/Math dictionary with search
2. ✅ **ElectrodeAct.kt** - Electrode potential series with search
3. ✅ **EquationsAct.kt** - Chemical equations with detail panel (special back logic)
4. ✅ **IonAct.kt** - Ionization energy data with detail panel (special back logic)
5. ✅ **NuclideAct.kt** - Nuclide chart with zoom & pan
6. ✅ **PHAct.kt** - pH indicators comparison

---

## 🔄 Chi Tiết Thay Đổi Cho Từng Class

### 1. DictionaryAct.kt

#### Deprecated APIs Đã Fix:
- ❌ `systemUiVisibility` → ✅ `WindowInsetsControllerCompat`
- ❌ `SYSTEM_UI_FLAG_*` → ✅ Modern insets APIs
- ❌ `onBackPressed()` → ✅ `OnBackPressedDispatcher`

#### Features:
```kotlin
// Dictionary categories: Chemistry, Physics, Math, Reactions
// Search functionality với filter
// Custom Tabs để mở Wikipedia links
// Chip buttons để chọn category
```

**Special Logic:**
- Filter theo category (chemistry, physics, math, reactions)
- SharedPreferences để lưu category đã chọn
- Empty search animation khi không có results
- Custom Tabs integration với Chrome

---

### 2. ElectrodeAct.kt

#### Deprecated APIs Đã Fix:
- ❌ `systemUiVisibility` → ✅ `WindowInsetsControllerCompat`
- ❌ `SYSTEM_UI_FLAG_*` → ✅ Modern insets APIs
- ❌ `onBackPressed()` → ✅ `OnBackPressedDispatcher`

#### Features:
```kotlin
// Electrode potential series data
// Search functionality with filter
// RecyclerView với LinearLayoutManager
// Empty search box animation
```

**Special Logic:**
- Simple search filter theo electrode name
- Fade in/out animations cho search bar
- Standard back navigation

---

### 3. EquationsAct.kt

#### Deprecated APIs Đã Fix:
- ❌ `systemUiVisibility` → ✅ `WindowInsetsControllerCompat`
- ❌ `SYSTEM_UI_FLAG_*` → ✅ Modern insets APIs
- ❌ `onBackPressed()` override → ✅ `OnBackPressedDispatcher`

#### Logic Đặc Biệt:
```kotlin
// ✅ Bảo tồn logic: Check equation info panel visibility trước khi back
onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        if (binding.eInc.root.visibility == View.VISIBLE) {
            // Panel đang hiện → ẩn panel thay vì finish activity
            hideInfoPanel()
        } else {
            // Panel đã ẩn → finish activity
            finish()
        }
    }
})
```

**Features:**
- Chemical equations list với search
- Equation detail panel với image và description
- ColorMatrixColorFilter for dark theme (negative filter)
- Click item để hiện detail panel
- Click background hoặc back button để ẩn panel

---

### 4. IonAct.kt

#### Deprecated APIs Đã Fix:
- ❌ `systemUiVisibility` → ✅ `WindowInsetsControllerCompat`
- ❌ `SYSTEM_UI_FLAG_*` → ✅ Modern insets APIs
- ❌ `onBackPressed()` override → ✅ `OnBackPressedDispatcher`

#### Logic Đặc Biệt:
```kotlin
// ✅ Bảo tồn logic: Check ion detail panel visibility trước khi back
onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        if (binding.ionDetail.root.visibility == View.VISIBLE) {
            // Panel đang hiện → ẩn panel với fade animation thay vì finish activity
            Utils.fadeOutAnim(binding.ionDetail.root, 300)
        } else {
            // Panel đã ẩn → finish activity
            finish()
        }
    }
})
```

**Features:**
- Ion list với element names
- Click item để hiện ionization energy levels (1-30)
- Dynamic TextView visibility (hiện energy levels theo số lượng)
- JSON parsing từ assets để load element data
- Fade in/out animations cho detail panel

---

### 5. NuclideAct.kt

#### Deprecated APIs Đã Fix:
- ❌ `systemUiVisibility` → ✅ `WindowInsetsControllerCompat`
- ❌ `SYSTEM_UI_FLAG_*` → ✅ Modern insets APIs
- ❌ `onBackPressed()` → ✅ `OnBackPressedDispatcher`

#### Features:
```kotlin
// Nuclide chart (N-Z diagram)
// Pinch-to-zoom với ScaleGestureDetector
// SeekBar để zoom
// Dynamic view creation từ JSON data
// Color coding theo decay type:
//   - stable: dark gray
//   - proton emission (p, 2p, 3p): red shades
//   - beta plus (B+): light red
//   - beta minus (B-, 2B-): blue shades
//   - neutron emission (n, 2n): teal shades
//   - alpha (a): yellow
//   - electron capture: purple
```

**Special Logic:**
- Complex zoom & pan functionality
- ViewStub inflation
- Loading screen với Handler delay
- JSON parsing cho isotope data
- Dynamic color tinting theo decay type

---

### 6. PHAct.kt

#### Deprecated APIs Đã Fix:
- ❌ `systemUiVisibility` → ✅ `WindowInsetsControllerCompat`
- ❌ `SYSTEM_UI_FLAG_*` → ✅ Modern insets APIs
- ❌ `onBackPressed()` → ✅ `OnBackPressedDispatcher`

#### Features:
```kotlin
// pH Indicators comparison
// Chip buttons: Bromothymol Blue, Methyl Orange, Congo Red, Phenolphthalein
// Color visualization cho acid, neutral, alkaline states
// Dynamic text updates cho pH ranges
```

**Special Logic:**
- Chip button selection với active state
- ColorFilter cho indicator colors (acid, neutral, alkali)
- Dynamic resource identifier lookup
- Handler delay để update button backgrounds

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

**Standard Pattern (DictionaryAct, ElectrodeAct, NuclideAct, PHAct):**
```kotlin
// Đăng ký callback
onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        // Simple finish
        finish()
    }
})

// UI button click
binding.backBtn.setOnClickListener {
    onBackPressedDispatcher.onBackPressed()
}
```

**Special Pattern (EquationsAct, IonAct):**
```kotlin
// Đăng ký callback với logic kiểm tra panel visibility
onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        if (binding.detailPanel.root.visibility == View.VISIBLE) {
            // Ẩn panel thay vì finish
            hideInfoPanel()
        } else {
            // Panel đã ẩn → finish activity
            finish()
        }
    }
})
```

### 3. Search Functionality Pattern

4 classes có search functionality (DictionaryAct, ElectrodeAct, EquationsAct, IonAct):

```kotlin
// Search button → hiện search bar + keyboard
binding.searchBtn.setOnClickListener {
    Utils.fadeInAnim(binding.searchBar, 150)
    Utils.fadeOutAnim(binding.titleBox, 1)
    binding.editText.requestFocus()

    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(binding.editText, InputMethodManager.SHOW_IMPLICIT)
}

// Close search → ẩn search bar + keyboard
binding.closeSearch.setOnClickListener {
    Utils.fadeOutAnim(binding.searchBar, 1)
    Handler(Looper.getMainLooper()).postDelayed({
        Utils.fadeInAnim(binding.titleBox, 150)
    }, 151)

    // Hide keyboard
    val view = currentFocus
    if (view != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

// TextWatcher để filter list
binding.editText.addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(s: Editable) {
        filter(s.toString(), list, recyclerView)
    }
})
```

---

## 📦 New Imports Added

Tất cả 6 classes đều thêm:

```kotlin
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
```

---

## ✅ Bảo Tồn Logic Gốc 100%

### DictionaryAct:
- ✅ Category filter (chemistry, physics, math, reactions)
- ✅ Chip button selection với active state
- ✅ SharedPreferences integration
- ✅ Custom Tabs untuk Wikipedia links
- ✅ Search functionality với empty state animation

### ElectrodeAct:
- ✅ Electrode series data display
- ✅ Search filter theo name
- ✅ Empty search animation
- ✅ RecyclerView với LinearLayoutManager

### EquationsAct:
- ✅ Equation detail panel show/hide logic
- ✅ ColorMatrixColorFilter for dark theme
- ✅ Click item để hiện detail
- ✅ Click background hoặc back để ẩn panel
- ✅ Search functionality

### IonAct:
- ✅ Ion detail panel show/hide logic
- ✅ Dynamic TextView visibility (1-30 levels)
- ✅ JSON parsing từ assets
- ✅ Fade animations với 300ms duration
- ✅ Search functionality

### NuclideAct:
- ✅ Pinch-to-zoom functionality
- ✅ ScaleGestureDetector & GestureDetector
- ✅ SeekBar zoom control
- ✅ Dynamic view creation từ JSON
- ✅ Color coding theo decay type
- ✅ ViewStub inflation
- ✅ Loading screen

### PHAct:
- ✅ pH indicator selection (4 indicators)
- ✅ Chip button active state
- ✅ ColorFilter cho acid/neutral/alkali colors
- ✅ Dynamic text updates
- ✅ Resource identifier lookup

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
- 🎯 Special back handling cho detail panels

### 3. Code Quality
- 📖 Vietnamese comments giải thích rõ ràng
- 📖 Organized code structure
- 📖 Backward compatible (minSdk 23)
- 📖 Future-proof implementation
- 📖 Consistent pattern across all classes

---

## 🔍 Testing Checklist

### DictionaryAct:
- [ ] Select chemistry category → Check filter works
- [ ] Search terms → Check filter results
- [ ] Click Wikipedia link → Opens in Custom Tab
- [ ] Clear category → Check all items visible
- [ ] Press back → Activity finish

### ElectrodeAct:
- [ ] Search electrode names → Check filter works
- [ ] Empty search → Check empty state animation
- [ ] Press back → Activity finish

### EquationsAct:
- [ ] Click equation item → Detail panel hiện
- [ ] Press back khi panel mở → Panel ẩn, không finish activity
- [ ] Press back khi panel đóng → Activity finish
- [ ] Check dark theme → Image có negative filter
- [ ] Search equations → Check filter works

### IonAct:
- [ ] Click ion item → Detail panel hiện với energy levels
- [ ] Press back khi panel mở → Panel ẩn với fade animation
- [ ] Press back khi panel đóng → Activity finish
- [ ] Check energy levels → Dynamic TextView visibility
- [ ] Search ions → Check filter works

### NuclideAct:
- [ ] Pinch to zoom → Check scale animation works
- [ ] SeekBar zoom → Check scale animation works
- [ ] Scroll chart → Check pan functionality
- [ ] Check loading screen → Disappears after data loads
- [ ] Verify decay colors → All types có correct colors
- [ ] Press back → Activity finish

### PHAct:
- [ ] Select Bromothymol Blue → Check colors update
- [ ] Select Methyl Orange → Check colors update
- [ ] Select Congo Red → Check colors update
- [ ] Select Phenolphthalein → Check colors update
- [ ] Check button active state → Only selected có active background
- [ ] Press back → Activity finish

---

## 📊 Summary

| Class | Deprecated APIs Fixed | Special Logic | Search | Comments Added |
|-------|----------------------|---------------|--------|----------------|
| DictionaryAct | 3 | Category filter, Custom Tabs | ✅ | ✅ Tiếng Việt |
| ElectrodeAct | 3 | Standard navigation | ✅ | ✅ Tiếng Việt |
| EquationsAct | 3 | Detail panel back handling | ✅ | ✅ Tiếng Việt |
| IonAct | 3 | Detail panel back handling | ✅ | ✅ Tiếng Việt |
| NuclideAct | 3 | Zoom & pan, ViewStub | ❌ | ✅ Tiếng Việt |
| PHAct | 3 | Indicator selection | ❌ | ✅ Tiếng Việt |
| **Total** | **18 APIs** | **2 special back cases** | **4 có search** | **✅ Full** |

---

## 🔗 Related Files

**Settings Package (Previously Fixed):**
- `AboutAct.kt` - ✅ Already fixed
- `FavoritePageAct.kt` - ✅ Already fixed
- `LicensesAct.kt` - ✅ Already fixed
- `OrderAct.kt` - ✅ Already fixed
- `SubmitAct.kt` - ✅ Already fixed
- `UnitAct.kt` - ✅ Already fixed

**Table Package (Fixed in this update):**
- `DictionaryAct.kt` - ✅ Fixed
- `ElectrodeAct.kt` - ✅ Fixed
- `EquationsAct.kt` - ✅ Fixed
- `IonAct.kt` - ✅ Fixed
- `NuclideAct.kt` - ✅ Fixed
- `PHAct.kt` - ✅ Fixed

---

## 🎉 Implementation Highlights

### Complex Features Preserved:

1. **DictionaryAct**: Multi-category dictionary với Custom Tabs integration
2. **EquationsAct**: Detail panel với dark theme ColorMatrix filter
3. **IonAct**: Dynamic ionization energy levels (1-30) từ JSON
4. **NuclideAct**: Interactive nuclide chart với zoom & pan + decay color coding

### Architecture Patterns:

- ✅ ViewBinding throughout
- ✅ RecyclerView với custom adapters
- ✅ TextWatcher cho search filtering
- ✅ Handler với postDelayed cho animations
- ✅ SharedPreferences cho user selections
- ✅ JSON parsing từ assets
- ✅ Custom gesture detectors
- ✅ ViewStub lazy loading

---

**✅ All 6 Table Activities optimized successfully!**

**Grand Total: 12 Activities (6 Settings + 6 Table) are now using modern APIs! 🎉🎉**
