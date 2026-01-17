# Multi-Language Implementation

## ✅ Implementation Status: COMPLETE

Ứng dụng đã được tích hợp chức năng đa ngôn ngữ với 3 ngôn ngữ:

- **English** (mặc định)
- **Tiếng Việt**
- **中文** (Chinese Simplified)

---

## Files Created/Modified

### New Files

| File | Description |
|------|-------------|
| `pref/LanguagePref.kt` | SharedPreferences để lưu ngôn ngữ đã chọn |
| `util/LocaleHelper.kt` | Utility để apply locale cho app |
| `drawable/ic_language.xml` | Icon ngôn ngữ (translate icon) |
| `layout/view_language_panel.xml` | Panel chọn ngôn ngữ (3 options) |
| `layout/view_confirm_dialog.xml` | Dialog xác nhận đổi ngôn ngữ |
| `values-vi/strings.xml` | Bản dịch tiếng Việt (~285 strings) |
| `values-zh/strings.xml` | Bản dịch tiếng Trung (~285 strings) |

### Modified Files

| File | Changes |
|------|---------|
| `build.gradle` | Thêm ProcessPhoenix dependency |
| `values/strings.xml` | Thêm 10 language picker strings |
| `layout/a_settings.xml` | Thêm language item + panels |
| `SettingsAct.kt` | Thêm language settings logic |
| `BaseAct.kt` | Apply locale trong attachBaseContext() |

---

## How It Works

1. User mở **Settings** → Language là item đầu tiên trong Personalization
2. Click **Language** → Hiện Language Panel với 3 options
3. Chọn ngôn ngữ khác → Hiện Confirm Dialog
4. Click **Confirm** → App restart bằng ProcessPhoenix với ngôn ngữ mới

---

## FAQ - Hướng dẫn mở rộng

### Q1: Làm thế nào để thêm ngôn ngữ mới (ví dụ: tiếng Thái)?

**Step 1**: Tạo thư mục ngôn ngữ mới

```
app/src/main/res/values-th/
```

**Step 2**: Copy file `values/strings.xml` vào thư mục mới và dịch

```xml
<!-- values-th/strings.xml -->
<string name="settings">การตั้งค่า</string>
<string name="language_thai">ภาษาไทย</string>
<!-- ... dịch tất cả strings còn lại ... -->
```

**Step 3**: Thêm constant trong `LanguagePref.kt`

```kotlin
const val LANG_THAI = "th"
```

**Step 4**: Thêm button trong `view_language_panel.xml`

```xml
<AppCompatTextView
    android:id="@+id/thaiBtn"
    android:text="@string/language_thai" />
```

**Step 5**: Thêm click listener trong `SettingsAct.kt`

```kotlin
binding.languagePanel.thaiBtn.setOnClickListener {
    showLanguageConfirmDialog(LanguagePref.LANG_THAI)
}
```

**Step 6**: Update `updateLanguageRadioButtons()` function

```kotlin
LanguagePref.LANG_THAI -> {
    binding.languagePanel.thaiBtn.setCompoundDrawablesWithIntrinsicBounds(
        R.drawable.ic_radio_checked, 0, 0, 0
    )
}
```

**Step 7**: Thêm string key `language_thai` vào **TẤT CẢ** file strings.xml

---

### Q2: Làm thế nào để thêm string mới cho tất cả ngôn ngữ?

**Step 1**: Thêm string vào `values/strings.xml` (English - source of truth)

```xml
<string name="new_feature_title">New Feature</string>
```

**Step 2**: Thêm bản dịch vào các file ngôn ngữ khác:

| File | Content |
|------|---------|
| `values-vi/strings.xml` | `<string name="new_feature_title">Tính năng mới</string>` |
| `values-zh/strings.xml` | `<string name="new_feature_title">新功能</string>` |

**Step 3**: Sử dụng trong code/layout

```xml
android:text="@string/new_feature_title"
```

```kotlin
getString(R.string.new_feature_title)
```

> **Tip**: Sử dụng Android Studio Translations Editor để quản lý:
> Right-click strings.xml → Open Translations Editor

---

## Testing Checklist

- [ ] Build & Install: `./gradlew installDevDebug`
- [ ] Settings → Language item hiển thị đầu tiên trong Personalization
- [ ] Click Language → Panel hiện với radio buttons đúng trạng thái
- [ ] Chọn ngôn ngữ khác → Confirm dialog hiện
- [ ] Confirm → App restart với ngôn ngữ mới
- [ ] Toàn bộ UI strings đã dịch sang ngôn ngữ mới
- [ ] Tắt app và mở lại → Ngôn ngữ được giữ nguyên
- [ ] Test tất cả 3 ngôn ngữ: EN → VI → ZH → EN
