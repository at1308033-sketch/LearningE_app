# Changelog — lịch sử thay đổi phiên bản

Ghi theo [Keep a Changelog](https://keepachangelog.com/vi/1.1.0/): mỗi PR thêm một dòng vào mục **[Chưa phát hành]**; khi phát hành, đổi tên mục thành phiên bản + ngày (xem [DEPLOYMENT](DEPLOYMENT.md)).

## [Chưa phát hành]
- T-122: đồng bộ chịu được việc server khôi phục từ bản sao lưu (`data_epoch`, CSDL phiên bản 3): kéo lại từ đầu, chỉ đẩy phần server đã mất; chia lô đẩy theo byte, chia đôi khi gặp `PAYLOAD_TOO_LARGE`.
- T-123: tải nội dung 10 bộ/đề mỗi lời gọi (EP-20, EP-21).
- T-020: SCR-36 Thiết bị đăng nhập: xem và đăng xuất từ xa các máy khác (EP-17..EP-19); proto theo LearningE_Server `ad4c552`.
- T-123: `content-data/` của LearningE_Server là nguồn nội dung duy nhất, seed là bản chép (`tools/sync-content.ps1`, `seed.lock`); ẩn bộ/đề ngừng dùng (CSDL phiên bản 2), sửa lỗi đề mất câu khi bỏ câu ở giữa.
- T-002: rà tương phản và cỡ chữ 200% ở 360 dp: viền thẻ rõ hơn, nút trái tim có viền, đầu danh sách từ và bài thi không còn chồng chữ.
- T-080: test hợp đồng DAO trên SQLite thật (cờ đồng bộ, không trùng dòng, CASCADE khi xóa tài khoản, index).
- T-123 (Could): tải bộ từ và đề thi mới từ máy chủ khi mở app (`ContentService`, `ContentWriter` dùng chung với seed), ghi tất cả trong một giao dịch.
- T-093: nhập/xuất CSV qua SAF (`ImportExportService`, SCR-28 Xem trước import, Xuất CSV ở SCR-19), tối đa 1 MB/2000 dòng, một giao dịch.
- T-092: `CsvCodec` theo RFC 4180 (BOM, nháy kép, xuống dòng trong ô) với unit test.
- T-097: bộ luyện thi X02 IELTS (30 từ).
- T-012: dữ liệu bộ T09 Quần áo & mua sắm, T10 Thời tiết & các mùa, T11 Thành phố & phương tiện, T12 Sở thích & thể thao (80 từ).
- T-095: SCR-30 Luyện thi, SCR-31 Làm bài (đồng hồ, bảng câu hỏi, đánh dấu), SCR-32 Kết quả, lịch sử thi, SCR-33 Tài nguyên chính thức; nút vô hiệu rõ chữ hơn; đề mẫu chỉ ở bản debug.
- T-094: `ExamService` (bắt đầu/tiếp tục lượt, lưu từng câu, tính giờ theo `started_at`, tự nộp, chấm điểm, từ sai về hộp 1).
- T-132: huy hiệu chuỗi ở Trang chủ, ngọn lửa + 5 rương ở Tiến độ, màn ăn mừng (cúp, confetti, rương +/−) ở Kết quả phiên.
- T-125: thanh "Đang ngoại tuyến" dùng chung và SCR-35 Trạng thái đồng bộ (5 trạng thái, nút "Đồng bộ ngay").
- T-124: sự kiện XP (`XpRules`, `XpService`, trần 500/ngày), SCR-34 Bảng xếp hạng tuần với bản lưu ngoại tuyến, lối vào ở Trang chủ và tab Tôi.
- T-131: 10 khung ngọn lửa, huy hiệu, 5 rương, cúp, confetti dạng VectorDrawable (≤ 30 path, không gradient); `FlameStage` (BR-070).
- T-002: tăng tương phản viền nút, ô nhập, chip, thẻ (token `outline_strong` ≥ 3:1), chip đang chọn tô đặc màu khu vực, cột biểu đồ và thanh tiến độ rõ hơn.
- T-122: đồng bộ hai chiều (`SyncManager`, `SyncScheduler`, `SyncRemote`, `SyncMapper`, `SyncOutbox`, `SyncInbox`): đẩy ≤ 500 bản ghi/lô, kéo theo trang, xử lý trùng bộ/từ giữa hai máy.
- T-090: bộ từ cá nhân (`DeckEditor`, `DeckRules`): SCR-26 Bộ của tôi với "Yêu thích", SCR-27 Tạo/sửa bộ, SCR-19 Chi tiết bộ, SCR-20 Thêm/sửa từ, "Lưu vào bộ" ở SCR-08.
- T-032, T-033: `StudyService` (học từ mới, ôn tập ≤ 50 thẻ), SCR-09 Flashcard lật 350 ms, SCR-10 Kết quả phiên; Trang chủ mở thẳng phiên học.
- T-031: SCR-08 Chi tiết từ, `TtsHelper` (báo thiếu giọng đọc), `FavoriteDao` và nút yêu thích.
- T-051: SCR-21 Tiến độ với biểu đồ cột 7 ngày tự vẽ (`BarChartView`), `StatisticsService`.
- T-050: `StudyLogDao`, `ProgressService` (hoạt động học, chuỗi ngày), SCR-05 Trang chủ.
- T-030: `DeckDao`, `WordDao`, `DeckService`; SCR-06 lưới chủ đề + lọc cấp độ, SCR-07 danh sách từ + trạng thái; 15 icon chủ đề.
- T-121: trạng thái online của đăng nhập/đăng ký (mất mạng, máy chủ bận, phiên hết hạn, tài khoản đã xóa, đổi tài khoản khi còn thay đổi chưa đồng bộ).
- T-021: giao diện SCR-02 Đăng nhập, SCR-03 Đăng ký (lỗi tại trường, khóa tạm đếm ngược, câu hỏi bảo mật).
- T-020: đăng ký/đăng nhập/đăng xuất qua máy chủ (`AuthService`, `AuthRemote`, `AccountRepository`, `UserDao`, `SettingsDao`), kiểm định dạng ở máy (`AccountRules`), bộ "Sổ tay của tôi" với mã tất định (BR-046).
- T-120: tầng gRPC `data.remote` (`GrpcClient` một kênh dùng chung, deadline, `RetryPolicy` BR-077, `GrpcErrorMapper`, `TokenStore` mã hóa Keystore, `NetworkMonitor`, chặn plaintext), chép proto kèm `proto.lock`.
- T-098: bộ luyện thi X03 Aptis (30 từ).
- T-011: dữ liệu bộ T05 Đồ ăn & thức uống, T06 Động vật, T07 Nhà cửa & đồ dùng, T08 Trường học (80 từ).
- T-004: `SeedLoader` nạp `assets/seed` trong một giao dịch, sửa tại chỗ theo `uid`; `languages.json`; unit test kiểm tra mọi file seed (thêm `org.json` ở phạm vi test).
- T-003: `DatabaseHelper` phiên bản 1 theo DATABASE-DESIGN phần A (cột đồng bộ, `sync_state`, `xp_events`, `leaderboard_cache`, `content_version`) với test trên SQLite thật.
- T-005: gói `core` (`ServiceLocator`, `AppExecutors`, `Result`, `ErrorCode`, `SessionStore`), `AuthActivity` + `MainActivity` 5 tab với Navigation + Safe Args, StrictMode bản debug.
- T-007: `LeitnerScheduler` (BR-010..014) và `StreakCalculator` (BR-031) với unit test.
- T-006: `TextNormalizer` chuẩn hóa chữ, bỏ dấu tiếng Việt và khóa tra từ theo ngôn ngữ (BR-053).
- T-002: theme Material 3 sáng với token màu v2, màu theo khu vực, kiểu chữ Baloo 2/Lexend/Noto Sans (kèm giấy phép OFL), 26 icon Lucide; bỏ `values-night` (dark mode là Won't).
- Nâng `minSdk` từ 24 lên 29: ứng dụng chạy trên Android 10 (API 29) trở lên. Bỏ core library desugaring (`java.time` có sẵn), băm mật khẩu dùng `PBKDF2WithHmacSHA256` của hệ thống (thông số không đổi). Vẫn không dùng UPSERT của SQLite vì cần API 30.
- Hướng dẫn mở, đồng bộ và chạy dự án trong Android Studio 2026.2 (Gradle JDK = JBR đi kèm, `JAVA_HOME` trỏ về `<Android Studio>\jbr` khi dùng dòng lệnh); cập nhật ma trận thiết bị kiểm thử (API 29, 33, 37) và mục xử lý sự cố.
- T-001: tạo khung project Android (Java 17, minSdk 24, compile/targetSdk 37, AGP 9.5.1, Gradle 9.6.0), version catalog, theme Material 3 sáng/tối với token màu, CI GitHub Actions, CONTRIBUTING.md.
- Đổi package thành `com.learninge.app` và tên ứng dụng thành LearningE.
- Thêm `docs/BLUEPRINT.md` (trang bắt đầu cho nhóm) và bộ tài liệu hệ thống `docs/system/` (13 tài liệu: yêu cầu, đặc tả, luồng, kiến trúc, dữ liệu, giao diện, kiểm thử, kế hoạch, quy ước).
- Chuẩn hóa bộ tài liệu dự án `docs/` theo 4 nhóm: project, engineering, quality, operations; chuyển CHANGELOG vào `docs/operations/`.
- Thiết kế giao diện v2: màu thương hiệu + màu theo khu vực tính năng, nền trang trí, 13 minh họa SVG, mockup đủ 32 màn hình (kể cả bộ từ, import/export, luyện thi) trong `docs/design/`.
