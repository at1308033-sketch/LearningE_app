# Changelog — lịch sử thay đổi phiên bản

Ghi theo [Keep a Changelog](https://keepachangelog.com/vi/1.1.0/): mỗi PR thêm một dòng vào mục **[Chưa phát hành]**; khi phát hành, đổi tên mục thành phiên bản + ngày (xem [DEPLOYMENT](DEPLOYMENT.md)).

## [Chưa phát hành]
- T-001: tạo khung project Android (Java 17, minSdk 24, compile/targetSdk 37, AGP 9.5.1, Gradle 9.6.0), version catalog, theme Material 3 sáng/tối với token màu, CI GitHub Actions, CONTRIBUTING.md.
- Đổi package thành `com.learninge.app` và tên ứng dụng thành LearningE.
- Thêm `docs/BLUEPRINT.md` (trang bắt đầu cho nhóm) và bộ tài liệu hệ thống `docs/system/` (13 tài liệu: yêu cầu, đặc tả, luồng, kiến trúc, dữ liệu, giao diện, kiểm thử, kế hoạch, quy ước).
- Chuẩn hóa bộ tài liệu dự án `docs/` theo 4 nhóm: project, engineering, quality, operations; chuyển CHANGELOG vào `docs/operations/`.
- Thiết kế giao diện v2: màu thương hiệu + màu theo khu vực tính năng, nền trang trí, 13 minh họa SVG, mockup đủ 32 màn hình (kể cả bộ từ, import/export, luyện thi) trong `docs/design/`.
