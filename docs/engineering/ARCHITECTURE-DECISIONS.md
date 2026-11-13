# Architecture Decisions — nhật ký quyết định (ADR)

> **TỰ SINH** từ bộ phân tích thiết kế đã duyệt của nhóm — không sửa trực tiếp tệp này; đề xuất thay đổi qua Issue/PR để nhóm trưởng cập nhật nguồn và sinh lại. Danh mục tài liệu: [docs/README.md](../README.md).

**Mục đích:** mỗi quyết định kiến trúc/kỹ thuật quan trọng: quyết định, lý do, phương án bị loại, ràng buộc, hệ quả, trạng thái.

## Quyết định sản phẩm ban đầu (09/11/2026)
| # | Quyết định | Đã chọn | Hệ quả nếu chọn khác |
|---|---|---|---|
| D1 | Kiến trúc triển khai | Ban đầu: chỉ ứng dụng Android ngoại tuyến. **Đổi ngày 10/11/2026:** ứng dụng online kiểu Duolingo — máy chủ Java (Javalin) + SQLite của nhóm, ứng dụng vẫn offline-first (xem DEC-012..017) | Giữ ngoại tuyến hoàn toàn: không có tài khoản đám mây, đồng bộ nhiều máy, bảng xếp hạng |
| D2 | Tầng dữ liệu | SQLite thuần qua `SQLiteOpenHelper` | Room: ít code lặp hơn nhưng ít thấy SQL trực tiếp để trình bày trong báo cáo |
| D3 | Mức tài liệu | Quy trình gọn cho nhóm 4 người nhưng giữ đầy đủ tài liệu phân tích thiết kế (giảng viên yêu cầu) | Ít tài liệu hơn: thiếu nội dung chương phân tích thiết kế của báo cáo |
| D4 | Thứ tự phạm vi | Tài khoản, từ vựng, quiz trước; tiến độ, ngữ pháp, bộ từ, luyện thi sau; tuần cuối kiểm thử và báo cáo | Làm song song tất cả: dễ không có nhóm tính năng nào hoàn chỉnh khi demo |

## Quyết định kiến trúc (ADR)
> Quyết định quan trọng, lý do, phương án bị loại.
> Quyết định sản phẩm đã chốt trước đó: D1–D4 (bảng đầu tài liệu này), thay đổi phạm vi (quên mật khẩu, dark mode, seed). Dưới đây là quyết định kỹ thuật phase 3.
> **Đối chiếu số hiệu với thay đổi phạm vi §3** (tài liệu này đánh số lại theo phân công D1 của nhóm trưởng; nội dung không đổi): CR DEC-018 → DEC-018 · CR DEC-019 → DEC-019 · CR DEC-021 (app gRPC) → **DEC-020** · CR DEC-020 (lưu trữ lai) → **DEC-021** · CR DEC-022 (proto) + CR DEC-023 (repo riêng) → **DEC-022** · CR DEC-024 (hạ tầng demo) → **DEC-023** (nay trung lập, Q3 chưa chốt) · CR DEC-025 (quan sát) → gộp vào DEC-020 + SYSTEM-BLUEPRINT §quan sát.

## Chỉ mục
| ID | Tiêu đề | Trạng thái | Ngày | Liên quan (SYSTEM-BLUEPRINT/SRS (phi chức năng)/CR) |
|---|---|---|---|---|
| DEC-001 | Kiến trúc MVVM 3 tầng, 2 Activity + Fragment + Navigation | Accepted | 2026-11-09 | SYSTEM-BLUEPRINT §Thành phần; NFR-M1 |
| DEC-002 | Dữ liệu seed dạng JSON trong assets | Accepted | 2026-11-09 | DATABASE-DESIGN §Seed; NFR-P1 |
| DEC-003 | Băm PBKDF2-HMAC-SHA256 bằng `SecretKeyFactory` | Accepted; thay thế một phần bởi DEC-013 | 2026-11-09 | NFR-S1 |
| DEC-004 | Nhắc học bằng AlarmManager không chính xác + BootReceiver | Accepted | 2026-11-09 | NFR-A5 |
| DEC-005 | Biểu đồ 7 ngày tự vẽ bằng Canvas | Accepted | 2026-11-09 | NFR-P5 |
| DEC-006 | Ôn tập theo Leitner 5 hộp | Accepted | 2026-11-09 | BR-011, BR-012 |
| DEC-007 | Màu sáng/tối trong Figma là 2 collection riêng | Accepted | 2026-11-09 | UI-UX-SPECIFICATION (hệ thống thiết kế) |
| DEC-008 | Import/export CSV theo RFC 4180, JSON dùng `org.json` | Accepted | 2026-11-09 | thay đổi phạm vi |
| DEC-009 | Ngôn ngữ nội dung là dữ liệu | Accepted | 2026-11-09 | thay đổi phạm vi, thay đổi phạm vi |
| DEC-010 | Lượt thi thử lưu từng câu ngay, tính giờ theo `started_at` | Accepted | 2026-11-09 | thay đổi phạm vi |
| DEC-011 | Không dùng UPSERT trên Android, luồng `io` riêng | Accepted | 2026-11-10 | NFR-D1; thay đổi phạm vi, thay đổi phạm vi |
| DEC-012 | Server Java 17 + Javalin 6 + SQLite (`sqlite-jdbc`) + JDBC thuần, module Gradle `:server` cùng repo | **Superseded by DEC-018+** | 2026-11-10 | thay đổi phạm vi; snapshot |
| DEC-013 | Xác thực trên server: PBKDF2 + token opaque lưu băm SHA-256 | Proposed — giữ ý; phần Javalin/`AuthFilter`/header HTTP **Superseded by DEC-018+** (hiện thực ở dịch vụ `auth` + edge, metadata gRPC) | 2026-11-10 | API_CONTRACT §Xác thực; NFR-S1, S7, S8; thay đổi phạm vi, thay đổi phạm vi |
| DEC-014 | Offline-first: SQLite máy là nguồn hiển thị, đồng bộ đẩy/kéo theo cursor, LWW | Proposed — giữ ý; phần REST `POST /sync/push`, `GET /sync/pull` **Superseded by DEC-018+** (RPC `SyncService.Push`/`Pull`, DEC-020) | 2026-11-10 | DATABASE-DESIGN §Đồng bộ; NFR-N1, N3; thay đổi phạm vi, thay đổi phạm vi |
| DEC-015 | Client HTTP: `HttpURLConnection` + `org.json`, chạy trên luồng `io` | **Superseded by DEC-018+** (DEC-020: gRPC trên app) | 2026-11-10 | thay đổi phạm vi; snapshot |
| DEC-016 | Hạ tầng demo: laptop + đường hầm HTTPS miễn phí, dự phòng LAN HTTP | Proposed — phần HTTP/1.1, `:server:run`, cổng 8080 **Superseded by DEC-018+** (DEC-020 HTTP/2, DEC-023 nơi triển khai) | 2026-11-10 | SYSTEM-BLUEPRINT §Triển khai; NFR-S3; thay đổi phạm vi, thay đổi phạm vi |
| DEC-017 | Pixel layer: VectorDrawable + PNG nodpi, không thư viện game | Proposed (không bị thay đổi phạm vi ảnh hưởng) | 2026-11-10 | UI-UX-SPECIFICATION (hệ thống thiết kế) §Pixel layer; NFR-P6; thay đổi phạm vi |
| DEC-018 | Backend viết bằng Rust: tokio + tonic/prost, rusqlite, driver mongodb, PBKDF2 bằng crate RustCrypto | Proposed | 2026-11-12 | SYSTEM-BLUEPRINT (công nghệ) §Server; SYSTEM-BLUEPRINT §Backend; thay đổi phạm vi Q1 |
| DEC-019 | Microservice 6 dịch vụ gRPC, database-per-service, chế độ gộp `--all` | Proposed | 2026-11-12 | SYSTEM-BLUEPRINT §Thành phần; NFR-N5, NFR-S9; thay đổi phạm vi |
| DEC-020 | gRPC từ đầu đến cuối: app gọi edge trực tiếp bằng `grpc-okhttp` + protobuf-lite; edge tự viết; metadata, deadline, request-id | Proposed | 2026-11-12 | SYSTEM-BLUEPRINT §Tầng mạng, §Metadata; SYSTEM-BLUEPRINT (công nghệ); NFR-N2, NFR-N4; thay đổi phạm vi Q1, Q2, Q5 |
| DEC-021 | Lưu trữ lai: SQLite là nguồn sự thật, MongoDB là kho phụ nạp qua outbox + relay idempotent | Proposed | 2026-11-12 | DATABASE-DESIGN (B); SYSTEM-BLUEPRINT §Outbox; NFR-N5, NFR-N6; thay đổi phạm vi |
| DEC-022 | Repo server riêng `LearningE_Server`; proto gốc ở repo server, app giữ bản sao + `proto.lock` | Proposed | 2026-11-12 | SYSTEM-BLUEPRINT §Repo; CODING-STANDARDS; thay đổi phạm vi Q4 |
| DEC-023 | Nơi triển khai demo/chấm (laptop + tunnel / Atlas / Railway / VPS) | **Proposed — chưa chốt** (Q3) | 2026-11-12 | SYSTEM-BLUEPRINT §Triển khai; thay đổi phạm vi Q3 |

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
Status: Accepted (Phan Anh Tuấn, 2026-11-09); thay thế một phần bởi DEC-013: thông số PBKDF2 giữ nguyên nhưng việc băm chuyển lên server (`javax.crypto` của JDK 17), app không còn băm mật khẩu

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

## DEC-012: Server Java 17 + Javalin 6 + SQLite (`sqlite-jdbc`) + JDBC thuần, module Gradle `:server` cùng repo

Context (bối cảnh, lực kéo, ràng buộc): thay đổi phạm vi chuyển LearningE sang app online (tài khoản server, đồng bộ, bảng xếp hạng, nội dung). Nhóm 4 sinh viên chỉ quen Java; vấn đáp 50 điểm yêu cầu người phụ trách giải thích được code server (R-38); máy chạy demo là laptop của nhóm; quy mô < 50 người dùng.
Decision: Server viết bằng Java 17 (biên dịch `--release 17`), framework HTTP **Javalin 6.7.0** (Jetty 11), CSDL **SQLite** một file qua **xerial `sqlite-jdbc` 3.53.4.0**, truy cập bằng **JDBC thuần + `PreparedStatement`** (không ORM), log **slf4j-simple 2.0.20**, test **JUnit 5.14.4** + `java.net.http.HttpClient` của JDK; mã nằm ở module Gradle `:server` (plugin `application`) trong cùng repo/cùng build với `:app`. JSON phía server: **`jackson-databind` 2.22.4** (cùng `jackson-core`, `jackson-annotations` — bộ ánh xạ JSON mặc định của Javalin) — **đã được người dùng (Phan Anh Tuấn) duyệt ngày 11/11/2026** với ràng buộc: ghim đúng phiên bản; **không** bật default typing (`activateDefaultTyping`/`@JsonTypeInfo` trên `Object`); chỉ ánh xạ vào DTO riêng cho từng endpoint (chống gán hàng loạt); `StreamReadConstraints.maxNestingDepth = 32`.
Reason: Javalin ít "phép thuật" (route là lambda, không annotation/DI), khởi động < 1 s, dễ giải thích; SQLite không cần cài dịch vụ, sao lưu = chép 1 file, cùng SQL với app; JDBC thuần cho thấy rõ câu SQL (dễ đưa vào báo cáo, chống SQL injection bằng `?` như NFR-S2); cùng repo + cùng build giúp chạy bằng 1 lệnh (NFR-E4) và dùng chung version catalog.
Alternatives considered: (a) Spring Boot 3 — mạnh nhưng nặng (nhiều annotation, auto-config khó giải thích khi vấn đáp, khởi động 3–5 s). (b) `com.sun.net.httpserver` thuần JDK — 0 dependency nhưng phải tự viết router, phân tích JSON, xử lý lỗi, giới hạn body: nhiều code tay dễ lỗi. (c) PostgreSQL/MySQL — phải cài dịch vụ trên laptop demo, thừa cho < 50 người dùng. (d) Build Gradle riêng trong `server/` có wrapper riêng — **phương án dự phòng** nếu AGP 9.5.1 xung đột với module Java thuần trong cùng build. (e) Firebase/Supabase — trái yêu cầu "server Java riêng dùng SQLite" của thay đổi phạm vi.
Constraints: Không thêm thư viện vào app Android (DEC-015); dependency server chỉ gồm danh sách đã duyệt (Javalin, sqlite-jdbc, slf4j, JUnit 5 — 10/11; thêm `jackson-databind` — 11/11); SQLite chỉ một người ghi tại một thời điểm (WAL cho phép đọc song song). Giai đoạn này **chỉ làm tài liệu, không code** (chỉ đạo của người dùng ngày 2026-11-10): chưa tạo module `:server`.
Consequences: (1) Khi bắt đầu build (T-110): thêm `include(":server")` vào `settings.gradle.kts`, các mục `javalin`, `sqliteJdbc`, `slf4j`, `junit5`, `jackson` (2.22.4) vào `gradle/libs.versions.toml`. (2) **Rủi ro chưa kiểm chứng:** AGP 9.5.1 + module `application` Java thuần trong cùng build — **xác minh ở đầu T-110** bằng `./gradlew :server:test :app:assembleDebug`; nếu xung đột thì chuyển sang phương án (d), không vá tạm. (3) Javalin 6 cần JSON mapper để dùng `ctx.json()`/`ctx.bodyAsClass()`; Jackson là phụ thuộc *optional* của Javalin nên phải khai báo riêng (đã duyệt 11/11; phương án thay `org.json:json` ≥ 20231013 không dùng — thiếu giới hạn độ sâu sẵn có, nhiều code tay hơn, THREAT_MODEL §Đề xuất bộ ánh xạ JSON). Javalin 6.7.0 được build với Jackson 2.18.3 còn ta ghim 2.22.4 — **kiểm chứng ghép** ở T-110 bằng test hợp đồng (T-115). (4) Javalin kéo theo Jetty 11 và `kotlin-stdlib` — chỉ ở server, không vào APK. (5) Không dùng `java { toolchain }` để khỏi bắt cài JDK 17 riêng: Gradle chạy bằng JBR của Android Studio, `options.release = 17` bảo đảm API Java 17. (6) Server SQLite (3.53) được dùng UPSERT và window function, khác với app (DEC-011). (7) Gradle 9 cần khai báo `junit-platform-launcher` ở `testRuntimeOnly`. (8) **Jetty 11 (Javalin 6.7.0 dùng 11.0.25) đã hết hỗ trợ cộng đồng từ 01/2025** (THREAT_MODEL TM-29): rủi ro chấp nhận cho đồ án (server chỉ chạy lúc demo, sau đường hầm/loopback); **kiểm tra phiên bản Jetty và lỗ hổng đã biết (quét OSV, RC-13) ở T-110**, nếu có CVE ảnh hưởng thì thử ép Jetty 11.0.x mới nhất hoặc ghi rủi ro vào PROJECT-PLAN (rủi ro) R-37; không nâng lên Javalin 7 (Jetty 12) khi chưa được duyệt.
Review khi: số người dùng đồng thời > 200 hoặc cần nhiều tiến trình server (xem PostgreSQL); có CVE nghiêm trọng của Jetty 11/Jackson mà không vá được; kiểm tra AGP ở đầu T-110 thất bại (chuyển build riêng).
Status: **Superseded by DEC-018+** (thay đổi phạm vi duyệt 2026-11-11, Phan Anh Tuấn): backend chuyển sang 6 dịch vụ Rust gRPC (DEC-018, DEC-019, DEC-021); hủy dependency Javalin, Jackson, sqlite-jdbc, slf4j, JUnit 5. Nội dung trên giữ làm lịch sử và làm cơ sở cho rollback RB-1 (go/no-go 29/11) từ `docs/changes/thay đổi phạm vi-snapshot/`.

## DEC-013: Xác thực trên server — PBKDF2 + token opaque lưu băm SHA-256

Context (bối cảnh, lực kéo, ràng buộc): Tài khoản phải dùng được trên nhiều máy (REQ-001); đặt lại mật khẩu bằng câu hỏi bảo mật chạy trên server (REQ-014); phải thu hồi được phiên khi đăng xuất/đổi mật khẩu (BR-058); không thêm thư viện vào app.
Decision: Server băm mật khẩu và câu trả lời bảo mật bằng PBKDF2-HMAC-SHA256 (120.000 vòng, salt 16 byte, khóa 32 byte — giữ thông số DEC-003; lớp `auth.PasswordHasher` chuyển từ app sang server). Đăng nhập thành công cấp **token opaque**: 32 byte (256 bit) từ `SecureRandom`, mã hóa base64url (43 ký tự); server chỉ lưu `SHA-256(token)` (hex) trong `auth_tokens` cùng `expires_at` = thời điểm cấp + 30 ngày (không gia hạn trượt). App gửi `Authorization: Bearer <token>`. Đăng xuất thu hồi token hiện tại; đổi mật khẩu, đặt lại mật khẩu, xóa tài khoản thu hồi **mọi** token của người dùng (đổi mật khẩu cấp token mới cho máy đang dùng). App giữ token trong `SharedPreferences` chế độ riêng tư, không ghi log (Should: mã hóa Android Keystore AES-GCM). **Bổ sung 11/11/2026 (THREAT_MODEL D-02, D-03; quyết định người dùng):** (a) **khóa tăng dần** cho đăng nhập (BR-003) và đặt lại mật khẩu bằng câu trả lời bảo mật (BR-008): sai 5 lần liên tiếp → khóa 60 s, mỗi lần khóa sau gấp đôi (2, 4, 8 phút …), tối đa 1 giờ; tính cả email không tồn tại; (b) email không tồn tại ở EP-08 nhận `security_question_id` giả = `HMAC-SHA256(khóa server, email) mod 5 + 1` (khóa từ `LEARNINGE_SERVER_KEY`), mọi sai → 401 "Thông tin không khớp"; (c) đăng ký giữ 409 `EMAIL_TAKEN` (đánh đổi chấp nhận được, giới hạn 10 đăng ký/giờ/IP); (d) EP-14..EP-16 cần token (D-08); (e) đăng xuất ngoại tuyến để token còn hiệu lực trên server đến hết hạn (D-01, chấp nhận).
Reason: Token opaque thu hồi được ngay (đặt `revoked_at`), không cần khóa ký/bí mật trên server (không có secret nào để lộ trong repo), không cần thư viện JWT; SHA-256 đủ vì token có 256 bit entropy (không dò từ điển được), nên lộ file CSDL không lộ token dùng được. Dùng lại thông số PBKDF2 đã duyệt và vector test RFC 7914 §11.
Alternatives considered: (a) JWT ký HS256 — không thu hồi được trước hạn nếu không có danh sách đen (khi đó lại cần bảng như token opaque), cần secret ký, cần thư viện hoặc tự viết mã mật mã. (b) Session cookie — hợp với web, không tự nhiên với `HttpURLConnection`, cần `CookieManager`. (c) Băm mật khẩu ở app rồi gửi hash — hash trở thành "mật khẩu", không tăng an toàn. (d) Đăng nhập Google/Facebook — Non-goal mới của thay đổi phạm vi.
Constraints: NFR-S1, NFR-S7, NFR-S8; HTTPS khi demo qua đường hầm (DEC-016); PBKDF2 tốn ≈ 100–300 ms CPU/lần trên laptop → phải giới hạn tần suất để không bị dùng làm DoS.
Consequences: Bảng `auth_tokens`, `security_answers` ở server (DATABASE-DESIGN); app bỏ các cột băm khỏi `users`; mọi route cần đăng nhập đi qua `AuthFilter` lấy `user_id` từ token (không từ body/path — chống IDOR); test: token hết hạn/thu hồi → 401; so sánh băm bằng `MessageDigest.isEqual`.
Consequences (bổ sung 11/11): bảng `auth_attempts` (email không tồn tại, khóa theo HMAC) và `server_meta`, cột `*_lock_count`/`*_last_fail_at` ở `users` (DATABASE-DESIGN); lớp `auth.LockPolicy` dùng chung hai bộ đếm; đánh đổi: kẻ tấn công có thể cố ý khóa tài khoản người khác tối đa 1 giờ/lần (chấp nhận — đổi lại không dò mật khẩu được); chấp nhận lộ email qua đăng ký (D-02).
Review khi: cần đăng nhập mạng xã hội, cần refresh token ngắn hạn, hoặc mở dịch vụ cho người ngoài nhóm.
Status: Proposed (Q5 của thay đổi phạm vi đã duyệt 2026-11-10). **thay đổi phạm vi:** thuật toán, thông số, token opaque, khóa tăng dần, HMAC câu hỏi giả **giữ nguyên**; phần hiện thực Java (`SecretKeyFactory`, `MessageDigest.isEqual`, `AuthFilter` Javalin, header HTTP `Authorization`, mã 401/409/429) **Superseded by DEC-018+**: băm ở dịch vụ `auth` bằng crate `pbkdf2` + `hmac` + `sha2`, so sánh bằng `subtle`; token gửi qua metadata gRPC `authorization: Bearer`, edge xác thực bằng `ValidateToken` và gắn `x-user-id` (DEC-020); mã lỗi theo gRPC (`UNAUTHENTICATED`, `ALREADY_EXISTS`, `RESOURCE_EXHAUSTED` + trailer `error-code`) — API_CONTRACT.

## DEC-014: Offline-first — SQLite máy là nguồn hiển thị, đồng bộ đẩy/kéo theo cursor, LWW

Context (bối cảnh, lực kéo, ràng buộc): Q3: học, ôn, quiz, thi thử phải chạy khi mất mạng; demo phụ thuộc mạng/laptop (R-35); lỗi đồng bộ có thể làm mất tiến độ (R-36); app vốn đọc/ghi qua Repository → SQLite.
Decision: Giao diện luôn đọc/ghi SQLite của máy như trước (không gọi mạng trực tiếp, trừ đăng nhập/đăng ký, bảng xếp hạng). Bảng cần đồng bộ thêm `uuid` (khóa toàn cục do máy sinh, UUID v4), `updated_at`, `deleted` (tombstone), `dirty`, `server_seq`. `SyncManager` chạy **đẩy rồi kéo**: đẩy bản ghi `dirty = 1` theo lô ≤ 500 (`POST /sync/push`), rồi kéo thay đổi có `server_seq > cursor` (`GET /sync/pull`) theo trang ≤ 500, lưu `cursor` vào `sync_state`. Xung đột: **last-write-wins** — bảng thường lấy bản server nhận sau cùng; `card_progress` lấy bản có `updated_at` lớn hơn (BR-062); bản ghi chỉ-thêm (`study_log`, lượt quiz/thi, `xp_events`) không xung đột (chèn nếu chưa có `uuid`). Xóa = `deleted = 1`, không xóa cứng trước khi server xác nhận. Kích hoạt: sau đăng nhập, khi app vào foreground (tối đa 1 lần/5 phút), sau mỗi phiên học/quiz/thi, nút "Đồng bộ ngay"; **không dùng WorkManager** (thêm thư viện, trái DEC-015).
Reason: Đơn giản nhất mà vẫn đáp ứng US-038..US-041: không cần CRDT hay log thao tác; cursor theo `server_seq` toàn cục không lệ thuộc đồng hồ máy khi kéo; LWW dễ giải thích và kiểm thử 2 máy; tombstone chống "hồi sinh" bản ghi đã xóa.
Alternatives considered: (a) Online-only (mọi màn hình gọi API) — đơn giản phía app nhưng hỏng demo khi rớt mạng, trái Q3. (b) Bảng outbox ghi từng thao tác (event log) — phát lại chính xác hơn nhưng phức tạp gấp đôi, phải giữ thứ tự thao tác; dữ liệu app là "trạng thái" (hộp Leitner) nên cờ `dirty` là đủ. (c) Kéo theo `updated_at` của máy — sai khi đồng hồ máy lệch. (d) Sao lưu/khôi phục toàn bộ mỗi lần — giữ làm phương án rollback (thay đổi phạm vi §5) nếu M2 trễ.
Constraints: minSdk 29 không có UPSERT (DEC-011) → app ghi dữ liệu kéo về bằng `UPDATE`, 0 dòng thì `INSERT`; không dùng `INSERT OR REPLACE` cho bảng cha (`decks`, `words`) vì REPLACE xóa dòng cũ và kích hoạt `ON DELETE CASCADE`.
Consequences: Mọi DAO đọc phải lọc `deleted = 0`; index UNIQUE của app thành index một phần `WHERE deleted = 0`; seed/nội dung cần `uid` ổn định cho từng từ để `card_progress` của 2 máy trỏ cùng một từ → định dạng file seed thêm trường `uid` (CODING-STANDARDS cập nhật ở phase 4); đồng hồ máy lệch có thể làm bản cũ thắng ở `card_progress` (server kẹp `updated_at` không quá giờ server + 5 phút); cần test 2 máy và mất mạng (TESTING-STRATEGY).
Review khi: cần cộng tác nhiều người trên cùng bộ từ, hoặc người dùng báo mất dữ liệu do LWW.
Status: Proposed (Q3 của thay đổi phạm vi đã duyệt 2026-11-10). **thay đổi phạm vi:** mô hình offline-first, LWW, cursor `server_seq`, tombstone **giữ nguyên**; phần REST (`POST /sync/push`, `GET /sync/pull`) **Superseded by DEC-018+**: thành RPC `SyncService.Push`/`Pull` (EP-11, EP-12) do dịch vụ `sync` hiện thực trên `sync.db` (DEC-019, DEC-020); `xp_events` ghi cùng giao dịch với outbox (DEC-021).

## DEC-015: Client HTTP — `HttpURLConnection` + `org.json`, chạy trên luồng `io`

Context (bối cảnh, lực kéo, ràng buộc): Người dùng chỉ duyệt dependency cho server; app Android **không thêm thư viện** (thay đổi phạm vi §6). Số endpoint ít (≈ 16), payload nhỏ (≤ 1 MB).
Decision: Gói `data.remote` có `ApiClient` dùng `java.net.HttpURLConnection` (timeout kết nối 10 s, đọc 15 s — NFR-N2), JSON bằng `org.json` có sẵn trong Android, chạy trên luồng `AppExecutors.io` (DEC-011); trả `Result<T>` với `ErrorCode` mạng (`NETWORK_OFFLINE`, `NETWORK_TIMEOUT`, `UNAUTHENTICATED`, …). Thử lại tối đa 2 lần (chờ 1 s rồi 3 s) chỉ cho lỗi mạng/5xx/429 và chỉ với yêu cầu idempotent (GET, `sync/push`). `NetworkMonitor` dùng `ConnectivityManager.registerDefaultNetworkCallback` (quyền `ACCESS_NETWORK_STATE`).
Reason: Không tăng kích thước APK, không cần duyệt thêm; `HttpURLConnection` đủ cho REST JSON đơn giản và dễ giải thích; `org.json` đã dùng cho seed/import (DEC-002, DEC-008).
Alternatives considered: (a) Retrofit + OkHttp + Gson — chuẩn công nghiệp nhưng thêm 3 thư viện, trái quyết định không thêm thư viện app. (b) Volley — thêm thư viện, API callback cũ. (c) `java.net.http.HttpClient` — không có trên Android.
Constraints: `org.json` chỉ là stub khi unit test JVM → lớp ánh xạ JSON (`data.remote.dto`) test bằng instrumented test hoặc thêm `testImplementation org.json:json` (đã ghi ở DEC-008/SYSTEM-BLUEPRINT (công nghệ)); phải tự đọc `getErrorStream()` khi mã ≥ 400.
Consequences: Viết tay ≈ 300–400 dòng (`ApiClient`, ánh xạ lỗi → `ErrorCode`, DTO ↔ JSON); test hợp đồng: app debug gọi server thật trên máy dev (TESTING-STRATEGY).
Review khi: số endpoint > 30 hoặc cần HTTP/2, tải tệp lớn, cache HTTP.
Status: **Superseded by DEC-018+** (DEC-020, thay đổi phạm vi Q1 duyệt 2026-11-11): app gọi gRPC qua `grpc-okhttp` + protobuf-lite; "app không thêm thư viện" bị đảo ngược có chủ ý. Giữ làm phương án rollback **RB-2** (SPIKE S-03 thất bại → edge thêm cổng JSON tối giản, app quay về `HttpURLConnection` + `org.json`).

## DEC-016: Hạ tầng demo — laptop chạy server + đường hầm HTTPS miễn phí, dự phòng LAN HTTP

Context (bối cảnh, lực kéo, ràng buộc): Không có ngân sách; cloud miễn phí dùng đĩa tạm làm mất file SQLite khi khởi động lại (R-40); điện thoại thật cần HTTPS (Android 9+ chặn cleartext mặc định); phòng chấm có thể không có Wi-Fi chung.
Decision: Server chạy trên laptop của nhóm bằng `./gradlew :server:run` (cổng 8080). Điện thoại thật truy cập qua **đường hầm HTTPS miễn phí** (Cloudflare quick tunnel `cloudflared tunnel --url http://localhost:8080`, hoặc ngrok); URL đưa vào `BuildConfig.API_BASE_URL` qua thuộc tính Gradle `learninge.apiBaseUrl` trong `local.properties` (không commit) hoặc ô "Địa chỉ server" chỉ có ở bản debug. Dự phòng: cùng Wi-Fi LAN qua HTTP tới IP laptop (chỉ bản debug, IP khai báo trong `network_security_config` của source set `debug`); emulator dùng `http://10.0.2.2:8080`. Server demo có dữ liệu mẫu (2 tài khoản demo, bảng xếp hạng có dữ liệu) và video quay trước.
Reason: Miễn phí, file SQLite nằm trên ổ thật, HTTPS hợp lệ không cần mua tên miền/chứng chỉ; app offline-first nên rớt mạng vẫn demo được phần học.
Alternatives considered: (a) Render/Railway/Fly free tier — đĩa tạm hoặc ngủ sau ≈ 15 phút, khởi động lạnh 30–60 s. (b) VPS trả phí — tốn tiền, phải quản trị. (c) Cho phép cleartext toàn bộ trong release — trái NFR-S3, MASVS-NETWORK.
Constraints: URL quick tunnel đổi mỗi lần chạy; đường hầm kết thúc TLS ở hạ tầng nhà cung cấp (nhà cung cấp thấy được lưu lượng) — chỉ dùng tài khoản demo, ghi rõ trong THREAT_MODEL; server chỉ đọc IP thật từ `CF-Connecting-IP`/`X-Forwarded-For` khi `LEARNINGE_TRUST_PROXY=true` (để giới hạn tần suất theo IP đúng, không cho client giả header khi chạy LAN).
Consequences: Ô "Địa chỉ server" ở bản debug (SCR-35 hoặc tùy chọn nhà phát triển); hướng dẫn sử dụng hướng dẫn chạy server + tunnel; kịch bản demo dự phòng (tunnel → LAN → video).
Review khi: cần chạy liên tục ngoài buổi demo hoặc có người dùng ngoài nhóm.
Status: Proposed (Q2 của thay đổi phạm vi đã duyệt 2026-11-10). **thay đổi phạm vi:** phần `./gradlew :server:run`, cổng 8080, đường hầm HTTP/1.1 (quick tunnel `--url http://localhost:8080`), `network_security_config` làm lớp chặn cleartext **Superseded by DEC-018+**: gRPC cần HTTP/2 tới origin (DEC-020, SPIKE S-02), nơi triển khai là DEC-023 (chưa chốt); nguyên tắc dự phòng (LAN chỉ bản debug, emulator, video) và cờ tin proxy vẫn giữ.

## DEC-017: Pixel layer — VectorDrawable + PNG nodpi, không thư viện game

Context (bối cảnh, lực kéo, ràng buộc): REQ-023/REQ-024 thêm ngọn lửa 5 giai đoạn, 5 rương Leitner, màn ăn mừng, bản đồ chủ đề kiểu pixel-art; thiết kế v2 phẳng vẫn là nền; không thêm thư viện app; NFR-P6 (vẽ khung ≤ 16 ms).
Decision: Sprite nhỏ (ngọn lửa, rương, huy hiệu, cúp) là **VectorDrawable** theo lưới 1 px = 4 dp, ≤ 30 path, màu lấy token v2; bản đồ là PNG trong `drawable-nodpi` hiển thị với `filterBitmap = false`; hoạt ảnh bằng `AnimatedVectorDrawable`/`ValueAnimator` có sẵn; chữ luôn nằm ngoài sprite (TextView, TalkBack đọc được). Logic Java thuần ở `domain.rules`: `FlameStage.of(streak)`, `ThemeStars`, `ChestCounts` (BR-070..BR-072) có unit test.
Reason: Không phụ thuộc thư viện, sắc nét mọi mật độ màn hình, APK nhỏ (NFR-P5), tách được khỏi luồng chính (rollback thay đổi phạm vi §5: bỏ pixel không ảnh hưởng chức năng).
Alternatives considered: (a) LibGDX/engine game — nặng, thừa cho vài sprite tĩnh. (b) Lottie — thêm thư viện, cần After Effects. (c) PNG cho mọi mật độ (mdpi..xxxhdpi) — tăng APK, dễ mờ khi co giãn.
Constraints: NFR-P6, NFR-X1 (mô tả nội dung cho TalkBack), NFR-P5.
Consequences: UI-UX-SPECIFICATION (hệ thống thiết kế) thêm mục Pixel layer (người thiết kế); lập trình viên đo jank bằng `dumpsys gfxinfo` ở SCR-06 chế độ Bản đồ.
Review khi: thêm hoạt ảnh phức tạp (nhân vật di chuyển) hoặc khung hình vượt 16 ms.
Status: Proposed (thay đổi phạm vi ngày 2026-11-10); không bị thay đổi phạm vi ảnh hưởng

## DEC-018: Backend viết bằng Rust — tokio + tonic/prost, rusqlite, driver mongodb

Context (bối cảnh, lực kéo, ràng buộc): thay đổi phạm vi (duyệt 2026-11-11) yêu cầu backend Rust để đạt điểm "Chất lượng — kiến trúc" và "Sáng tạo & mở rộng", và để nhóm học Rust; 2 thành viên làm server chưa dùng Rust (R-41); vấn đáp yêu cầu mỗi người giải thích được code của mình (R-47); chỉ có gRPC, không cần HTTP/JSON công khai; quy tắc bảo mật đã duyệt (PBKDF2 120k) phải giữ.
Decision: Rust stable **1.99.1** ghim trong `rust-toolchain.toml`, edition 2021, Cargo workspace. Runtime **tokio 1.54.1**; gRPC **tonic 0.14.9** + **prost 0.14.4** (sinh mã bằng `tonic-prost-build` 0.14.9 + `protoc-bin-vendored` 3.3.0), `tonic-health`, `tower` 0.5.3; SQLite bằng **`rusqlite` 0.41.1** (`bundled`, chạy trong `spawn_blocking`, một kết nối ghi); MongoDB bằng driver chính thức **`mongodb` 3.9.1**; băm **`pbkdf2` 0.13.0 + `hmac` 0.13.0 + `sha2` 0.11.1** (PBKDF2-HMAC-SHA256 120.000 vòng, salt 16 byte, khóa 32 byte — đúng DEC-003/DEC-013), `subtle`, `rand`, `base64`, `uuid` (v4, v7), `chrono` + `chrono-tz`, `tracing` + `tracing-subscriber` (JSON), `thiserror`, `serde`/`serde_json` (chỉ cho công cụ và cấu hình). Phiên bản và lý do chi tiết: SYSTEM-BLUEPRINT (công nghệ) §Server; tất cả phát hành ≥ 2 tuần trước 2026-11-12.
Reason: tonic là gRPC thuần Rust phổ biến nhất, chạy trên tokio cùng driver mongodb → một runtime duy nhất; `rusqlite` cho SQL viết tay nhìn thấy được (dễ vấn đáp, chống injection bằng tham số), `bundled` cho build giống nhau trên Windows và Docker; crate RustCrypto cho PBKDF2 là hiện thực chuẩn, có test vector, giữ nguyên thông số đã duyệt; `protoc-bin-vendored` tránh cài `protoc` tay trên Windows.
Alternatives considered: (a) Giữ Java/Javalin (DEC-012) — nhóm quen, ít rủi ro, nhưng trái quyết định người dùng (Q1 thay đổi phạm vi); giữ làm rollback RB-1. (b) Go + grpc-go — học nhanh hơn Rust nhưng người dùng chọn Rust. (c) `sqlx` thay `rusqlite` — async, kiểm truy vấn lúc biên dịch, nhưng macro + chế độ offline phức tạp cho người mới, không lợi hiệu năng với SQLite (SYSTEM-BLUEPRINT (công nghệ)). (d) `axum` + `tonic` chung — thừa vì không có HTTP công khai. (e) Argon2id thay PBKDF2 — mạnh hơn trước GPU nhưng trái quy tắc bảo mật đã duyệt (BR-002, DEC-003); chỉ xem lại qua CR mới.
Constraints: Q1 đã duyệt danh sách crate; không thêm crate ngoài danh sách khi chưa duyệt; `Cargo.lock` commit; `cargo audit` 0 lỗi mức cao (NFR-S10); MSRV ≥ 1.89 (do `rusqlite` 0.41).
Consequences: (1) SPIKE S-01 (T-141) dựng workspace + tonic hello + rusqlite + mongodb + compose + chế độ gộp, xác nhận build Windows và Docker trước khi code dịch vụ. (2) T-140 học Rust trước khi viết dịch vụ. (3) Lỗi `rusqlite` là đồng bộ → mọi truy cập CSDL phải bọc `spawn_blocking`, code review chặn gọi `rusqlite` trực tiếp trong hàm async. (4) Không còn phụ thuộc JVM ở server; app vẫn Java. (5) Nếu go/no-go 29/11 thất bại → RB-1 quay về DEC-012.
Review khi: go/no-go Rust 29/11 thất bại; `cargo audit` báo lỗi mức cao không vá được; nâng tonic/prost lên dòng mới (đổi API sinh mã).
Status: Proposed (thay đổi phạm vi Q1 duyệt hướng 2026-11-11, Phan Anh Tuấn; chờ duyệt tài liệu G3/M0b)

## DEC-019: Microservice 6 dịch vụ gRPC, database-per-service, có chế độ gộp

Context (bối cảnh, lực kéo, ràng buộc): Người dùng chọn 6 dịch vụ (Q3 trong 6 câu trả lời của thay đổi phạm vi); yêu cầu NFR-N5 (Mongo hoặc dịch vụ phụ ngừng thì đăng nhập + đồng bộ vẫn chạy), NFR-S9 (chỉ edge lộ cổng); nhóm 4 người, mỗi người phụ trách trọn dịch vụ để vấn đáp (người phụ trách: **chưa phân công**, Q6); laptop Windows có thể không chịu được 7 container lúc demo (R-45).
Decision: 6 dịch vụ — `edge` (cổng vào duy nhất, 50051), `auth` (50052, `auth.db`), `sync` (50053, `sync.db`), `content` (50054, `learninge_content`), `gamification` (50055, `learninge_game`), `insight` (50056, `learninge_insight`). Mỗi dịch vụ **sở hữu riêng** kho dữ liệu, không đọc kho của dịch vụ khác; giao tiếp chỉ qua gRPC gói `learninge.internal.v1` (đồng bộ) hoặc sự kiện outbox (bất đồng bộ, DEC-021). Mỗi dịch vụ là một crate thư viện có `serve(config, shutdown)` + `main` mỏng; binary `learninge-server --all` chạy cả 6 trong một tiến trình (chế độ gộp, rollback RB-3). Trách nhiệm, RPC sở hữu và chế độ lỗi: SYSTEM-BLUEPRINT §Backend, §Chế độ lỗi.
Reason: Tách theo ranh giới dữ liệu và mức quan trọng: lõi (auth, sync) chỉ phụ thuộc SQLite nên sống độc lập với Mongo; dịch vụ phụ (content, gamification, insight) hỏng thì chỉ chức năng tương ứng suy giảm; mỗi dịch vụ nhỏ (≈ 1–4 ngày công) vừa một người phụ trách; chế độ gộp giữ lợi ích kiến trúc khi trình bày nhưng giảm chi phí vận hành lúc demo.
Alternatives considered: (a) 4 dịch vụ (gộp content vào sync, insight vào gamification) — ít vận hành hơn, nhóm trưởng đề xuất ban đầu, người dùng chọn 6. (b) Monolith modular một tiến trình — đơn giản nhất nhưng trái yêu cầu microservice; vẫn có dưới dạng chế độ gộp. (c) Dịch vụ notification riêng — không cần vì nhắc học là `AlarmManager` cục bộ (DEC-004). (d) CSDL dùng chung giữa dịch vụ — đơn giản truy vấn nhưng phá ranh giới và NFR-N5.
Constraints: Không service mesh, không service discovery ngoài DNS của compose/nền tảng; không gọi vòng (chỉ gamification → sync khi rebuild); không streaming RPC.
Consequences: (1) Cần crate `common` (config, tracing, lỗi → `Status`, interceptor, outbox, migrator) để 6 dịch vụ không lặp mã. (2) `GetMe` phải ghép ở edge từ 3 nguồn, có `stats_partial`. (3) Test hợp đồng chạy trên cả chế độ tách và gộp. (4) Mỗi dịch vụ một image Docker; compose có healthcheck. (5) Q6 phân công phải chốt trước khi code (thay đổi phạm vi §6).
Review khi: chi phí vận hành vượt lợi ích ở M2 (→ demo bằng chế độ gộp, RB-3); cần nhiều bản edge (cache token phải chung).
Status: Proposed (thay đổi phạm vi duyệt 2026-11-11; chờ duyệt tài liệu G3/M0b)

## DEC-020: gRPC từ đầu đến cuối — app gọi edge trực tiếp bằng `grpc-okhttp` + protobuf-lite

Context (bối cảnh, lực kéo, ràng buộc): thay đổi phạm vi chọn app gọi gRPC trực tiếp, không qua cổng REST (câu trả lời (4)); Q1 duyệt thư viện app — **đảo ngược "app không thêm thư viện" của DEC-015**; Q2 duyệt edge tự viết bằng Rust/tonic thay Envoy; Q5 chốt cách lộ gRPC ra Internet sau SPIKE S-02. App minSdk 29, Java 17, AGP 9.5.1; offline-first giữ nguyên.
Decision: (1) **App:** `io.grpc:grpc-okhttp`, `grpc-protobuf-lite`, `grpc-stub` **1.85.1**; `com.google.protobuf:protobuf-javalite` **4.36.2**; plugin Gradle `com.google.protobuf` **0.10.0** + `protoc` 4.36.2 + `protoc-gen-grpc-java` 1.85.1 (`lite`); `org.apache.tomcat:annotations-api` 6.0.53 `compileOnly`. Một `ManagedChannel` dùng chung, **blocking stub chạy trên `AppExecutors.io`**, deadline từng lời gọi, `ClientInterceptor` gắn `authorization`, `x-request-id`, `x-client-version`; TLS mặc định, plaintext chỉ khi `BuildConfig.DEBUG` và host trong danh sách trắng (chặn bằng mã vì `grpc-okhttp` không đọc `network_security_config`); thử lại tường minh theo BR-077 cho RPC idempotent; ánh xạ mã gRPC + trailer `error-code` → `ErrorCode`. Chỉ `data.remote` thấy lớp proto. (2) **Edge** tự viết bằng tonic: hiện thực toàn bộ `learninge.app.v1` bằng hàm chuyển tiếp tường minh, xác thực token, xóa metadata nội bộ từ client, rate limit theo IP, truyền deadline (còn lại − 100 ms), không thử lại xuống dưới. (3) **Hợp đồng:** proto3, unary hết, 16 RPC ánh xạ 1-1 EP-01..EP-16; mã lỗi gRPC chuẩn + trailer `error-code`/`retry-after-seconds`, không dùng `google.rpc.Status`; message vào edge ≤ 1 MiB. (4) **Quan sát** (CR DEC-025): `x-request-id` từ app qua edge tới mọi dịch vụ và payload sự kiện; `tracing` JSON. Chi tiết: SYSTEM-BLUEPRINT §Tầng mạng, §Metadata, §Deadline; API_CONTRACT.
Reason: Một giao thức, một hợp đồng có kiểu cho cả app và server (không viết tay DTO JSON hai phía); protobuf-lite nhỏ, thân thiện R8; `grpc-okhttp` là transport khuyến nghị cho Android, dùng TLS nền tảng (API 29 có TLS 1.3 + ALPN); blocking stub trên luồng `io` khớp mô hình luồng sẵn có (DEC-011) và `Result<T>`, dễ giải thích hơn future/async stub; edge tự viết thì ai cũng đọc được và tính là 1 trong 6 dịch vụ.
Alternatives considered: (a) Edge REST/JSON (grpc-gateway hoặc tự viết) + app giữ `HttpURLConnection` — không thêm thư viện app nhưng thêm lớp chuyển đổi, trái câu trả lời (4); giữ làm RB-2. (b) Envoy làm edge (gRPC-JSON transcoder hoặc proxy) — cấu hình YAML khó vấn đáp, không tính như dịch vụ tự viết (Q2). (c) gRPC-Web — cần proxy, không cần thiết cho app native. (d) Future stub / Kotlin coroutine — app là Java, thêm mô hình đồng thời mới. (e) Retry qua service config của gRPC — ẩn trong cấu hình JSON, khó test; chọn vòng thử lại tường minh. (f) `grpc-netty`/Cronet — nặng hơn hoặc thêm phụ thuộc Play Services.
Constraints: `protobuf-javalite` phải cùng phiên bản với `protoc` (mã sinh yêu cầu runtime ≥ phiên bản sinh); `grpc-protobuf-lite` 1.85.1 khai báo 3.25.9 → ghi đè tường minh lên 4.36.2, xác minh ở S-03; tương thích plugin protobuf 0.10.0 với AGP 9.5.1 **chưa kiểm chứng**; gRPC bắt buộc HTTP/2 end-to-end (tunnel/proxy phải hỗ trợ h2 tới edge — S-02).
Consequences: (1) **SPIKE S-03** (T-143): plugin + `grpc-okhttp` với AGP 9.5.1, emulator gọi `Health.Check`, bản release R8 vẫn gọi được; thất bại → sinh mã bằng `protoc` ngoài Gradle và commit mã sinh, nếu vẫn không được → RB-2. (2) APK tăng ≈ 1–2 MB (okhttp nhúng, okio, protobuf-lite) — vẫn trong NFR-P5. (3) Thêm quy tắc R8 cho lớp sinh protobuf-lite. (4) Mã lỗi trong SRS/Use-Cases (luồng) đổi cách ghi sang gRPC (người phân tích). (5) `TokenStore`, `SyncManager`, `NetworkMonitor` giữ; `ApiClient`/`*Api` thay bằng `GrpcClient`/`*Remote`.
Review khi: S-02 hoặc S-03 thất bại; cần streaming (đồng bộ thời gian thực); số RPC > 40.
Status: Proposed (thay đổi phạm vi Q1, Q2, Q5 duyệt 2026-11-11; phiên bản chốt sau S-03; chờ duyệt tài liệu G3/M0b)

## DEC-021: Lưu trữ lai — SQLite là nguồn sự thật, MongoDB là kho phụ nạp qua outbox

Context (bối cảnh, lực kéo, ràng buộc): Người dùng chọn MongoDB chạy song song SQLite, **SQLite vẫn là CSDL chính**, Mongo cho 4 nhóm dữ liệu: sự kiện XP + bảng xếp hạng, nội dung học, log/audit, bản sao phân tích. Ghi kép (dual-write) SQLite + Mongo trong một yêu cầu dễ lệch dữ liệu khi một bên lỗi (R-44); NFR-N5 yêu cầu lõi chạy khi Mongo ngừng; không muốn yêu cầu replica set (change stream/transaction của Mongo cần replica set).
Decision: (1) `auth.db` (tài khoản, token, câu trả lời bảo mật, bộ đếm khóa) và `sync.db` (tiến độ, bộ từ, lượt làm bài, **`xp_events` gốc**) là **nguồn sự thật**, không chép nguyên bản sang Mongo. (2) Mongo giữ: `learninge_content` (nguồn = JSON trong git, nạp bằng `content-import`, idempotent theo `code`), `learninge_game` (`xp_events` bản sao, `weekly_xp`, `players`), `learninge_insight` (`audit_events` TTL 90 ngày, `request_logs` TTL 7 ngày, `analytics_daily` khử định danh). (3) **Outbox transactional:** thay đổi nghiệp vụ và bản ghi outbox ghi trong **cùng giao dịch SQLite**; relay nền trong dịch vụ phát đọc theo `id`, gọi `EventSink.Publish` của từng đích, ack thì đánh dấu đã phát, lỗi thì lùi dần 1 s → trần 15 s và thử lại ngay khi đích `SERVING` (để NFR-N6 ≤ 60 s đạt); **tiến độ phát riêng cho từng đích**. (4) Bên nhận upsert theo `event_id` (UUIDv7) → hiệu quả đúng một lần; `weekly_xp` tính lại từ `xp_events` (không `$inc`). (5) Không dùng transaction/change stream của Mongo. (6) Mất dữ liệu Mongo → `content-import` + `gamification rebuild` (từ `sync.ExportXpEvents`); audit/log chấp nhận mất. Bảng phân bổ: thay đổi phạm vi §3; lược đồ: DATABASE-DESIGN (B).
Reason: Outbox cùng giao dịch loại bỏ ghi kép — không thể có thay đổi nghiệp vụ mà mất sự kiện, hoặc sự kiện mà không có thay đổi; giao ít nhất một lần + idempotent đơn giản hơn exactly-once phân tán; Mongo phù hợp dữ liệu ghi nối đuôi, tài liệu lồng nhau, TTL và aggregation xếp hạng; SQLite giữ ràng buộc duy nhất và giao dịch ACID cho khóa tăng dần, LWW.
Alternatives considered: (a) Ghi kép trực tiếp SQLite rồi Mongo trong yêu cầu — lệch khi Mongo lỗi giữa chừng, làm chậm/hỏng Push khi Mongo ngừng (trái NFR-N5). (b) Change stream của Mongo / CDC từ SQLite — cần replica set hoặc công cụ ngoài (Debezium), quá nặng. (c) Hàng đợi tin nhắn (NATS, Kafka, RabbitMQ) — thêm một hạ tầng nữa phải vận hành và giải thích. (d) Chỉ Mongo cho mọi thứ — trái quyết định "SQLite là CSDL chính". (e) Chỉ SQLite (bỏ Mongo) — đơn giản nhất nhưng trái lựa chọn của người dùng; là rollback RB-4 khi demo.
Constraints: Dữ liệu sang Mongo **không chứa email, hash, token, câu trả lời bảo mật**; insight chỉ dùng `user_hmac`; xóa tài khoản lan qua `user.deleted` (purge ≤ 1 phút khi đích chạy).
Consequences: (1) Bảng `outbox` (và tiến độ theo đích) trong `auth.db`, `sync.db` — DATABASE-DESIGN chốt lược đồ. (2) Crate `common::outbox` dùng chung; test: Mongo tắt → Push OK; gửi trùng → 1 bản ghi; bật lại → xả ≤ 60 s (NFR-N6). (3) Bảng xếp hạng nhất quán cuối cùng (trễ ≤ 60 s sau Push). (4) Cảnh báo log khi outbox > 10 000 dòng chưa phát.
Review khi: cần đọc nhất quán mạnh từ Mongo; khối lượng sự kiện vượt khả năng relay (> 1000 sự kiện/giây); chuyển Mongo sang replica set có sẵn change stream.
Status: Proposed (thay đổi phạm vi duyệt 2026-11-11; chờ duyệt tài liệu G3/M0b)

## DEC-022: Repo server riêng `LearningE_Server`; proto gốc ở repo server, app giữ bản sao + `proto.lock`

Context (bối cảnh, lực kéo, ràng buộc): Người dùng chọn backend ở repo riêng `https://github.com/at1308033-sketch/LearningE_Server` (câu trả lời (5)); Q4 duyệt nơi giữ proto; app (Gradle/Java) và server (Cargo/Rust) có công cụ build khác nhau; nhóm sinh viên dễ lỗi với git submodule (R-50).
Reason: Hai chuỗi công cụ độc lập, build/CI riêng, lịch sử riêng; một nơi duy nhất sửa proto tránh lệch; sao chép có khóa SHA đơn giản và kiểm được hơn submodule hoặc repo proto thứ ba; app không thấy gói nội bộ.
Alternatives considered: (a) Monorepo app + server — một lần clone nhưng trộn Gradle và Cargo, trái câu trả lời (5). (b) Git submodule proto — dễ quên cập nhật, khó với nhóm mới. (c) Repo proto thứ ba — thêm một repo phải đồng bộ. (d) Sinh mã Java ở repo server rồi phát hành artifact Maven — cần kho Maven, thừa.
Constraints: Mọi thay đổi gói công khai phải qua PR ở repo server trước, rồi `sync-proto` sang app trong PR riêng; proto v1 **đóng băng ở M0b (16/11)**.
Consequences: (1) Nộp bài kèm 2 link GitHub. (2) CI/kiểm tra thủ công: `sync-proto` báo khớp SHA trước mỗi lần build release. (3) CODING-STANDARDS thêm mục repo server (rustfmt, clippy, quy tắc proto). (4) App build lại sinh stub khi bản sao proto đổi.
Review khi: cần v2 phá tương thích (thêm gói `learninge.app.v2`, giữ v1 tới khi app cũ hết dùng); thêm client thứ hai (web).
Status: Proposed (thay đổi phạm vi Q4 duyệt 2026-11-11; chờ duyệt tài liệu G3/M0b)

## DEC-023: Nơi triển khai demo/chấm — CHƯA CHỐT (thay đổi phạm vi Q3)

Context (bối cảnh, lực kéo, ràng buộc): Q3 của thay đổi phạm vi **chưa chốt** (người dùng cân nhắc MongoDB Atlas, Railway, VPS; thay đổi phạm vi ban đầu khuyến nghị laptop + Docker Compose + tunnel). gRPC cần HTTP/2 end-to-end tới edge; `auth.db`, `sync.db` cần đĩa bền vững; không có ngân sách cố định; phòng chấm có thể không ổn định mạng; THREAT_MODEL yêu cầu chỉ edge lộ ra.
Decision: _(chờ người dùng — chốt trước T-145, tức trước khi bắt đầu code server; muộn nhất trước T-152 runbook demo)_. Cho tới khi chốt, mọi tài liệu và mã **trung lập với nơi chạy**: mỗi dịch vụ là một image Docker; `deploy/docker-compose.yml` cho dev (Mongo `mongo:8.0.34` + 6 dịch vụ, chỉ edge publish `127.0.0.1:50051`); khác biệt môi trường chỉ nằm ở `docker-compose.override.yml`, biến môi trường (`LEARNINGE_MONGO_URI`, `EDGE_TRUST_PROXY`, `LISTEN_ADDR`) và `BuildConfig.GRPC_HOST/PORT/TLS` của bản release. Phương án:
| Phương án | Ưu | Nhược / cần xác minh |
|---|---|---|
| P1 Laptop + Docker Compose + tunnel HTTP/2 (ngrok upstream h2 hoặc Cloudflare Tunnel có tên miền + gRPC) | 0 đồng; dữ liệu trên ổ của nhóm; giống dev | Laptop + Docker Desktop phải chạy lúc chấm; tunnel miễn phí có hỗ trợ gRPC không (SPIKE S-02, hạn 20/11); URL có thể đổi; nhà cung cấp tunnel thấy lưu lượng |
| P2 MongoDB Atlas (free) cho Mongo + dịch vụ chạy ở P1/P3/P4 | Mongo được quản lý, sao lưu, TLS | Phụ thuộc Internet lúc chấm; dữ liệu (đã khử định danh) ở bên thứ ba; giới hạn gói free; không giải quyết việc lộ edge |
| P3 Railway (PaaS container) | Không phụ thuộc laptop; URL ổn định; TLS sẵn; mạng riêng giữa dịch vụ | Phí sau credit dùng thử; phải xác minh proxy công khai hỗ trợ gRPC/HTTP/2 (hoặc TCP proxy + TLS ở edge); volume SQLite gắn 1 replica; 7 container có thể vượt hạn mức |
| P4 VPS (1–2 vCPU, 2 GB) + compose + reverse proxy TLS (vd Caddy) chuyển h2c tới edge | Ổn định, toàn quyền, giống dev nhất, phí cố định thấp | Tốn phí hằng tháng; tự quản trị (SSH key, firewall chỉ mở 443, cập nhật, sao lưu); cần tên miền/chứng chỉ |
Reason: Tách quyết định nơi chạy khỏi kiến trúc để không chặn thiết kế và code; Docker image là đơn vị chung cho cả 4 phương án.
Alternatives considered: Như bảng trên; ngoài ra Render/Fly free tier (ngủ, đĩa tạm — đã loại ở DEC-016), chạy tiến trình thường không Docker (giữ làm dự phòng bằng chế độ gộp `--all`).
Constraints: Không đưa bí mật vào repo (dùng `.env` không commit hoặc secret của nền tảng); release app bắt buộc TLS; Mongo, cổng 50052–50056 không lộ ra Internet ở mọi phương án; nếu dùng Atlas thì bật IP allowlist + user quyền tối thiểu.
Consequences: Khi chốt: cập nhật Decision + Status của DEC-023, SYSTEM-BLUEPRINT §Triển khai, THREAT_MODEL (biên tin cậy: TLS kết thúc ở đâu, ai thấy lưu lượng), PROJECT-PLAN (rủi ro) (R-42, R-45), runbook T-152; nếu phương án cần TLS tại edge thì bật feature TLS của tonic (thêm crate phải duyệt). **Khi chốt Q3, chép nguyên cột "Kiểm soát tối thiểu (bắt buộc)" của phương án được chọn từ bảng P1–P4 ở THREAT_MODEL §Phương án triển khai (cùng đoạn "Kiểm soát chung") vào Consequences của DEC-023** — các kiểm soát đó là điều kiện để phương án được dùng cho demo/chấm (kiểm bằng TC-SEC-38 trên phương án đã chốt) và là đầu vào cho runbook T-152.
Review khi: kết quả SPIKE S-02; thay đổi ngân sách; phòng chấm không có Internet.
Status: **Proposed — chưa chốt** (Q3 thay đổi phạm vi; người quyết định: Phan Anh Tuấn)
