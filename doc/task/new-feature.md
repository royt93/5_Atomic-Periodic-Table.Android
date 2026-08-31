# NEW TASK — Việc mới có thể lên backlog ngay

Cụ thể, actionable, không phải ý tưởng mơ hồ. Nguồn: tự audit + agent ideation sản phẩm + codex + agy + claude. Status theo thang: 💭 Ideas (mặc định cho toàn bộ file này — chưa cái nào được Pick).

---

## Data & kiến trúc nền tảng

- **TASK-001 — Test symbol-uniqueness cho toàn bộ dataset.** Unit test quét `ElementModel`, `IonModel`, `SeriesModel` đảm bảo 118 symbol duy nhất, atomic number liên tục 1-118, symbol khớp giữa các model — bug FIX-001/002/003 (Pt/Pd/Cu) đáng lẽ bị bắt bởi test này. *(agy, tự audit)* — **Estimate: M**
- **TASK-002 — Xây `ElementDataRepository` cache JSON 1 lần.** Thay vì `TableExt`/`InfoExt`/`CompareAct`/`IonAdapter`/`NuclideAct` mỗi nơi tự mở/parse asset riêng, gom về 1 nguồn load 1 lần lúc cold start (trên background thread), expose theo symbol/number. Giải quyết đồng thời ENH-001, ENH-003, FIX-017. *(codex, claude, tự audit)* — **Estimate: L**
- **TASK-003 — Room Database thay 118 file JSON rời rạc.** Có index Symbol/AtomicNumber/Group/Period, tăng tốc tìm kiếm/lọc đa thuộc tính. Có thể làm sau TASK-002 (repository trước, DB sau nếu cần). *(codex, claude)* — **Estimate: XL**
- **TASK-004 — Viết lại Chart of Nuclides bằng Custom View Canvas có viewport culling** thay vì inflate >3.000 View, giải quyết ENH-002 tận gốc. *(codex, claude)* — **Estimate: L**
- **TASK-005 — Hợp nhất hệ thống Preferences.** Migrate 20+ file `*Pref.kt` sang Jetpack DataStore hoặc 1 `AppPreferences` chung; đồng bộ 2 hệ thống pref nhiệt độ đang lệch nhau (FIX-024). *(claude, codex)* — **Estimate: L**

## Fix ưu tiên cao nên đưa vào sprint đầu tiên

*(Danh sách đầy đủ + chi tiết xem `fix.md`, đây chỉ là rút gọn để lên sprint plan)*
- Sửa 3 bug dữ liệu khoa học sai (FIX-001/002/003) — bắt buộc trước khi release tiếp theo.
- Cancel `postDelayed` trong `QuizAct.onDestroy()` (FIX-006).
- Guard redeem VIP khi đang active + loại trừ VIP prefs khỏi backup (FIX-005, FIX-007).
- Fix `initName(elementList)` copy-paste bug (FIX-004).

## Test coverage bổ sung (theo gap đã liệt kê ở `enhance.md`)

- **TASK-006 — Unit test toàn bộ `adt/`** (7 adapter hiện 0 test).
- **TASK-007 — Instrumented test cho `act/table/*` và `act/setting/*`** (0 test hiện tại).
- **TASK-008 — Fix 11 Mockito failures đã biết trong `VipPrefsTest`** (`doc/feat.md:15`) trước khi tin coverage VIP.
- **TASK-009 — Test edge-case VIP:** redeem khi đang active, fallback `grantedAtMs=0`, reward earned/failed, revoke→re-redeem, xoay màn hình/process-death giữa countdown.
- **TASK-010 — Regression suite cho `ChemicalFormulaParser`:** mismatched bracket, multiplier 0, overflow Int, hydrate notation (`CuSO4·5H2O`), charge (`SO4²⁻`).
- **TASK-011 — Data validation test quét đủ 118 asset JSON:** hợp lệ cú pháp, atomic number duy nhất/liên tục, symbol-filename khớp, isotope count không vượt key thực tế.

## Tính năng lấp gap sản phẩm

- **TASK-012 — Widget "Nguyên tố mỗi ngày".** `ShortCommandWidget` hiện chỉ là shortcut mở `MainAct`, không hiển thị dữ liệu gì. Đổi thành hiển thị tên/ký hiệu/số nguyên tử random, cập nhật mỗi 24h bằng `WorkManager`. *(agent ideation)* — **Estimate: M**
- **TASK-013 — Chức năng Share nguyên tố.** Grep toàn repo không thấy `Intent.ACTION_SEND` ở đâu — app hiện không có share. Thêm nút share (card ảnh/text) ở `ElementInfoAct` và `FavoritePageAct`. *(agent ideation, codex)* — **Estimate: M**
- **TASK-014 — Unit Converter thật.** `UnitAct.kt` hiện chỉ chỉnh đơn vị nhiệt độ hiển thị (°C/°F/K), chưa phải converter đa năng dù CLAUDE.md liệt kê "unit converter" là tính năng có sẵn. Bổ sung áp suất, khối lượng mol, nồng độ mol/lít vào `CalculatorAct`. *(agent ideation)* — **Estimate: L**
- **TASK-015 — So sánh nguyên tố từ Favorite + mở rộng 3 nguyên tố.** `CompareAct` hiện chỉ so 2 nguyên tố qua ô nhập text độc lập. Thêm nút "So sánh từ Favorite", mở rộng lên 3 nguyên tố cùng lúc. *(agent ideation, claude)* — **Estimate: M**
- **TASK-016 — Lưu lịch sử/high-score cho Quiz.** `QuizAct` hiện không lưu điểm qua các lần chơi. Thêm `QuizPref` theo đúng pattern nhẹ đã dùng cho `VipPrefs`/`NotesPref`. *(agent ideation)* — **Estimate: S**
- **TASK-017 — Chế độ "Ôn nguyên tố đã ghi chú".** Liên kết `NotesPref` (hiện chỉ lưu text tĩnh, chưa dùng ở đâu khác) với `QuizAct` — lấy element có note không rỗng làm bộ câu hỏi ưu tiên. *(agent ideation)* — **Estimate: M**
- **TASK-018 — Hoàn thiện hoặc gỡ `OrderAct`.** Đang ship dở dang (data giả, callback rỗng — xem `enhance.md`); quyết định 1 trong 2: implement thật (cho phép custom thứ tự property trên card nguyên tố) hoặc gỡ khỏi Settings. *(claude, tự audit)* — **Estimate: M nếu hoàn thiện, S nếu gỡ**
- **TASK-019 — Export/Share dữ liệu tính toán.** Xuất bảng tóm tắt nguyên tố / kết quả cân bằng phương trình / khối lượng mol ra PDF, ảnh, hoặc copy clipboard. *(codex)* — **Estimate: M**
- **TASK-020 — Backup/export/import cho Notes và Favorites.** JSON qua Storage Access Framework, có schema version, validate trước khi merge — hiện dữ liệu chỉ nằm cục bộ trong SharedPreferences, mất khi đổi máy. *(codex)* — **Estimate: M**

## Nợ kỹ thuật đã note trong doc/ cũ, giờ đưa vào backlog chính thức

- **TASK-021 — Thêm LeakCanary (debug-only)**, theo khuyến nghị treo sẵn trong `doc/memory_leak.md`.
- **TASK-022 — Migrate `CalculatorAct.onBackPressed()` sang `OnBackPressedDispatcher`**, cũng theo `doc/memory_leak.md`.
- **TASK-023 — Update `doc/BUILD_OPTIMIZATION.md`, `doc/GRADLE_MIGRATION_STATUS.md`, `doc/memory_leak.md` cho khớp code hiện tại** (đang ref class không còn tồn tại như `AdMobManager.kt`, mô tả ABI split đang tắt như đang bật).
- **TASK-024 — Thêm CI (GitHub Actions) chạy unit test.** Hiện không có `.github/workflows` nào, mọi test chạy local. Cần tách phần yêu cầu `ads.properties` ra khỏi path bắt buộc cho JVM unit test/debug build để CI chạy được mà không cần secrets thật (dùng debug placeholder hợp lệ). *(codex)* — **Estimate: L**
- **TASK-025 — Benchmark/Macrobenchmark cho cold start, Main table, Nuclide.** Đặt ngưỡng số liệu để việc tối ưu cache/R8 sau này có thể đo được thay vì ước lượng. *(codex, claude/idea "Baseline Profile")* — **Estimate: M**
