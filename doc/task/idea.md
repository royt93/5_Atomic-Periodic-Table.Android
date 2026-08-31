# IDEA — Ý tưởng roadmap dài hạn

Chưa chắc làm ngay, đáng cân nhắc. Nguồn: agent ideation sản phẩm + codex + agy + claude. Status: 💭 Ideas.

## Học tập / Gamification

- **Flashcard & Spaced Repetition (SRS).** Nâng Quiz thành hệ thống ôn tập kiểu Anki/SuperMemo theo chủ đề: số oxi hoá, dãy điện hoá, tính tan, nhóm nguyên tố. *(agent ideation, codex, claude)* — nhắc lại nhiều nguồn nhất trong toàn bộ idea list, đáng ưu tiên xem xét trước.
- **Spaced-repetition reminder widget** — nhắc ôn nguyên tố từng làm sai trong Quiz, dùng `WorkManager` lên lịch định kỳ. *(agent ideation)*
- **Chế độ học theo giáo trình** — chọn lớp/quốc gia/syllabus, app tạo lộ trình bài học + quiz đúng phạm vi thay vì đưa toàn bộ dữ liệu 118 nguyên tố cùng lúc. *(codex)*
- **Chemical Reaction Predictor** — mở rộng equation balancer: nhập chất tham gia, app tự dự đoán sản phẩm + điều kiện phản ứng (nhiệt độ, xúc tác). *(claude)* — phức tạp, cần knowledge base phản ứng riêng, không chỉ cân bằng phương trình.
- **Teacher mode — export/import bộ câu hỏi tuỳ chỉnh**, chia sẻ qua file hoặc QR code trong lớp. *(agent ideation)*
- **Multiplayer quiz LAN qua Nearby Connections** — 2 học sinh thi trực tiếp không cần backend. *(agent ideation)*

## Trực quan hoá dữ liệu

- **Trend Lens / Interactive Trend Heatmap** — kéo slider thuộc tính (nhiệt độ, bán kính nguyên tử, độ âm điện...) để bảng tuần hoàn biến thành heatmap động theo thời gian thực, kèm biểu đồ xu hướng theo group/period. *(codex, claude)* — 2 nguồn độc lập đề xuất tương tự nhau.
- **Timeline khám phá nguyên tố tương tác** — vuốt/kéo theo dòng thời gian lịch sử hoá học (1670s → Oganesson 2016), dữ liệu `element_year`/`element_discovered_name` đã có sẵn trong JSON, chỉ cần build UI. *(agent ideation, agy, codex — 3 nguồn)*
- **Mô hình Electron 3D Orbitals** — hiển thị đám mây obitan s/p/d/f động (OpenGL ES/SceneView), xoay 360°. *(codex)*
- **AR electron shell / AR Periodic Table** — dùng ARCore hiển thị electron shell hoặc cấu trúc tinh thể mạng (fcc/bcc/hcp) nổi 3D; dữ liệu `element_shells_electrons` đã có sẵn dạng text. *(agent ideation, codex, agy — 3 nguồn, xem thêm bản EXCLUSIVE ở `exclusive-feature.md`)*

## Hạ tầng / tích hợp hệ thống

- **App Actions / voice search** — tích hợp Google Assistant, hỏi "nguyên tố số 26 là gì" mở thẳng `ElementInfoAct` của Iron. *(agent ideation)*
- **Baseline Profile / Macrobenchmark cho cold-start** — đo và tối ưu thời gian khởi động có số liệu. *(claude)* — liên quan trực tiếp TASK-025.
- **Remote Config cho ad placement / xoay VIP secret** — không cần ship APK mới khi cần đổi cấu hình ads hoặc xoay secret VIP định kỳ (giảm rủi ro ENH-010). *(claude)*
- **Thay Picasso cũ (2.71828) bằng Coil** — thư viện ảnh hiện đại hơn, Kotlin-first, giải quyết một phần rủi ro ở FIX-016 (client instance quản lý tốt hơn). *(claude)*
- **Tách state holder cho Activity phức tạp** (`QuizAct`, `VipManagementAct`) để dễ unit test hơn mà không cần đổi toàn bộ kiến trúc sang MVVM. *(claude)*
- **`OrderAct` thành tính năng thật** — tuỳ biến thứ tự property hiển thị trên Favorite bar, có persist. *(claude)* — trùng với TASK-018 (hoàn thiện thay vì gỡ).
