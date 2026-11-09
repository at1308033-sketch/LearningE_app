# Environment — biến môi trường và cấu hình

**Mục đích:** liệt kê mọi biến môi trường, tệp cấu hình và cờ build của dự án, giá trị mẫu và tệp nào được/không được commit.
LearningE không có máy chủ nên **không có biến môi trường lúc chạy** (không API key, không URL máy chủ); cấu hình chỉ ở phía build.

## 1. Biến môi trường trên máy phát triển
| Biến | Bắt buộc | Ví dụ | Dùng cho |
|---|---|---|---|
| `JAVA_HOME` | Khi chạy `gradlew` từ terminal | `C:\Program Files\Android\Android Studio\jbr` | JDK 17+ để chạy Gradle |
| `ANDROID_HOME` | Không (đã có `sdk.dir`) | `D:\Android\Sdk` | Công cụ dòng lệnh (`adb`, `sdkmanager`); phải trỏ tới thư mục tồn tại |
| `PATH` | Khuyên dùng | thêm `%ANDROID_HOME%\platform-tools` | Gọi `adb` từ terminal |

## 2. Tệp cấu hình
| Tệp | Commit? | Nội dung |
|---|---|---|
| `local.properties` | **Không** | `sdk.dir=<đường dẫn SDK>` — Android Studio tự tạo |
| `keystore.properties` | **Không** | Thông tin ký bản release (mục 3) |
| `gradle.properties` | Có | `org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8`, `org.gradle.caching=true`, `android.useAndroidX=true`, `android.nonTransitiveRClass=true` |
| `gradle/libs.versions.toml` | Có | Phiên bản AGP và thư viện (một nơi duy nhất) |
| `gradle/wrapper/gradle-wrapper.properties` | Có | Phiên bản Gradle (9.6.0) |
| `app/build.gradle.kts` | Có | `compileSdk`/`targetSdk` 37, `minSdk` 24, `versionCode`, `versionName`, build types |

## 3. `keystore.properties` (chỉ máy nhóm trưởng)
```properties
storeFile=../learninge-release.jks   # đường dẫn tới keystore, nằm NGOÀI repo
storePassword=********
keyAlias=learninge
keyPassword=********
```
Không gửi mật khẩu keystore qua nhóm chat công khai; không đưa lên CI.

## 4. Build type và cờ
| Build type | Đặc điểm | Dùng khi |
|---|---|---|
| `debug` | `BuildConfig.DEBUG = true`, bật StrictMode, đo độ phủ test, ký bằng khóa debug | Phát triển, kiểm thử |
| `release` | R8 rút gọn mã + tài nguyên (`isMinifyEnabled`, `isShrinkResources`), ký bằng keystore nhóm | Nghiệm thu, nộp bài |

| Hằng số / cấu hình trong app | Giá trị | Ở đâu |
|---|---|---|
| `applicationId` | `com.learninge.app` | `app/build.gradle.kts` |
| Tên CSDL | `learninge.db` | `DatabaseHelper` |
| Phiên bản CSDL | Bắt đầu 1 | `DatabaseHelper.DATABASE_VERSION` |
| Số vòng PBKDF2 | 120.000 | `PasswordHasher` |
| Giới hạn import | 1 MB, 2000 dòng | `ImportExportService` |
| Thẻ ôn tối đa mỗi phiên | 50 | `StudyService` |
| Cửa sổ nhắc học | 15 phút | `ReminderService` |
| Tag log | `LearningE` (+ tên lớp) | [MONITORING](MONITORING.md) |

## 5. Môi trường kiểm thử
| Môi trường | Cấu hình |
|---|---|
| Emulator chính | Pixel 6, API 34, tiếng Việt |
| Emulator minSdk | Nexus 5, API 24, không cài giọng đọc tiếng Anh |
| Máy thật | Android ≥ 10 của một thành viên |
| CI | GitHub Actions, Ubuntu, JDK 17 Temurin |
Tài khoản test: [test-accounts.md](../quality/test-accounts.md).
