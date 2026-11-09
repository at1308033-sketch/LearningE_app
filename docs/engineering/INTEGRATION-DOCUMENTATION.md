# Integration Documentation — tích hợp dịch vụ bên thứ ba

**Mục đích:** liệt kê mọi thành phần bên ngoài mã nguồn của nhóm mà LearningE dùng tới, cách tích hợp, quyền cần có, cách xử lý khi lỗi và cách kiểm thử.
**Nguyên tắc:** ứng dụng chạy ngoại tuyến 100%, **không gọi API mạng nào**, không khai báo quyền `INTERNET`, không có SDK phân tích/quảng cáo. Mọi tích hợp đều là dịch vụ có sẵn của Android hoặc công cụ phát triển.

## 1. Danh sách tích hợp
| # | Thành phần | Loại | Dùng cho | Quyền / cấu hình |
|---|---|---|---|---|
| 1 | Android TextToSpeech | Dịch vụ hệ điều hành | Phát âm từ, câu ví dụ, câu hỏi dạng nghe | Không cần quyền |
| 2 | AlarmManager + NotificationManager | Dịch vụ hệ điều hành | Nhắc học hằng ngày | `POST_NOTIFICATIONS` (Android 13+), `RECEIVE_BOOT_COMPLETED` |
| 3 | Storage Access Framework (SAF) | Dịch vụ hệ điều hành | Chọn tệp import, chọn nơi lưu export | Không cần quyền bộ nhớ |
| 4 | Trình duyệt mặc định (Intent `ACTION_VIEW`) | Ứng dụng khác trên máy | Mở trang chính thức TOEIC/IELTS/Aptis | Không cần quyền; khai báo `<queries>` cho Android 11+ |
| 5 | Material Components, AndroidX | Thư viện | Giao diện, ViewModel, Navigation | Gradle (version catalog) |
| 6 | Phông Baloo 2, Lexend, Noto Sans | Tài nguyên (SIL OFL 1.1) | Chữ giao diện, phiên âm | `res/font/` |
| 7 | Nguồn dữ liệu mở (Tatoeba, Wiktionary) | Nội dung | Câu ví dụ, tham khảo nghĩa khi soạn dữ liệu | Ghi nguồn trong tệp dữ liệu và màn hình Giới thiệu |
| 8 | GitHub + GitHub Actions | Công cụ phát triển | Mã nguồn, CI build/lint/test | `.github/workflows/ci.yml` |
| 9 | Figma | Công cụ thiết kế | Thiết kế giao diện | Liên kết trong UI-UX-SPECIFICATION |

## 2. Android TextToSpeech
- **Lớp bao:** `ui.common.TtsHelper` — khởi tạo khi Fragment hiển thị, giải phóng ở `onDestroyView` (`tts.shutdown()`).
- **Chọn giọng:** `setLanguage(language.ttsLocale)` lấy từ bảng `languages` (en-US, ja-JP, zh-CN, ko-KR).
- **Kiểm tra sẵn sàng:** kết quả `setLanguage` là `LANG_MISSING_DATA` hoặc `LANG_NOT_SUPPORTED` → trả `TTS_UNAVAILABLE`, hiện thông báo + nút mở `TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA`; quiz không sinh câu dạng nghe.
- **Khởi tạo chậm:** yêu cầu đọc trong lúc chưa `onInit` được xếp hàng tối đa 3 giây.
- **Kiểm thử:** emulator API 24 không có gói giọng (AC-010.2, AC-013.3); máy thật có giọng.

## 3. Nhắc học: AlarmManager + thông báo
- **Đặt lịch:** `ReminderService.schedule(minuteOfDay)` dùng `AlarmManager.setWindow(RTC_WAKEUP, triggerAt, 15 phút, pendingIntent)` — không cần quyền báo thức chính xác.
- **Khi đến giờ:** `ReminderReceiver` (`exported=false`) gọi `goAsync()`, kiểm tra người dùng đang đăng nhập và `hasMetGoalToday()`; chưa đạt mục tiêu thì hiện thông báo kênh "Nhắc học"; luôn đặt lịch cho ngày hôm sau.
- **Khởi động lại máy / đổi giờ:** `BootReceiver` nhận `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED` và đặt lại lịch.
- **Quyền:** Android 13+ xin `POST_NOTIFICATIONS` khi người dùng bật nhắc học; bị từ chối → lưu nhắc học = tắt, hướng dẫn bật lại.
- **PendingIntent:** luôn `FLAG_IMMUTABLE`.
- **Giới hạn đã biết:** chế độ tiết kiệm pin (Doze) và một số hãng điện thoại có thể trễ thông báo; xử lý ở [TROUBLESHOOTING](../operations/TROUBLESHOOTING.md).

## 4. Storage Access Framework (import/export)
- **Import:** `registerForActivityResult(new ActivityResultContracts.OpenDocument(), …)` với loại `text/csv`, `text/comma-separated-values`, `text/plain`, `application/json`; đọc bằng `getContentResolver().openInputStream(uri)` trên luồng nền; giới hạn 1 MB / 2000 dòng.
- **Export:** `ActivityResultContracts.CreateDocument("text/csv")` hoặc `("application/json")`, tên gợi ý `<tên bộ>_<yyyyMMdd>.csv`; ghi bằng `openOutputStream(uri)`; lỗi giữa chừng → `DocumentsContract.deleteDocument` để không để lại tệp dở dang.
- **Định dạng:** [DATABASE-DESIGN](DATABASE-DESIGN.md) mục định dạng dữ liệu; mã lỗi `FILE_TOO_LARGE`, `FILE_FORMAT`, `NO_VALID_ROWS`, `IO` ở [API-SPECIFICATION](API-SPECIFICATION.md).

## 5. Mở trang tài nguyên luyện thi
- URL cố định trong `strings.xml` (trang chính thức của ETS cho TOEIC, IELTS, British Council cho Aptis); nhóm kiểm tra lại URL còn hoạt động trước khi nộp.
- Mở bằng `Intent(Intent.ACTION_VIEW, Uri.parse(url))`; kiểm tra `resolveActivity` trước; không có trình duyệt → "Không tìm thấy trình duyệt để mở liên kết".
- Android 11+ cần khai báo trong manifest:
```xml
<queries>
    <intent>
        <action android:name="android.intent.action.VIEW" />
        <data android:scheme="https" />
    </intent>
</queries>
```
- App không khai báo `INTERNET`; trình duyệt tự tải trang.

## 6. Thư viện và tài nguyên
| Thư viện / tài nguyên | Phiên bản | Giấy phép | Ghi chú |
|---|---|---|---|
| AndroidX AppCompat | 1.8.0 | Apache 2.0 | |
| Material Components for Android | 1.14.0 | Apache 2.0 | Theme `Theme.Material3.DayNight` |
| AndroidX ConstraintLayout | 2.2.2 | Apache 2.0 | |
| AndroidX Lifecycle, Navigation | theo version catalog khi thêm (T-005) | Apache 2.0 | |
| JUnit 4, AndroidX Test, Espresso | 4.13.2 / 1.3.0 / 3.7.0 | EPL 1.0 / Apache 2.0 | Chỉ dùng để test |
| Baloo 2, Lexend, Noto Sans | — | SIL OFL 1.1 | Đóng gói trong `res/font/` |
| Icon nét kiểu Lucide | — | ISC | Lấy SVG gốc, nhập bằng Vector Asset |
Phiên bản chính xác luôn nằm ở `gradle/libs.versions.toml`.

## 7. GitHub Actions (CI)
- Tệp `.github/workflows/ci.yml`: chạy khi push và PR vào `main`; JDK 17 Temurin; `./gradlew lint testDebugUnitTest assembleDebug`; tải báo cáo lint/test và APK debug làm artifact.
- Instrumented test (cần emulator) chạy trên máy người kiểm thử trước mỗi mốc, không chạy trên CI.
- Không lưu secret nào trong CI; ký bản release làm trên máy nhóm trưởng ([DEPLOYMENT](../operations/DEPLOYMENT.md)).
