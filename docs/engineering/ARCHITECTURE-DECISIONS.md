# Architecture Decisions — nhật ký quyết định (ADR)

> **TỰ SINH** từ bộ phân tích thiết kế đã duyệt của nhóm — không sửa trực tiếp tệp này; đề xuất thay đổi qua Issue/PR để nhóm trưởng cập nhật nguồn và sinh lại. Danh mục tài liệu: [docs/README.md](../README.md).

**Mục đích:** mỗi quyết định kiến trúc/kỹ thuật quan trọng: quyết định, lý do, phương án bị loại, ràng buộc, hệ quả, trạng thái.

## Quyết định sản phẩm ban đầu (09/11/2026)
| # | Quyết định | Đã chọn | Hệ quả nếu chọn khác |
|---|---|---|---|
| D1 | Kiến trúc triển khai | Chỉ ứng dụng Android chạy ngoại tuyến, không máy chủ, không API mạng | Thêm máy chủ Spring Boot: gấp đôi khối lượng, cần hạ tầng, ngoài phạm vi môn học |
| D2 | Tầng dữ liệu | SQLite thuần qua `SQLiteOpenHelper` | Room: ít code lặp hơn nhưng ít thấy SQL trực tiếp để trình bày trong báo cáo |
| D3 | Mức tài liệu | Quy trình gọn cho nhóm 4 người nhưng giữ đầy đủ tài liệu phân tích thiết kế (giảng viên yêu cầu) | Ít tài liệu hơn: thiếu nội dung chương phân tích thiết kế của báo cáo |
| D4 | Thứ tự phạm vi | Tài khoản, từ vựng, quiz trước; tiến độ, ngữ pháp, bộ từ, luyện thi sau; tuần cuối kiểm thử và báo cáo | Làm song song tất cả: dễ không có nhóm tính năng nào hoàn chỉnh khi demo |

## Quyết định kiến trúc (ADR)
> Quyết định quan trọng, lý do, phương án bị loại.
> Quyết định sản phẩm đã chốt trước đó: D1–D4 (bảng đầu tài liệu này), thay đổi phạm vi (quên mật khẩu, dark mode, seed). Dưới đây là quyết định kỹ thuật phase 3.

## DEC-001: Kiến trúc MVVM 3 tầng, 2 Activity + Fragment + Navigation

Decision: Tầng giao diện (`AuthActivity`, `MainActivity` + Fragment, ViewModel/LiveData) → tầng nghiệp vụ (Service + quy tắc Java thuần) → tầng dữ liệu (Repository → DAO → `SQLiteOpenHelper`). Phụ thuộc khởi tạo qua một `ServiceLocator` trong lớp `Application`.
Reason: Tách quy tắc nghiệp vụ (Leitner, chuỗi ngày, chấm điểm) ra Java thuần để unit test (NFR-M2); ViewModel giữ trạng thái khi xoay màn hình (NFR-A4); dễ vẽ sơ đồ lớp/sequence cho báo cáo.
Alternatives considered: (a) Mỗi màn hình một Activity, gọi SQLite trực tiếp — nhanh viết nhưng logic lẫn giao diện, khó test, dễ chặn luồng UI. (b) Clean Architecture đầy đủ với UseCase từng hành động + Hilt — quá nặng cho nhóm 4 người / 6 tuần.
Constraints: Java + XML; không thêm thư viện DI.
Consequences: Thêm vài lớp trung gian; cần quy ước gói rõ ràng (CODING-STANDARDS). Mỗi thành viên làm một nhánh tính năng theo gói `ui.*` tương ứng, ít xung đột.
Status: Accepted (Phan Anh Tuấn, 2026-11-09)

## DEC-002: Dữ liệu seed dạng JSON trong assets, nạp lúc tạo CSDL

Decision: Nội dung học (15 chủ đề, 450 từ, các bộ luyện thi X01..X03, bộ tiếng Nhật J01, 10 bài ngữ pháp, 100 câu hỏi, đề thi thử) lưu thành file JSON trong `assets/seed/`: **mỗi bộ một tệp** dạng `{deck, source, words}` (`T01.json` … `T15.json`, `X01.json` … `X03.json`, `J01.json`, đặt trong `assets/seed/decks/`) cùng `languages.json`, `grammar.json`, `exams.json` (định dạng ở CODING-STANDARDS, khớp DATABASE-DESIGN; trường biểu tượng bộ là `icon`). `DatabaseHelper.onCreate` đọc bằng `org.json` và chèn trong một giao dịch; `seed_meta.seed_version` để nâng cấp. Không có `topics.json` hay `words_T*.json`: chủ đề là bộ (`decks`) từ thay đổi phạm vi.
Reason: Mỗi thành viên soạn file bộ của mình độc lập (PRD §Nội dung seed), ít xung đột Git; kiểm tra định dạng được bằng script; không cần thư viện ngoài.
Alternatives considered: (a) File `.db` dựng sẵn copy từ assets — nạp nhanh nhất nhưng khó sửa và review nội dung, dễ lệch lược đồ. (b) Câu lệnh SQL `INSERT` viết tay — khó đọc, dễ lỗi dấu nháy trong câu ví dụ. (c) CSV — không biểu diễn tốt mảng đáp án và ví dụ ngữ pháp.
Constraints: NFR-P1 (nạp seed ≤ 5 giây lần đầu); bản quyền nội dung (Glossary (kiến thức miền) §Pháp lý).
Consequences: Cần một bước kiểm tra seed (đếm số lượng, trường bắt buộc, trùng từ) chạy trong unit test; định dạng JSON chốt ở CODING-STANDARDS phase 4.
Status: Accepted (Phan Anh Tuấn, 2026-11-09)

## DEC-003: Băm PBKDF2-HMAC-SHA256 bằng `SecretKeyFactory`

Decision: `PasswordHasher` dùng `SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")` (có sẵn từ Android 8.0, API 26) với `PBEKeySpec`: 120.000 vòng, salt 16 byte, khóa 32 byte; unit test với vector kiểm thử PBKDF2-HMAC-SHA256 của RFC 7914 §11.
Reason: minSdk 29 ≥ API 26 nên dùng được bản có sẵn của nền tảng, không phải tự viết mã mật mã. Giữ đúng BR-002 và thông số cũ.
Alternatives considered: (a) Tự hiện thực PBKDF2 trên `Mac("HmacSHA256")` — chỉ cần khi minSdk < 26, nay thừa và tăng rủi ro sai. (b) Dùng `PBKDF2WithHmacSHA1` có từ API 1 — trái BR-002. (c) Thư viện bcrypt/Argon2 — thêm phụ thuộc, Argon2 cần mã native.
Constraints: NFR-S1; không thêm thư viện; băm phải chạy trên luồng nền.
Consequences: Không còn mã mật mã tự viết; vẫn bắt buộc unit test với vector chuẩn (kiểm tra đúng thông số) và người review kiểm tra. Băm vẫn chạy trên luồng nền.
Status: Accepted (Phan Anh Tuấn, 2026-11-09)

## DEC-004: Nhắc học bằng AlarmManager không chính xác + BootReceiver

Decision: Đặt một báo thức không chính xác mỗi ngày (`setWindow` với cửa sổ 15 phút quanh giờ nhắc, `RTC_WAKEUP`); khi báo thức chạy thì kiểm tra mục tiêu, hiện thông báo nếu cần, rồi đặt báo thức ngày hôm sau; `BootReceiver` đặt lại sau khi khởi động máy.
Reason: Đúng yêu cầu ±15 phút (BR-030, NFR-A5) mà không cần quyền `SCHEDULE_EXACT_ALARM` (bị hạn chế từ Android 14); không thêm thư viện.
Alternatives considered: (a) `setExactAndAllowWhileIdle` — chính xác hơn nhưng cần quyền báo thức chính xác, người dùng phải cấp trong Cài đặt. (b) WorkManager `PeriodicWorkRequest` 24 giờ — giờ chạy trôi dần, không bảo đảm đúng giờ đã chọn. (c) WorkManager `OneTimeWorkRequest` với độ trễ tính toán — hợp lệ nhưng thêm thư viện và vẫn có thể trễ khi Doze.
Constraints: Android 6+ Doze có thể trễ báo thức không chính xác; Android 13+ cần `POST_NOTIFICATIONS`.
Consequences: Phải xử lý đổi giờ/đổi múi giờ máy (`ACTION_TIME_CHANGED`, `ACTION_TIMEZONE_CHANGED` → đặt lại lịch); người kiểm thử kiểm tra trên emulator API 33 có Doze.
Status: Accepted (Phan Anh Tuấn, 2026-11-09)

## DEC-005: Biểu đồ 7 ngày tự vẽ bằng Canvas

Decision: `BarChartView` kế thừa `View`, vẽ 7 cột + nhãn bằng `Canvas`, màu lấy từ theme.
Reason: Chỉ cần một biểu đồ cột đơn giản (US-017); tránh thư viện ngoài (MPAndroidChart lấy từ JitPack, cần cấu hình kho, tăng kích thước APK); tự vẽ còn là điểm cộng kỹ thuật cho báo cáo.
Alternatives considered: MPAndroidChart; vẽ bằng nhiều `View` cao thấp trong `LinearLayout`.
Constraints: NFR-P5 (APK ≤ 30 MB); NFR-X1 (cần `contentDescription` mô tả số liệu).
Consequences: Tự xử lý đổi kích thước và dark mode; khoảng 100 dòng mã.
Status: Accepted (Phan Anh Tuấn, 2026-11-09)

## DEC-006: Ôn tập theo Leitner 5 hộp (1/2/4/7/14 ngày)

Decision: Dùng hệ thống Leitner 5 hộp như BR-011, BR-012; SM-2 để làm hướng phát triển.
Reason: Đề xuất ở Glossary (kiến thức miền) §2 và đã thành quy tắc trong SRS; hiện thực bằng 2 cột (`box`, `next_review_date`), dễ vẽ sơ đồ trạng thái.
Alternatives considered: SM-2 (hệ số dễ, người dùng chấm 0–5) — chính xác hơn nhưng giao diện chấm 6 mức phức tạp với người mới.
Constraints: Giao diện chỉ có 2 nút Đã nhớ / Chưa nhớ (UI-UX-SPECIFICATION SCR-09).
Consequences: Không cá nhân hóa khoảng ôn theo độ khó từng từ.
Status: Accepted (Phan Anh Tuấn, 2026-11-09)

## DEC-007: Màu sáng/tối trong Figma là 2 collection riêng

Decision: Trong Figma tạo collection "Color Light" và "Color Dark" (17 biến cùng tên) thay vì 1 collection 2 mode.
Reason: Gói Figma Starter chỉ cho 1 mode mỗi collection (lỗi "Limited to 1 modes only" khi tạo).
Alternatives considered: Nâng gói Figma Professional để dùng mode — tốn phí.
Constraints: Gói Figma hiện tại của nhóm.
Consequences: Đổi giao diện sáng/tối trong Figma phải đổi biến thủ công; trong code không bị ảnh hưởng (`values/` và `values-night/`).
Status: Accepted

## DEC-008: Import/export CSV tự hiện thực theo RFC 4180, JSON dùng `org.json`

Decision: `CsvCodec` (Java thuần) đọc/ghi CSV UTF-8 theo RFC 4180: ô bọc nháy kép khi chứa dấu phẩy/nháy/xuống dòng, nháy trong ô viết `""`, bỏ BOM đầu tệp, ghi kèm BOM để Excel mở đúng tiếng Việt. JSON dùng cùng định dạng file seed và `org.json`; `JsonCodec` đặt ở `data.io` (cạnh mã SAF), không ở `domain.rules`, vì `org.json` của Android chỉ là bản stub khi chạy unit test JVM — muốn test `JsonCodec` trên JVM thì thêm `testImplementation` của `org.json:json` khi có test cần. Đọc tệp theo luồng (stream), dừng khi vượt 1 MB hoặc 2000 dòng.
Reason: Đáp ứng rubric "import, export từ vựng" mà không thêm thư viện; logic phân tích tách riêng nên unit test được các ca khó (dấu phẩy trong nghĩa, câu ví dụ có nháy, tiếng Việt có dấu, xuống dòng trong ô).
Alternatives considered: (a) Thư viện OpenCSV/Apache Commons CSV — chuẩn hơn nhưng thêm phụ thuộc, khó giải thích khi vấn đáp. (b) Chỉ hỗ trợ JSON — người dùng khó soạn bằng Excel/Google Sheets. (c) Excel `.xlsx` — cần Apache POI, nặng.
Constraints: Offline, không quyền bộ nhớ (dùng SAF), NFR-P5 kích thước APK.
Consequences: Phải có bộ unit test cho `CsvCodec` (TESTING-STRATEGY TC-032.x); tài liệu hướng dẫn sử dụng có tệp CSV mẫu.
Status: Accepted

## DEC-009: Ngôn ngữ nội dung là dữ liệu, không phải mã

Decision: Bảng `languages` (mã, tên, locale TTS, nhãn cách đọc) là nguồn duy nhất; mọi màn hình lấy cấu hình qua `LanguageRegistry`. Trường từ vựng đặt tên trung tính (`term`, `reading`, `meaning_vi`, `example`, `example_translation`) thay cho `word`/`ipa`. Chuẩn hóa tìm kiếm: chữ thường + bỏ khoảng trắng cho mọi ngôn ngữ; bỏ dấu cho tiếng Việt (nghĩa) và cho `reading` dạng Latin.
Reason: Rubric chấm "dễ dàng thay đổi sang học ngôn ngữ khác"; minh chứng bằng bộ mẫu tiếng Nhật J01 và AC-034.3 (thêm ngôn ngữ không sửa Java).
Alternatives considered: (a) Enum ngôn ngữ trong code — phải sửa code mỗi lần thêm. (b) Một bảng từ riêng cho mỗi ngôn ngữ — trùng lặp DAO/màn hình.
Constraints: Giao diện vẫn chỉ tiếng Việt (Non-goal); TTS phụ thuộc gói giọng của máy.
Consequences: Không hỗ trợ tách từ cho tiếng Nhật/Trung (tìm kiếm khớp tiền tố/chứa chuỗi là đủ cho phạm vi đồ án).
Status: Accepted

## DEC-010: Lượt thi thử lưu từng câu ngay và tính giờ theo `started_at`

Decision: Bắt đầu thi tạo `exam_attempts` (IN_PROGRESS, `started_at`); mỗi lần chọn đáp án/đánh dấu ghi ngay `exam_answers`; thời gian còn lại = `duration_sec` − (now − `started_at`), tính lại mỗi khi mở màn hình; về 0 thì tự nộp. Mở lại đề có lượt IN_PROGRESS thì tiếp tục.
Reason: AC-036.4 (thoát app không mất bài, không "dừng đồng hồ" gian lận); không cần Service chạy nền.
Alternatives considered: (a) Giữ trạng thái trong ViewModel — mất khi tiến trình bị hệ thống hủy. (b) Foreground service đếm giờ — phức tạp, cần quyền thông báo.
Constraints: Đồng hồ dựa vào giờ máy (BR-042).
Consequences: Người dùng đổi giờ máy có thể làm sai thời gian còn lại — chấp nhận cho bài luyện tập.
Status: Accepted

## DEC-011: Không dùng UPSERT, luồng `io` riêng

Decision: (1) Dùng trực tiếp `java.time` (`LocalDate`, `Instant`, `Clock`) có sẵn từ API 26; không bật core library desugaring (đã bỏ ). (2) Không dùng `INSERT … ON CONFLICT DO UPDATE`; ghi tiến độ bằng `UPDATE`, 0 dòng thì `INSERT`, cùng giao dịch; được dùng `INSERT OR IGNORE` / `INSERT OR REPLACE`. (3) `AppExecutors` có 4 luồng: `main`, `dbRead`, `dbWrite`, `io` (đọc/ghi tệp SAF). `Result<T>` và `ErrorCode` nằm ở gói `core`.
Reason: `minSdk 29` (NFR-D1) ≥ API 26 nên `java.time` có sẵn; UPSERT cần SQLite 3.24 (API 30) mà minSdk 29 < API 30. Tách luồng `io` để đọc/ghi tệp chậm không chặn hàng đợi ghi CSDL.
Alternatives considered: (a) Dùng `Calendar`/`SimpleDateFormat` — dễ sai múi giờ và khó test; (b) nâng `minSdk` lên 30 để dùng UPSERT — loại thêm thiết bị Android 10, không đáng cho một câu lệnh có cách thay thế.
Constraints: APK ≤ 30 MB (NFR-P5).
Consequences: Không thêm dependency; code review phải chặn câu UPSERT; test DAO kiểm tra ghi hai lần cùng (user, word) chỉ có một dòng.
Status: Accepted
