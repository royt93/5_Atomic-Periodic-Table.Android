# Multi-Language Implementation

## ✅ Implementation Status: COMPLETE (v1.0)

Ứng dụng đã được tích hợp chức năng đa ngôn ngữ toàn diện (UI + Element Descriptions) với 3 ngôn ngữ:

- **English** (Mặc định)
- **Tiếng Việt** (Vietnamese)
- **中文** (Chinese Simplified)
- **中文 (繁體)** (Chinese Traditional - Taiwan)
- **Français** (French)
- **Deutsch** (German)
- **日本語** (Japanese)
- **한국어** (Korean)
- **Русский** (Russian)
- **Español** (Spanish)
- **ไทย** (Thai)
- **العربية** (Arabic)
- **Português** (Portuguese)
- **Português (Brasil)** (Portuguese - Brazil)
- **हिन्दी** (Hindi)
- **Italiano** (Italian)
- **Bahasa Indonesia** (Indonesian)

---

## Architecture Overview

### 1. Localization Context (`BaseAct`, `LocaleHelper`)

- **Cơ chế**: Tất cả Activity (bao gồm `InfoExt`) kế thừa từ `BaseAct`. `BaseAct` sử dụng `LocaleHelper` để đè (override) cấu hình `Locale` trong `attachBaseContext`.
- **Lưu trữ**: Ngôn ngữ được chọn lưu trong `SharedPreferences` (key: `LanguagePref`).
- **Thay đổi**: Khi đổi ngôn ngữ, app restart lại bằng `ProcessPhoenix` để apply toàn bộ resource mới.

### 2. Element Description Override (JSON Fallback)

Mô tả nguyên tố (Description) vốn nằm trong file JSON (assets). Để đa ngôn ngữ hóa mà không sửa JSON, chúng ta dùng cơ chế **Resource Override**:

1. **Ưu tiên**: Code tìm key `desc_[element_name]` (ví dụ: `desc_hydrogen`) trong file `strings_desc.xml` của ngôn ngữ hiện tại.
2. **Fallback**: Nếu không tìm thấy resource string (hoặc key rỗng), code sẽ lấy giá trị `description` gốc từ file JSON (tiếng Anh).

---

## Files Created/Modified

### New Files

| File | Type | Description |
|------|------|-------------|
| `pref/LanguagePref.kt` | Logic | SharedPreferences quản lý ngôn ngữ |
| `util/LocaleHelper.kt` | Logic | Utility apply locale |
| `layout/view_language_panel.xml` | UI | Panel chọn ngôn ngữ trong Settings |
| `values/strings_desc.xml` | Res | **Chứa 118 descriptions tiếng Anh** (trích xuất từ JSON) |
| `values-vi/strings_desc.xml` | Res | **Chứa 118 descriptions tiếng Việt** |
| `values-zh/strings_desc.xml` | Res | **Chứa 118 descriptions tiếng Trung** |
| `values-vi/strings.xml` | Res | UI Strings tiếng Việt |
| `values-zh/strings.xml` | Res | UI Strings tiếng Trung |

### Modified Files

| File | Changes |
|------|---------|
| `BaseAct.kt` | Thêm `attachBaseContext` để inject LocaleHelper |
| `InfoExt.kt` | **Quan trọng**: Đổi kế thừa `AppCompatActivity` -> `BaseAct` để nhận context đa ngôn ngữ. Thêm logic fallback Resource/JSON. |
| `SettingsAct.kt` | Thêm logic hiển thị Language Panel |

---

## FAQ - Hướng dẫn mở rộng (Cho Dev kế tiếp)

### Q1: Làm thế nào để thêm ngôn ngữ mới (ví dụ: tiếng Thái - `th`)?

**Step 1: Tạo thư mục resource**
Tạo thư mục: `app/src/main/res/values-th/`

**Step 2: Copy và dịch UI Strings**
Copy `values/strings.xml` -> `values-th/strings.xml`. Dịch toàn bộ nội dung.

**Step 3: Copy và dịch Element Descriptions (Quan trọng)**
Copy `values/strings_desc.xml` -> `values-th/strings_desc.xml`.

- File này chứa 118 key (từ `desc_actinium` đến `desc_zirconium`).
- Dịch nội dung sang tiếng Thái.
- **Lưu ý**: Nếu không tạo file này, app sẽ hiển thị mô tả tiếng Anh (fallback).

**Step 4: Đăng ký trong Code**

1. **`LanguagePref.kt`**: Thêm hằng số `const val LANG_THAI = "th"`
2. **`view_language_panel.xml`**: Thêm Button cho tiếng Thái.
3. **`SettingsAct.kt`**: Thêm sự kiện `setOnClickListener` cho button mới -> gọi `showLanguageConfirmDialog(LanguagePref.LANG_THAI)`.

---

### Q2: Tại sao sửa `InfoExt` lại quan trọng?

Trước đây `InfoExt` kế thừa trực tiếp `AppCompatActivity` nên nó bỏ qua lớp `LocaleHelper` trong `BaseAct`.
-> **Hậu quả**: Dù chọn tiếng Việt, màn hình chi tiết nguyên tố vẫn hiện tiếng Anh (System default).
-> **Fix**: `InfoExt : BaseAct()`. Luôn nhớ kế thừa `BaseAct` cho các Activity mới.

### Q3: Batch Translation Scripts nằm ở đâu?

Nếu cần dịch số lượng lớn (như 118 nguyên tố), hãy tham khảo các script Python đã dùng (đã xóa sau khi chạy, nhưng logic là: read XML -> replace content -> write XML). Đừng dịch tay từng dòng một nếu có thể dùng tool.
