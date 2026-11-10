# Installation — cài đặt môi trường phát triển

**Mục đích:** từ máy mới đến build và chạy được LearningE trên emulator trong khoảng 30–60 phút. Cả nhóm dùng cùng phiên bản công cụ để tránh lỗi "máy tôi chạy được".

## 1. Yêu cầu
| Thành phần | Phiên bản | Ghi chú |
|---|---|---|
| Hệ điều hành | Windows 10/11, macOS hoặc Linux 64-bit | RAM ≥ 8 GB (khuyên 16 GB để chạy emulator) |
| Android Studio | 2026.2.x trở lên (đi kèm Android Gradle Plugin 9.5, cùng dòng với bản của dự án) | Bản cũ hơn sẽ báo lỗi không hỗ trợ phiên bản plugin khi Sync — xem [TROUBLESHOOTING](TROUBLESHOOTING.md) |
| JDK | JBR đi kèm Android Studio | Gradle JDK = `jbr` (Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK); mã nguồn vẫn là Java 17 |
| Android SDK Platform | 37 (compileSdk/targetSdk) | Gradle tự tải nếu đã chấp nhận license |
| Android SDK Build-Tools | Theo Android Studio đề xuất | |
| Git | 2.40+ | |
| Thiết bị chạy thử | Android 10 (API 29) trở lên; emulator Pixel 6 API 33 (chính), emulator API 29 (tối thiểu), API 37 (mới nhất) | Tạo trong Device Manager; máy thật cần Android 10 trở lên |

## 2. Các bước
1. **Cài Android Studio**, mở lần đầu, chọn *Standard setup* để cài SDK, emulator, platform-tools. Ghi lại đường dẫn SDK (Settings → Languages & Frameworks → Android SDK).
2. **Chấp nhận license SDK:** Settings → Android SDK → SDK Tools, hoặc chạy `sdkmanager --licenses`.
3. **Đặt `JAVA_HOME`** (chỉ cần khi chạy Gradle từ terminal; Android Studio tự dùng JBR của nó): trỏ về thư mục `jbr` trong thư mục cài Android Studio để dòng lệnh và Android Studio dùng cùng một JDK.
   - Windows: System Properties → Environment Variables → `JAVA_HOME` = `<thư mục cài Android Studio>\jbr` (vd `D:\Program Files\Android\Android Studio\jbr`). Thư mục phải tồn tại — xem [TROUBLESHOOTING](TROUBLESHOOTING.md) nếu báo "JAVA_HOME is set to an invalid directory".
   - macOS/Linux: `export JAVA_HOME=…` trong `~/.zshrc`/`~/.bashrc`.
4. **Lấy mã nguồn:**
```bash
git clone https://github.com/at1308033-sketch/LearningE_app.git
```
5. **Mở dự án:** Android Studio → File → Open → chọn thư mục `LearningE_app` → *Trust Project* → chờ Gradle Sync xong. Không cần sửa cấu hình nào: Android Studio tự tạo `local.properties` chứa `sdk.dir` (không commit tệp này) và dùng JBR làm Gradle JDK. Nếu Sync báo lỗi JDK, kiểm tra Gradle JDK = `jbr` như ở mục 1.
6. **Tạo emulator:** Device Manager → Create device → Pixel 6 → system image API 33 (Google APIs). Tạo thêm một emulator API 29 (Android 10) để kiểm thử mức tối thiểu và một emulator API 37 cho bản mới nhất.
7. **Chạy app:** chọn emulator (API 29 trở lên) → chọn cấu hình chạy `app` → Run ▶. Hoặc dòng lệnh:
```bash
./gradlew installDebug
```
8. **Kiểm tra môi trường đúng:**
```bash
./gradlew lint testDebugUnitTest assembleDebug
```
Kết quả `BUILD SUCCESSFUL` là môi trường đã sẵn sàng. Trên Windows dùng `gradlew.bat` (PowerShell: `.\gradlew`).

## 3. Công cụ hỗ trợ (khuyên dùng)
| Công cụ | Dùng cho |
|---|---|
| DB Browser for SQLite | Mở `learninge.db` lấy từ emulator để kiểm tra dữ liệu ([BACKUP-RESTORE](BACKUP-RESTORE.md)) |
| Accessibility Scanner (trên emulator) | Kiểm tra vùng chạm, tương phản |
| draw.io | Vẽ use case, class diagram, ERD cho báo cáo |
| Figma (tài khoản miễn phí) | Xem thiết kế, lấy kích thước và màu |

## 4. Cài app lên điện thoại thật
- Bật *Tùy chọn nhà phát triển* → *Gỡ lỗi USB*, cắm cáp, chấp nhận khóa RSA, chọn máy trong Android Studio → Run.
- Hoặc cài APK: chép `LearningE-v<phiên bản>.apk` vào máy, mở và cho phép cài từ nguồn không xác định; hoặc `adb install -r LearningE-v<phiên bản>.apk`.
