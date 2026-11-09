# Handover — bàn giao, vận hành và bảo trì

**Mục đích:** những gì cần để một người mới (thành viên được xoay tua, giảng viên, hoặc nhóm khóa sau) nhận dự án và tiếp tục được: sản phẩm gồm gì, ở đâu, chạy thế nào, còn gì dở dang và bảo trì ra sao. Cập nhật tệp này ở mỗi mốc và trước khi nộp.

## 1. Tóm tắt dự án
| Mục | Nội dung |
|---|---|
| Sản phẩm | LearningE — ứng dụng Android hỗ trợ học tiếng Anh, chạy ngoại tuyến |
| Học phần | Lập trình di động (MOPR331279), HK1 2026–2027, Trường Đại học Công nghệ Kỹ thuật TP.HCM |
| Thành viên | Phan Anh Tuấn (nhóm trưởng), Nguyễn Đức Quang Vinh, Nguyễn Tấn Thành, Lê Văn Tấn Lực |
| Mã nguồn | https://github.com/at1308033-sketch/LearningE_app (nhánh `main`) |
| Thiết kế | [Figma v2 — V2_App_English_Learning](https://www.figma.com/design/exLWP8ZETekA11XOaD3K25/V2_App_English_Learning) (bản hiện hành) · [Figma v1](https://www.figma.com/design/wOdb07dQkcBh18PKijh8Jx/Learning-English-Mobile-App) (giữ để đối chiếu) |
| Công nghệ | Java 17, Android SDK 37 (min 24), Material 3, SQLite, Gradle 9.6 / AGP 9.5 |
| Phiên bản hiện tại | Xem `versionName` trong `app/build.gradle.kts` và [CHANGELOG](CHANGELOG.md) |

## 2. Gói bàn giao
- [ ] Mã nguồn trên GitHub, tag phiên bản nộp (`v1.0.0`).
- [ ] APK release đã ký `LearningE-v1.0.0.apk` ([DEPLOYMENT](DEPLOYMENT.md)).
- [ ] Bộ tài liệu `docs/` (danh mục: [docs/README.md](../README.md)).
- [ ] Báo cáo Word đồ án, "File báo cáo kết quả dự án", sơ đồ `.drawio` + PNG.
- [ ] Keystore + mật khẩu: **chỉ bàn giao trực tiếp cho người nhận được nhóm chỉ định**, không đưa vào repo hay file zip.
- [ ] CSDL demo `learninge-demo.db` ([BACKUP-RESTORE](BACKUP-RESTORE.md)).

## 3. Bắt đầu cho người mới (ngày đầu tiên)
1. Đọc [SYSTEM-BLUEPRINT](../engineering/SYSTEM-BLUEPRINT.md) (tổng quan, bản đồ gói mã, quy trình làm task, rubric).
2. Cài môi trường theo [INSTALLATION](INSTALLATION.md), build và chạy được app.
3. Đọc [CODING-STANDARDS](../engineering/CODING-STANDARDS.md) và [API-SPECIFICATION](../engineering/API-SPECIFICATION.md).
4. Xem task đang mở trong [PROJECT-PLAN](../project/PROJECT-PLAN.md) và Issues trên GitHub; nhận task, tạo nhánh.
5. Hỏi người phụ trách trước của phần việc đó (lịch sử commit `git log -- <thư mục>` cho biết ai đã làm).

## 4. Trạng thái hiện tại
> Cập nhật ở mỗi mốc.

| Mục | Trạng thái (cập nhật 10/11/2026) |
|---|---|
| Phân tích, thiết kế, kế hoạch | Xong và đã duyệt (yêu cầu, đặc tả, kiến trúc, dữ liệu, giao diện, kiểm thử) |
| Khung project (T-001) | Xong: build, lint, CI; còn bật bảo vệ nhánh `main` trên GitHub |
| Thiết kế | Định hướng hình ảnh v2: mockup đủ 32 màn hình + 13 minh họa trong [docs/design/](../design/); đã nhập mockup v2 vào Figma v2 (46 khung, 10/11/2026); còn tách thành component và đặt lại tên style theo token |
| Tính năng F1–F7 | Chưa bắt đầu — theo mốc M1–M4 trong [PROJECT-PLAN](../project/PROJECT-PLAN.md) |
| Dữ liệu nạp sẵn | Đã chốt danh sách 15 chủ đề, 10 bài ngữ pháp, bộ luyện thi, bộ tiếng Nhật; chưa soạn |

## 5. Lỗi đã biết và giới hạn
| Mục | Mô tả | Ảnh hưởng | Hướng xử lý |
|---|---|---|---|
| Đồng hồ thi thử theo giờ máy | Đổi giờ máy làm sai thời gian còn lại | Thấp (bài luyện tập) | Chấp nhận; ghi trong hướng dẫn sử dụng |
| Nhắc học có thể trễ | Doze / tối ưu pin của hãng | Trung bình | Hướng dẫn tắt tối ưu pin ([TROUBLESHOOTING](TROUBLESHOOTING.md)) |
| Không sao lưu đám mây | Thiết kế để bảo vệ hash mật khẩu | Gỡ app mất dữ liệu | Người dùng tự Export bộ từ |
| (thêm lỗi chưa sửa khi nộp) | | | |

## 6. Vận hành và bảo trì
| Công việc | Tần suất | Cách làm |
|---|---|---|
| Cập nhật thư viện | Mỗi học kỳ / khi Lint báo | Sửa `gradle/libs.versions.toml`, chạy lại toàn bộ test |
| Nâng `targetSdk` | Khi có Android mới | Đọc thay đổi hành vi của bản Android đó; kiểm tra quyền thông báo, báo thức |
| Thêm chủ đề / từ / đề thi | Khi cần | Thêm tệp JSON theo [DATABASE-DESIGN](../engineering/DATABASE-DESIGN.md), tăng phiên bản dữ liệu, test kiểm tra dữ liệu |
| Thêm ngôn ngữ học mới | Khi cần | Thêm 1 dòng vào `languages.json` (+ tệp bộ từ), không sửa mã Java |
| Đổi lược đồ CSDL | Khi có tính năng mới | Tăng `DATABASE_VERSION`, viết bước `onUpgrade`, test nâng cấp từ bản cũ |
| Kiểm tra liên kết tài nguyên luyện thi | Trước mỗi lần phát hành | Mở từng URL trong `strings.xml` |
| Gia hạn keystore | Hiệu lực 10.000 ngày | Không cần; chỉ cần giữ an toàn |

## 7. Hướng phát triển
Thuật toán ôn tập SM-2; đồng bộ nhiều thiết bị khi có máy chủ; chấm phát âm; widget màn hình chính; thêm đề thi thử; giao diện đa ngôn ngữ.

## 8. Liên hệ
| Vai trò | Người | Kênh |
|---|---|---|
| Nhóm trưởng (mã nguồn, keystore, phát hành) | Phan Anh Tuấn | GitHub `@at1308033-sketch`, nhóm chat |
| Thiết kế | Nguyễn Đức Quang Vinh | Figma, nhóm chat |
| CSDL, dữ liệu | Nguyễn Tấn Thành | Nhóm chat |
| Kiểm thử | Lê Văn Tấn Lực | GitHub Issues, nhóm chat |
Phân công có thể xoay tua — người phụ trách thực tế xem trong PR/Issue gần nhất.
