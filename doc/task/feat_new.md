# Feature mới — task breakdown (2026-09-01)

Chọn qua AskUserQuestion sau Sprint 3 + ponytail-audit. User chọn "cả 4 option đều tốt" — rã task để loop lần lượt.

## 1. Molar Mass / Stoichiometry Calculator — ✅ Đã có sẵn (không phải feature mới)

Khảo sát phát hiện feature này **đã được implement từ trước**, không nằm trong 4 lựa chọn thực sự cần làm:
- Parser: `util/ChemicalFormulaParser.kt` (`parse(formula): Map<String, Int>`, xử lý ngoặc lồng nhau + hệ số nhân), có test JVM đầy đủ `ChemicalParserAndBalancerTest.kt`.
- UI: `act/CalculatorAct.kt:67` — nhập công thức, tính tổng khối lượng qua `ElementWeightCache.getMass(symbol)`.
- Entry point: `menuMolarMassBtn` trong nav menu → `MainAct.kt:453`.

→ Không cần làm lại. 3 feature còn lại tiến hành theo thứ tự effort tăng dần:

## 2. Element of the Day (widget) — ✅ Đã fix (2026-09-01)

- Pick nguyên tố theo ngày: hàm pure `pickElementOfDay(dateEpochDay: Long, totalElements: Int): Int` (index theo `dateEpochDay % totalElements`, deterministic, unit-testable JVM không cần Android).
- Cần 1 dòng "fact" mỗi nguyên tố — tái dùng dữ liệu JSON asset đã có (`assets/<element>.json`) qua field có sẵn (kiểm tra field nào hợp lý, ví dụ description/category) thay vì tạo dữ liệu fact mới.
- Widget: mở rộng `widget/ShortCommandWidget.kt` (đã có `onUpdate` + `updatePeriodMillis=86400000` = 24h sẵn) — thêm hiển thị tên/ký hiệu/fact nguyên tố hôm nay vào `RemoteViews`, tap mở `ElementInfoAct` đúng nguyên tố đó (không phải mở `MainAct` chung chung như hiện tại).
- Không thêm WorkManager/AlarmManager mới — `updatePeriodMillis` có sẵn đã đủ (ponytail: platform feature đã có, không thêm dependency).
- Test: JVM unit test cho `pickElementOfDay` (deterministic, đổi ngày → đổi index, index luôn trong `0 until totalElements`).
- **Đã implement:** `util/ElementOfDay.kt` (`indexForDay(epochDay, total)`, dùng `System.currentTimeMillis() / 86_400_000L` thay `java.time.LocalDate` vì minSdk 24 < API 26). `ElementWeightCache` mở rộng thêm `descriptionCache`/`getFact()` (câu đầu tiên của field `description` có sẵn trong JSON asset). `ShortCommandWidget.onUpdate()` set `tvWidgetName`/`tvWidgetFact` qua `RemoteViews`, ghi `ElementSendAndLoad` rồi trỏ click PendingIntent sang `ElementInfoAct` (trước đây mở `MainAct` chung chung). Cả 2 layout variant (`layout/`, `layout-v31/`) đều khai báo 2 id mới.
- Test: `ElementOfDayTest` (4 case JVM), `ShortCommandWidgetLayoutTest` mở rộng để check cả `setTextViewText` id (không chỉ `setOnClickPendingIntent`) tồn tại ở mọi layout variant, `ShortCommandWidgetTest.onUpdate_pointsElementSendAndLoad_atTodaysElement` (instrumented) — verify bằng revert-test: comment dòng `setValue` → test fail đúng như kỳ vọng, khôi phục → pass. 3/3 instrumented pass trên TECNO KJ7.
- Trạng thái: ✅ Đã fix

## 3. Flashcard / Spaced-repetition study — 📋 Picked

- Thuật toán SM-2 rút gọn, pure object tương tự `VipCalculator` (JVM-testable, Android-independent): input rating (Again/Hard/Good/Easy) + state hiện tại (easeFactor, intervalDays, repetitions) → state mới.
- Persist theo từng nguyên tố bằng pattern `NotesPref` (`pref/NotesPref.kt`) — key `"flashcard_next_$symbol"` (Long, epoch ms), `"flashcard_interval_$symbol"` (Int), `"flashcard_ease_$symbol"` (Float) — không cần Map/JSON phức tạp, đúng pattern đã có trong codebase.
- UI: `act/FlashcardAct.kt` mới — mặt trước ký hiệu, tap lật mặt sau (tên + số nguyên tử), 4 nút rating. Tham khảo animation pattern từ `QuizAct.kt`.
- Entry point: nav menu (`view_nav_menu_view.xml` + `MainAct.kt` wiring), theo đúng pattern "Compare Elements" đã ghi trong `doc/quick_win.md`.
- Test: JVM unit test cho thuật toán SM-2 rút gọn (input/output rõ ràng, giống `VipCalculatorTest`).
- Trạng thái: ⏸️ (sau mục 2)

## 4. Periodic Trends Chart — 📋 Picked

- Custom View tự vẽ Canvas (tham khảo cấu trúc `view/ConfettiView.kt` — Paint/Canvas/Path), KHÔNG thêm thư viện chart mới (ladder: platform feature đã đủ cho line/scatter đơn giản).
- Chọn property hiển thị: electronegativity (`Element.electro` có sẵn trong model) là lựa chọn an toàn nhất vì đã có sẵn trong `Element` data class, không cần tra JSON asset cho toàn bộ 118 phần tử. Nếu muốn nhiều property hơn (atomic radius, ionization energy...) phải đọc JSON asset của cả 118 file — cân nhắc effort trước khi mở rộng, có thể để iteration sau.
- Vẽ trục X = atomic number (1-118), trục Y = giá trị property, chấm/line nối các điểm có dữ liệu (một số nguyên tố `electro == 0.0` = không có dữ liệu, phải bỏ qua điểm đó khi vẽ, không vẽ giá trị 0 giả).
- Tap vào 1 điểm hiện tooltip tên nguyên tố + giá trị (dùng toạ độ chạm so khớp điểm gần nhất, tương tự cách `NuclideAct` xử lý touch).
- Test: JVM unit test cho hàm mapping toạ độ dữ liệu → toạ độ pixel (pure function, tách khỏi View để test không cần Android).
- Trạng thái: ⏸️ (cuối cùng, effort lớn nhất)

## Quy tắc thực hiện chung

- Mỗi mục xong: build xanh, chạy test liên quan, cập nhật status trong file này + `doc/feat.md` mục Picked/Implemented, commit + push origin/dev.
- Không thêm dependency mới (Gson đã có sẵn nếu cần, nhưng ưu tiên flat SharedPreferences key theo pattern `NotesPref` trước).
- Không thêm WorkManager — mọi lịch định kỳ dùng cơ chế Android có sẵn (`updatePeriodMillis`, hoặc check-on-app-open).
