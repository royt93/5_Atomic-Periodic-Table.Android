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

## 3. Flashcard / Spaced-repetition study — ✅ Đã fix (2026-09-01)

- Thuật toán SM-2 rút gọn, pure object tương tự `VipCalculator` (JVM-testable, Android-independent): input rating (Again/Hard/Good/Easy) + state hiện tại (easeFactor, intervalDays, repetitions) → state mới.
- Persist theo từng nguyên tố bằng pattern `NotesPref` (`pref/NotesPref.kt`) — key `"flashcard_next_$symbol"` (Long, epoch ms), `"flashcard_interval_$symbol"` (Int), `"flashcard_ease_$symbol"` (Float) — không cần Map/JSON phức tạp, đúng pattern đã có trong codebase.
- UI: `act/FlashcardAct.kt` mới — mặt trước ký hiệu, tap lật mặt sau (tên + số nguyên tử), 4 nút rating. Tham khảo animation pattern từ `QuizAct.kt`.
- Entry point: nav menu (`view_nav_menu_view.xml` + `MainAct.kt` wiring), theo đúng pattern "Compare Elements" đã ghi trong `doc/quick_win.md`.
- Test: JVM unit test cho thuật toán SM-2 rút gọn (input/output rõ ràng, giống `VipCalculatorTest`).
- **Đã implement:** `feature/flashcard/FlashcardScheduler.kt` (pure, SM-2 rút gọn — AGAIN reset về 1 ngày + giảm ease, HARD/GOOD/EASY tăng interval theo ease factor với mốc khởi đầu khác nhau), `feature/flashcard/FlashcardPref.kt` (flat key theo symbol, đúng pattern `NotesPref`), `act/FlashcardAct.kt` (queue = nguyên tố đến hạn `isDue`, rơi về full deck nếu chưa nguyên tố nào đến hạn — practice mode), entry point nav menu (`menuFlashcardBtn` cạnh `menuQuizBtn`).
- UI Material You: `MaterialCardView`/`CardView` tông màu `colorPrimaryContainer`, `LinearProgressIndicator` animated (`setProgressCompat`), 4 nút rating dùng `MaterialButton` với `style=` **explicit** (`Widget.App.MaterialButton`/`.Outlined`) — bài học thật: bỏ sót `style=` cho 2 nút khiến chúng rơi vào default MDC (sai màu + ALL CAPS), phát hiện qua screenshot thật trên device (không chỉ đọc code). Animation: flip 3D thật (`ObjectAnimator` xoay `rotationY` 2 chặn, swap mặt trước/sau ở giữa) + slide-fade khi chuyển thẻ.
- Bug thật bắt được bởi test trước khi push: `cardFlashcardComplete` ban đầu constrain theo cạnh `cardFlashcard`, nhưng `cardFlashcard` bị set GONE khi hết thẻ → bounds sụp về 0, complete-card thành invisible. Sửa bằng cách cho `cardFlashcardComplete` constraint độc lập giống hệt `cardFlashcard` (neo vào `flashcardProgressBar`/`layoutFlashcardRatings`, không neo vào nhau).
- Test: `FlashcardSchedulerTest` (17 case JVM, cover đủ 4 rating × biên ease-factor/interval), `FlashcardPrefTest` (3 case instrumented: default/roundtrip/isolation theo symbol), `FlashcardActTest` (6 case instrumented: launch/flip/rating-Good/rating-Again/ignore-trước-flip/edge-case-practice-mode/integration hết-bộ-thẻ-118-lần), `NavigationIntegrationTest.testNavigateFromMainToFlashcard`. Verify: 7/7 + 3/3 + 4/4 pass trên emulator sạch (Pixel 10 Pro XL AVD) sau khi loại trừ 2 lần nhiễu môi trường (TECNO KJ7 bị `com.galaxyjoy.cpuinfo` cướp focus giữa test 118-vòng — xác nhận qua `dumpsys window`; 1 lần "Process crashed" do harness gộp nhiều test class uninstall app giữa chừng — xác nhận qua logcat `pm uninstall`). Full unit suite xanh.
- Trạng thái: ✅ Đã fix

## 4. Periodic Trends Chart — ✅ Đã fix (2026-09-01)

- Custom View tự vẽ Canvas (tham khảo cấu trúc `view/ConfettiView.kt` — Paint/Canvas/Path), KHÔNG thêm thư viện chart mới (ladder: platform feature đã đủ cho line/scatter đơn giản).
- Chọn property hiển thị: electronegativity (`Element.electro` có sẵn trong model) là lựa chọn an toàn nhất vì đã có sẵn trong `Element` data class, không cần tra JSON asset cho toàn bộ 118 phần tử. Nếu muốn nhiều property hơn (atomic radius, ionization energy...) phải đọc JSON asset của cả 118 file — cân nhắc effort trước khi mở rộng, có thể để iteration sau.
- Vẽ trục X = atomic number (1-118), trục Y = giá trị property, chấm/line nối các điểm có dữ liệu (một số nguyên tố `electro == 0.0` = không có dữ liệu, phải bỏ qua điểm đó khi vẽ, không vẽ giá trị 0 giả).
- Tap vào 1 điểm hiện tooltip tên nguyên tố + giá trị (dùng toạ độ chạm so khớp điểm gần nhất, tương tự cách `NuclideAct` xử lý touch).
- Test: JVM unit test cho hàm mapping toạ độ dữ liệu → toạ độ pixel (pure function, tách khỏi View để test không cần Android).
- **Đã implement:** `feature/trends/TrendsMapper.kt` (pure: `mapX`/`mapY`/`nearestPointIndex`), `view/TrendsChartView.kt` (Canvas tự vẽ, tham khảo `ConfettiView`), `act/TrendsChartAct.kt` + `a_trends_chart.xml` (MaterialCardView tông `colorSurfaceVariant`, tooltip text dưới chart), entry point nav menu (`menuTrendsBtn`). Property hiển thị: electronegativity (`Element.electro`, có sẵn trong model, không cần đọc JSON). Bỏ qua điểm `electro == 0.0` khi vẽ (đúng yêu cầu, không vẽ giá trị 0 giả).
- Test: `TrendsMapperTest` (9 case JVM: biên min/max X/Y, đảo chiều Y, nearest-point kể cả list rỗng), `TrendsChartActTest` (4 case instrumented: hiện chart+hint, tap đúng điểm đã biết → tooltip đúng ký hiệu nguyên tố, tap xa mọi điểm → tooltip không đổi, tap 2 điểm khác nhau → tooltip cập nhật đúng lần sau), `NavigationIntegrationTest.testNavigateFromMainToTrendsChart`. Verify bằng screenshot thật (base64-log-in-logcat) trên emulator sạch — xác nhận UI Material You đúng chuẩn (card tông màu, dot/line màu `colorPrimary`, điểm được chọn nổi bật màu `colorTertiary`, tooltip hiển thị đúng "Xe — Xenon: 2.60"), không phải đọc code suông. 9/9 + 4/4 + 5/5 (cả bộ NavigationIntegrationTest) pass trên Pixel 10 Pro XL AVD. Full unit suite xanh.
- Trạng thái: ✅ Đã fix

## Quy tắc thực hiện chung

- Mỗi mục xong: build xanh, chạy test liên quan, cập nhật status trong file này + `doc/feat.md` mục Picked/Implemented, commit + push origin/dev.
- Không thêm dependency mới (Gson đã có sẵn nếu cần, nhưng ưu tiên flat SharedPreferences key theo pattern `NotesPref` trước).
- Không thêm WorkManager — mọi lịch định kỳ dùng cơ chế Android có sẵn (`updatePeriodMillis`, hoặc check-on-app-open).
