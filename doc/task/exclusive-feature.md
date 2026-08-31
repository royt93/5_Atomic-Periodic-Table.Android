# EXCLUSIVE FEATURE — Tính năng độc quyền / khác biệt hoá

Cả 5 nguồn đều đồng ý điểm khởi đầu: app đã có bộ công cụ **dày hơn hẳn** đa số app bảng tuần hoàn đối thủ trên Play Store (Calculator, Equation Balancer, Quiz, Unit setting, Compare, Solubility, Dictionary, PH, Nuclide, Electrode, Ion, Isotope) — nhưng các công cụ này rải rác, không ai "bán" nó như một USP. Dưới đây là hướng khác biệt hoá, xếp theo effort/rủi ro tăng dần.

---

### EXC-001 — "Chemistry Toolkit Suite" — đóng gói lại bộ công cụ đã có
- **Ý tưởng:** Gom 8+ công cụ hiện rải rác trong `act/table/` + `act/` (Electrode, PH Indicator, Ion, Nuclide, Dictionary, Equations, Isotope, Calculator, Solubility) vào 1 bottom-sheet "Toolkit" truy cập nhanh từ mọi màn hình, dùng chính làm USP marketing ("bộ công cụ hoá học đầy đủ nhất").
- **Ưu điểm:** Effort thấp nhất (0 tính năng mới, chỉ tái tổ chức UI/navigation) — có thể ship trong 1 sprint. Rủi ro kỹ thuật gần như 0 vì logic nghiệp vụ đã chạy ổn định.
- **Nhược điểm:** Không tạo giá trị mới thật sự cho user cũ, chỉ cải thiện discoverability — nếu user đã biết các màn hình này thì không thấy khác biệt.
- **Nguồn:** agent ideation, claude (2 nguồn độc lập đề xuất giống nhau)

### EXC-002 — Cam kết Zero-Permission / Offline-First
- **Ý tưởng:** Kiến trúc hiện tại vốn đã 100% local JSON + SharedPreferences, không billing thật, không tracking ngoài ads — biến điểm kỹ thuật có sẵn thành lời hứa sản phẩm rõ ràng trên store listing ("hoạt động hoàn toàn offline, không thu thập dữ liệu cá nhân").
- **Ưu điểm:** Effort ~0 về code (chỉ cần audit lại permission thật + viết marketing copy đúng sự thật). Định vị mạnh với phụ huynh/giáo viên quan tâm privacy cho học sinh.
- **Nhược điểm:** Cần audit kỹ trước khi tuyên bố — `allowBackup`/data extraction rule hiện bao gồm toàn bộ SharedPreferences (xem FIX-005), và ads SDK bên thứ 3 (AdMob/AppLovin) tự thu thập dữ liệu riêng ngoài tầm kiểm soát của app — tuyên bố "không thu thập dữ liệu" có thể sai lệch nếu không làm rõ ngoại lệ về ads. Rủi ro pháp lý/niềm tin nếu tuyên bố quá tay.
- **Nguồn:** agent ideation, claude

### EXC-003 — "Balance Under Pressure" — minigame cân bằng phương trình có tính giờ
- **Ý tưởng:** `ChemicalEquationBalancer.kt` (Gaussian elimination + `Fraction.kt`) đã là engine chính xác cho công cụ tra cứu tĩnh. Biến `EquationBalancerAct` thành minigame chấm điểm/đếm giờ, tái dùng animation/confetti đã có sẵn ở `QuizAct`/`ConfettiView`.
- **Ưu điểm:** Tái dùng ~80% hạ tầng đã có (engine cân bằng + animation Quiz), effort trung bình. Đối thủ hầu hết chỉ có bảng tra cứu tĩnh cho phần này — gamification quanh cân bằng phương trình gần như trống trên thị trường.
- **Nhược điểm:** Cần thiết kế độ khó/level progression hợp lý để không nhàm chán; UX minigame khác hẳn UX tra cứu, có thể cần A/B test giữ cả 2 chế độ (Practice vs Timed) thay vì thay thế hoàn toàn màn hình cũ.
- **Nguồn:** agent ideation

### EXC-004 — "Class Code" — chia sẻ VIP theo nhóm/lớp
- **Ý tưởng:** Cơ chế VIP hiện tại (`VipKeys.lookupDays`, `activateVipByKey`) vốn là redeem-code chứ không phải subscription thật, và SDK chỉ validate theo 1 secret cố định (`AdKeys.VIP_SECRET`). Cho phép user VIP tạo 1 mã dùng chung (giới hạn số lần) để chia sẻ cho bạn học/cả lớp — chỉ cần thêm UI generate mã + đếm lượt dùng trong `VipPrefs`, không cần hạ tầng backend mới.
- **Ưu điểm:** Tận dụng đúng đặc thù kỹ thuật hiện có (1 secret cố định) thành lợi thế thay vì giới hạn; mô hình "share VIP" kiểu peer-to-peer gần như chưa app bảng tuần hoàn nào trên Play Store làm; viral loop tự nhiên trong môi trường lớp học.
- **Nhược điểm:** **Rủi ro lớn nhất trong toàn bộ list này** — vì SDK chỉ có 1 secret cố định cho toàn app, một mã "share" thực chất là chia sẻ chính secret gốc; nếu implement sai cách (không giới hạn đúng số lượt/thời gian ở phía client) thì tương đương phát tán key VIP công khai, tự phá vỡ toàn bộ mô hình monetization. Cần thiết kế cẩn thận cơ chế giới hạn trước khi làm, có thể cần thay đổi phía SDK `AdManager` (ngoài tầm kiểm soát của repo này) chứ không chỉ code app.
- **Nguồn:** agent ideation

### EXC-005 — AR Electron Shell Viewer
- **Ý tưởng:** Dùng ARCore hiển thị mô hình 3D electron shell cho ~20 nguyên tố phổ biến nhất, dựa trên field `element_shells_electrons` đã có sẵn dạng text (vd. "K2 L6 M0...").
- **Ưu điểm:** Trải nghiệm "wow factor" rõ rệt, khác biệt mạnh so với đối thủ (đa số app bảng tuần hoàn chỉ có hình 2D tĩnh); dữ liệu nguồn đã có sẵn, không cần thu thập thêm.
- **Nhược điểm:** Effort cao nhất trong list — cần thêm dependency ARCore, testing trên nhiều thiết bị (không phải máy nào cũng hỗ trợ ARCore tốt), risk tăng đáng kể kích thước APK và bug bề mặt lớn (rendering 3D, permission camera). Không phù hợp làm trước khi các bug P0 ở `fix.md` được giải quyết.
- **Nguồn:** agent ideation, codex, agy (3 nguồn độc lập đề xuất hướng AR tương tự nhau)

---

## Quyết định (2026-08-31)

📋 **Picked** — kết hợp cả 3, theo thứ tự effort tăng dần để ship dần từng đợt thay vì chờ làm 1 lần:

1. **EXC-001 — Toolkit Suite** 📋 Picked (làm trước, effort thấp nhất, ship trong 1 sprint)
2. **EXC-003 — Balance Under Pressure** 📋 Picked (làm sau, tái dùng hạ tầng Quiz + engine cân bằng có sẵn)
3. **EXC-005 — AR Electron Shell Viewer** 📋 Picked (làm sau cùng, effort/rủi ro cao nhất — chỉ nên bắt đầu sau khi nền tảng đã ổn định, tức sau khi dọn xong P0/P1 trong `fix.md`)

❌ **Skipped** — **EXC-004 (Class Code / share VIP)**: rủi ro cao nhất trong toàn bộ list (SDK chỉ có 1 secret cố định cho toàn app, làm sai cách dễ thành phát tán key VIP công khai, tự phá vỡ monetization) — quyết định không làm hướng này.
