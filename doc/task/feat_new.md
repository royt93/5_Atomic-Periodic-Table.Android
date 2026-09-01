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

---

# Vòng 2 (2026-09-02) — 4 feature mới chọn qua AskUserQuestion

User chọn "cả 4 idea đều hay" sau khi được đề xuất Unit Converter mở rộng (Recommended)/Share ảnh/Study Streak/Practice Exam. Rã task để loop lần lượt, theo đúng effort tăng dần và quy tắc chung ở cuối file.

## 5. Unit Converter (công cụ đổi đơn vị hoá học) — 📋 Picked

- Khảo sát: `pref/TemperatureUnits.kt:7-23` chỉ là pref lưu đơn vị **hiển thị** (1 key, 3 giá trị celsius/kelvin/fahrenheit) cho các màn hình đã có sẵn số liệu theo Kelvin trong JSON — không phải máy tính đổi qua lại. Field `element_density` trong JSON asset là string nhúng đơn vị (`"0.0000899 (g/cm^3)"`), không phải số thuần, và không có field áp suất/thể tích mol nào trong data — nghĩa là **không thể** làm kiểu "toggle hiển thị" như nhiệt độ cho áp suất/khối lượng.
- Quyết định phạm vi: làm 1 **công cụ đổi đơn vị độc lập** (nhập giá trị + chọn đơn vị nguồn/đích + tính), không phải gắn với dữ liệu nguyên tố. Công thức đổi (atm↔kPa↔mmHg↔psi cho áp suất; g↔kg↔mg cho khối lượng; L↔mL cho thể tích) là hằng số toán học cố định, không cần đọc JSON.
- UI: `act/UnitConverterAct.kt` mới — 1 màn hình dùng chung cho cả 3 nhóm đơn vị (chip chọn nhóm Pressure/Mass/Volume, rồi 2 dropdown/chip from-unit và to-unit, EditText nhập giá trị, TextView kết quả). Tham khảo layout pattern `a_calculator.xml`/`CalculatorAct.kt` (input → validate → hiển thị kết quả qua `TextInputLayout`).
- Logic pure: `feature/converter/UnitConverter.kt` — object với `convertPressure(value, from, to): Double`, `convertMass(...)`, `convertVolume(...)`, mỗi hàm quy đổi qua 1 đơn vị gốc trung gian (ví dụ Pascal cho áp suất) rồi ra đơn vị đích — JVM-testable thuần, không cần Context.
- Không cần SharedPreferences mới (không có gì persist — mỗi lần mở lại reset, giống Calculator/EquationBalancer đã có).
- Entry point: nav menu (`menuUnitConverterBtn` theo đúng pattern các mục trước).
- Test: JVM unit test cho `UnitConverter` (mọi cặp đơn vị, giá trị biên 0, giá trị âm cho áp suất nên reject hay cho qua — cần quyết định khi code, không đoán).
- Trạng thái: ⏸️ (chưa code)

## 6. Chia sẻ thẻ nguyên tố dưới dạng ảnh — 📋 Picked

- Khảo sát: codebase đã có share text (`ext/Activity.kt:89-103`, `shareApp()`) nhưng **chưa có share ảnh** — không có `FileProvider`, không có `Bitmap`/`drawToBitmap` ở đâu (grep toàn repo ra rỗng). Đây là feature mới hoàn toàn về mặt hạ tầng.
- Ladder: dùng thẳng `View.drawToBitmap()` (extension có sẵn trong `androidx-core-ktx`, đã là dependency của project) trên 1 layout card riêng dựng cho mục đích share — KHÔNG tự vẽ Canvas thủ công như `ConfettiView`/`TrendsChartView` (không cần thiết, chỉ là export 1 layout tĩnh).
- Cần khai báo `FileProvider` mới trong `AndroidManifest.xml` (`res/xml/file_paths.xml`) để share file ảnh ra ngoài app — kiểm tra kỹ quyền `authorities` trùng `applicationId` để tránh crash `FileUriExposedException`.
- UI card share: layout riêng nhỏ gọn (symbol lớn, tên, số nguyên tử, khối lượng, category) lấy data từ `model/Element` (đã có sẵn qua `ElementModel.getList()`, không cần đọc JSON) — đặt nút "Share" trong `ElementInfoAct.kt`.
- Luồng: render layout ẩn (không add vào UI thấy được, hoặc render rồi `drawToBitmap()` ngay) → lưu vào `cacheDir` (dùng `context.cacheDir`, tự dọn được, không cần quyền storage) → `FileProvider.getUriForFile()` → `Intent.ACTION_SEND` với `type = "image/png"` + `EXTRA_STREAM` → `createChooser()` (theo đúng try/catch pattern của `shareApp()`).
- Test: instrumented test verify bitmap tạo ra không rỗng (width/height > 0) và file tồn tại sau khi gọi hàm share; JVM test không khả thi cho phần vẽ (cần Android Bitmap API) — ghi rõ lý do trong doc nếu quyết định vậy khi code.
- Trạng thái: ⏸️ (chưa code)

## 7. Study Streak / Achievement cho Flashcard + Quiz — 📋 Picked

- Khảo sát: `feature/flashcard/FlashcardPref.kt` chỉ lưu SM-2 state per-symbol, không có streak/last-open. `act/QuizAct.kt:28-30` score chỉ là biến in-memory, đóng app mất hết — không có nền tảng nào tái dùng, phải xây từ đầu.
- Pref mới: `feature/streak/StudyStreakPref.kt` — flat key: `last_study_epoch_day` (Long), `current_streak` (Int), `longest_streak` (Int). Logic cập nhật (pure, JVM-testable): `feature/streak/StreakCalculator.kt` — hàm `updateStreak(lastEpochDay, currentStreak, todayEpochDay): StreakResult` (nếu `todayEpochDay == lastEpochDay` → không đổi; nếu `== lastEpochDay + 1` → streak+1; nếu cách xa hơn → reset về 1) — tương tự pure-object pattern `FlashcardScheduler`/`VipCalculator`.
- Điểm gọi: `FlashcardAct` sau khi rate 1 thẻ bất kỳ, và `QuizAct` sau khi hoàn thành 1 bài quiz (`showResults()`) — gọi `StudyStreakPref` cập nhật, dùng chung 1 streak cho cả 2 feature (không tách streak riêng từng feature, đơn giản hơn và hợp lý vì cùng mục đích "học mỗi ngày").
- UI hiện streak: thêm 1 dòng nhỏ "🔥 N ngày liên tiếp" — vị trí đề xuất: nav menu header hoặc `MainAct` (cần xem layout thật khi code để chọn chỗ hợp lý, không đoán trước). Badge/achievement (ví dụ "học 7 ngày liên tiếp", "thắt nhớ 20 nguyên tố") để mức đơn giản: chỉ hiện text/icon mốc đạt được, không làm màn hình riêng liệt kê tất cả badge (over-engineering cho v1).
- Test: JVM unit test cho `StreakCalculator` (case liên tiếp, case bỏ 1 ngày reset, case học 2 lần cùng ngày không tăng đôi, case ngày đầu tiên).
- Trạng thái: ⏸️ (chưa code)

## 8. Practice Exam Mode — 📋 Picked

- Khảo sát: `act/QuizAct.kt:179-298` (`setupQuestionData`) sinh 6 loại câu hỏi trắc nghiệm, hard-code `totalQuestions = 10` (dòng 29) và `maxTimeSeconds = 15` (dòng 42) — tái dùng được bằng cách tham số hoá 2 giá trị này thay vì viết lại từ đầu. `CalculatorAct`/`EquationBalancerAct` là input tự do (không phải trắc nghiệm) — chỉ tái dùng phần logic tính toán (`ChemicalFormulaParser`, `ElementWeightCache`), không tái dùng UI.
- Phạm vi v1: KHÔNG viết lại QuizAct — thêm 1 chế độ mới `act/PracticeExamAct.kt` tái dùng generator câu hỏi hiện có của QuizAct (refactor tối thiểu: đưa `generateQuestion()`/6 loại câu hỏi vào 1 nơi dùng chung được — cân nhắc khi code có nên extract ra `object QuizQuestionGenerator` hay giữ nguyên trong QuizAct và gọi chéo, tuỳ độ khó refactor thực tế) + thêm 1-2 loại câu hỏi mới dạng "tính khối lượng mol công thức X, chọn đáp án đúng trong 4 lựa chọn" (dùng `ChemicalFormulaParser` + `ElementWeightCache`, tạo 3 đáp án sai bằng cách lệch %).
- Khác biệt với Quiz thường: số câu nhiều hơn (ví dụ 20-30, tham số hoá), không giới hạn thời gian mỗi câu (hoặc giới hạn tổng thời gian toàn bài thay vì mỗi câu — quyết định khi code), có lưu lịch sử điểm.
- Pref mới: `feature/exam/ExamHistoryPref.kt` — lưu list điểm số các lần thi trước. Vì mỗi lần thi có nhiều field (điểm, ngày, thời gian làm bài), cân nhắc dùng Gson (đã có sẵn dependency, `com.google.code.gson:gson:2.13.2`) để serialize 1 `List<ExamResult>` thành JSON string lưu trong 1 key duy nhất — đây là trường hợp hợp lý để dùng Gson thay vì flat-key (khác Flashcard/Streak vì dữ liệu ở đây là list có cấu trúc, không phải giá trị đơn theo symbol).
- Entry point: nav menu (`menuPracticeExamBtn`).
- Test: JVM unit test cho loại câu hỏi tính toán mới (đáp án đúng luôn có trong 4 lựa chọn, 3 đáp án sai không trùng đáp án đúng), JVM test cho `ExamHistoryPref` serialize/deserialize roundtrip, instrumented test luồng làm bài end-to-end + xem lịch sử.
- Trạng thái: ⏸️ (chưa code, effort lớn nhất trong 4 mục)

## Quy tắc thực hiện chung

- Mỗi mục xong: build xanh, chạy test liên quan, cập nhật status trong file này + `doc/feat.md` mục Picked/Implemented, commit + push origin/dev.
- Không thêm dependency mới (Gson đã có sẵn nếu cần, nhưng ưu tiên flat SharedPreferences key theo pattern `NotesPref` trước — chỉ dùng Gson khi dữ liệu thật sự có cấu trúc list/object như mục 8, không dùng cho dữ liệu giá trị đơn).
- Không thêm WorkManager — mọi lịch định kỳ dùng cơ chế Android có sẵn (`updatePeriodMillis`, hoặc check-on-app-open).
- **Test rigor bắt buộc mỗi feature** (yêu cầu user 2026-09-01): unit test JVM cho mọi pure logic (mọi nhánh/case), instrumented test cho UI/edge-case, integration test luồng end-to-end thật, smoke test THẬT trên device đang connect (ưu tiên Samsung S24 Ultra — serial `R5CX613VZBR` — vì TECNO KJ7 hay bị app `com.galaxyjoy.cpuinfo` từ 1 session Claude Code khác dùng chung máy cướp focus gây false-fail, luôn kiểm tra `dumpsys window | grep mCurrentFocus` trước khi kết luận fail nào là bug thật). UI mới PHẢI chụp screenshot thật (base64-log-qua-logcat, xem cách làm ở mục 3/4) để tự verify trước khi báo xong, không chỉ đọc code. Tự audit + chấm điểm /10 sau mỗi feature — chỉ push nếu >9/10.
