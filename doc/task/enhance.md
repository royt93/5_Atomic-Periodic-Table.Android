# ENHANCE — Cải thiện code hiện có

Kiến trúc, performance, tech debt, test coverage, dead code, build hygiene. Nguồn: tự audit + codex + agy + claude. Priority/Estimate cùng thang với `fix.md`.

---

## Kiến trúc & Performance

| ID | Vấn đề | File | Nguồn |
|---|---|---|---|
| ENH-001 | `ElementWeightCache.init()` đọc + parse đồng bộ 118 file JSON trên main thread trong `Application.onCreate()` — kéo dài cold start trước cả khi splash kịp animate. 2 dependency `kotlinx-coroutines-*` đã khai trong `build.gradle:254-255` (comment "for async file I/O") nhưng **grep toàn app/src/main không có 1 chỗ dùng** `kotlinx.coroutines`/`CoroutineScope`/`lifecycleScope` | `RoyApp.kt:36`; `util/ElementWeightCache.kt:17-48` | tự audit — ✅ Verified |
| ENH-002 | `NuclideAct.addViews()` inflate động >3.000 View vào 1 `RelativeLayout` duy nhất trên main thread — đo lường `RelativeLayout` là O(N²), rủi ro ANR rõ rệt trên máy yếu | `act/table/NuclideAct.kt:186-316` | tự audit, agy, claude (3 nguồn) |
| ENH-003 | 118 file JSON riêng lẻ trong assets, đọc lại nhiều lần cho từng thuộc tính khác nhau (boiling/melting/weight/...) thay vì 1 nguồn dữ liệu tập trung có index | `ext/TableExt.kt`, `ext/InfoExt.kt`, rải khắp `act/`, `adt/` | agy, claude |
| ENH-004 | Không Activity nào giữ state qua Configuration Change (không ViewModel) — đây là quyết định kiến trúc đã biết (xem CLAUDE.md), nhưng với các màn phức tạp (`QuizAct`, `VipManagementAct`, `CalculatorAct`, `EquationBalancerAct`) việc mất state khi xoay màn hình là trải nghiệm thật, không chỉ lý thuyết | toàn `act/` | claude, tự audit |
| ENH-005 | `resources.getIdentifier()` (reflection) dùng trong hot loop — `ElementTranslator.kt:15` gọi 118 lần lúc mở app + mọi `onBindViewHolder`; tương tự ở `TableExt.kt`, `IonAct.kt:147-159`, `PHAct.kt:140-175` — vừa chậm vừa cản R8 xác định resource usage | `util/ElementTranslator.kt:15`, `ext/TableExt.kt:98-99,147-529`, `act/table/IonAct.kt:147-159`, `act/table/PHAct.kt:140-175` | claude, tự audit |
| ENH-006 | SharedPreferences instantiate mới lặp lại trong loop/bind thay vì cache instance: `ElementAdt.onBindViewHolder`, `DictionaryAct.filter()` (đã note ở FIX, mục "chưa fix") | `adt/ElementAdt.kt:55`, `act/table/DictionaryAct.kt:233` | claude, tự audit |
| ENH-007 | Không adapter nào dùng `ListAdapter`/`DiffUtil` — mọi filter đều `notifyDataSetChanged()` hoặc gán adapter mới, mất animation/scroll state | `adt/DictionaryAdt.kt:77-80`, `ElectrodeAdt.kt:65-68`, `EquationsAdt.kt:101-104`, `IonAdapter.kt:92-97`, `IsotopeAdt.kt:68-72` | tự audit, claude |
| ENH-008 | `bindingAdapterPosition` truyền vào click listener không guard `RecyclerView.NO_POSITION` | `adt/DictionaryAdt.kt:66`, `EquationsAdt.kt:84`, `IonAdapter.kt:86`, `IsotopeAdt.kt:59` | tự audit |
| ENH-009 | Parser công thức hoá học `dfs()` backtracking đệ quy không memoize khi tách symbol viết thường mập mờ — input dài/mập mờ có thể blowup theo cấp số mũ, nguy cơ ANR trong ô nhập Calculator/Equation Balancer | `util/ChemicalFormulaParser.kt:30-78` | tự audit |
| ENH-010 | `AdKeys.VIP_SECRET` là secret tĩnh baked vào `BuildConfig`, không rotate/revoke được nếu bị leak khỏi APK — giới hạn thiết kế cần biết trước khi scale mô hình VIP | `common/const/AdKeys.kt`, `feature/vip/VipKeys.kt` | codex, claude |

## Code smell / DRY

| ID | Vấn đề | File | Nguồn |
|---|---|---|---|
| ENH-011 | 9 hàm `init*()` gần giống hệt nhau (`initBoiling/Melting/Phase/Year/Groups/Weight/Heat/Specific/Vape`), mỗi hàm mở/parse lại cùng JSON chỉ để đọc 1 field khác nhau — nên gộp thành 1 helper tham số hoá `(jsonKey, targetHandler)`. Chính duplication này là nguyên nhân sinh ra FIX-004 (copy-paste nhầm biến) | `ext/TableExt.kt` | claude, tự audit |
| ENH-012 | `FavoriteBarPref.kt` có ~13 class SharedPreferences wrapper gần như copy-paste cho từng thuộc tính Int — nên gộp thành 1 generic `IntPref`. Kéo theo `FavoritePageAct.kt` lặp if/else 13 lần | `pref/FavoriteBarPref.kt`, `act/setting/FavoritePageAct.kt` | claude |
| ENH-013 | `anim/Anim.kt` và `util/Utils.kt:9-58` có 2 bộ helper `fadeIn`/`fadeOut` song song làm cùng việc | `anim/Anim.kt`, `util/Utils.kt:9-58` | claude |
| ENH-014 | `ElementInfoAct.nextPrev()` có 2 block gần như trùng lặp ~25 dòng (Next/Previous) — có thể gộp thành 1 hàm nhận `delta: Int` | `act/ElementInfoAct.kt:221-270` | claude |
| ENH-015 | `SettingsAct.kt:429-655` — ~17 block lặp cho từng ngôn ngữ, nên chuyển data-driven (list `Locale` + loop) thay vì hard code từng if | `act/SettingsAct.kt:429-655` | claude |
| ENH-016 | setTheme gọi thủ công lặp lại ở nhiều Activity dù `BaseAct` đã tự làm việc này (`TableAct`, `SettingsAct`, `FavoritePageAct`, `DictionaryAct`, `NuclideAct`, `IsotopesActExperimental`, `SubmitAct`, `OrderAct`, `PHAct`) | rải rác `act/` | claude |
| ENH-017 | Block loading banner ad copy-paste 3 lần giống hệt nhau ở `ElementInfoAct`/`SettingsAct`/`FavoritePageAct` — nên gộp thành helper dùng chung trong `BaseAct` | `act/ElementInfoAct.kt`, `act/SettingsAct.kt`, `act/setting/FavoritePageAct.kt` | claude |
| ENH-018 | `VipManagementAct` là God Activity ~335 dòng, không tách state → không unit-test được logic cốt lõi (progress %, format countdown đã tách ra `VipCalculator` rồi, nhưng phần điều phối UI/SDK vẫn dồn hết vào 1 Activity) | `feature/vip/VipManagementAct.kt` | claude |
| ENH-019 | `onCreate()` và `onResume()` cùng gọi `bindUi()` → `CountDownTimer`/`loadRewarded` có thể chạy trùng lặp thừa | `feature/vip/VipManagementAct.kt` | claude |
| ENH-020 | Unused import `ViewGroup` | `act/CalculatorAct.kt:5`, `act/EquationBalancerAct.kt:5` | claude |
| ENH-021 | `LocaleHelper.kt:38` dùng string replace mong manh để xử lý locale tag thay vì API `Locale`/`LocaleListCompat` chuẩn | `util/LocaleHelper.kt:38` | claude |

## Dead code — xoá được ngay

- `util/Applovin.kt` — toàn bộ file (~165 dòng) là code AppLovin/MAX cũ đã comment hết, đã note dead trong CLAUDE.md. *(tự audit, claude)*
- `ext/ContextExtension.kt` — cả file chỉ có 1 hàm comment-out. *(tự audit)*
- `act/MainAct.kt:705-789`, `act/ElementInfoAct.kt:272-361` — block MAX interstitial cũ bị comment nguyên khối. *(tự audit)*
- `ext/Context.kt:9-47`, `ext/Activity.kt:19-56` — ~40 dòng hàm/Logger call đã comment chết. *(tự audit)*
- `act/SettingsAct.kt:288-296`, `act/setting/FavoritePageAct.kt:312-316` — tàn dư `finishScreen()`/`createAdBanner()` đã comment. *(tự audit)*

## Tính năng dở dang / non-functional đang ship

- **`OrderAct.kt`** (Settings → sắp xếp thứ tự): kéo-thả dùng data giả cứng (`"Item 1".."Item 5"`) và callback `onItemDragged`/`onItemDropped` **rỗng hoàn toàn** — màn hình tồn tại trong Settings nhưng không làm gì thật. `act/setting/OrderAct.kt:55-104`. *(claude, tự audit)* → nên quyết định: hoàn thiện thật (xem TASK-xxx) hoặc gỡ khỏi Settings để tránh gây nhầm cho user.

## Build / dependency hygiene

| ID | Vấn đề | File |
|---|---|---|
| ENH-022 | `com.google.code.gson:gson:2.13.2` có `-keep` rule riêng trong proguard nhưng **grep 0 chỗ dùng Gson** trong `app/src/main/java`; tương tự Retrofit có keep rule dù không phải dependency — ngược với khuyến nghị "Remove Unused Dependencies" trong chính `doc/BUILD_OPTIMIZATION.md` | `app/build.gradle:239`, `app/proguard-rules.pro:80-95,116-122` |
| ENH-023 | `release` buildType khai 2 lần ở 2 block `buildTypes {}` không liền nhau, với `minifyEnabled` mâu thuẫn (`false` ~line 93, `true` ~line 165) — dựa vào Gradle merge semantics để ra giá trị đúng cuối cùng, dễ vỡ nếu sửa 1 trong 2 block mà quên block kia | `app/build.gradle:61-100,162-184` |
| ENH-024 | `org.gradle.parallel=true` khai trùng 2 lần trong cùng file | `gradle.properties:13,33` |
| ENH-025 | ABI splits `enable false` nhưng `doc/BUILD_OPTIMIZATION.md:31-47` mô tả như đang active tạo 4 APK riêng — doc lệch thực tế (giờ chỉ build AAB) | `app/build.gradle:126-133` vs `doc/BUILD_OPTIMIZATION.md` |
| ENH-026 | Rule `-keep class x.** { *; }` quá rộng cho AdMob/AppLovin/Material/thư viện UI bên thứ 3 — làm giảm hiệu quả shrink/obfuscate của R8, ngược mục tiêu giảm size mà chính doc này đề ra | `app/proguard-rules.pro:48-77,127-153` |
| ENH-027 | Dependency cũ ít maintain: Picasso 2.71828, twowaynestedscrollview 0.1, slidinguppanel 3.4.0 — cân nhắc thay thế (xem IDEA: Picasso → Coil) | `app/build.gradle` |
| ENH-028 | `doc/BUILD_OPTIMIZATION.md`, `doc/GRADLE_MIGRATION_STATUS.md`, `doc/memory_leak.md` (còn ref `AdMobManager.kt` — class không còn tồn tại trong code hiện tại) — stale so với code, nên update lại theo state thật | `doc/*.md` |

## Accessibility

- `LocaleHelper.kt:50` ép cứng `config.fontScale = 1.0f` khi đổi ngôn ngữ — vô hiệu hoá cài đặt phóng to cỡ chữ hệ thống, ảnh hưởng người khiếm thị/người lớn tuổi dùng font scale lớn. *(agy)*
- Chưa có accessibility audit toàn diện: TalkBack order, content description cho 118 ô bảng tuần hoàn, touch target size, contrast, RTL layout. *(codex)*

## Test coverage gap

- Toàn bộ `adt/` (7 adapter: Dictionary/Electrode/Element/Equations/Ion/Isotope/Order) — **0 test**. *(claude, codex)*
- Toàn bộ `act/table/*` (Ion/Equations/PH/Nuclide/Electrode/Dictionary) — 0 test riêng, dù `DictionaryAct` từng có memory leak bug đã note không có regression test. *(claude, codex)*
- Toàn bộ `act/setting/*` (About/Order/Unit/Submit/Licenses) — 0 test. *(claude, codex)*
- `TableAct`, `CompareAct`, `SolubilityAct`, `IsotopesActExperimental` — 0 test. *(claude)*
- Không Activity nào có unit test JVM thật (chỉ có `SplashFlowLogicTest` test logic tách riêng). *(claude)*
- `util/ElementWeightCache.kt` — không có unit test dù là cache trung tâm nuôi Calculator/Quiz/Compare, khởi tạo 1 lần lúc app start. *(tự audit)*
- `ext/TableExt.kt` — 0 test cho họ hàm `init*()`, ngay cả bug FIX-004 lẽ ra 1 test instrumented đơn giản trên hover menu đã bắt được. *(tự audit)*
- VIP: chưa có test cho redeem-khi-đang-active, fallback `grantedAtMs=0`, reward earned/failed, revoke→re-redeem, xoay màn hình/process death giữa lúc countdown, ẩn/hiện banner sau redeem. *(claude)*
- `doc/feat.md:15` note "11 pre-existing Mockito failures trong `VipPrefsTest`" — **cần re-run và fix trước**, một test suite đỏ/flaky làm giảm độ tin cậy của toàn bộ coverage VIP hiện có. *(tự audit — chưa re-run được vì cần secrets file riêng)*
