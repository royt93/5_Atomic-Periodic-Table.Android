# FIX — Bug cần sửa

## Trạng thái Sprint 1 (2026-08-31)

**7/7 P0 đã fix** (FIX-001 → FIX-007) và verify bằng `./gradlew :app:testDevDebugUnitTest` + `:app:testProductionReleaseUnitTest`: 111 test devDebug, 0 fail; toàn bộ `VipPrefsTest` (11 test) cũng 0 fail — note "11 pre-existing Mockito failures" trong `doc/feat.md:15` không còn đúng với code hiện tại, nên coi là đã tự hết/không tái hiện được, không phải đang bị che giấu. `initName(elementList)` sau khi sửa còn để lại field `elementList` không dùng nữa trong `TableExt.kt` — đã xoá luôn. Chưa chạy instrumented test (`connectedDevDebugAndroidTest`) vì cần thiết bị/emulator — theo R3, cần hỏi device target trước khi chạy, chưa làm trong phiên này.

Nguồn: tự audit (agent đọc toàn bộ code) + 3 AI Agent độc lập (`codex exec -s read-only`, `agy --dangerously-skip-permissions --print`, `claude -p --permission-mode plan`), mỗi agent đọc code không biết ý kiến của agent khác. Item nào tôi đã tự `grep`/đọc lại code để xác nhận thì đánh dấu **✅ Verified**; nếu 2+ nguồn độc lập cùng tìm ra thì **🔶 Cross-confirmed**; nếu chỉ 1 nguồn nêu và tôi chưa tự đọc lại thì **⚪ Cần verify** — pick task đó thì đọc lại code trước khi sửa.

Priority: P0 = critical (sai dữ liệu khoa học / vỡ tính năng lõi / rủi ro doanh thu) · P1 = high · P2 = medium · P3 = low.
Estimate: S (~1-2h) · M (~nửa ngày-1 ngày) · L (~2-3 ngày) · XL (~1 tuần+).

---

## P0 — Critical

### FIX-001 — Platinum và Protactinium trùng ký hiệu hoá học `"Pa"` ✅ Verified — ✅ Đã fix
- **Nguồn:** agy
- **File:** `model/ElementModel.kt:82` (`Element("platinum", "Pa", 78, ...)`, đúng ra phải là `"Pt"`), `model/IonModel.kt:85` (`Ion("platinum", "Pa", 3)`). Trùng với `ElementModel.kt:95` và `IonModel.kt:98` (`"protactinium", "Pa"`, 91) — đúng.
- **Tác động:** Bất kỳ lookup theo symbol nào (vd. `ElementWeightCache`, `IonAdapter`) sẽ nhầm lẫn 2 nguyên tố khác số hiệu nguyên tử hoàn toàn (78 vs 91) → tính sai khối lượng mol, hiển thị sai dữ liệu ion hoá. Đây là app giáo dục hoá học — sai dữ liệu gốc là lỗi nghiêm trọng nhất tìm được.
- **Acceptance criteria:** Sửa 2 dòng thành `"Pt"`. Thêm unit test quét toàn bộ `ElementModel.getList()` + `IonModel` đảm bảo 118 symbol duy nhất, không trùng (test này cũng bắt các lỗi tương tự trong tương lai — xem ENH-TEST-01).
- **Estimate:** S

### FIX-002 — Palladium bị gán sai ký hiệu `"Ph"` ✅ Verified — ✅ Đã fix
- **Nguồn:** agy
- **File:** `model/IonModel.kt:53` — `Ion("palladium", "Ph", 3)`, đúng ra `"Pd"`.
- **Tác động:** Sai dữ liệu hiển thị số oxi hoá/ion của Palladium.
- **Acceptance criteria:** Sửa thành `"Pd"`, thêm vào cùng test symbol-uniqueness ở FIX-001.
- **Estimate:** S

### FIX-003 — Đồng (Cu) sai dấu thế điện cực chuẩn ✅ Verified — ✅ Đã fix (đổi thành +0.34, cần double-check lại với SGK/IUPAC nếu muốn số chính xác hơn)
- **Nguồn:** agy
- **File:** `model/SeriesModel.kt:21` — `Series("copper", -0.159, "2+", "Cu")`. Giá trị chuẩn Cu²⁺/Cu là dương (khoảng +0.34V, hoặc +0.159V nếu là cặp Cu²⁺/Cu⁺). Dấu âm hiện tại còn phá vỡ thứ tự tăng dần của dãy điện hoá (đứng sau Pb ở -0.126 nhưng lại nhỏ hơn).
- **Tác động:** Dãy điện hoá — kiến thức hoá học cơ bản (dự đoán phản ứng oxi hoá-khử) — bị sai và sai thứ tự hiển thị trong `SolubilityAct`/dãy hoạt động hoá học.
- **Acceptance criteria:** Đổi dấu thành dương, xác nhận lại giá trị chính xác theo tài liệu hoá học chuẩn (SGK/IUPAC), viết test snapshot cho `SeriesModel.getList()` kiểm tra dãy tăng dần đúng thứ tự.
- **Estimate:** S

### FIX-004 — `TableExt.initName(elementList)` dùng nhầm list rỗng — 5/10 thuộc tính hover-menu bị vô hiệu ✅ Verified — ✅ Đã fix (đổi thành `initName(list)` x5, xoá field `elementList` không dùng nữa)
- **Nguồn:** tự audit
- **File:** `ext/TableExt.kt:82` khai báo `private var elementList = ArrayList<Element>()` — **không bao giờ được gán/thêm phần tử ở đâu khác trong file** (đã grep xác nhận). `initBoiling` (148), `initMelting` (182), `initPhase` (214), `initYear` (243), `initElectro` (272) đều gọi `initName(elementList)` (list rỗng); trong khi `initGroups` (343), `initWeight` (407), `initHeat` (438), `initSpecific` (472), `initVape` (505) gọi đúng `initName(list)` (tham số được truyền vào, có dữ liệu).
- **Tác động:** Refresh màu/tint theo theme bên trong `initName()` no-op lặng lẽ cho 5 property view (Boiling/Melting/Phase/Year/Electronegativity) trong hover menu của `MainAct` — bug đã tồn tại nhưng không ai để ý vì không crash.
- **Acceptance criteria:** Đổi 5 lời gọi trên từ `initName(elementList)` → `initName(list)`. Thêm instrumented test lái `MainAct` qua cả 10 hover-menu item, assert tint/text đổi đúng theo theme.
- **Estimate:** S

### FIX-005 — `allowBackup=true` bao gồm cả SharedPreferences VIP ✅ Verified — ✅ Đã fix hoàn toàn (2026-09-02)
- **Nguồn:** tự audit
- **File:** `AndroidManifest.xml:23-25` (`allowBackup="true"`, `dataExtractionRules`, `fullBackupContent`) + `res/xml/data_extraction_rules.xml` — `<include domain="sharedpref" path="." />` cho cả cloud-backup lẫn device-transfer, tức là **toàn bộ** SharedPreferences (đã đọc file, xác nhận không có `<exclude>` nào cho `vip_screen_prefs`).
- **Tác động:** `VipPrefs` (`granted_at_ms`, `activated_days`, `user_redeemed_once`) và state nội bộ của `AdManager` bị backup/transfer. Vì `user_redeemed_once` là cờ chặn redeem nhiều lần / logic ân hạn, hành vi backup-restore hoặc "xoá dữ liệu app rồi restore" có thể là đường vòng reset trạng thái này — làm suy yếu chính cơ chế mà `feature/vip` được dựng lên để bảo vệ.
- **Acceptance criteria:** Thêm `<exclude domain="sharedpref" path="vip_screen_prefs.xml"/>` (và tên file pref thật của `AdManager` nếu SDK expose được) vào cả `<cloud-backup>` và `<device-transfer>`. Test: backup rồi restore trên thiết bị test, xác nhận trạng thái VIP không phục hồi ngược.
- **Cập nhật 2026-09-02 — tìm ra tên file pref thật của SDK**: giải nén `AdmobApplovinWrapper-1.1.5.aar` (từ Gradle cache `~/.gradle/caches/modules-2/.../AdmobApplovinWrapper/1.1.5/`), extract `classes.jar`, `grep` chuỗi ASCII trong `com/roy/sdkadbmob/AppPreferences.class` — tìm thấy literal `"loitp_admob"` ngay cạnh lời gọi `getSharedPreferences(...)`. File này chứa `keyAddVIPMemberFirstInitSuccess`/`keyVipByKeyUntil` (VIP entitlement riêng của SDK, tách biệt với `VipPrefs` của app) cộng GAID list + ad-safety counter (`ad_safety_daily_count`, `ad_safety_suspicious_count`, `ad_safety_suspicious_pause_until`). Đã thêm `<exclude domain="sharedpref" path="loitp_admob.xml" />` vào cả `data_extraction_rules.xml` (cloud-backup + device-transfer) và `backup_rules.xml` (API≤11). Gap đã đóng hoàn toàn.
- **Estimate:** M — đã hoàn thành, không cần hỏi vendor vì tự tìm được qua decompile AAR đã cache sẵn cục bộ.

### FIX-006 — `QuizAct` không huỷ `postDelayed` khi Activity destroy ✅ Verified (4/4 nguồn đồng thuận) — ✅ Đã fix
- **Nguồn:** codex, agy, claude, tự audit — cả 4 nguồn độc lập đều tìm ra.
- **File:** `QuizAct.kt:531-535` và `:718-721` (`binding.root.postDelayed({ currentQuestionIndex++; generateQuestion() }, 1500)`); `onDestroy()` ở `:730-734` chỉ cancel `countDownTimer`, `gradientAnimator`, `restartPulseAnimator` — không có biến nào giữ reference tới 2 Runnable trên để remove.
- **Tác động:** Người dùng chọn đáp án hoặc hết giờ rồi thoát `QuizAct` trong vòng 1.5s → callback vẫn chạy trên main Looper, đụng vào `binding`/animator của Activity đã destroy.
- **Acceptance criteria:** Lưu Runnable vào field, gọi `binding.root.removeCallbacks(runnable)` trong `onDestroy()` (giữ đúng pattern "tie to View lifecycle" đã áp dụng ở `ElementInfoAct`). Thêm instrumented test "đóng Quiz trong lúc auto-advance" xác nhận không crash/không side-effect.
- **Estimate:** S

### FIX-007 — `VipManagementAct.redeemInputKey()` không guard khi VIP đang active ✅ Verified — ✅ Đã fix (thêm dialog xác nhận khi mã mới cấp ít ngày hơn số ngày còn lại; đã thêm string resource `vip_redeem_replace_confirm_title/message` cho đủ 17 locale để không vỡ `XmlLocalizationIntegrityTest`)
- **Nguồn:** claude
- **File:** `feature/vip/VipManagementAct.kt:193-201` — `redeemInputKey()` gọi thẳng `activateVip(AdKeys.VIP_SECRET, days)` không kiểm tra `AdManager.isVipByKeyActive()` trước (biến `active` chỉ được đọc trong `bindUi()`, không dùng lại ở đây). `activateVip()` ghi đè `vipPrefs.saveGrantedAtMs()`/`saveActivatedDays()` vô điều kiện.
- **Tác động đã xác nhận trong code app:** metadata hiển thị local (`granted_at_ms`, `activated_days`) bị ghi đè không điều kiện nếu user lỡ redeem key 3D trong khi đang có 30D active. **Chưa xác nhận được** hành vi thật của entitlement (việc này nằm trong SDK ngoài `AdManager.activateVipByKey`, không có source trong repo) — cần test thủ công trên thiết bị trước khi coi đây là mất ngày VIP thật hay chỉ sai hiển thị.
- **Acceptance criteria:** Thêm guard: nếu đang active và `days` mới < ngày còn lại, hỏi xác nhận người dùng trước khi redeem đè. Test case: redeem 3D key trong khi đang có 30D active, xác nhận UI/metadata phản ánh đúng lựa chọn của user.
- **Estimate:** M

---

## P1 — High

### Trạng thái Sprint 2 (2026-09-01) — ✅ 12/12 đã fix

**Review độc lập (`/code-review high`) sau khi fix xong tìm thêm 4 vấn đề, đã fix cả 4:**
1. `BaseAct.refreshVipGatedBanner()` không set lại `container`/`tvLabelAd` về `VISIBLE` khi banner load lại cho user không-VIP → sau flow "kích hoạt VIP rồi revoke", banner ẩn vĩnh viễn cho tới khi Activity bị huỷ tạo lại. Fix: set `VISIBLE` trước khi `loadBanner()`.
2. `MainAct.setupViews()` đăng ký callback `initializeAdsIfNeeded` (load interstitial) giống hệt pattern FIX-012 ở `SplashAct` nhưng không deregister ở `onDestroy()`. Fix: áp dụng cùng pattern `adInitCallback` + `removePendingAdInitCallback()`.
3–4. FIX-010 đổi `catch (_: IOException)` thành `catch (_: Exception)` trên try-block ~150 dòng UI code (không chỉ phần parse JSON) ở `InfoExt.kt` và `IsotopesActExperimental.kt` → nuốt luôn cả exception không liên quan JSON (vd `NumberFormatException`), che mất bug thật. Fix: chỉ nuốt `IOException`/`JSONException`, còn lại `throw e`.

| ID | Vấn đề | File | Nguồn | Độ tin cậy | Trạng thái |
|---|---|---|---|---|---|
| FIX-008 | `mAdapter` field không bao giờ gắn vào `RecyclerView` thật; mọi lần filter tạo adapter mới thay thế, mất scroll position, việc `notifyDataSetChanged()`/`filterList()` gọi trên field cũ là vô nghĩa | `act/MainAct.kt`, `act/table/DictionaryAct.kt`, `act/IsotopesActExperimental.kt`, `act/table/EquationsAct.kt`, `act/table/ElectrodeAct.kt`, `act/table/IonAct.kt` | claude + tự audit | 🔶 Cross-confirmed | ✅ Đã fix — `mAdapter` giờ là adapter thật gắn vào RecyclerView 1 lần, filter chỉ gọi `mAdapter.filterList(...)`, không tạo instance mới nữa (giữ scroll position) |
| FIX-009 | `nextPrev()` nuốt lỗi im lặng khi ở biên (Hydrogen/Oganesson) — không crash (đã verify), chỉ im lặng không làm gì | `act/ElementInfoAct.kt:220-266` | codex (đúng), claude (sai — báo nhầm là crash) | ✅ Verified | ✅ Đã fix — `InfoExt.readJson()` disable nút Previous ở nguyên tố 1, disable nút Next ở 118, không cho chạm biên nữa |
| FIX-010 | `catch (_: IOException)` không bắt được `JSONException` khi parse JSON lỗi cú pháp → crash không kiểm soát | `act/IsotopesActExperimental.kt:375`, `ext/InfoExt.kt:366` | codex, agy | 🔶 Cross-confirmed | ✅ Đã fix — đổi thành `catch (_: Exception)` cả 2 chỗ |
| FIX-011 | `mailto:` URI ghép trực tiếp title/content người dùng nhập không qua `Uri.encode()` | `act/setting/SubmitAct.kt:203-211` | agy, claude, tự audit | 🔶 Cross-confirmed (3 nguồn) | ✅ Đã fix — `Uri.encode()` subject/body + try/catch `ActivityNotFoundException` (string mới `email_client_not_found` cho đủ 17 locale) |
| FIX-012 | `RoyApp.pendingAdInitCallbacks` giữ lambda capture Activity không có API huỷ; state init ads không đồng bộ, callback SDK có thể chạy ngoài main thread → race | `RoyApp.kt`, `act/SplashAct.kt` | codex, claude | 🔶 Cross-confirmed | ✅ Đã fix — thêm `removePendingAdInitCallback()` (gọi từ `SplashAct.onDestroy()`), đồng bộ hoá state bằng `synchronized`, ép callback SDK chạy trên main thread qua `Handler.post` |
| FIX-013 | Banner VIP chỉ check ẩn/hiện trong `onCreate()`, không re-check ở `onResume()` | `act/ElementInfoAct.kt`, `act/SettingsAct.kt`, `act/setting/FavoritePageAct.kt` | claude | ⚪ Cần verify → ✅ Verified | ✅ Đã fix — gộp logic vào `BaseAct.refreshVipGatedBanner()` (dùng `AdManager.bannerDestroy()` khi VIP active), gọi từ cả `onCreate()` và `onResume()` ở cả 3 màn (tiện thể xoá luôn dead code `createAdBanner` comment-out ở FavoritePageAct — ENH-017) |
| FIX-014 | Random Element và kết quả Search bypass interstitial gate, không nhất quán với click từ grid chính | `act/MainAct.kt` | claude | ⚪ Cần verify → ✅ Verified | ✅ Đã fix — gộp logic gate vào `navigateToElementInfoGated()`, áp dụng cho cả 3 điểm điều hướng (grid/random/search) |
| FIX-015 | So khớp số oxi hoá bằng `contains("0")`/`contains("1")` trên toàn chuỗi → false positive | `ext/InfoExt.kt:258-319` | agy, tự audit | 🔶 Cross-confirmed | ✅ Đã fix — split thành token list rồi so khớp chính xác thay vì substring |
| FIX-016 | `loadImage()` tạo mới `OkHttpClient` + `Picasso` instance ở mỗi lần gọi | `ext/InfoExt.kt:388-401` | tự audit | ⚪ Cần verify → ✅ Verified | ✅ Đã fix — share 1 instance qua `companion object` (lazy init theo `applicationContext`) |
| FIX-017 | `onBindViewHolder` đọc + parse lại JSON asset ở mỗi lần bind/recycle; parse lỗi không reset text | `adt/IonAdapter.kt:53-77` | tự audit, claude | 🔶 Cross-confirmed | ✅ Đã fix — cache theo tên nguyên tố trong `companion object`, luôn set text (cache hoặc "---") nên không còn stale value khi recycle |
| FIX-018 | Parser công thức hoá học chấp nhận cặp ngoặc sai loại (`Ca(OH]2`, `{H2O)`) | `util/ChemicalFormulaParser.kt:94-125` | codex | ⚪ Cần verify → ✅ Verified | ✅ Đã fix — thêm `bracketStack` theo dõi loại ngoặc mở, throw nếu đóng sai loại. Test mới: `testFormulaParser_MismatchedBracketTypeThrows` |
| FIX-019 | Splash 30s "emergency fallback" gọi lại `goToMain()` nhưng bị chính guard cũ chặn lại nếu ad vẫn showing → có thể kẹt vĩnh viễn | `act/SplashAct.kt:157-184` | codex | ⚪ Cần verify → ✅ Verified | ✅ Đã fix — thêm tham số `force: Boolean`, escape timer 30s gọi `goToMain(force = true)` để bypass hẳn check `isFullscreenAdShowing`. Test mới: 3 case trong `SplashFlowLogicTest.kt` |

---

## P2 — Medium

### Trạng thái Sprint 3 (2026-09-01) — ✅ 14/14 đã xử lý

| ID | Vấn đề | File | Trạng thái |
|---|---|---|---|
| FIX-020 | `recyclerView.adapter!!.itemCount` dùng non-null assertion, khác các màn hình khác đều dùng `?.itemCount` an toàn → NPE risk | `act/table/IonAct.kt:217` | ✅ Đã fix — đổi `!!` thành `?.`. Đọc lại `onDestroy()` thấy `filterHandler` đã bị cancel nên NPE thực ra khó xảy ra qua đường thường — đây là fix phòng thủ/đồng nhất style, không viết test giả tạo cho nó |
| FIX-021 | Row set `isClickable/isFocusable=true` + ripple foreground nhưng không bao giờ gọi `setOnClickListener` → trông bấm được nhưng không làm gì | `adt/ElectrodeAdt.kt:18,25,46-60` | ✅ Đã fix — `item.name` khớp tên nguyên tố thật, wire click mở `ElementInfoAct` (cùng pattern `MainAct.getRandomItem()`). Test mới: `ElectrodeActTest` |
| FIX-022 | `onApplySystemInsets` cộng dồn `params.height += top`/`params2.topMargin += top` — header phình to liên tục mỗi lần insets đổi (bật bàn phím, xoay màn hình) | `setting/ExperimentalAct.kt:84,88` | ✅ Đã fix — cache base height/margin gốc 1 lần, luôn tính `base + top` thay vì cộng dồn. Test mới: `ExperimentalActTest` (gọi `onApplySystemInsets` 2 lần cùng giá trị, assert không phình) |
| FIX-023 | Widget đăng ký PendingIntent vào `R.id.flWidgetSearchBar` nhưng layout mặc định cho API < 31 không có ID này → widget liệt click trên Android ≤ 11 | `widget/ShortCommandWidget.kt:35` + `res/layout/view_short_command_widget.xml` | ✅ Đã fix — thêm id `widgetRoot` chung cho cả 2 layout variant, bind PendingIntent vào đó thay vì id chỉ tồn tại ở `layout-v31`. Test mới: `ShortCommandWidgetLayoutTest` (JVM, đọc raw XML + source, đảm bảo mọi layout variant có id được bind) |
| FIX-024 | 2 hệ thống pref nhiệt độ độc lập không đồng bộ: `TemperatureUnits` (String) vs `FavoriteBarPref.DegreePref` (Int) — đổi ở Settings không ảnh hưởng `FavoritePageAct` | `pref/UnitsPref.kt`, `pref/FavoriteBarPref.kt:84`, dùng ở `act/setting/UnitAct.kt`, `ext/InfoExt.kt`, `act/setting/FavoritePageAct.kt` | ✅ Đã fix — hỏi user qua AskUserQuestion, chọn hợp nhất về `TemperatureUnits` (màn Settings chính thức). Sửa `FavoritePageAct`/`InfoExt` đọc/ghi `TemperatureUnits`, xoá hẳn class `DegreePref` chết. Test mới: `FavoritePageTemperatureUnitTest` |
| FIX-025 | `drawCard()` gọi `ElementModel.getList(elementList)` không `clear()` trước → danh sách tăng dồn 118→236→354... sau mỗi lần chọn nguyên tố | `act/IsotopesActExperimental.kt` | ✅ Đã fix — thêm `list.clear()` trước khi gọi `getList()`. Test mới: `IsotopesActExperimentalTest` (click 3 lần, assert list vẫn đúng 118) |
| FIX-026 | `filter()` gán `filterHandler = Handler(...)` mới ở mỗi ký tự gõ, không `removeCallbacksAndMessages(null)` callback cũ → race condition khi gõ nhanh | `IsotopesActExperimental.kt`, `ElectrodeAct.kt`, `EquationsAct.kt`, `IonAct.kt` | ✅ Đã fix cả 4 file — tái sử dụng 1 Handler, `removeCallbacksAndMessages(null)` trước khi post callback mới. Test mới: `IonActTest` (đại diện, verify Handler instance giữ nguyên qua 2 lần gọi) |
| FIX-027 | `fadeOutAnim()` không `removeCallbacks` trước khi post callback mới → fade out rồi fade in liên tiếp nhanh khiến callback cũ fire trễ, UI biến mất bất ngờ | `util/Utils.kt` | ✅ Đã fix — đổi sang `view.animate().withEndAction{...}` (native, tự huỷ khi có animation mới đè lên), bỏ hẳn `postDelayed` độc lập. Test mới: `UtilsAnimTest` (verify bằng revert-and-rerun: fail trên code cũ, pass trên code mới) |
| FIX-028 | `.background.setTint()` thiếu `.mutate()` trước khi gọi → tint lem sang các item khác khi RecyclerView tái sử dụng view (Drawable bị share theo constant state) | `adt/ElementAdt.kt`, `ext/TableExt.kt`, `ext/InfoExt.kt`, `act/table/NuclideAct.kt` (~50 chỗ, rộng hơn fix.md liệt kê ban đầu) | ✅ Đã fix toàn bộ ~50 chỗ (sed đồng loạt). Đây là fix phòng thủ theo đúng khuyến cáo chính thức của Android docs — thử viết test verify bằng `ConstantState` equality nhưng **không tái hiện được bug trên API hiện đại của TECNO KJ7** (test pass y hệt dù có/không `.mutate()`) nên đã bỏ test đó thay vì giữ 1 test không phân biệt được đúng/sai |
| FIX-029 | `cacheDir.deleteRecursively()` xoá luôn thư mục cache (không chỉ nội dung bên trong) | `act/SettingsAct.kt` | ✅ Đã fix — chỉ xoá `listFiles()` bên trong, giữ nguyên thư mục gốc. **Bonus fix phát hiện khi viết test:** listener thật ra gắn nhầm vào `cacheLay` (chỉ phần text) thay vì `clearCacheSettings` (cả hàng có ripple) — ripple hiện toàn hàng nhưng phần lớn diện tích bấm không ăn. Đã sửa gắn đúng vào `clearCacheSettings`. Test mới: `SettingsActTest` |
| FIX-030 | Khi `storedGrantedAt <= 0`, hardcode giả định "kích hoạt cách đây 24h" để tính % elapsed → sai % hiển thị khi giả định không đúng thực tế | `feature/vip/VipManagementAct.kt` | ✅ Đã fix — bỏ giả định bịa, trả `0L` (unknown) và ẩn hẳn `progressVip` khi không biết thời điểm kích hoạt thật thay vì hiện % sai. Logic tách thành `VipCalculator.resolveGrantedAtMs()` (pure, JVM-testable). Test mới trong `VipCalculatorTest` |
| FIX-031 | `resistivity.toFloat()` có thể ném `NumberFormatException` khi `rMultiplier != "---"` nhưng `resistivity` không parse được | `ext/InfoExt.kt` | ✅ Đã fix — đổi sang `toFloatOrNull()`, fallback "---" khi 1 trong 2 giá trị không parse được. Grep toàn bộ 118 file asset JSON hiện tại: **không có element nào rơi vào case bất đối xứng này** → fix phòng thủ, không viết test giả (không có data thật để tái hiện) |
| FIX-032 | `ScaleGestureDetector.onTouchEvent(event)` bị gọi 2 lần cho cùng 1 `MotionEvent` → xử lý gesture 2 lần trên cùng sự kiện | `act/table/NuclideAct.kt` | ✅ Đã fix — gọi 1 lần, lưu kết quả, dùng lại cho return. Cùng 1 diff với FIX-033 |
| FIX-033 | Pinch-zoom vô dụng: `mScale += scale` gọi 2 lần, cả 2 nhánh clamp đều ép về đúng `1f` → zoom không bao giờ đổi | `act/table/NuclideAct.kt` | ✅ Đã fix — bỏ dòng cộng dồn thừa; hỏi user chọn khoảng zoom qua AskUserQuestion → `MIN_SCALE=0.4f` (zoom in tối đa 2.5x), `MAX_SCALE=1f` (giữ nguyên không cho thu nhỏ hơn gốc). Test mới: `NuclideActZoomTest` — dựng `MotionEvent` 2 ngón tay thật (pinch-out), verify `mScale` thực sự đổi khỏi `1f` (verify bằng revert-and-rerun: fail trên code cũ) |

---

## P3 — Low / nhỏ

### Trạng thái Sprint 3 (2026-09-01) — 5/6 đã xử lý, 1 không phải bug

- ~~NotesPref lưu ghi chú bằng `commit=true`~~ — ❌ **Không phải bug.** Đọc lại `NotesPref.kt:28`: `preference.edit { ... }` dùng KTX extension `SharedPreferences.edit()`, mặc định `commit=false` (tức đã dùng `apply()` sẵn). Claim trong fix.md không đúng với code hiện tại — không sửa gì.
- **`ClosedRange<Int>.random()` tự viết loại trừ `endInclusive`** ✅ Đã fix — xoá hẳn hàm tự viết, dùng thẳng `IntRange.random()` chuẩn của Kotlin stdlib (đã đúng inclusive, không cần code riêng). `view/ConfettiView.kt`
- **`IsoPref`/`SendIso` lưu boolean dưới dạng String** ✅ Đã fix — đổi `SendIso.getValue()/setValue()` sang `Boolean` + `getBoolean/putBoolean`. Đổi luôn tên key pref để tránh `ClassCastException` nếu máy cũ còn giá trị String kiểu cũ. `pref/IsoPref.kt`, 2 call site ở `InfoExt.kt`/`IsotopesActExperimental.kt`
- **`indicatorList[0..3]` truy cập index cứng** ✅ Đã fix — tra theo `item.name` (`"bromothymol_blue"`, `"methyl_orange"`, ...) thay vì vị trí, không phụ thuộc thứ tự trả về của `IndicatorModel.getList()`. `act/table/PHAct.kt`
- **`Log.i` không debug-gate** ✅ Đã fix — bọc `if (BuildConfig.DEBUG)`. Xác nhận claim "vô hại vì R8 strip" trong fix.md **sai**: không có rule `-assumenosideeffects` cho `android.util.Log` trong `proguard-rules.pro`, nên các log này **có chạy** ở bản release trước khi sửa. `pref/LanguagePref.kt`, `util/LocaleHelper.kt`
- **`LocaleHelper.kt` ép cứng `config.fontScale = 1.0f`** ✅ Đã fix — xoá dòng ép cứng, để `Configuration` copy nguyên `fontScale` thật của hệ thống. Test mới: `LocaleHelperFontScaleTest` (verify bằng revert-and-rerun). **Lưu ý từ review độc lập:** cho phép fontScale thật có thể khiến vài layout fixed-height (vd `ExperimentalAct` title bar 80dp) tràn/cắt chữ ở fontScale lớn (1.3-2.0x) — đây là đánh đổi cố hữu khi bật lại accessibility đúng chuẩn thay vì khoá cứng 1.0x, không phải regression riêng của fix này. Chưa audit toàn bộ layout fixed-height trong app cho mọi fontScale — để làm sau nếu có báo cáo thật từ user dùng cỡ chữ lớn.

### Review độc lập (`/code-review high`) sau Sprint 3 — 3 góp ý style, quyết định không áp dụng

Không tìm thấy bug thật nào (0 correctness issue ngoài đã note ở FIX-LocaleHelper trên). 3 góp ý còn lại đều là style/convention, cân nhắc rồi **quyết định không sửa** vì đúng tinh thần "không thêm abstraction khi chưa cần":
1. `ElectrodeAdt` gọi thẳng `ElectrodeAct` thay vì qua `interface OnXClickListener` như các adapter khác (`IonAdapter`, `EquationsAdt`, ...). Không sửa: hiện chỉ có đúng 1 nơi dùng `ElectrodeAdt`, thêm interface cho 1 caller duy nhất là abstraction thừa.
2. Pattern debounce Handler (`if (filterHandler==null)...removeCallbacksAndMessages`) lặp lại y hệt ở 4 file thay vì tách hàm dùng chung. Không sửa: 2 dòng lặp ở 4 chỗ chưa đủ đau để đánh đổi lấy 1 extension function mới + rủi ro sửa nhầm khi trích xuất.
3. `NuclideAct` vẫn dùng chuỗi `if (decayTypeResult == "...")` thay vì `when`. Không sửa: thuần cosmetic, không có nguy cơ sai logic (10 giá trị loại trừ lẫn nhau), đổi sang `when` không đổi hành vi.

---

## Phát hiện mới khi chạy instrumented test thật trên device (2026-09-01) — ✅ Đã xử lý

Chạy `connectedDevDebugAndroidTest` trên 2 thiết bị thật (TECNO KJ7, Pixel 7 Pro) lộ ra 2 vấn đề **có sẵn từ trước**, không liên quan tới 7 fix P0. Đã điều tra sâu và xử lý dứt điểm cả 2:

- **FIX-038 — `btnRevokeVip`/`btnRedeemVip` không click được trên thiết bị thật ✅ Đã fix.** Root cause thật (2 lớp, cả 2 đều nằm ở **test code**, không phải app code):
  1. `btnRevokeVip` nằm gần cuối `NestedScrollView` (`scrollVip`). Dùng `ViewActions.scrollTo()` để cuộn tới trước khi click → Espresso tổng hợp một cử chỉ vuốt (swipe); vì điểm cuộn nằm gần rìa dưới màn hình, **hệ điều hành (gesture navigation) hiểu nhầm thành thao tác "vuốt lên để về Home"**, đẩy cả app ra nền (`mCurrentFocus` chuyển sang launcher) → mọi tương tác sau đó fail với `RootViewWithoutFocusException`. Verify bằng `adb shell dumpsys window` ngay sau khi fail, thấy `mCurrentFocus` là `QuickstepLauncher`/`NexusLauncher`, không phải app. Sửa: cuộn bằng code trực tiếp (`NestedScrollView.fullScroll(View.FOCUS_DOWN)` trong `scenario.onActivity {}`), không tạo touch event nào, tránh hẳn vùng gesture hệ thống. Áp dụng cho `VipManagementActTest` (4 chỗ, gộp thành helper `clickRevokeButton()`) và `VipManagementWidgetTest` (1 chỗ).
  2. `emptyKey_showsFailedDialog` **không phải flaky — test sai giả định**: `bindKeyWatcher()` set `binding.btnRedeemVip.isEnabled = false` khi field rỗng, chỉ bật lại khi có text. Click vào nút đang disable không bao giờ gọi tới `redeemInputKey()`, nên dialog "Failed" không bao giờ xuất hiện — ở BẤT KỲ môi trường nào, không phải do thiết bị hay do tải. Sửa: đổi test thành `emptyKey_redeemButtonDisabled` — assert đúng hành vi thật (nút bị disable khi field rỗng), khớp pattern đã có sẵn của `freeState_revokeButtonDisabled`.
  - **Kết quả verify cuối:** `VipManagementActTest` + `VipManagementWidgetTest` = 38/38 pass trên cả 2 thiết bị (2026-09-01).
- **FIX-040 — `privacyPolicyFooter_isDisplayedAndClickable` + `revoke_cancelled_vipCardStillVisible` fail trên Samsung SM-S928B (One UI) ✅ Đã fix (test code).** Root cause thật (không liên quan Sprint 2 — `VipManagementAct*.kt` không nằm trong diff, lần sửa gần nhất là FIX-038/`34bd78b`): `wm density` của máy này bị override lên 480 (Display size lớn hơn mặc định) → chiều cao khả dụng tính theo dp bị thu hẹp, khiến nội dung `scrollVip` (NestedScrollView) thật sự cần cuộn trên máy này, trong khi ở TECNO KJ7/Pixel 7 Pro vừa đủ 1 màn hình nên trước giờ "pass may mắn" mà không cần cuộn. 2 lỗi cụ thể: (1) `privacyPolicyFooter_isDisplayedAndClickable` chưa từng cuộn tới `tvPrivacyPolicy` (item cuối layout); (2) `revoke_cancelled_vipCardStillVisible` gọi `clickRevokeButton()` (cuộn xuống đáy để bấm nút) rồi assert `activeVipCard` (nằm gần đầu layout) đang hiển thị mà không cuộn lại lên trước — trên máy màn hình đủ cao thì không cần cuộn nên bug ẩn giấu, trên Samsung thì card đã bị cuộn khuất khỏi màn hình thật. Ban đầu nghi ngờ do race animation ẩn IME (thử thêm `Thread.sleep(500)` trước assertion — không fix được, loại trừ giả thuyết này). Sửa: thêm `NestedScrollView.fullScroll(View.FOCUS_DOWN)`/`FOCUS_UP` bằng code (cùng pattern FIX-038, không dùng swipe gesture) trước mỗi assertion. Verify: `VipManagementActTest` 24/24 pass trên cả 2 máy; full suite 108/108 pass riêng từng máy (Samsung và TECNO chạy song song bị nhiễu bởi app khác trên máy dùng chung — xem ghi chú TECNO bên dưới, không liên quan fix này).
- **FIX-039 — Nghi ngờ race giữa `countDownTimer` (15s) và `postDelayed` (1.5s) — ❌ Không phải bug, đã điều tra và bác bỏ.** Đọc lại `checkAnswer()` (`QuizAct.kt:421`) xác nhận `countDownTimer?.cancel()` đã được gọi **ngay dòng đầu tiên** khi trả lời — không có khoảng hở nào cho timer cũ tự bắn tiếp. Lần fail duy nhất quan sát được (`testQuizSelectionAdvancesToNextQuestion` nhảy tới "4/10" sau 36s) xảy ra khi chạy đồng thời toàn bộ 107 test trên cả 2 thiết bị cùng lúc — bản chất là: khi máy quá tải, toàn bộ quá trình thực thi test (bao gồm cả `Thread.sleep(2000)` và bước kiểm tra) bị trì hoãn tới mức thời gian thực trôi qua tới 36 giây; trong khoảng đó, cơ chế đếm giờ 15s/câu (vốn hoạt động đúng thiết kế) tự động advance thêm 2 câu do không có tương tác nào khác. Verify: chạy cô lập test này 3/3 lần trên code gốc + 1/1 lần trên cả 2 thiết bị với code đã sửa → pass tuyệt đối. Không sửa code app vì không có bug thật để sửa — sửa nhầm chỗ này sẽ là thay đổi không cần thiết.

## Đã note trong doc/ cũ nhưng audit xác nhận **chưa** thực sự fix

- ~~`doc/memory_leak.md` mục Priority 3 (#4): `DictionaryPref(this)` vẫn được khởi tạo mới **bên trong vòng lặp `for`** của `filter()`~~ ✅ **Đã fix (2026-09-01).** `act/table/DictionaryAct.kt:230` (cũ: dòng 233) — giá trị pref không đổi trong suốt vòng lặp `for (item in list)`, nên chỉ cần đọc 1 lần trước loop thay vì tạo mới `DictionaryPref` + gọi `getValue()` (đụng `getSharedPreferences`) mỗi phần tử. Không viết test riêng: đây là bug hiệu năng (số lần allocation/đọc SharedPreferences), không phải bug hành vi quan sát được — output `filteredList` giống hệt trước/sau fix nên không có assertion nào phân biệt được đúng/sai (giống trường hợp `ElementAdtTintTest` đã bỏ ở Sprint 3, tránh test giả tạo cho có).
- `doc/feat.md:15` note "11 pre-existing Mockito failures trong `VipPrefsTest`" — **đã chạy lại**: `./gradlew :app:testDevDebugUnitTest` → `VipPrefsTest` 11 test, 0 failures, 0 errors. Không tái hiện được trên môi trường hiện tại. Đã cập nhật `doc/feat.md:15` phản ánh đúng thực tế.

## ponytail-audit toàn repo (2026-09-01) — 5 áp dụng, 1 declined

Quét over-engineering toàn bộ codebase (không phải bug/security/performance — chỉ code thừa/abstraction thừa). Đã verify usage count thật bằng grep trước khi kết luận, không đoán.

**Đã áp dụng:**
- `shrink` `ext/TableExt.kt` — 8 hàm gần giống hệt nhau (`initBoiling/initMelting/initPhase/initYear/initWeight/initHeat/initSpecific/initVape`), mỗi hàm tự tạo 1 `Handler` riêng rồi mở `"$element.json"` copy đúng 1 key JSON vào `TextView`, chỉ khác nhau ở key. Gộp thành 1 `initJsonField(list, jsonKey: (JSONObject) -> String)` dùng chung 1 `Handler` (`jsonFieldHandler`), 8 hàm cũ giờ là 1-liner gọi `initJsonField`. Xác nhận cả 8 chỉ được gọi 1 lần/click (`MainAct.kt` — mỗi hàm gắn với 1 nút hover-menu riêng, không bao giờ gọi đồng thời) nên dùng chung Handler an toàn. `initElectro`/`initGroups` giữ nguyên (có logic branching màu thật, không phải copy JSON đơn thuần). Verify: test có sẵn `MainActTest.allHoverMenuLenses_runOverFullElementList_withoutCrashing` (chạy cả 8 hàm + initGroups/initElectro qua full 118 element) — 3/3 pass trên TECNO KJ7 sau refactor.
- `delete` `util/Pasteur.kt` — object log wrapper, `init(debug)` không ai gọi nên `debugMode` luôn `false`, nghĩa là **không hàm nào từng thực sự log** dù có gọi. Chỉ 1 call site thật (`TableExt.kt`, log chiều cao insets, thuần diagnostic). Xoá cả file + call site + import mồ côi ở `InfoExt.kt`; không đổi hành vi vì trước đó nó chưa từng log gì.
- `delete` `util/Utils.kt` — `slideUp(panel: SlidingUpPanelLayout)` và `fadeInAnimCard(view, time)`: 0 call site (grep chỉ khớp chính khai báo). Xoá cả 2 + import `SlidingUpPanelLayout`/`Handler`/`Looper` không còn dùng.
- `yagni` `feature/vip/VipManagementAct.kt` — `computeElapsedProgress(...)` private chỉ passthrough tới `VipCalculator.computeElapsedProgress(...)`, 1 call site duy nhất. Gọi thẳng `VipCalculator.computeElapsedProgress` tại chỗ, xoá wrapper.
- `stdlib` `feature/vip/VipManagementAct.kt` — `formatRemaining` bọc `LongArray` thành `List` chỉ để destructure. `LongArray` đã có sẵn `component1()`..`component4()`, destructure thẳng `VipCalculator.remainingParts(ms)`.

**Declined (không áp dụng):**
- `yagni` `ext/TableExt.kt`/`ext/InfoExt.kt` là `abstract class` chỉ có đúng 1 subclass (`MainAct`/`ElementInfoAct`), tồn tại chỉ để tách code 1 Activity ra file riêng chứ không phải để đa hình. Đề xuất gộp thẳng vào `MainAct.kt`/`ElementInfoAct.kt`. **Quyết định: không áp dụng.** `TableExt.kt` hiện ~500 dòng, `MainAct.kt` đã lớn sẵn — gộp thủ công 500+ dòng vào 1 file đã lớn là thay đổi cơ học rủi ro cao (dễ sai visibility modifier `protected`→cần đổi, dễ miss 1 tham chiếu) để đổi lấy lợi ích thuần tổ chức file, không giảm dòng code thật, không sửa bug. Không đúng tinh thần "diff ngắn nhất, rủi ro thấp nhất" của ponytail khi effort/benefit lệch quá xa. Giữ nguyên tách file như hiện tại.

Build: `compileDevDebugKotlin` + `testDevDebugUnitTest` xanh. Instrumented `MainActTest` 3/3 pass (TECNO KJ7).

## FIX-041 — ShortCommandWidget crash thật khi pin lên home, mọi launcher (P0, ✅ Đã fix, phát hiện 2026-09-02)

- **Nguồn:** tự phát hiện khi verify trực quan widget trên thiết bị thật thay vì chỉ tin instrumented test (`ShortCommandWidgetTest`) — test đó chỉ gọi thẳng `onUpdate()`/kiểm tra không crash trong process app, KHÔNG thực sự inflate layout qua `RemoteViews` ở process launcher nên không bắt được lỗi này.
- **File:** `res/layout/view_short_command_widget.xml` và `res/layout-v31/view_short_command_widget.xml` — toàn bộ cây view dùng `androidx.appcompat.widget.LinearLayoutCompat`/`AppCompatTextView`/`AppCompatImageView`.
- **Tác động:** `RemoteViews` (dùng cho App Widget) chỉ cho phép whitelist view class chuẩn framework (`LinearLayout`, `FrameLayout`, `TextView`, `ImageView`, `Button`, ...) — **không** chấp nhận bất kỳ view AppCompat nào. Pin widget lên home ở bất kỳ launcher nào (đã tái hiện trên Pixel Launcher + HiOS Launcher/Transsion Tecno) đều crash inflate với `InflateException: Class not allowed to be inflated androidx.appcompat.widget.LinearLayoutCompat`, hiển thị "Problem loading widget" — tính năng quick-trivia widget (vòng 5 mục 20) coi như hỏng hoàn toàn ngoài thực tế dù mọi unit/instrumented test đều xanh. Đây là lỗi P0 vì widget hoàn toàn không dùng được, không phải lỗi thẩm mỹ.
- **Vì sao lọt qua test:** Preview thumbnail trong widget picker dùng `android:previewImage`/ảnh tĩnh, không inflate layout thật nên không lộ lỗi. Instrumented test chỉ verify `Activity.onUpdate()`/`AppWidgetManager.updateAppWidget()` không throw ở phía app — nhưng exception xảy ra ở phía launcher (process khác) khi launcher tự inflate `RemoteViews`, ngoài tầm với của Espresso.
- **Fix:** Thay toàn bộ tag `androidx.appcompat.widget.LinearLayoutCompat` → `LinearLayout`, `AppCompatTextView` → `TextView`, `AppCompatImageView` → `ImageView` ở cả 2 file layout. Không đổi attribute/id/style nào khác.
- **Verify:** Build lại `assembleDevDebug`, cài + pin widget thật trên Tecno BG6 (Android 13, HiOS launcher) — widget render đúng: search bar "Search for element" + card "In — Indium" + fact text + 4 nút đáp án (Titanium/Indium/Ruthenium/Beryllium). Lưu ý: sau lần cài đầu tiên (trước khi sửa), launcher cache lại `AssetManager` cũ nên lần pin thử lại đầu tiên sau khi sửa layout vẫn báo lỗi cũ dù APK đã đúng — phải `am force-stop <launcher-package>` để launcher tự nạp lại resources.arsc mới thì mới thấy đúng; đây là launcher-cache quirk, không phải bug còn sót trong app.
- **Estimate:** S (fix), nhưng phát hiện được nhờ verify trực quan trên thiết bị thật — bài học: **RemoteViews/App Widget layout phải test bằng cách pin thật lên launcher ít nhất 1 lần**, instrumented test gọi thẳng `onUpdate()` không đủ để bắt lỗi inflate loại này.

## Full suite trước release (2026-09-02) — 239/239 pass, 5 flaky đã loại trừ

Sau khi push FIX-041 (`5f6abbd`), chạy `connectedDevDebugAndroidTest` full suite trên Pixel 7 Pro (2B051FDH3006MU, `ANDROID_SERIAL` để tránh chạy trùng lên Tecno BG6 đang cắm chung máy) trước khi quyết định release. Lần đầu: 234/239 pass, 5 fail — `NavigationIntegrationTest.testNavigateFromMainToCategoryPractice_pickingSpecificCategory` (`RootViewWithoutFocusException`), `ElementInfoSpeakTest.tappingSpeakButton_onceReady_doesNotCrash` + `TrendsChartActTest.launch_showsChartCardAndTapHint` (`NoMatchingViewException`), `FlashcardActTest.allCardsNotYetDue_fallsBackToFullDeckPracticeMode` + `.ratingEveryCard_reachesSessionComplete` (`NoMatchingViewException`/`NoActivityResumedException`). Cả 4 class này **không nằm trong diff** của `5f6abbd` (file sửa chỉ là `ElementInfoAct.kt` nextBtn/previousBtn catch-block, không đụng `speakElementBtn`; `FlashcardActTest`/`TrendsChartActTest`/`NavigationIntegrationTest` hoàn toàn không liên quan). Rerun riêng 4 class đó (24 test) ngay sau đó: **0 fail** — xác nhận flaky do timing/window-focus của Espresso trên máy này (cùng dạng với FIX-038/039/040), không phải regression từ release-audit. Không sửa gì thêm.
