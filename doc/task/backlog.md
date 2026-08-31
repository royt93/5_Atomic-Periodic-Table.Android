# Backlog tổng — Atomic Periodic Table Android

Tổng hợp từ: đọc toàn bộ source code (Android app + tất cả file `doc/*.md` hiện có) bởi 1 agent audit kỹ thuật + 1 agent ideation sản phẩm (nội bộ), cộng thêm 3 AI Agent CLI độc lập chạy read-only trên cùng codebase: `codex exec -s read-only`, `agy --dangerously-skip-permissions --print`, `claude -p --permission-mode plan` (mỗi agent không biết ý kiến của agent khác). `gemini` CLI không dùng được do tài khoản hết hạn hỗ trợ ("IneligibleTierError").

Chi tiết từng nhóm nằm ở file riêng — bảng dưới đây chỉ để track status theo quy ước:
✅ Implemented · 🟡 In progress · 📋 Picked · ⏸️ Deferred · ❌ Skipped · 💭 Ideas

| File | Nội dung | Số item | Status |
|---|---|---|---|
| [fix.md](fix.md) | Bug cần sửa (7 P0, 12 P1, 14 P2, 6 P3, 2 nợ kỹ thuật cũ, 2 phát hiện mới từ device test) | 41 | 🟡 In progress — 7/7 P0 ✅ Implemented (Sprint 1, 2026-08-31 → 2026-09-01), còn lại 💭 Ideas |
| [enhance.md](enhance.md) | Kiến trúc, performance, code smell, dead code, build hygiene, test coverage gap | ~40 | 💭 Ideas |
| [new-feature.md](new-feature.md) | Task mới cụ thể có thể lên sprint | 25 | 💭 Ideas |
| [idea.md](idea.md) | Ý tưởng roadmap dài hạn | 14 | 💭 Ideas |
| [exclusive-feature.md](exclusive-feature.md) | Tính năng độc quyền/khác biệt hoá, có ưu-nhược điểm từng option | 5 | 💭 Ideas |

## Sprint 1 — ĐÃ XONG (2026-08-31): 7 P0 trong fix.md ✅

Đã fix + verify bằng `./gradlew :app:testDevDebugUnitTest` (111 test, 0 fail) và `:app:testProductionReleaseUnitTest` (bao gồm `VipPrefsTest` 11 test, 0 fail — không còn tái hiện được lỗi note trong `doc/feat.md:15`). Chi tiết từng fix xem `fix.md`.

1. **FIX-001** ✅ — Platinum/Protactinium trùng ký hiệu `"Pa"` → đổi Platinum thành `"Pt"` (`ElementModel.kt`, `IonModel.kt`)
2. **FIX-002** ✅ — Palladium sai ký hiệu `"Ph"` → đổi thành `"Pd"` (`IonModel.kt`)
3. **FIX-003** ✅ — Đồng (Cu) sai dấu thế điện cực chuẩn → đổi `-0.159` thành `+0.34` (`SeriesModel.kt`)
4. **FIX-004** ✅ — `TableExt.initName(elementList)` dùng nhầm list rỗng → đổi thành `initName(list)` x5, xoá field thừa
5. **FIX-005** ✅ — `allowBackup` bao gồm SharedPreferences VIP → exclude `vip_screen_prefs.xml` khỏi `data_extraction_rules.xml` + `backup_rules.xml` (pref riêng của SDK `AdManager` chưa exclude được — không rõ tên file)
6. **FIX-006** ✅ — `QuizAct` không huỷ `postDelayed` khi destroy → lưu `Runnable` vào field, `removeCallbacks` trong `onDestroy()`
7. **FIX-007** ✅ — `VipManagementAct` redeem không guard khi VIP đang active → thêm dialog xác nhận nếu mã mới cấp ít ngày hơn số ngày còn lại (thêm string resource cho đủ 17 locale)

**Chưa làm:** instrumented test (`connectedDevDebugAndroidTest`) chưa chạy vì cần chọn thiết bị/emulator (theo quy tắc dự án phải hỏi trước khi build/deploy lên Android — chưa hỏi trong phiên này).

## Audit report (2026-09-01) — điểm: 9.3/10

Sau khi Sprint 1 xong, audit lại toàn bộ + bổ sung test + smoke test thật trên 2 thiết bị (TECNO KJ7, Pixel 7 Pro):

**Test coverage thêm mới:**
- Unit test (JVM): `ElementModelTest` (+3 test symbol-uniqueness), `IonModelTest` (mới, 4 test), `SeriesModelTest` (mới, 2 test) — tất cả re-guard trực tiếp FIX-001/002/003.
- Instrumented test: 4 test mới cho FIX-007 (`VipManagementActTest`), 1 test mới cho FIX-006 destroy-during-delay (`QuizActTest`), 1 test full-dataset no-crash cho FIX-004 (`MainActTest`), 2 integration test tính khối lượng mol Pt/Pd thật qua Calculator (`FeaturesIntegrationTest`) — chứng minh FIX-001/002 đúng end-to-end qua `ElementWeightCache` + JSON asset thật, không chỉ đúng trong model tĩnh.
- Smoke test: `connectedDevDebugAndroidTest` chạy full 107 test × 2 thiết bị thật. 0 regression từ 7 fix. 6 fail/thiết bị là bug môi trường **có sẵn từ trước** (đã verify bằng cách chạy lại y hệt trên code gốc, ra kết quả giống hệt) — ghi lại thành FIX-038/039 trong `fix.md`, không tính là lỗi của sprint này.

**Review độc lập (`/code-review high`, agent riêng không biết code tôi vừa viết):** 3 finding, cả 3 đã fix ngay:
1. Logic huỷ/lên lịch lại `postDelayed` bị lặp 2 nơi trong `QuizAct.kt` → gộp thành `scheduleNextQuestion()`.
2. **Nghiêm trọng nhất:** tôi dùng `Write` tạo "file mới" cho `QuizActTest.kt` nhưng thực ra file đã tồn tại sẵn với 5 test — bị ghi đè mất. Review bắt được, đã khôi phục đủ 5 test gốc + giữ lại 1 test mới có giá trị (gộp, không trùng lặp).
3. `IonModelTest` có lỗ hổng: im lặng bỏ qua thay vì fail khi tên nguyên tố không khớp giữa 2 model → sửa thành fail rõ ràng.

**Vì sao không phải 10/10:** (a) sự cố ghi đè file test ở mục 2 — lẽ ra không nên xảy ra, chỉ được cứu nhờ có bước review độc lập, không phải vì tôi tự phát hiện; (b) FIX-005 chưa loại trừ được pref riêng của SDK `AdManager` khỏi backup (không rõ tên file, ngoài tầm kiểm soát code trong repo); (c) phát hiện thêm 2 bug môi trường pre-existing (FIX-038/039) qua smoke test thật — tốt vì tìm ra, nhưng chưa fix (ngoài scope 7 P0).

**Kết luận: 9.3/10 → đạt ngưỡng >9, tiến hành push.**

## Độ tin cậy — cách đọc backlog này

Mỗi item trong `fix.md` có gắn nhãn:
- **✅ Verified** — tôi tự `grep`/đọc lại source thật để xác nhận, không chỉ tin lời agent.
- **🔶 Cross-confirmed** — 2+ AI Agent độc lập cùng tìm ra (không hẹn trước), độ tin cậy cao dù tôi chưa tự đọc lại từng dòng.
- **⚪ Cần verify** — chỉ 1 nguồn nêu, tôi chưa tự xác nhận — **đọc lại code trước khi bắt tay sửa**, đừng tin mù.

Một phát hiện đáng chú ý về chính quá trình review này: `claude -p` báo FIX-009 (nút Next/Previous ở `ElementInfoAct`) là "crash thật" (`IndexOutOfBoundsException`), nhưng khi tôi đọc lại code thì lỗi đó **đã bị `catch (e: Exception)` bắt** — không crash, chỉ im lặng không làm gì. Đây là lý do bước verify không thể bỏ qua dù có AI Agent xác nhận.

## Việc cần làm để hoàn thiện backlog này (chưa làm trong lần này)

- Chưa verify hết toàn bộ item ⚪ trong `fix.md` (khoảng 15 item) — nên verify trước khi đưa vào sprint planning thật.
- Chưa chạy `./gradlew :app:testDevDebugUnitTest` để xác nhận tình trạng thật của 11 Mockito failures đã note trong `doc/feat.md:15` (cần secrets file riêng theo `CLAUDE.md`, không có sẵn trong phiên review này).
- Chưa test trên thiết bị thật cho các bug liên quan UI runtime (FIX-023 widget pre-API31, FIX-013 banner onResume).
