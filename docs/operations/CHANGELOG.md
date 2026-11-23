# Changelog — lịch sử thay đổi phiên bản

Ghi theo [Keep a Changelog](https://keepachangelog.com/vi/1.1.0/): mỗi PR thêm một dòng vào mục **[Chưa phát hành]**; khi phát hành, đổi tên mục thành phiên bản + ngày (xem [DEPLOYMENT](DEPLOYMENT.md)).

## [Chưa phát hành]
- T-098: bộ luyện thi X03 Aptis (30 từ).
- T-011: dữ liệu bộ T05 Đồ ăn & thức uống, T06 Động vật, T07 Nhà cửa & đồ dùng, T08 Trường học (80 từ).
- T-004: `SeedLoader` nạp `assets/seed` trong một giao dịch, sửa tại chỗ theo `uid`; `languages.json`; unit test kiểm tra mọi file seed (thêm `org.json` ở phạm vi test).
- T-003: `DatabaseHelper` phiên bản 1 theo DATABASE-DESIGN phần A (cột đồng bộ, `sync_state`, `xp_events`, `leaderboard_cache`, `content_version`) với test trên SQLite thật.
- T-005: gói `core` (`ServiceLocator`, `AppExecutors`, `Result`, `ErrorCode`, `SessionStore`), `AuthActivity` + `MainActivity` 5 tab với Navigation + Safe Args, StrictMode bản debug.
- T-007: `LeitnerScheduler` (BR-010..014) và `StreakCalculator` (BR-031) với unit test.
- T-006: `TextNormalizer` chuẩn hóa chữ, bỏ dấu tiếng Việt và khóa tra từ theo ngôn ngữ (BR-053).
- T-002: theme Material 3 sáng với token màu v2, màu theo khu vực, kiểu chữ Baloo 2/Lexend/Noto Sans (kèm giấy phép OFL), 26 icon Lucide; bỏ `values-night` (dark mode là Won't).
- Nâng `minSdk` từ 24 lên 29: ứng dụng chạy trên Android 10 (API 29) trở lên. Bỏ core library desugaring (`java.time` có sẵn), băm mật khẩu dùng `PBKDF2WithHmacSHA256` của hệ thống (thông số không đổi). Vẫn không dùng UPSERT của SQLite vì cần API 30.
- Hướng dẫn mở, đồng bộ và chạy dự án trong Android Studio 2026.2 (Gradle JDK = JBR đi kèm, `JAVA_HOME` trỏ về `<Android Studio>\jbr` khi dùng dòng lệnh); cập nhật ma trận thiết bị kiểm thử (API 29, 33, 37) và mục xử lý sự cố.
- T-001: tạo khung project Android (Java 17, minSdk 24, compile/targetSdk 37, AGP 9.5.1, Gradle 9.6.0), version catalog, theme Material 3 sáng/tối với token màu, CI GitHub Actions, CONTRIBUTING.md.
- Đổi package thành `com.learninge.app` và tên ứng dụng thành LearningE.
- Thêm `docs/BLUEPRINT.md` (trang bắt đầu cho nhóm) và bộ tài liệu hệ thống `docs/system/` (13 tài liệu: yêu cầu, đặc tả, luồng, kiến trúc, dữ liệu, giao diện, kiểm thử, kế hoạch, quy ước).
- Chuẩn hóa bộ tài liệu dự án `docs/` theo 4 nhóm: project, engineering, quality, operations; chuyển CHANGELOG vào `docs/operations/`.
- Thiết kế giao diện v2: màu thương hiệu + màu theo khu vực tính năng, nền trang trí, 13 minh họa SVG, mockup đủ 32 màn hình (kể cả bộ từ, import/export, luyện thi) trong `docs/design/`.
