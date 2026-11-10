# Release Checklist — kiểm tra trước khi phát hành / nộp bài

**Mục đích:** danh sách bắt buộc trước khi tạo bản phát hành (mốc nghiệm thu M5) và trước khi nộp đồ án (M6). Người phát hành đánh dấu từng mục và dán kết quả vào PR phát hành hoặc biên bản bàn giao.

## 1. Mã nguồn và build
- [ ] `main` đã merge mọi PR của các task P0; không còn nhánh tính năng đang mở cần cho bản phát hành.
- [ ] CI trên commit phát hành xanh (`lint`, `testDebugUnitTest`, `assembleDebug`).
- [ ] `./gradlew connectedDebugAndroidTest` pass trên emulator API 33.
- [ ] Độ phủ nhánh `domain.rules` ≥ 80% (báo cáo JaCoCo).
- [ ] Android Lint: 0 lỗi; không còn `HardcodedText`.
- [ ] Tăng `versionCode` và `versionName` trong `app/build.gradle.kts` (xem [DEPLOYMENT](../operations/DEPLOYMENT.md)).
- [ ] [CHANGELOG](../operations/CHANGELOG.md) có mục cho phiên bản này.

## 2. Chất lượng (theo [TESTING-STRATEGY](TESTING-STRATEGY.md))
- [ ] 100% test case ưu tiên Cao đạt; ≥ 90% ưu tiên Trung bình đạt ([TEST-CASES](TEST-CASES.md) đã cập nhật cột Trạng thái).
- [ ] 0 lỗi mức Nghiêm trọng/Cao đang mở; lỗi còn lại đã ghi vào "Lỗi đã biết" trong [HANDOVER](../operations/HANDOVER.md).
- [ ] 0 crash/ANR khi chạy toàn bộ kịch bản nghiệm thu trên 4 cấu hình: emulator API 29, emulator API 33, emulator API 37, 1 máy thật.
- [ ] Chạy kịch bản ở chế độ máy bay: mọi chức năng hoạt động.
- [ ] Theme tối, xoay màn hình, cỡ chữ 200% không vỡ giao diện ở các màn hình chính.
- [ ] Nhắc học hiện đúng giờ (±15 phút) và vẫn chạy sau khởi động lại máy.
- [ ] Import CSV 2000 dòng ≤ 3 giây; export rồi import lại cho đúng số từ.

## 3. Bảo mật và quyền riêng tư
- [ ] Manifest không có quyền `INTERNET`; `allowBackup="false"`; chỉ Activity khởi chạy và `BootReceiver` có `exported="true"`.
- [ ] Mở tệp `learninge.db` bằng DB Browser for SQLite: không có mật khẩu hoặc câu trả lời bảo mật dạng rõ.
- [ ] Logcat khi chạy kịch bản không có mật khẩu, tên đăng nhập, dữ liệu cá nhân.
- [ ] Không có keystore, `local.properties`, mật khẩu trong repo (`git ls-files` kiểm tra).
- [ ] Nội dung học có ghi nguồn; không có dữ liệu chép từ ứng dụng khác hoặc đề thi chính thức.

## 4. Bản APK
- [ ] `./gradlew assembleRelease` với keystore của nhóm; APK đã ký (`apksigner verify`).
- [ ] APK ≤ 30 MB; cài đè lên bản cũ không mất dữ liệu (nâng phiên bản CSDL chạy đúng).
- [ ] Cài APK release lên máy thật, chạy luồng chính một lần: đăng ký → thiết lập → học → ôn → quiz → bộ từ → import/export → thi thử → tiến độ.
- [ ] Đặt tên tệp `LearningE-v<versionName>.apk`.

## 5. Hồ sơ nộp đồ án (M6)
- [ ] File Word báo cáo đủ 7 chương + phụ lục + tài liệu tham khảo, ảnh chụp app thật (không còn chỗ "[CẦN CẬP NHẬT]").
- [ ] "File báo cáo kết quả dự án" điền đủ: mã nhóm, lớp, link GitHub, % đóng góp, **file + tên hàm minh chứng** cho từng tính năng, tự chấm rubric.
- [ ] Sơ đồ use case, class diagram, ERD (tệp `.drawio` + ảnh PNG).
- [ ] Toàn bộ mã nguồn (không gồm `build/`, `.gradle/`, `local.properties`).
- [ ] APK release.
- [ ] Nén **một** tệp đúng tên `GroupXX_MSSV1_MSSV2_MSSV3_MSSV4.zip` (sai tên không được chấm).
- [ ] Mỗi thành viên đã tập giải thích luồng code mình phụ trách và sửa nhanh tham số/thêm log rồi chạy lại.
