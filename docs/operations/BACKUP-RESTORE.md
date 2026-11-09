# Backup & Restore — sao lưu và khôi phục dữ liệu

**Mục đích:** những gì cần sao lưu, ai làm, và cách khôi phục — cho dữ liệu người dùng trong app, dữ liệu kiểm thử và tài sản của dự án.

## 1. Dữ liệu người dùng trong app
- Dữ liệu nằm trong CSDL riêng của app `learninge.db` trên máy; **không có sao lưu đám mây** (`allowBackup="false"`, `dataExtractionRules` loại trừ mọi miền) vì CSDL chứa hash mật khẩu.
- **Cách người dùng tự sao lưu:** mở một bộ từ → *Export* → CSV hoặc JSON → lưu vào bộ nhớ máy/Drive. Chỉ xuất nội dung từ, **không xuất tiến độ học hay tài khoản** (BR-051).
- **Khôi phục:** *Bộ của tôi* → *Import* → chọn tệp đã xuất → xem trước → nhập vào bộ mới hoặc bộ có sẵn. Tiến độ học bắt đầu lại từ trạng thái Mới.
- Gỡ ứng dụng hoặc xóa dữ liệu app = mất toàn bộ dữ liệu cục bộ; màn hình Giới thiệu và hướng dẫn sử dụng nêu rõ điều này.

## 2. Lấy và khôi phục CSDL khi phát triển (chỉ bản debug)
Dùng để kiểm tra dữ liệu, tái hiện lỗi hoặc chuẩn bị dữ liệu demo.
```bash
# sao CSDL từ emulator ra máy tính
adb exec-out run-as com.learninge.app cat databases/learninge.db > learninge.db
# xem bằng DB Browser for SQLite, hoặc:
sqlite3 learninge.db ".tables"

# đưa CSDL đã chuẩn bị (vd dữ liệu demo) trở lại emulator — đóng app trước
adb shell am force-stop com.learninge.app
adb push learninge.db /data/local/tmp/learninge.db
adb shell run-as com.learninge.app cp /data/local/tmp/learninge.db databases/learninge.db
```
- `run-as` chỉ hoạt động với bản **debug**; bản release không cho đọc CSDL từ ngoài (đúng yêu cầu bảo mật).
- Không commit tệp `*.db` (đã có trong `.gitignore`); không chia sẻ CSDL có tài khoản thật.

## 3. Dữ liệu demo cho buổi bảo vệ
1. Trên emulator demo, đăng ký tài khoản demo, học vài phiên, làm quiz và thi thử để có số liệu tiến độ, chuỗi ngày.
2. Sao CSDL ra (`learninge-demo.db`) bằng lệnh mục 2 và lưu kèm hồ sơ nhóm.
3. Trước buổi demo, khôi phục lại tệp này nếu dữ liệu bị thay đổi.

## 4. Tài sản dự án cần sao lưu
| Tài sản | Nơi lưu chính | Sao lưu | Người chịu trách nhiệm |
|---|---|---|---|
| Mã nguồn, tài liệu | GitHub `LearningE_app` | Mỗi thành viên có bản clone; tag mỗi bản phát hành | Cả nhóm |
| Keystore release + mật khẩu | Máy nhóm trưởng (ngoài repo) | USB + ổ đám mây cá nhân (2 nơi) | Nhóm trưởng |
| Thiết kế Figma | Figma | Xuất PDF/PNG các trang trước khi nộp | Người thiết kế |
| Sơ đồ draw.io, báo cáo Word | Máy người soạn | Thư mục chung của nhóm (Drive) | Người soạn |
| APK các bản phát hành | GitHub Release / Drive | — | Nhóm trưởng |

## 5. Khôi phục khi mất máy / hỏng repo cục bộ
- Clone lại từ GitHub ([INSTALLATION](INSTALLATION.md)); nhánh chưa push sẽ mất → push nhánh làm việc ít nhất mỗi ngày.
- Mất keystore: tạo keystore mới, tăng `versionCode`; người dùng phải gỡ app cũ trước khi cài (mất dữ liệu cục bộ — xuất bộ từ trước).
