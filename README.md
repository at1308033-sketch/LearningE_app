# LearningE — Ứng dụng hỗ trợ người dùng học tiếng Anh

Đồ án môn **Lập trình di động (MOPR331279)**, HK1 2026–2027 — Trường Đại học Công nghệ Kỹ thuật TP.HCM.
Ứng dụng Android (Java, SQLite) chạy ngoại tuyến: học từ vựng theo chủ đề và bộ từ tự tạo bằng flashcard, ôn tập theo thuật toán Leitner, quiz, ngữ pháp, tra từ, import/export, luyện thi TOEIC/IELTS/Aptis, theo dõi tiến độ và nhắc học.

## Thành viên
- Phan Anh Tuấn (nhóm trưởng)
- Nguyễn Đức Quang Vinh
- Nguyễn Tấn Thành
- Lê Văn Tấn Lực

## Build nhanh
Yêu cầu: Android Studio bản ổn định mới nhất (JDK 17 trở lên), Android SDK Platform 37.
```bash
./gradlew assembleDebug          # APK: app/build/outputs/apk/debug/
./gradlew lint testDebugUnitTest # kiểm tra trước khi mở PR
```
Trên Windows dùng `gradlew.bat` (PowerShell: `.\gradlew`). Quy ước làm việc: [CONTRIBUTING.md](CONTRIBUTING.md).

## Tài liệu
- **Danh mục tài liệu:** [docs/README.md](docs/README.md) — PRD, SRS, use case, quy tắc nghiệp vụ, thiết kế hệ thống, CSDL, API, quy chuẩn code, UI/UX, kiểm thử, cài đặt, phát hành, vận hành.
- **Bắt đầu tại:** [SYSTEM-BLUEPRINT](docs/engineering/SYSTEM-BLUEPRINT.md) · Quy ước: [CONTRIBUTING.md](CONTRIBUTING.md) · Lịch sử thay đổi: [CHANGELOG](docs/operations/CHANGELOG.md)
- Thiết kế giao diện: [Figma v2](https://www.figma.com/design/exLWP8ZETekA11XOaD3K25/V2_App_English_Learning) · mockup gốc: [docs/design/](docs/design/)
