# Troubleshooting — xử lý sự cố thường gặp

**Cách dùng:** tìm theo triệu chứng. Chưa có trong danh sách thì tạo Issue theo [BUG-REPORT-TEMPLATE](../quality/BUG-REPORT-TEMPLATE.md); giải quyết xong thì bổ sung vào đây.

## 1. Môi trường và build
| Triệu chứng | Nguyên nhân | Cách xử lý |
|---|---|---|
| `ERROR: JAVA_HOME is set to an invalid directory` | Biến `JAVA_HOME` trỏ tới JDK đã gỡ | Sửa `JAVA_HOME` sang `<thư mục cài Android Studio>\jbr` (vd `D:\Program Files\Android\Android Studio\jbr`), mở terminal mới; hoặc build trong Android Studio |
| `SDK location not found` | Thiếu `local.properties` | Mở dự án bằng Android Studio để tự tạo, hoặc tạo tay: `sdk.dir=D\:/Android/Sdk` (dùng `/` hoặc `\\`) |
| `Failed to find target with hash string 'android-37'` | Chưa cài SDK Platform 37 | SDK Manager → cài Android SDK Platform 37, hoặc để Gradle tự tải sau khi chấp nhận license |
| `You have not accepted the license agreements` | Chưa chấp nhận license SDK | `sdkmanager --licenses` hoặc SDK Manager |
| Gradle Sync treo / tải chậm | Mạng, cache hỏng | File → Invalidate Caches; xóa `~/.gradle/caches` của phiên bản lỗi; thử lại |
| `Unsupported class file major version` | JDK quá mới/cũ so với Gradle | Dùng JBR của Android Studio (hoặc JDK 17 trở lên) cho Gradle (Settings → Build Tools → Gradle → Gradle JDK) |
| Gradle Sync báo phiên bản Android Gradle Plugin (AGP) không được hỗ trợ ("The project is using an incompatible version of the Android Gradle plugin" hoặc tương tự) | Android Studio cũ hơn dòng AGP 9.5 của dự án | Cập nhật Android Studio lên 2026.2 trở lên (Help → Check for Updates), rồi Sync lại; không hạ phiên bản AGP trong `libs.versions.toml` |
| Android Studio báo Gradle JDK không hợp lệ / `JAVA_HOME` trỏ tới JDK không còn tồn tại | Gradle JDK hoặc `JAVA_HOME` trỏ tới thư mục đã gỡ | Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK = `jbr` (hoặc `GRADLE_LOCAL_JAVA_HOME`); với terminal, đặt `JAVA_HOME` = `<thư mục cài Android Studio>\jbr` rồi mở terminal mới |
| Lint báo `HardcodedText` | Chuỗi viết cứng trong layout | Chuyển vào `strings.xml` |
| CI đỏ nhưng máy mình xanh | Khác JDK, quên commit tệp, khác hệ điều hành (hoa/thường tên tệp) | Xem log trên tab Actions; chạy `./gradlew clean lint testDebugUnitTest` lại; kiểm tra `git status` |

## 2. Chạy ứng dụng
| Triệu chứng | Nguyên nhân | Cách xử lý |
|---|---|---|
| Emulator không khởi động / rất chậm | Chưa bật ảo hóa | Bật VT-x/AMD-V trong BIOS; Windows bật *Windows Hypervisor Platform* |
| Màn hình chờ báo "Không thể khởi tạo dữ liệu" | Tệp dữ liệu nạp sẵn sai định dạng | Chạy unit test kiểm tra dữ liệu; sửa JSON theo [DATABASE-DESIGN](../engineering/DATABASE-DESIGN.md); gỡ app để tạo lại CSDL |
| Crash sau khi đổi lược đồ CSDL | Quên tăng `DATABASE_VERSION` / thiếu bước `onUpgrade` | Tăng phiên bản và viết bước nâng cấp; khi phát triển có thể gỡ app để tạo CSDL mới |
| Logcat báo StrictMode `DiskReadViolation` | Truy vấn trên luồng giao diện | Chuyển sang `AppExecutors` ([CODING-STANDARDS](../engineering/CODING-STANDARDS.md)) |
| Đăng nhập chậm trên máy yếu | PBKDF2 120.000 vòng | Băm đã chạy nền, hiện trạng thái đang xử lý; nếu > 1 s trên máy chuẩn, báo nhóm để cân nhắc giảm số vòng (ghi quyết định mới) |
| Quên mật khẩu tài khoản test | — | Dùng chức năng Quên mật khẩu, hoặc xóa dữ liệu app (Settings → Apps → LearningE → Storage → Clear) |

## 3. Tính năng cụ thể
| Triệu chứng | Nguyên nhân | Cách xử lý |
|---|---|---|
| Bấm loa không đọc / báo "Máy chưa có giọng đọc" | Máy thiếu gói giọng cho ngôn ngữ đó | Cài đặt → Hệ thống → Ngôn ngữ → Chuyển văn bản thành giọng nói → tải giọng tiếng Anh (hoặc Nhật, Trung, Hàn) |
| Nhắc học không hiện | Chưa cấp quyền thông báo; hôm đó đã đạt mục tiêu; Doze/tối ưu pin của hãng | Cấp quyền trong Cài đặt app; kiểm tra lịch bằng `adb shell dumpsys alarm`; tắt tối ưu pin cho LearningE (Xiaomi, Oppo, Samsung có mục riêng); thử đặt giờ nhắc +2 phút |
| Nhắc học mất sau khi khởi động lại máy | `BootReceiver` chưa đăng ký / chưa mở app lần nào sau cài | Mở app một lần sau khi cài; kiểm tra manifest có `RECEIVE_BOOT_COMPLETED` |
| Import báo "Sai định dạng" với tệp Excel | Lưu CSV dạng ANSI hoặc dùng dấu `;` | Excel: Save As → *CSV UTF-8 (Comma delimited)*; dòng đầu đúng `term,reading,meaning,example,example_translation` |
| Import mất dấu tiếng Việt | Tệp không phải UTF-8 | Lưu lại dạng UTF-8 (Notepad: Save As → Encoding UTF-8) |
| Export mở bằng Excel bị lỗi dấu | Excel cũ không đọc UTF-8 | Bản export đã kèm BOM; nếu vẫn lỗi dùng Data → From Text/CSV, chọn UTF-8 |
| Đồng hồ thi thử chạy sai sau khi đổi giờ máy | Đồng hồ tính theo giờ máy (quyết định đã biết) | Không đổi giờ máy khi làm bài; đây là giới hạn chấp nhận được với bài luyện tập |
| Chuỗi ngày học về 0 bất ngờ | Bỏ một ngày, hoặc giờ máy bị đổi | Kiểm tra bảng `study_log` bằng cách lấy CSDL ra ([BACKUP-RESTORE](BACKUP-RESTORE.md)) |

## 4. Git
| Triệu chứng | Cách xử lý |
|---|---|
| Bị từ chối push lên `main` | Đúng quy định: tạo nhánh, mở PR |
| Xung đột khi `pull --rebase` | Sửa xung đột trong tệp, `git add`, `git rebase --continue`; không chắc thì hỏi người sở hữu tệp |
| Lỡ commit `local.properties` / keystore | Báo nhóm trưởng ngay; xóa khỏi lịch sử trước khi push; nếu đã push keystore thì tạo keystore mới |
