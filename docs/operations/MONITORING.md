# Monitoring — logging, theo dõi và cảnh báo

**Mục đích:** LearningE chạy ngoại tuyến, không có máy chủ và **không gửi dữ liệu ra ngoài**, nên không dùng dịch vụ crash reporting hay analytics trực tuyến. Việc theo dõi chất lượng dựa vào log trên máy, công cụ của Android Studio, CI và quy trình báo lỗi.

## 1. Quy tắc ghi log
| Mức (`android.util.Log`) | Dùng khi | Ví dụ |
|---|---|---|
| `Log.e` | Lỗi làm hỏng thao tác của người dùng | `SQLiteException` khi lưu tiến độ; nạp dữ liệu thất bại |
| `Log.w` | Tình huống bất thường nhưng app tự xử lý | Thiếu giọng đọc TTS; tệp import có dòng lỗi |
| `Log.i` | Mốc quan trọng | Nạp dữ liệu xong (số bản ghi, thời gian); đặt lịch nhắc học |
| `Log.d` | Chi tiết khi phát triển | Thời gian băm mật khẩu; số thẻ đến hạn |
- **Tag:** `LearningE/<TênLớp>` (vd `LearningE/StudyService`) để lọc trong Logcat bằng `tag:LearningE`.
- **Không bao giờ ghi:** mật khẩu, câu trả lời bảo mật, tên đăng nhập, nội dung tệp import, mã phiên.
- Bản release: R8 có thể bỏ `Log.d`/`Log.v`; chỉ giữ `Log.w`/`Log.e`.

## 2. Công cụ theo dõi khi phát triển và kiểm thử
| Công cụ | Theo dõi gì | Ngưỡng (Yêu cầu phi chức năng) |
|---|---|---|
| **StrictMode** (bật sẵn ở bản debug trong `LearningEApp`) | Đọc/ghi đĩa trên luồng giao diện | 0 vi phạm trong Logcat khi chạy kịch bản |
| **Logcat** | Lỗi, cảnh báo, crash (stack trace `FATAL EXCEPTION`) | 0 crash/ANR khi nghiệm thu |
| **Android Studio Profiler** | CPU, bộ nhớ, khung hình khi cuộn danh sách và lật thẻ | ≤ 5% khung hình chậm |
| `adb shell am start -W com.learninge.app/.ui.main.MainActivity` | Thời gian khởi động | ≤ 2 s (lần đầu có nạp dữ liệu ≤ 5 s) |
| `adb shell dumpsys gfxinfo com.learninge.app` | Thống kê khung hình | ≤ 5% jank |
| `adb shell dumpsys alarm \| findstr learninge` (Windows) / `grep` | Lịch nhắc học đã đặt chưa | Có một báo thức cho ngày kế tiếp |
| **Accessibility Scanner** | Vùng chạm, tương phản, nhãn | 0 lỗi ở 5 màn hình chính |
| **Báo cáo Lint / JaCoCo** (`app/build/reports/`) | Lỗi tĩnh, độ phủ test | Lint 0 lỗi; `domain.rules` ≥ 80% |

## 3. Theo dõi trên CI (GitHub Actions)
- Mỗi push/PR vào `main` chạy lint + unit test + build; trạng thái đỏ hiện trên PR và gửi email cho người push.
- Báo cáo lint/test và APK debug được lưu thành artifact của lần chạy (tab Actions → lần chạy → Artifacts).
- **Cảnh báo:** CI đỏ trên `main` là ưu tiên số 1 — người gây lỗi sửa hoặc revert trong ngày.

## 4. Cảnh báo và phản ứng
| Dấu hiệu | Ai phát hiện | Phản ứng |
|---|---|---|
| CI đỏ trên `main` | Người push (email GitHub) | Sửa/revert trong ngày |
| Crash khi kiểm thử | Người kiểm thử | Tạo Issue theo [BUG-REPORT-TEMPLATE](../quality/BUG-REPORT-TEMPLATE.md), mức Nghiêm trọng |
| StrictMode báo vi phạm | Người viết code | Chuyển truy vấn sang `AppExecutors` trước khi mở PR |
| Nhắc học không hiện | Người kiểm thử | Xem [TROUBLESHOOTING](TROUBLESHOOTING.md) mục nhắc học |
| Khởi động/tra từ chậm hơn ngưỡng | Người kiểm thử | Profiler; xem index trong [DATABASE-DESIGN](../engineering/DATABASE-DESIGN.md) |

## 5. Thu thập log khi người dùng báo lỗi (bản debug)
```bash
adb logcat -c                      # xóa log cũ
# tái hiện lỗi trên máy
adb logcat -d -s "LearningE/*:V" "AndroidRuntime:E" > bug.log
```
Rà `bug.log` để xóa thông tin cá nhân trước khi đính kèm vào Issue.
