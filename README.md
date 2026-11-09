# DAMH English — Ứng dụng hỗ trợ người dùng học tiếng Anh

Đồ án môn **Lập trình di động (MOPR331279)**, HK1 2026–2027 — Trường Đại học Công nghệ Kỹ thuật TP.HCM.
Ứng dụng Android (Java, SQLite) chạy ngoại tuyến: học từ vựng theo chủ đề và bộ từ tự tạo bằng flashcard, ôn tập theo thuật toán Leitner, quiz, ngữ pháp, tra từ, import/export, luyện thi TOEIC/IELTS/Aptis, theo dõi tiến độ và nhắc học.

## Thành viên
| Thành viên | Phụ trách chính |
|---|---|
| Phan Anh Tuấn (nhóm trưởng) | Tài khoản, khung ứng dụng, nhắc học, đa ngôn ngữ, báo cáo |
| Nguyễn Đức Quang Vinh | UI/UX, Figma, trang chủ, tiến độ, tra từ, bộ từ vựng |
| Nguyễn Tấn Thành | CSDL SQLite, nạp dữ liệu, từ vựng, flashcard, import/export |
| Lê Văn Tấn Lực | Thuật toán Leitner, quiz, ngữ pháp, luyện thi, kiểm thử |

## Build nhanh
Yêu cầu: Android Studio bản ổn định mới nhất (JDK 17 trở lên), Android SDK Platform 37.
```bash
./gradlew assembleDebug          # APK: app/build/outputs/apk/debug/
./gradlew lint testDebugUnitTest # kiểm tra trước khi mở PR
```
Trên Windows dùng `gradlew.bat` (PowerShell: `.\gradlew`). Quy ước làm việc: [CONTRIBUTING.md](CONTRIBUTING.md).

## Tài liệu
- Quy ước làm việc: [CONTRIBUTING.md](CONTRIBUTING.md)
- Kiểm thử: `docs/testing/`
- Thiết kế giao diện: [Figma](https://www.figma.com/design/wOdb07dQkcBh18PKijh8Jx/Learning-English-Mobile-App)
"# LearningE_app" 
