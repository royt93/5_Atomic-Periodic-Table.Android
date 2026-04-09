# Quick Win – Tính Năng Đề Xuất

---

## ✅ [DONE] Element Comparison Mode (So Sánh Nguyên Tố)

> **Trạng thái:** Đã triển khai thành công – 2026-04-06
> **Build:** ✅ Debug + Release SUCCESSFUL (4 rounds)

---

### Tính Năng Đã Triển Khai

Tính năng **Compare Elements** cho phép user chọn **2 nguyên tố bất kỳ** và xem màn hình so sánh song song, highlight giá trị lớn hơn/nhỏ hơn bằng màu xanh/đỏ.

**Entry point:** Nav menu → "Compare Elements"

---

### Files Đã Tạo / Chỉnh Sửa

| File | Loại | Mô tả |
|---|---|---|
| `act/CompareAct.kt` | **NEW** | Activity chính: search picker, dropdown, bảng so sánh |
| `res/layout/a_compare.xml` | **NEW** | Layout CompareAct: title bar, card picker, table |
| `res/layout/view_compare_row.xml` | **NEW** | Row layout: label / val1 / indicator / val2 |
| `res/drawable/shape_search_bar.xml` | **NEW** | Background rounded cho search input |
| `res/values/colors.xml` | **MODIFY** | Thêm `compare_higher` (#2E7D32) và `compare_lower` (#C62828) |
| `res/values-night/colors.xml` | **MODIFY** | Dark mode colors: #66BB6A / #EF5350 |
| `res/values/strings.xml` | **MODIFY** | Thêm 8 strings cho Compare feature |
| `AndroidManifest.xml` | **MODIFY** | Đăng ký `CompareAct` |
| `res/layout/view_nav_menu_view.xml` | **MODIFY** | Thêm nút "Compare Elements" vào nav menu |
| `act/MainAct.kt` | **MODIFY** | Wire up `compareBtn` → `CompareAct` |

---

### Memory Leak Audit (Tự Check)

| Potential Leak | Status | Giải pháp |
|---|---|---|
| `TextWatcher` (searchElement1) | ✅ Clean | Lưu `textWatcher1`, remove trong `onDestroy()` |
| `TextWatcher` (searchElement2) | ✅ Clean | Lưu `textWatcher2`, remove trong `onDestroy()` |
| `allElements` ArrayList | ✅ Clean | `.clear()` trong `onDestroy()` |
| Dynamic TextViews trong dropdown | ✅ Clean | `removeAllViews()` được gọi trước mỗi lần filter |
| `loadElementJson` InputStream | ✅ Clean | Dùng `bufferedReader().use { }` (auto-close) |
| `OnBackPressedCallback` | ✅ Clean | Tự cleanup khi Activity destroyed (AndroidX lifecycle) |

---

### Tính Năng Chi Tiết

**So sánh 20 properties:**
- Atomic Number, Atomic Mass
- Group, Block, Phase (STP)
- Electronegativity, Boiling/Melting Point (K)
- Density, Ionization Energy, Atomic Radius
- Covalent Radius, Electron Config, Shells
- Year Discovered, Radioactive
- Magnetic/Electrical Type
- Fusion Heat, Specific Heat, Vaporization Heat

**UX:**
- Search dropdown live bằng `TextWatcher` (limit 6 kết quả)
- Màu `▲` xanh = giá trị cao hơn, `▼` đỏ = thấp hơn
- Hỗ trợ Dark Mode (colors.xml + values-night)
- Window insets / edge-to-edge đúng chuẩn BaseAct

---

### Build Results

| Round | Command | Kết quả |
|---|---|---|
| 1 | `assembleDebug` | ❌ FAILED – typo `textcolor` trong layout |
| 2 | `assembleDebug` (fixed) | ✅ SUCCESSFUL (26s) |
| 3 | `clean assembleDebug` | ✅ SUCCESSFUL (13s) |
| 4 | `assembleRelease` | ✅ SUCCESSFUL (3m 2s) |

---

*Triển khai bởi: Antigravity | 2026-04-06*
