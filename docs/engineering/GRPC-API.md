# gRPC API — hợp đồng API máy chủ

> **TỰ SINH** từ bộ phân tích thiết kế đã duyệt của nhóm — không sửa trực tiếp tệp này; đề xuất thay đổi qua Issue/PR để nhóm trưởng cập nhật nguồn và sinh lại. Danh mục tài liệu: [docs/README.md](../README.md).

**Mục đích:** hợp đồng giữa ứng dụng và máy chủ: dịch vụ và RPC gRPC `learninge.app.v1` (tài khoản, đồng bộ, bảng xếp hạng, nội dung) và RPC nội bộ, định nghĩa .proto, mã trạng thái + `error-code`, giới hạn, deadline, xác thực bằng token. Đổi .proto phải theo quy trình trong REPO_CONVENTIONS.

## Kiểu interface (REST/GraphQL/gRPC/CLI/SDK)
- **gRPC** trên HTTP/2, thông điệp **proto3**; mọi RPC là **unary** (một yêu cầu, một phản hồi).
- Điểm vào duy nhất cho app: dịch vụ `edge`, cổng **50051**. Các dịch vụ `auth` 50052, `sync` 50053, `content` 50054, `gamification` 50055, `insight` 50056 chỉ nghe trên mạng nội bộ, **không bao giờ** được app gọi trực tiếp.
- Vận chuyển: bản release chỉ dùng **TLS** (`useTransportSecurity()`; TLS kết thúc ở tunnel/nền tảng chứa hoặc ở `edge` tùy phương án Q3 — SYSTEM-BLUEPRINT). Plaintext h2c chỉ ở bản debug và chỉ tới host trong danh sách trắng (`10.0.2.2`, IP LAN khai báo trong `BuildConfig`) — `grpc-okhttp` **không** tuân `network_security_config`, nên chặn bằng mã (`GrpcChannelFactory` từ chối `usePlaintext()` khi `!BuildConfig.DEBUG`).
- Client app: `grpc-okhttp` + `grpc-protobuf-lite` + `grpc-stub`, stub **blocking** chạy trên luồng `io` (DEC-011, DEC-021); một `ManagedChannel` dùng chung cho cả app.
- **Không dùng streaming** (cả server-stream, client-stream, bidi): dữ liệu lớn nhất (Pull) đã phân trang bằng cursor ≤ 500 bản ghi; app offline-first chủ động kéo theo mốc (DEC-014), không cần đẩy thời gian thực (Non-goal thay đổi phạm vi); stream dài bị tunnel/proxy cắt khi rảnh và khó thử lại an toàn. Ngoại lệ duy nhất: `grpc.health.v1.Health/Watch` có sẵn trong `tonic-health` nhưng app không dùng.

## Link spec (OpenAPI/GraphQL schema/...)
- **Nguồn sự thật**: các tệp `.proto` trong repo server `https://github.com/at1308033-sketch/LearningE_Server` (DEC-022, thay đổi phạm vi Q4):
```text
LearningE_Server/proto/
  learninge/app/v1/common.proto            # User, Stats, AuthSession
  learninge/app/v1/auth.proto              # AuthService      (EP-02..EP-10)
  learninge/app/v1/sync.proto              # SyncService      (EP-11, EP-12)
  learninge/app/v1/leaderboard.proto       # LeaderboardService (EP-13)
  learninge/app/v1/content.proto           # ContentService   (EP-14..EP-16)
  learninge/internal/v1/backends.proto # TokenValidator, AuthBackend, SyncBackend, ContentBackend, GamificationBackend, InsightBackend
  learninge/internal/v1/events.proto   # Event + EventSink (outbox relay)
  grpc/health/v1/health.proto          # bản chuẩn của gRPC (EP-01)
```
- Rust: crate `crates/proto` sinh mã bằng `tonic-build` + `prost` (`protoc` từ `protoc-bin-vendored`, không cài tay). App: plugin Gradle `com.google.protobuf`, `option lite` cho `java` và `grpc`; app **chỉ** nhận gói công khai + `grpc/health` (không nhận `learninge/internal`).
- Bản sao trong repo app: `app/src/main/proto/` + tệp `app/src/main/proto/proto.lock`, do `tools/sync-proto.ps1` tạo (chép tệp, ghi SHA commit server và SHA-256 từng tệp); chế độ `-Check` chạy trong task Gradle `verifyProtoLock` trước `generateProto` và **làm hỏng build** nếu tệp bị sửa tay:
```text
# proto.lock — sinh bởi tools/sync-proto.ps1, KHÔNG sửa tay
server_repo=https://github.com/at1308033-sketch/LearningE_Server
server_commit=<40 ký tự hex>
synced_at=2026-11-16T10:00:00Z
sha256 learninge/app/v1/common.proto <64 hex>
sha256 learninge/app/v1/auth.proto <64 hex>
... (mỗi tệp một dòng)
```
- Tài liệu này mô tả proto v1 **để duyệt** (M0b, 16/11). Sau khi đóng băng, tệp `.proto` là chuẩn; mọi khác biệt giữa code, `.proto` và tài liệu là lỗi, hoặc phải có CR. Test hợp đồng (T-153): client Rust sinh từ proto + kịch bản `grpcurl` cho EP-01..EP-16 kiểm mã trạng thái, trailer `error-code`, trường trả về. Server reflection chỉ bật ở `edge` khi `LEARNINGE_GRPC_REFLECTION=true` **và** `APP_ENV=dev`; `APP_ENV=prod` mà cờ bật → từ chối khởi động; dịch vụ nội bộ không bao giờ bật (THREAT_MODEL D-15); bản demo tắt, `grpcurl` dùng `-import-path proto -proto …`.

## Xác thực và phân quyền
- Cơ chế (Bearer/OAuth/API key), phạm vi quyền, hết hạn, giới hạn tốc độ:
  - **Bearer token opaque** (DEC-013, BR-057), gửi trong metadata `authorization: Bearer <token>`; token = 32 byte ngẫu nhiên an toàn (`rand::rngs::OsRng`) mã hóa base64url không đệm (43 ký tự `[A-Za-z0-9_-]`). `auth` lưu `SHA-256(token)` hex; hạn **30 ngày** kể từ khi cấp, không gia hạn trượt.
  - Luồng xác thực ở `edge`: đọc `authorization` → tính `SHA-256` → tra cache (≤ 60 s, không quá `expires_at`) → nếu trượt gọi `TokenValidator.ValidateToken(token_sha256)` → gắn metadata nội bộ `x-user-id` (UUID) rồi chuyển tiếp. **Token thô không đi vào mạng nội bộ.** `edge` **xóa** mọi metadata `x-user-id`, `x-internal-*` do client gửi trước khi xử lý (chống giả danh nội bộ).
  - Thu hồi: EP-04 thu hồi token hiện tại; EP-07, EP-09, EP-10 thu hồi **mọi** token của người dùng (BR-058); `edge` xóa ngay các mục cache của người dùng đó khi RPC trả OK — user lấy từ `x-user-id` (EP-04, EP-07, EP-10) hoặc `AuthSession.user.id` trả về (EP-09); cache theo `token_sha256`, TTL = min(60 s, `expires_at`) (chỉ có một `edge` nên cache không lệch; THREAT_MODEL D-12).
  - Một vai trò ("người học"); **phân quyền theo chủ sở hữu**: `user_id` chỉ lấy từ `x-user-id` do `edge` gắn, **không bao giờ** từ message. Bản ghi của người khác coi như không tồn tại (`NOT_FOUND` hoặc `REJECTED` trong sync) — chống IDOR/BOLA (OWASP API1).
  - Token thiếu/sai/hết hạn/bị thu hồi → `UNAUTHENTICATED` + reason `UNAUTHENTICATED` (một mã chung, không nói lý do) → app áp BR-059 (xóa token, giữ dữ liệu `dirty`, về SCR-02).
  - RPC công khai (không cần token): EP-01 `Health/Check`, EP-02 `Register`, EP-03 `Login`, EP-08 `GetResetQuestion`, EP-09 `ResetPassword`. **ContentService cần token** (THREAT_MODEL D-08, giữ).
  - **Giới hạn tốc độ** (bộ nhớ của `edge` theo IP + bền vững theo email trong `auth`). IP của yêu cầu = địa chỉ peer của kết nối HTTP/2; chỉ khi `EDGE_TRUST_PROXY=true` **và** peer thuộc `EDGE_TRUSTED_PROXIES` (mặc định loopback; đặt theo phương án triển khai DEC-023) mới đọc metadata `cf-connecting-ip` hoặc phần tử **cuối** của `x-forwarded-for`; giá trị không phải IP hợp lệ → dùng IP peer (TM-04). Thứ tự ở RPC auth: kiểm định dạng → giới hạn IP (`edge`) → khóa email (`auth`) → mới chạy PBKDF2.

| Nhóm | Giới hạn | Khi vượt (mã gRPC + `error-code`) |
|---|---|---|
| `Login` theo IP | 5 yêu cầu/phút/IP (cửa sổ trượt 60 s) — NFR-S8 | `RESOURCE_EXHAUSTED` `RATE_LIMITED` + `retry-after-seconds` |
| `Login` theo email | Sai mật khẩu 5 lần liên tiếp → khóa 60 s; mỗi lần khóa sau gấp đôi (2, 4, 8 … phút), tối đa 1 giờ; đúng/đặt lại thành công → về 0; 24 giờ không sai → bậc khóa về 0; email không tồn tại cũng vậy (BR-003, BR-076; DATABASE-DESIGN §Quy tắc khóa tăng dần) | `RESOURCE_EXHAUSTED` `ACCOUNT_LOCKED` + `retry-after-seconds` |
| `Register` | 10 yêu cầu/giờ/IP | `RESOURCE_EXHAUSTED` `RATE_LIMITED` |
| `GetResetQuestion`, `ResetPassword` | 5 yêu cầu/phút/IP; sai câu trả lời 5 lần liên tiếp → khóa đặt lại 60 s, gấp đôi, tối đa 1 giờ, 24 giờ về 0 (BR-008, bộ đếm riêng) | `RESOURCE_EXHAUSTED` `RATE_LIMITED` / `RESET_LOCKED` + `retry-after-seconds` |
| `ChangePassword`, `DeleteAccount` | Sai mật khẩu hiện tại 5 lần/15 phút/tài khoản | `RESOURCE_EXHAUSTED` `RATE_LIMITED` |
| PBKDF2 toàn `auth` (EP-02, 03, 07, 09, 10) | Tối đa 4 phép băm đồng thời (`tokio::sync::Semaphore`); chờ quá 2 s → từ chối (TM-05) | `UNAVAILABLE` `SERVICE_UNAVAILABLE` + `retry-after-seconds: 5` |
| `Push` | 30 yêu cầu/phút/token | `RESOURCE_EXHAUSTED` `RATE_LIMITED` |
| Còn lại (đã đăng nhập) | 120 yêu cầu/phút/token | `RESOURCE_EXHAUSTED` `RATE_LIMITED` |
| Đồng thời toàn `edge` | ≤ 64 RPC đang xử lý (`tower::limit::ConcurrencyLimit`), hàng đợi ≤ 2 s | `UNAVAILABLE` `SERVICE_UNAVAILABLE` |

## Endpoint / lệnh / hàm công khai
Bảng ánh xạ EP cũ → RPC (ID EP giữ nguyên để SRS, TESTING-STRATEGY không đánh số lại). "Hiện thực" = dịch vụ thật sự xử lý sau `edge`. Deadline = giá trị app đặt (`withDeadlineAfter`); `edge` chuyển cho dịch vụ đích phần còn lại trừ 100 ms. "Thử lại" = app tự thử lại theo BR-077 (§Quy ước chung).

| Tên | Input | Output | Lỗi (mã gRPC · reason) | Quyền |
|---|---|---|---|---|
| EP-01 `grpc.health.v1.Health/Check` (REST cũ `GET /health`) | `HealthCheckRequest{service}` | `HealthCheckResponse{status}` + header `x-server-version`, `x-server-time-ms` | — (`NOT_SERVING` trong thân) · hiện thực: `edge` · 3 s · thử lại | Công khai |
| EP-02 `AuthService/Register` (`POST /auth/register`) | `RegisterRequest` | `AuthSession` | `INVALID_ARGUMENT` VALIDATION_ERROR · `ALREADY_EXISTS` EMAIL_TAKEN/NICKNAME_TAKEN · `RESOURCE_EXHAUSTED` RATE_LIMITED · `UNAVAILABLE` · `auth` · 10 s · không thử lại | Công khai |
| EP-03 `AuthService/Login` (`POST /auth/login`) | `LoginRequest` | `AuthSession` | `INVALID_ARGUMENT` · `UNAUTHENTICATED` INVALID_CREDENTIALS · `RESOURCE_EXHAUSTED` ACCOUNT_LOCKED/RATE_LIMITED · `UNAVAILABLE` · `auth` · 10 s · không | Công khai |
| EP-04 `AuthService/Logout` (`POST /auth/logout`) | `LogoutRequest{}` | `LogoutResponse{}` | `UNAUTHENTICATED` · `edge` → `AuthBackend.Logout` · 10 s · không | Token |
| EP-05 `AuthService/GetMe` (`GET /auth/me`) | `GetMeRequest{}` | `GetMeResponse{user, stats, stats_partial}` | `UNAUTHENTICATED` · `UNAVAILABLE` (khi `auth` không trả hồ sơ) · `edge` ghép `auth`+`sync`+`gamification` · 10 s · thử lại | Token |
| EP-06 `AuthService/UpdateMe` (`PATCH /auth/me`) | `UpdateMeRequest{nickname?, display_name?}` | `UpdateMeResponse{user}` | `INVALID_ARGUMENT` · `UNAUTHENTICATED` · `ALREADY_EXISTS` NICKNAME_TAKEN · `auth` · 10 s · không | Token |
| EP-07 `AuthService/ChangePassword` (`POST /auth/change-password`) | `ChangePasswordRequest` | `ChangePasswordResponse{token, expires_at_ms}` | `INVALID_ARGUMENT` · `UNAUTHENTICATED` (token) · `PERMISSION_DENIED` INVALID_CREDENTIALS (sai mật khẩu hiện tại) · `RESOURCE_EXHAUSTED` · `auth` · 10 s · không | Token |
| EP-08 `AuthService/GetResetQuestion` (`POST /auth/reset/question`) | `GetResetQuestionRequest{email}` | `GetResetQuestionResponse{security_question_id}` | `INVALID_ARGUMENT` · `RESOURCE_EXHAUSTED` · `auth` · 10 s · không | Công khai |
| EP-09 `AuthService/ResetPassword` (`POST /auth/reset`) | `ResetPasswordRequest` | `AuthSession` | `INVALID_ARGUMENT` · `UNAUTHENTICATED` INVALID_SECURITY_ANSWER · `RESOURCE_EXHAUSTED` RESET_LOCKED/RATE_LIMITED · `auth` · 10 s · không | Công khai |
| EP-10 `AuthService/DeleteAccount` (`DELETE /auth/account`) | `DeleteAccountRequest{password}` | `DeleteAccountResponse{}` | `INVALID_ARGUMENT` · `UNAUTHENTICATED` · `PERMISSION_DENIED` INVALID_CREDENTIALS · `RESOURCE_EXHAUSTED` · `auth` · 10 s · không | Token |
| EP-11 `SyncService/Push` (`POST /sync/push`) | `PushRequest{device_id, changes[≤ 500]}` | `PushResponse{server_time_ms, results[]}` | `INVALID_ARGUMENT` BATCH_TOO_LARGE/VALIDATION_ERROR · `RESOURCE_EXHAUSTED` PAYLOAD_TOO_LARGE/RATE_LIMITED · `UNAUTHENTICATED` · `PERMISSION_DENIED` ACCOUNT_DELETED · `UNAVAILABLE` · `sync` · 20 s · thử lại (idempotent) | Token |
| EP-12 `SyncService/Pull` (`GET /sync/pull`) | `PullRequest{since, limit}` | `PullResponse{changes[], next_cursor, has_more, server_time_ms}` | `INVALID_ARGUMENT` · `UNAUTHENTICATED` · `UNAVAILABLE` · `sync` · 15 s · thử lại | Token |
| EP-13 `LeaderboardService/GetWeekly` (`GET /leaderboard/weekly`) | `GetWeeklyRequest{week}` | `GetWeeklyResponse{week_start, week_end, timezone, generated_at_ms, entries[≤ 50], me}` | `INVALID_ARGUMENT` · `UNAUTHENTICATED` · `UNAVAILABLE` SERVICE_UNAVAILABLE · `gamification` · 5 s · thử lại | Token |
| EP-14 `ContentService/GetManifest` (`GET /content/manifest`) | `GetManifestRequest{if_version}` | `GetManifestResponse{content_version, not_modified, languages[], decks[], exams[]}` | `UNAUTHENTICATED` · `UNAVAILABLE` · `content` · 15 s · thử lại | Token |
| EP-15 `ContentService/GetDeck` (`GET /content/decks/{code}`) | `GetDeckRequest{code}` | `GetDeckResponse{deck, words[]}` | `INVALID_ARGUMENT` · `UNAUTHENTICATED` · `NOT_FOUND` · `UNAVAILABLE` · `content` · 15 s · thử lại | Token |
| EP-16 `ContentService/GetExam` (`GET /content/exams/{code}`) | `GetExamRequest{code}` | `GetExamResponse{exam, questions[]}` | `INVALID_ARGUMENT` · `UNAUTHENTICATED` · `NOT_FOUND` · `UNAVAILABLE` · `content` · 15 s · thử lại | Token |

RPC **nội bộ** (`learninge.internal.v1`, chỉ trong mạng nội bộ, app không có stub). Tên service theo SYSTEM-BLUEPRINT §Dịch vụ (`*Backend`, `TokenValidator`, `EventSink`); các `*Backend` **dùng lại message của `learninge.app.v1`** để `edge` chuyển tiếp tường minh không phải chép trường; dịch vụ đích đọc `user_id` từ metadata `x-user-id`:
| RPC | Gọi bởi → hiện thực | Mục đích | Deadline | Idempotent |
|---|---|---|---|---|
| `TokenValidator/ValidateToken` | edge → auth | Token SHA-256 → `user_id`, `expires_at_ms` | 500 ms | Có |
| `AuthBackend/Register`, `Login`, `UpdateMe`, `ChangePassword`, `GetResetQuestion`, `ResetPassword`, `DeleteAccount` | edge → auth | EP-02, 03, 06..10 (cùng message công khai) | ≤ 8 s | Như EP tương ứng |
| `AuthBackend/Logout` | edge → auth | EP-04: thu hồi token theo `token_sha256` | ≤ 8 s | Có |
| `AuthBackend/GetProfile` | edge → auth | Phần `user` của EP-05 (bắt buộc) | ≤ 3 s | Có |
| `AuthBackend/ExportProfiles` | gamification (lệnh `rebuild`) → auth | Nickname công khai, phân trang | 10 s | Có |
| `SyncBackend/Push`, `Pull` | edge → sync | EP-11, EP-12 (cùng message công khai) | ≤ 18 s / 13 s | Có |
| `SyncBackend/GetStreak` | edge → sync | Chuỗi ngày + `total_xp` cho EP-05 (không bắt buộc) | 300 ms | Có |
| `SyncBackend/ExportXpEvents`, `ExportUserStats` | gamification (`rebuild`) → sync | XP gốc theo `server_seq`; chuỗi ngày mọi user; phân trang ≤ 1 000 | 10 s | Có |
| `ContentBackend/GetManifest`, `GetDeck`, `GetExam` | edge → content | EP-14..EP-16 | ≤ 3 s | Có |
| `GamificationBackend/GetWeekly` | edge → gamification | EP-13 | ≤ 3 s | Có |
| `GamificationBackend/GetStats` | edge → gamification | `week_xp`, `total_xp` cho EP-05 (không bắt buộc) | 300 ms | Có |
| `EventSink/Publish` | relay của auth, sync → sync, gamification, insight | Giao sự kiện outbox (≤ 200/lô) | 3 s | Có (theo `event_id`) |
| `InsightBackend/LogRequests` | edge → insight | `request_logs` best-effort (≤ 100/lô) | 2 s | Không cần (chấp nhận trùng/mất) |
| `InsightBackend/GetDailyStats` | người vận hành (`grpcurl`) → insight | Số liệu `analytics_daily` cho báo cáo | 5 s | Có |
| `grpc.health.v1.Health/Check` | edge, relay → mọi dịch vụ | Sẵn sàng (SQLite mở được / Mongo ping) | 1 s | Có |

## Chi tiết từng endpoint

### Proto công khai — `learninge/app/v1/common.proto`
```proto
syntax = "proto3";
package learninge.app.v1;
option java_multiple_files = true;
option java_package = "com.learninge.proto.app.v1";
option java_outer_classname = "CommonProto";

// Mọi thời điểm: epoch milliseconds UTC (int64, hậu tố _ms). Ngày lịch: chuỗi "yyyy-MM-dd".
message User {
  string id = 1;              // users.uuid (UUID v4 chữ thường)
  string email = 2;           // chỉ trả cho chính chủ
  string nickname = 3;
  string display_name = 4;
  int64 created_at_ms = 5;
}

message Stats {
  int64 total_xp = 1;
  int32 week_xp = 2;
  int32 current_streak = 3;
  int32 longest_streak = 4;
  string last_study_date = 5; // "" nếu chưa học ngày nào
}

message AuthSession {
  string token = 1;           // 43 ký tự base64url; KHÔNG ghi log
  int64 expires_at_ms = 2;
  User user = 3;
}
```

### `learninge/app/v1/auth.proto` (EP-02..EP-10)
```proto
syntax = "proto3";
package learninge.app.v1;
import "learninge/app/v1/common.proto";
option java_multiple_files = true;
option java_package = "com.learninge.proto.app.v1";
option java_outer_classname = "AuthProto";

service AuthService {
  rpc Register(RegisterRequest) returns (AuthSession);                              // EP-02
  rpc Login(LoginRequest) returns (AuthSession);                                    // EP-03
  rpc Logout(LogoutRequest) returns (LogoutResponse);                               // EP-04
  rpc GetMe(GetMeRequest) returns (GetMeResponse);                                  // EP-05
  rpc UpdateMe(UpdateMeRequest) returns (UpdateMeResponse);                         // EP-06
  rpc ChangePassword(ChangePasswordRequest) returns (ChangePasswordResponse);       // EP-07
  rpc GetResetQuestion(GetResetQuestionRequest) returns (GetResetQuestionResponse); // EP-08
  rpc ResetPassword(ResetPasswordRequest) returns (AuthSession);                    // EP-09
  rpc DeleteAccount(DeleteAccountRequest) returns (DeleteAccountResponse);          // EP-10
}

message RegisterRequest {
  string email = 1;
  string password = 2;
  string nickname = 3;
  string display_name = 4;
  int32 security_question_id = 5;   // 1..5
  string security_answer = 6;
  string device_name = 7;           // "" = không có
}
message LoginRequest {
  string email = 1;
  string password = 2;
  string device_name = 3;
}
message LogoutRequest {}
message LogoutResponse {}
message GetMeRequest {}
message GetMeResponse {
  User user = 1;
  Stats stats = 2;
  bool stats_partial = 3;           // true khi sync/gamification không trả lời kịp
}
message UpdateMeRequest {
  optional string nickname = 1;     // không có = không đổi
  optional string display_name = 2;
}
message UpdateMeResponse { User user = 1; }
message ChangePasswordRequest {
  string current_password = 1;
  string new_password = 2;
  string device_name = 3;
}
message ChangePasswordResponse {
  string token = 1;
  int64 expires_at_ms = 2;
}
message GetResetQuestionRequest { string email = 1; }
message GetResetQuestionResponse { int32 security_question_id = 1; }
message ResetPasswordRequest {
  string email = 1;
  string security_answer = 2;
  string new_password = 3;
  string device_name = 4;
}
message DeleteAccountRequest { string password = 1; }
message DeleteAccountResponse {}
```

### `learninge/app/v1/sync.proto` (EP-11, EP-12)
```proto
syntax = "proto3";
package learninge.app.v1;
option java_multiple_files = true;
option java_package = "com.learninge.proto.app.v1";
option java_outer_classname = "SyncProto";

service SyncService {
  rpc Push(PushRequest) returns (PushResponse);   // EP-11
  rpc Pull(PullRequest) returns (PullResponse);   // EP-12
}

message PushRequest {
  string device_id = 1;             // UUID v4 của máy (sync_state.device_id)
  repeated Change changes = 2;      // ≤ 500 phần tử
}
message PushResponse {
  int64 server_time_ms = 1;
  repeated ChangeResult results = 2; // cùng số phần tử, cùng thứ tự với changes
}
message PullRequest {
  int64 since = 1;                  // cursor, ≥ 0; lần đầu = 0
  int32 limit = 2;                  // 1..500; 0 = 500
}
message PullResponse {
  repeated Change changes = 1;      // sắp tăng theo server_seq, gồm cả tombstone
  int64 next_cursor = 2;
  bool has_more = 3;
  int64 server_time_ms = 4;
}

// Một bản ghi đồng bộ. Loại bản ghi = trường đang đặt trong oneof `data` (thay cho "entity" của REST).
message Change {
  string record_id = 1;   // uuid (deck, word, study_log, quiz_attempt, exam_attempt, xp_event)
                          // | word_uid (card_progress, favorite) | "" (settings)
  int64 updated_at_ms = 2;
  bool deleted = 3;
  int64 server_seq = 4;   // chỉ server đặt (Pull, ChangeResult.current); Push: bỏ qua
  oneof data {
    SettingsData settings = 10;
    DeckData deck = 11;
    WordData word = 12;
    CardProgressData card_progress = 13;
    FavoriteData favorite = 14;
    StudyLogData study_log = 15;
    QuizAttemptData quiz_attempt = 16;
    ExamAttemptData exam_attempt = 17;
    XpEventData xp_event = 18;
  }
}

enum ChangeStatus {
  CHANGE_STATUS_UNSPECIFIED = 0;
  CHANGE_STATUS_APPLIED = 1;
  CHANGE_STATUS_STALE = 2;      // card_progress cũ hơn bản server → app ghi đè bằng `current`
  CHANGE_STATUS_DUPLICATE = 3;  // bản ghi chỉ-thêm đã có
  CHANGE_STATUS_REJECTED = 4;
}
message ChangeResult {
  int32 index = 1;              // vị trí trong PushRequest.changes (record_id có thể trùng giữa các loại)
  string record_id = 2;
  ChangeStatus status = 3;
  int64 server_seq = 4;
  Change current = 5;           // STALE: bản server; xp_event APPLIED/DUPLICATE: có xp_awarded
  RecordError error = 6;        // chỉ khi REJECTED
}
message RecordError {
  string code = 1;              // DECK_NOT_OWNED | UNKNOWN_ENTITY | INVALID_RECORD | UNKNOWN_TEST
  string message = 2;           // tiếng Việt, hiển thị được
}

enum CefrLevel { CEFR_LEVEL_UNSPECIFIED = 0; CEFR_LEVEL_A1 = 1; CEFR_LEVEL_A2 = 2; CEFR_LEVEL_B1 = 3; }
enum ActivityType {
  ACTIVITY_TYPE_UNSPECIFIED = 0; ACTIVITY_TYPE_LEARN = 1; ACTIVITY_TYPE_REVIEW = 2;
  ACTIVITY_TYPE_QUIZ = 3; ACTIVITY_TYPE_EXAM = 4;
}
enum QuizSourceType { QUIZ_SOURCE_TYPE_UNSPECIFIED = 0; QUIZ_SOURCE_TYPE_DECK = 1; }
enum XpSourceType {
  XP_SOURCE_TYPE_UNSPECIFIED = 0; XP_SOURCE_TYPE_CARD_SESSION = 1;
  XP_SOURCE_TYPE_QUIZ = 2; XP_SOURCE_TYPE_EXAM = 3;
}

message SettingsData { CefrLevel level = 1; int32 daily_goal = 2; }        // 5 | 10 | 15 | 20
message DeckData {
  string name = 1;
  optional string description = 2;
  string language_code = 3;
  optional string icon = 4;
  bool is_default = 5;
  int32 sort_order = 6;
  int64 created_at_ms = 7;
}
message WordData {
  string deck_uuid = 1;
  string term = 2;
  optional string reading = 3;
  optional string part_of_speech = 4;
  string meaning_vi = 5;
  optional string example = 6;
  optional string example_translation = 7;
  int32 sort_order = 8;
  int64 created_at_ms = 9;
}
message CardProgressData {
  int32 box = 1;                        // 1..5
  string next_review_date = 2;
  string first_learned_date = 3;
  optional int64 last_reviewed_at_ms = 4;
  int32 review_count = 5;
}
message FavoriteData { int64 created_at_ms = 1; }
message StudyLogData {
  ActivityType activity_type = 1;
  optional string ref_uuid = 2;
  string study_date = 3;
  int64 created_at_ms = 4;
}
message QuizAttemptData {
  QuizSourceType source_type = 1;
  optional string deck_ref = 2;         // decks.uuid
  int32 score = 3;                      // 0..100
  int32 correct_count = 4;
  int32 total_count = 5;
  int32 duration_sec = 6;
  int64 created_at_ms = 7;
}
message ExamAttemptData {               // chỉ lượt SUBMITTED
  string test_code = 1;
  int32 score = 2;
  int32 correct_count = 3;
  int32 total_count = 4;
  int32 duration_sec = 5;
  int64 started_at_ms = 6;
  int64 submitted_at_ms = 7;
}
message XpEventData {
  XpSourceType source_type = 1;
  optional string ref_uuid = 2;
  int32 correct_count = 3;              // 0..200
  int32 wrong_count = 4;                // 0..200
  int32 total_count = 5;                // = correct + wrong
  int64 occurred_at_ms = 6;
  optional int32 xp_awarded = 7;        // CHỈ server đặt (BR-066); giá trị app gửi bị bỏ qua
}
```
Quy tắc Push (giữ nguyên thay đổi phạm vi, DEC-014; chỉ đổi cách diễn đạt):
- **≤ 500 phần tử** `changes` và message ≤ 1 MiB; vượt số lượng → `INVALID_ARGUMENT` `BATCH_TOO_LARGE` (`grpc-message` nêu giới hạn 500); vượt kích thước → `RESOURCE_EXHAUSTED` `PAYLOAD_TOO_LARGE` (tonic từ chối khi giải mã; mã thực tế xác minh ở S-01, app coi `RESOURCE_EXHAUSTED`/`OUT_OF_RANGE` có reason này hoặc không có chi tiết là "lô quá lớn" và chia đôi lô). App chia lô theo thứ tự cha → con: `settings`, `deck`, `word`, `favorite`, `card_progress`, `study_log`, `quiz_attempt`, `exam_attempt`, `xp_event`.
- Khóa bản ghi: `record_id` = uuid (v4 do app sinh) hoặc `word_uid` (`T01-0007` / uuid từ của người dùng) cho `card_progress`, `favorite`; `settings` không có khóa (1 bản/người dùng). `data` chưa đặt (oneof rỗng, vd app mới gửi loại server cũ chưa biết) → `REJECTED` `UNKNOWN_ENTITY`.
- **Danh sách trắng trường** (TM-15) nay là chính lược đồ proto: message không có `user_id`, `server_seq` (bị bỏ qua khi Push), `received_at`, `local_date`, `week_start`; `xp_awarded` gửi lên bị bỏ qua. Trường lạ từ client mới hơn bị `prost` bỏ qua.
- proto3 không phân biệt "thiếu" với giá trị mặc định cho trường không `optional`: trường bắt buộc kiểu chuỗi rỗng `""`, enum `*_UNSPECIFIED`, số ngoài miền → `REJECTED` `INVALID_RECORD`. Trường `optional` không đặt = `NULL`.
- `updated_at_ms` > `server_time + 5 phút` → kẹp về `server_time`. Bảng thường (`settings`, `deck`, `word`, `favorite`): LWW theo thứ tự `sync` nhận → `APPLIED`. `card_progress`: chỉ ghi khi `updated_at_ms` **lớn hơn** bản đang có, ngược lại `STALE` + `current`. Chỉ-thêm (`study_log`, `quiz_attempt`, `exam_attempt`, `xp_event`): chưa có → `APPLIED`, đã có (cùng user) → `DUPLICATE`. `xp_event`: `sync` tự tính `xp_awarded` (BR-065..BR-068, DATABASE-DESIGN §Quy tắc XP), trả trong `current.xp_event.xp_awarded` cho cả `APPLIED` và `DUPLICATE`.
- Sở hữu (TM-14): `record_id` đã tồn tại nhưng của người khác → `REJECTED` `INVALID_RECORD`, không ghi, không trả `current`. `word.deck_uuid` không thuộc user → `REJECTED` `DECK_NOT_OWNED`. Chuỗi vượt giới hạn SRS §Validation (trần chung 1 000 ký tự) → `INVALID_RECORD` (TM-18). XP: số đếm ngoài 0..200 → `REJECTED`; vô lý hoặc `occurred_at_ms` ngoài `[received_at − 7 ngày, received_at + 5 phút]` → `APPLIED` với `xp_awarded = 0` (BR-068, TM-16). Một bản ghi lỗi **không** hủy cả lô.
- Mỗi bản ghi `APPLIED` nhận `server_seq` mới (tăng dần trong `sync.db`).
- Tài khoản đã bị xóa (sự kiện `user.deleted` tới trước lô) → cả lô `PERMISSION_DENIED` `ACCOUNT_DELETED`, không ghi gì.
- **Idempotency:** gửi lại cùng lô cho cùng kết quả cuối (chỉ-thêm thành `DUPLICATE`, trạng thái ghi lại cùng giá trị) → Push **được** thử lại tự động. App chỉ xóa `dirty` khi `updated_at` cục bộ chưa đổi từ lúc gửi (`UPDATE … SET dirty = 0 WHERE uuid = ? AND updated_at = ?`).
- Ví dụ (định dạng text của protobuf, dùng với `grpcurl -d @`):
```text
device_id: "2b1f0c9e-4d1a-4f6b-9a77-0f3c2e1d5b10"
changes { record_id: "d7a1e2c4-0000-4000-8000-000000000001" updated_at_ms: 1762761600000
          deck { name: "Du lịch" language_code: "en" sort_order: 3 created_at_ms: 1762761000000 } }
changes { record_id: "T01-0007" updated_at_ms: 1762761700000
          card_progress { box: 3 next_review_date: "2026-11-14" first_learned_date: "2026-11-02"
                          last_reviewed_at_ms: 1762761700000 review_count: 4 } }
changes { record_id: "e51b7a10-0000-4000-8000-000000000002" updated_at_ms: 1762761800000
          xp_event { source_type: XP_SOURCE_TYPE_CARD_SESSION ref_uuid: "s0f3…" correct_count: 18
                     wrong_count: 2 total_count: 20 occurred_at_ms: 1762761800000 } }
# → results { index: 2 record_id: "e51b…" status: CHANGE_STATUS_APPLIED server_seq: 10453
#             current { xp_event { xp_awarded: 38 } } }
```

Pull (EP-12): `since` < 0 hoặc `limit` ngoài 0..500 → `INVALID_ARGUMENT` `VALIDATION_ERROR`. Trả bản ghi của user có `server_seq > since`, tăng dần, gồm tombstone và bản do chính máy này đẩy (app áp dụng idempotent). App lặp tới `has_more = false`, lưu `next_cursor` vào `sync_state.pull_cursor` trong cùng giao dịch ghi trang.

### `learninge/app/v1/leaderboard.proto` (EP-13)
```proto
syntax = "proto3";
package learninge.app.v1;
option java_multiple_files = true;
option java_package = "com.learninge.proto.app.v1";
option java_outer_classname = "LeaderboardProto";

service LeaderboardService {
  rpc GetWeekly(GetWeeklyRequest) returns (GetWeeklyResponse);   // EP-13
}
enum WeekSelector { WEEK_SELECTOR_UNSPECIFIED = 0; WEEK_SELECTOR_CURRENT = 1; WEEK_SELECTOR_PREVIOUS = 2; }
message GetWeeklyRequest { WeekSelector week = 1; }   // UNSPECIFIED = CURRENT
message GetWeeklyResponse {
  string week_start = 1;            // thứ Hai, "yyyy-MM-dd"
  string week_end = 2;              // Chủ nhật
  string timezone = 3;              // "Asia/Ho_Chi_Minh"
  int64 generated_at_ms = 4;
  repeated LeaderboardEntry entries = 5;   // ≤ 50
  LeaderboardEntry me = 6;                 // luôn có
}
message LeaderboardEntry {
  int32 rank = 1;                   // 0 = chưa có hạng (thay cho null của REST)
  string nickname = 2;
  int32 xp = 3;
  int32 streak = 4;
  bool is_me = 5;
}
```
Tuần ISO thứ Hai 00:00 → Chủ nhật 23:59:59 giờ `Asia/Ho_Chi_Minh`; XP tuần = tổng `xp_awarded` (đã áp trần 500/ngày); hòa → người đạt tổng sớm hơn đứng trước (BR-069); người 0 XP không có trong danh sách. Không trả email/id người khác. MongoDB hoặc `gamification` ngừng → `UNAVAILABLE` `SERVICE_UNAVAILABLE` → app hiện `leaderboard_cache` kèm "Cập nhật lúc …" (US-042). App lưu bytes của phản hồi vào `leaderboard_cache.payload`.

### `learninge/app/v1/content.proto` (EP-14..EP-16)
```proto
syntax = "proto3";
package learninge.app.v1;
option java_multiple_files = true;
option java_package = "com.learninge.proto.app.v1";
option java_outer_classname = "ContentProto";

service ContentService {
  rpc GetManifest(GetManifestRequest) returns (GetManifestResponse);   // EP-14
  rpc GetDeck(GetDeckRequest) returns (GetDeckResponse);               // EP-15
  rpc GetExam(GetExamRequest) returns (GetExamResponse);               // EP-16
}
message GetManifestRequest { int64 if_version = 1; }   // thay If-None-Match; 0 = luôn trả đủ
message GetManifestResponse {
  int64 content_version = 1;
  bool not_modified = 2;            // true khi if_version == content_version → các trường sau rỗng (thay 304)
  repeated Language languages = 3;
  repeated ManifestItem decks = 4;
  repeated ManifestItem exams = 5;
}
message Language {
  string code = 1; string name_vi = 2; string tts_locale = 3; string reading_label = 4; int32 sort_order = 5;
}
message ManifestItem {
  string code = 1;                  // T01 | E01
  int32 version = 2;
  int32 item_count = 3;             // số từ / số câu
  int64 updated_at_ms = 4;
}
message GetDeckRequest { string code = 1; }   // ^[A-Z][0-9]{2}$
message GetDeckResponse { ContentDeck deck = 1; repeated ContentWord words = 2; }
message ContentDeck {
  string code = 1; string name = 2; optional string description = 3; string language_code = 4;
  optional string level = 5; optional string icon = 6; optional string exam_tag = 7;
  int32 sort_order = 8; int32 version = 9;
}
message ContentWord {
  string uid = 1;                   // "T01-0007": khóa card_progress/favorite
  string term = 2; optional string reading = 3; optional string part_of_speech = 4;
  string meaning_vi = 5; optional string example = 6; optional string example_translation = 7;
  int32 sort_order = 8;
  bool deleted = 9;                 // gỡ khỏi bộ: app ẩn từ, giữ tiến độ
}
message GetExamRequest { string code = 1; }
message GetExamResponse { ContentExam exam = 1; repeated ExamQuestion questions = 2; }
message ContentExam {
  string code = 1; optional string exam_tag = 2; string title = 3; optional string part_label = 4;
  int32 question_count = 5; int32 duration_sec = 6; optional string description = 7;
  int32 sort_order = 8; int32 version = 9;
}
message ExamQuestion {
  int32 number = 1; optional string passage = 2; string question = 3;
  repeated string options = 4;      // đúng 4 phần tử
  int32 correct_index = 5;          // 0..3
  optional string explanation = 6; optional string word_uid = 7;
}
```
`code` sai mẫu → `INVALID_ARGUMENT`; không có → `NOT_FOUND`. App so `version` với bảng `content_version` cục bộ, chỉ tải mục có `version` lớn hơn. MongoDB ngừng → `UNAVAILABLE` → app dùng seed trong APK + bản đã tải.

### EP-01 Health
Dùng proto chuẩn `grpc/health/v1/health.proto` (`grpc.health.v1.Health/Check`, `tonic-health`). `service = ""` → `SERVING` khi `edge` chạy và `auth` + `sync` trả `SERVING` (lõi); `service = "learninge.app.v1.LeaderboardService"` / `"learninge.app.v1.ContentService"` phản ánh `gamification`/`content` + MongoDB. Phiên bản và giờ server (EP-01 cũ) nằm ở metadata phản hồi `x-server-version`, `x-server-time-ms` (có trên **mọi** phản hồi của `edge`).

### Ràng buộc trường (kiểm ở cả app và `auth`, BR-001..BR-009, BR-060, BR-061; giữ thay đổi phạm vi)
| Trường | Ràng buộc |
|---|---|
| `email` | 5–254 ký tự, đúng một `@`, cắt khoảng trắng; so sánh không phân biệt hoa thường (lưu chữ thường) |
| `password`, `new_password`, `current_password` | 8–64 ký tự, ≥ 1 chữ cái và ≥ 1 chữ số; không cắt khoảng trắng; không ghi log |
| `nickname` | 3–20 ký tự `[A-Za-z0-9_.]`, duy nhất không phân biệt hoa thường; công khai (BR-061) |
| `display_name` | 2–30 ký tự sau khi cắt khoảng trắng, không công khai |
| `security_question_id` | 1..5 (BR-009) |
| `security_answer` | 2–50 ký tự sau khi cắt; server chuẩn hóa (cắt, chữ thường, bỏ dấu) rồi băm (BR-007) |
| `device_name` | ≤ 60 ký tự, `""` = không có |

### Hành vi từng RPC tài khoản (giữ nguyên nghiệp vụ thay đổi phạm vi)
- **EP-02 Register** (US-001, REQ-001): `auth` tạo trong 1 giao dịch `users`, `security_answers`, token, dòng outbox `user.registered`. `user_settings` **không** tạo sẵn ở server (app có mặc định BR-006 và đẩy lên ở lần đồng bộ đầu). Bộ "Sổ tay của tôi" do `sync` tạo khi nhận `user.registered` với `uuid` tất định (§Webhook / sự kiện — Bộ mặc định). Không idempotent: gửi lại sau khi đã tạo → `ALREADY_EXISTS` `EMAIL_TAKEN` ("Email đã được dùng, hãy đăng nhập").
- **EP-03 Login** (US-002, BR-003, BR-057): `UNAUTHENTICATED` `INVALID_CREDENTIALS` cho cả email không tồn tại và sai mật khẩu (thời gian xử lý tương đương — vẫn PBKDF2 với salt giả); email không tồn tại **cũng bị đếm và khóa tăng dần** (`auth_attempts`, BR-076). Đang bị giới hạn IP/khóa → `RESOURCE_EXHAUSTED` ngay, **không** chạy PBKDF2; trailer `retry-after-seconds` = số giây khóa còn lại (60, gấp đôi mỗi lần, tối đa 3600). Đăng nhập trên máy mới → Pull từ `since = 0` (FLOW-018).
- **EP-04 Logout**: thu hồi token hiện tại. Token đã hết hạn → `UNAUTHENTICATED`, app coi như đã đăng xuất. App đẩy dữ liệu trước khi đăng xuất (BR-073).
- **EP-05 GetMe**: `edge` gọi song song `AuthBackend.GetProfile` (bắt buộc), `SyncBackend.GetStreak` và `GamificationBackend.GetStats` (không bắt buộc, deadline 300 ms); thiếu một trong hai lời gọi sau → `stats_partial = true` (thiếu gamification: `week_xp = 0`, `total_xp` lấy từ `sync`; thiếu sync: chuỗi ngày = 0).
- **EP-06 UpdateMe**: chỉ đổi `nickname`, `display_name` (trường `optional` không đặt = không đổi); phát `user.profile_changed`.
- **EP-07 ChangePassword** (BR-058): thu hồi **mọi** token cũ, cấp token mới cho máy đang gọi. Sai mật khẩu hiện tại → `PERMISSION_DENIED` `INVALID_CREDENTIALS` (không dùng `UNAUTHENTICATED` để app **không** xóa token theo BR-059); mật khẩu mới trùng cũ/sai quy tắc → `INVALID_ARGUMENT`.
- **EP-08 GetResetQuestion** (REQ-014): email không tồn tại → vẫn OK với id giả **ổn định** `(HMAC-SHA256(LEARNINGE_SERVER_KEY, email chuẩn hóa) mod 5) + 1` (khóa chỉ ở biến môi trường của `auth`; thiếu khi `APP_ENV=prod` → `auth` **từ chối khởi động**; chỉ `APP_ENV=dev` mới dùng khóa tạm + log WARN). Không dùng `SHA-256(email)` trần (TM-03, F-02).
- **EP-09 ResetPassword** (BR-007, BR-008): OK → `AuthSession`, thu hồi mọi token cũ, xóa bộ đếm `LOGIN`. Sai → `UNAUTHENTICATED` `INVALID_SECURITY_ANSWER` (cùng thông báo cho email giả, BR-076); lần sai thứ 5 → `RESOURCE_EXHAUSTED` `RESET_LOCKED` + `retry-after-seconds`. Email không tồn tại vẫn PBKDF2 với salt giả và bị đếm/khóa như thật.
- **EP-10 DeleteAccount** (BR-005): `auth` xóa trong 1 giao dịch (CASCADE token, câu trả lời) + outbox `user.deleted` tới `sync`, `gamification`, `insight`; `edge` xóa cache token. Dữ liệu ở 3 dịch vụ kia bị xóa khi nhận sự kiện (bình thường < 2 s; người dùng biến khỏi bảng xếp hạng khi `gamification` xử lý). Sai mật khẩu → `PERMISSION_DENIED` `INVALID_CREDENTIALS`.

## Quy ước lỗi
**Cơ chế duy nhất (chuẩn) cho lỗi nghiệp vụ** — thống nhất với SYSTEM-BLUEPRINT §Metadata gRPC và SRS:
1. **Mã trạng thái gRPC chuẩn** (`grpc-status`) — phân loại lỗi (bảng dưới).
2. **Trailer `error-code`** (ASCII) = **mã lỗi nghiệp vụ thay đổi phạm vi giữ nguyên tên** (vd `ACCOUNT_LOCKED`, `EMAIL_TAKEN`). Có trên **mọi** lỗi do server LearningE sinh ra; thiếu trailer nghĩa là lỗi tầng vận chuyển/thư viện (mất mạng, deadline, giải mã hỏng).
3. Trailer phụ (chỉ khi áp dụng): `retry-after-seconds: <n>` (số nguyên giây, làm tròn lên) cho `RESOURCE_EXHAUSTED` và `UNAVAILABLE` do quá tải; `error-fields: <trường>=<mã>[,<trường>=<mã>…]` cho `VALIDATION_ERROR` (vd `password=TOO_SHORT,email=INVALID_FORMAT`, mã `REQUIRED`/`TOO_SHORT`/`TOO_LONG`/`INVALID_FORMAT`/`OUT_OF_RANGE`), thay `details.fields` của REST.
4. `grpc-message` = câu tiếng Việt ngắn, hiển thị được (percent-encoded UTF-8 theo chuẩn gRPC); không stack trace, SQL, đường dẫn; app **không** dựa vào nó.

App ánh xạ **(mã gRPC, `error-code`) → `ErrorCode`** (SYSTEM-BLUEPRINT §Mô hình lỗi). **Không** dùng `google.rpc.Status`/`ErrorInfo`/`RetryInfo` (`grpc-status-details-bin`) để app khỏi cần proto googleapis — thay đổi phạm vi §3. `request_id` để đối chiếu log nằm ở header `x-request-id` của phản hồi.

Bảng ánh xạ từ mã HTTP thay đổi phạm vi:
| Mã gRPC · `error-code` | HTTP cũ | Ý nghĩa | Khi nào | `grpc-message` mẫu · trailer phụ |
|---|---|---|---|---|
| `INVALID_ARGUMENT` · `VALIDATION_ERROR` | 400 | Trường sai ràng buộc | Chuỗi rỗng, sai độ dài, sai miền, enum UNSPECIFIED | "Dữ liệu chưa hợp lệ" · `error-fields: password=TOO_SHORT` |
| (bỏ) `BAD_JSON` | 400 | — | Không còn JSON; message protobuf hỏng do tonic từ chối (`INTERNAL`/`INVALID_ARGUMENT` không có `error-code`) → app coi là lỗi lập trình | — |
| `UNAUTHENTICATED` · `UNAUTHENTICATED` | 401 | Token thiếu/sai/hết hạn/bị thu hồi | RPC cần token | "Phiên đăng nhập đã hết hạn" → BR-059 |
| `UNAUTHENTICATED` · `INVALID_CREDENTIALS` | 401 | Sai email hoặc mật khẩu | EP-03 (không có token → không kích hoạt BR-059) | "Email hoặc mật khẩu không đúng" |
| `PERMISSION_DENIED` · `INVALID_CREDENTIALS` | 401 | Sai mật khẩu hiện tại khi đã đăng nhập | EP-07, EP-10 | "Mật khẩu hiện tại không đúng" |
| `UNAUTHENTICATED` · `INVALID_SECURITY_ANSWER` | 401 | Sai câu trả lời bảo mật | EP-09 | "Thông tin không khớp" (BR-076) |
| `PERMISSION_DENIED` · `ACCOUNT_DELETED` | (mới) | Tài khoản đã xóa, lô Push đến muộn | EP-11 | "Tài khoản đã bị xóa" |
| `NOT_FOUND` · `NOT_FOUND` | 404 | Không có hoặc không thuộc người dùng | EP-15, EP-16 | "Không tìm thấy" |
| `UNIMPLEMENTED` (không `error-code`) | 404 route lạ, 405 | RPC không tồn tại / chưa hiện thực | App cũ gọi RPC đã bỏ, hoặc gọi nhầm cổng | — |
| `ALREADY_EXISTS` · `EMAIL_TAKEN` / `NICKNAME_TAKEN` | 409 | Trùng email/biệt danh | EP-02, EP-06 | "Email đã được dùng" |
| `INVALID_ARGUMENT` · `BATCH_TOO_LARGE` | 413 | > 500 bản ghi | EP-11 | "Tối đa 500 bản ghi mỗi lần" |
| `RESOURCE_EXHAUSTED` · `PAYLOAD_TOO_LARGE` | 413 | Message > 1 MiB | Mọi RPC vào `edge` | "Dữ liệu gửi lên quá lớn" |
| (bỏ) `UNSUPPORTED_MEDIA_TYPE` | 415 | — | gRPC tự kiểm `content-type: application/grpc` | — |
| `FAILED_PRECONDITION` · `UPGRADE_REQUIRED` | 426 | Bản app quá cũ | `x-client-version` < `EDGE_MIN_CLIENT` hoặc thiếu | "Vui lòng cập nhật ứng dụng" |
| `RESOURCE_EXHAUSTED` · `RATE_LIMITED` / `ACCOUNT_LOCKED` / `RESET_LOCKED` | 429 | Vượt giới hạn / khóa tạm | Bảng §Xác thực | "Thử lại sau 60 giây" · `retry-after-seconds: 60` |
| `INTERNAL` · `INTERNAL_ERROR` | 500 | Lỗi không lường trước | Panic/lỗi chưa bắt — log kèm `request_id` | "Máy chủ gặp lỗi, vui lòng thử lại" · (header `x-request-id`) |
| `UNAVAILABLE` · `SERVICE_UNAVAILABLE` | 503 | Dịch vụ bận: SQLite quá `busy_timeout` 5 s, hàng đợi PBKDF2 > 2 s, `edge` quá tải | Ghi đồng thời lâu, tấn công CPU | "Máy chủ đang bận" · `retry-after-seconds: 5` |
| `UNAVAILABLE` · `SERVICE_UNAVAILABLE` | 503 (mới tách) | Dịch vụ đích hoặc MongoDB không trả lời | EP-13..EP-16, (EP-05 nếu `auth` ngừng) | "Tính năng tạm thời không dùng được" |
| `UNAVAILABLE` (không `error-code`) | lỗi mạng | Không kết nối được `edge` (mất mạng, tunnel tắt) | Phía client | — |
| `DEADLINE_EXCEEDED` | timeout | Hết deadline (client đặt, hoặc `edge` hết phần còn lại) | Mạng chậm, server chậm | — |
| `CANCELLED` | — | App hủy lời gọi (rời màn hình, WorkManager dừng) | Phía client | — |

Mã lỗi theo bản ghi trong EP-11 (`ChangeResult.error.code`): `DECK_NOT_OWNED`, `UNKNOWN_ENTITY`, `INVALID_RECORD`, `UNKNOWN_TEST` (giữ thay đổi phạm vi).

Dịch vụ nội bộ trả lỗi cho `edge` theo cùng quy ước; `edge` **chuyển nguyên** mã + chi tiết của dịch vụ đích cho app, trừ: lỗi kết nối tới dịch vụ đích → `UNAVAILABLE` `SERVICE_UNAVAILABLE`; `INTERNAL` của dịch vụ đích → giữ mã nhưng thay `grpc-message` bằng câu chung.

## Quy ước chung
- Định dạng thời gian/tiền tệ, đặt tên trường, phân trang, null vs thiếu, header chuẩn, correlation id:
  - Tên trường `snake_case` (Java lite sinh `getUpdatedAtMs()`); enum `UPPER_SNAKE` có tiền tố tên enum và giá trị 0 = `*_UNSPECIFIED`; tên service `PascalCase` + `Service`; RPC `PascalCase` động từ.
  - **Thời điểm**: `int64` epoch milliseconds UTC, hậu tố `_ms` (thống nhất với cột SQLite của app; REST cũ dùng ISO cho `expires_at`, `generated_at` — nay cũng là `_ms`). **Ngày lịch**: chuỗi `yyyy-MM-dd`. Không có tiền tệ.
  - **Rỗng vs thiếu**: trường có ý nghĩa NULL dùng `optional` (có `hasX()`); trường không `optional` thì giá trị mặc định (`""`, `0`, `false`) nghĩa là "không có" chỉ khi bảng ràng buộc cho phép, ngược lại là lỗi `INVALID_ARGUMENT`/`INVALID_RECORD`. `UpdateMe`: `optional` không đặt = không đổi.
  - Phân trang: chỉ EP-12 (cursor `since`/`next_cursor`, `limit` ≤ 500) và RPC nội bộ `Export*`. Danh sách khác có giới hạn cố định (bảng xếp hạng 50).
  - **Metadata phản hồi (edge → app)**: header `x-request-id`, `x-server-version`, `x-server-time-ms`; khi lỗi thêm trailer `error-code`, và nếu áp dụng `retry-after-seconds`, `error-fields` (§Quy ước lỗi).
  - **Metadata nội bộ (edge → dịch vụ)**: `x-user-id` (UUID), `x-request-id`, `x-client-version`, `grpc-timeout` (phần còn lại − 100 ms). Chỉ `edge` được đặt `x-user-id`. **Mọi** lời gọi nội bộ (edge, relay, công cụ vận hành → dịch vụ, kể cả `EventSink`, `Export*`, `InsightBackend`) mang thêm `x-internal-auth: <LEARNINGE_INTERNAL_TOKEN>`; interceptor của `common` so sánh hằng thời gian, thiếu/sai → `UNAUTHENTICATED` (chỉ `grpc.health.v1.Health/Check` được miễn); dịch vụ chỉ đọc `x-user-id` sau khi qua kiểm này. `edge` xóa `x-user-id`, `x-user-id-bin` và mọi `x-internal-*` từ client (THREAT_MODEL D-09, TM-33).
  - **Log**: không ghi metadata `authorization`, message của `AuthService` (mật khẩu, câu trả lời, token), email; span `tracing` gắn `request_id`, `rpc`, `user_hmac` (HMAC-SHA256 với `LEARNINGE_PSEUDONYM_KEY`, do `auth`/`sync` tính; `edge` nhận qua `ValidateTokenResponse.user_hmac`, không giữ khóa — THREAT_MODEL D-10), `code`, `duration_ms` (NFR-S7, DEC-025).
  - **Giới hạn kích thước message**: `edge` nhận ≤ **1 MiB** (`max_decoding_message_size`), gửi ≤ 4 MiB; dịch vụ nội bộ nhận/gửi ≤ 4 MiB; app `maxInboundMessageSize(4 MiB)`. Một trang Pull 500 bản ghi ≈ 150–300 KB, một bộ 1 000 từ ≈ 200 KB.
  - **Deadline phía app** (đặt trên mọi lời gọi bằng `withDeadlineAfter`; không có deadline = lỗi review; theo SYSTEM-BLUEPRINT §Deadline và thử lại): Health 3 s; mọi RPC `AuthService` (EP-02..EP-10) 10 s; Push 20 s; Pull 15 s; GetWeekly 5 s; `ContentService` 15 s. `edge` → dịch vụ: phần còn lại − 100 ms, chặn trên auth 8 s, Push 18 s, Pull 13 s, còn lại 3 s. Nội bộ khác: `ValidateToken` 500 ms; phần không bắt buộc của `GetMe` (`SyncBackend.GetStreak`, `GamificationBackend.GetStats`) 300 ms; `Export*` 10 s; `EventSink.Publish` 3 s/lô. `edge` không bao giờ kéo dài deadline của app.
  - **Giới hạn phần tử và giao thức** (THREAT_MODEL TM-44): kiểm số phần tử `repeated` ở đầu handler, trước mọi xử lý — `changes` ≤ 500, `PublishRequest.events` ≤ 200, `LogRequests.logs` ≤ 100 (vượt → `INVALID_ARGUMENT`); `limit` của `Export*` kẹp ≤ 1 000, `GetDailyStats` ≤ 92 ngày; metadata mỗi yêu cầu ≤ 16 KiB (`http2_max_header_list_size`); `max_concurrent_streams` ≤ 100/kết nối; server đặt `timeout` (trần deadline nhóm + 2 s) để lời gọi không có deadline vẫn bị cắt.
  - **Keepalive**: app tắt keepalive ping (tunnel/proxy miễn phí có thể cắt kết nối rảnh và phạt ping dày); kênh tự kết nối lại khi gọi. `edge` đặt `http2_keepalive_interval` 60 s chỉ cho kết nối nội bộ.
  - **Chính sách thử lại = BR-077** (một lớp duy nhất, ở `GrpcClient` của app — SYSTEM-BLUEPRINT §Deadline và thử lại):
    - Chỉ RPC **idempotent**: `Health/Check`, `GetMe`, `Push` (idempotent theo `record_id` + `updated_at_ms`), `Pull`, `GetWeekly`, `GetManifest`, `GetDeck`, `GetExam`.
    - Điều kiện: `UNAVAILABLE` (kể cả lỗi kết nối) hoặc `DEADLINE_EXCEEDED`. Tối đa **3 lần thử lại** sau lần đầu, chờ **2 s, 4 s, 8 s** (nhiễu ±20%); mỗi lần thử có deadline mới theo bảng trên. Hết 3 lần → trả lỗi cho tầng gọi (`SyncEngine` ghi `sync_state.last_error_code` và chờ mốc đồng bộ kế tiếp — DEC-014).
    - `UNAVAILABLE` kèm `retry-after-seconds` (quá tải): lần chờ = max(mốc BR-077, giá trị trailer).
    - `RESOURCE_EXHAUSTED`: **không** thử lại tự động; app chặn gọi lại RPC đó tới hết `retry-after-seconds` và hiện thông báo (vd "Thử lại sau 60 giây").
    - **Không** thử lại: Register, Login, Logout, UpdateMe, ChangePassword, GetResetQuestion, ResetPassword, DeleteAccount (tránh đếm sai khóa tăng dần, tạo trùng).
    - Kênh tắt cơ chế thử lại có sẵn của grpc-java (`disableRetry()`) để không chồng hai lớp; service config chỉ dùng để khai báo `timeout` mặc định từng RPC (cùng giá trị bảng deadline), nạp bằng `defaultServiceConfig(...)`:
```json
{ "methodConfig": [
    { "name": [ { "service": "learninge.app.v1.AuthService" } ], "timeout": "10s" },
    { "name": [ { "service": "learninge.app.v1.SyncService", "method": "Push" } ], "timeout": "20s" },
    { "name": [ { "service": "learninge.app.v1.SyncService", "method": "Pull" },
                { "service": "learninge.app.v1.ContentService" } ], "timeout": "15s" },
    { "name": [ { "service": "learninge.app.v1.LeaderboardService" } ], "timeout": "5s" },
    { "name": [ { "service": "grpc.health.v1.Health" } ], "timeout": "3s" } ] }
```
    - `edge` **không** thử lại lời gọi tới dịch vụ đích (tránh khuếch đại tải; app đã thử lại). Relay outbox có lịch lùi riêng (DATABASE-DESIGN §B3).

## Versioning & tương thích ngược
- Cách đánh phiên bản, chính sách ngừng hỗ trợ, thay đổi phá vỡ cần CR:
  - Phiên bản nằm ở **tên gói**: `learninge.app.v1` (công khai), `learninge.internal.v1` (nội bộ). Tên gói, tên service, tên RPC là một phần của đường dẫn `/learninge.app.v1.AuthService/Login` → **không đổi** trong v1.
  - Trong v1 chỉ thay đổi **cộng thêm**: thêm RPC, thêm message, thêm trường mới với **số trường chưa từng dùng**, thêm giá trị enum, thêm nhánh vào `oneof data` của `Change` (loại bản ghi đồng bộ mới).
  - **Cấm** trong v1: đổi số trường, đổi kiểu trường (kể cả `int32` ↔ `int64`, `string` ↔ `bytes`), đổi tên trường/enum (ảnh hưởng JSON/`grpcurl`, mã sinh), chuyển trường vào/ra `oneof`, đổi `optional` ↔ không `optional`, đổi `repeated` ↔ đơn, đổi nghĩa mã lỗi/`reason`, đổi quy tắc LWW.
  - Xóa trường/giá trị enum: **không xóa trần** mà thêm `reserved <số>;` và `reserved "<tên>";` trong cùng message/enum, ghi chú lý do + ngày; **không bao giờ dùng lại** số hoặc tên đã reserved. Ví dụ:
```proto
message LeaderboardEntry {
  reserved 6;                 // 2026-12-01: bỏ avatar_url (CR-0xx)
  reserved "avatar_url";
  int32 rank = 1;
  // ...
}
```
  - Số trường 1–15 (1 byte) dành cho trường dùng thường xuyên; 19000–19999 cấm (dành cho protobuf). Trong `Change`, số 5–9 để trống cho trường chung sau này, `oneof data` bắt đầu từ 10.
  - Client và server phải **chịu được trường/enum lạ**: Java lite giữ giá trị enum lạ là `UNRECOGNIZED` (app xử lý như "không biết", vd bỏ qua bản ghi Pull loại lạ và ghi log); `prost` bỏ trường lạ. App phải bỏ qua `Change` có `oneof data` rỗng khi kéo về.
  - Thay đổi phá vỡ → CR mới + gói `learninge.v2` chạy **song song** v1 trên cùng `edge` tới hết đợt demo; bản app cũ bị chặn bằng `x-client-version` < `EDGE_MIN_CLIENT` (mặc định 1) → `FAILED_PRECONDITION` `UPGRADE_REQUIRED`.
  - Gói nội bộ có thể đổi nhanh hơn nhưng vẫn theo các luật trên vì các dịch vụ có thể chạy lệch phiên bản trong lúc khởi động lại; payload `Event` trong outbox cũ phải giải mã được bằng code mới (DATABASE-DESIGN §Migration).
  - **Quy trình đồng bộ proto giữa 2 repo**: (1) sửa `.proto` ở `LearningE_Server` qua PR có người review hợp đồng (lập trình viên) — PR phải nêu "cộng thêm" hay "phá vỡ"; (2) merge → chạy `tools/sync-proto.ps1 -ServerRepo <đường dẫn> ` trong repo app → chép gói công khai + cập nhật `proto.lock` (SHA commit + SHA-256 từng tệp) → commit trong repo app; (3) CI/`verifyProtoLock` của app hỏng nếu tệp lệch lock; (4) test hợp đồng Rust chạy trên chính tệp đã chép. Lược đồ server (SQLite/Mongo) đổi bằng migration, không làm đổi hợp đồng v1.

## Webhook / sự kiện (nếu có)
- **Không có webhook hay đẩy sự kiện tới app** (realtime là Non-goal); app chủ động gọi EP-11/EP-12 theo mốc của DEC-014 (đăng nhập, mở app, sau phiên học, "Đồng bộ ngay").
- **Sự kiện nội bộ** giữa các dịch vụ đi qua **outbox + relay** (DATABASE-DESIGN §B3 — lược đồ bảng, danh mục sự kiện, ngữ nghĩa relay). Bên nhận `EventSink` kiểm: khóa nội bộ; cặp (`type` → `source`) cho phép (`user.*`, `auth.*` chỉ từ `auth`; `xp.recorded`, `progress.changed` chỉ từ `sync`) và đích hợp lệ; payload theo ràng buộc nghiệp vụ (`xp_awarded` 0..500, UUID hợp lệ, nickname BR-061) — sự kiện sai bị bỏ (vẫn ack để không kẹt relay) + log ERROR; bỏ qua sự kiện của user đã xóa (tombstone, THREAT_MODEL D-13, TM-39). Hợp đồng gRPC nội bộ:

`learninge/internal/v1/events.proto`
```proto
syntax = "proto3";
package learninge.internal.v1;

// Hiện thực bởi: sync (nhận user.deleted), gamification, insight. Relay của auth và sync gọi.
service EventSink {
  rpc Publish(PublishRequest) returns (PublishResponse);
}
message PublishRequest { repeated Event events = 1; }        // ≤ 200 sự kiện, cùng destination
message PublishResponse { repeated string acked_event_ids = 1; } // đã xử lý hoặc đã có từ trước

message Event {
  string event_id = 1;        // UUIDv7, khóa idempotent
  string type = 2;            // "user.registered" | "user.profile_changed" | "user.deleted"
                              // | "auth.login_failed" | "auth.locked" | "xp.recorded" | "progress.changed"
  string aggregate = 3;       // "user" | "xp_event" | "auth"
  string aggregate_id = 4;
  int64 created_at_ms = 5;
  string source = 6;          // "auth" | "sync"
  oneof payload {
    UserRegistered user_registered = 10;
    UserProfileChanged user_profile_changed = 11;
    UserDeleted user_deleted = 12;
    AuthLoginFailed auth_login_failed = 13;
    AuthLocked auth_locked = 14;
    XpRecorded xp_recorded = 15;
    ProgressChanged progress_changed = 16;
  }
}
// Payload được cắt theo bên nhận: bản gửi insight KHÔNG đặt user_id, nickname (chỉ user_hmac).
message UserRegistered { string user_id = 1; string user_hmac = 2; string nickname = 3; int64 created_at_ms = 4; }
message UserProfileChanged { string user_id = 1; string user_hmac = 2; string nickname = 3; int64 version = 4; }
message UserDeleted { string user_id = 1; string user_hmac = 2; }
enum AuthScope { AUTH_SCOPE_UNSPECIFIED = 0; AUTH_SCOPE_LOGIN = 1; AUTH_SCOPE_RESET = 2; }
message AuthLoginFailed { AuthScope scope = 1; string user_hmac = 2; }   // user_hmac rỗng nếu email không tồn tại
message AuthLocked { AuthScope scope = 1; int32 lock_level = 2; int32 lock_seconds = 3; string user_hmac = 4; }
message XpRecorded {
  string xp_uuid = 1; string user_id = 2; string user_hmac = 3; string source_type = 4;
  int32 xp_awarded = 5; int64 occurred_at_ms = 6; string local_date = 7; string week_start = 8;
  int64 server_seq = 9; int64 received_at_ms = 10;
}
message ProgressChanged {
  string user_id = 1; string user_hmac = 2;
  int32 current_streak = 3; int32 longest_streak = 4; string last_study_date = 5; int64 version = 6;
  string date = 7; int32 cards = 8; int32 quizzes = 9; int32 exams = 10; int32 records = 11;
}
```

`learninge/internal/v1/backends.proto`
```proto
syntax = "proto3";
package learninge.internal.v1;
import "learninge/app/v1/common.proto";
import "learninge/app/v1/auth.proto";
import "learninge/app/v1/sync.proto";
import "learninge/app/v1/leaderboard.proto";
import "learninge/app/v1/content.proto";
import "learninge/internal/v1/events.proto";

service TokenValidator {                                    // auth
  rpc ValidateToken(ValidateTokenRequest) returns (ValidateTokenResponse);
}
message ValidateTokenRequest { bytes token_sha256 = 1; }   // đúng 32 byte; token thô không rời edge
message ValidateTokenResponse { string user_id = 1; int64 expires_at_ms = 2; string user_hmac = 3; }  // sai/hết hạn/thu hồi → UNAUTHENTICATED; user_hmac để edge log/request_logs mà không giữ khóa bút danh

service AuthBackend {                                       // auth; RPC cần user đọc x-user-id
  rpc Register(learninge.app.v1.RegisterRequest) returns (learninge.app.v1.AuthSession);
  rpc Login(learninge.app.v1.LoginRequest) returns (learninge.app.v1.AuthSession);
  rpc Logout(BackendLogoutRequest) returns (learninge.app.v1.LogoutResponse);
  rpc GetProfile(GetProfileRequest) returns (GetProfileResponse);
  rpc UpdateMe(learninge.app.v1.UpdateMeRequest) returns (learninge.app.v1.UpdateMeResponse);
  rpc ChangePassword(learninge.app.v1.ChangePasswordRequest) returns (learninge.app.v1.ChangePasswordResponse);
  rpc GetResetQuestion(learninge.app.v1.GetResetQuestionRequest) returns (learninge.app.v1.GetResetQuestionResponse);
  rpc ResetPassword(learninge.app.v1.ResetPasswordRequest) returns (learninge.app.v1.AuthSession);
  rpc DeleteAccount(learninge.app.v1.DeleteAccountRequest) returns (learninge.app.v1.DeleteAccountResponse);
  rpc ExportProfiles(ExportProfilesRequest) returns (ExportProfilesResponse);
}
message BackendLogoutRequest { bytes token_sha256 = 1; }
message GetProfileRequest {}                                // user lấy từ x-user-id
message GetProfileResponse { learninge.app.v1.User user = 1; }
message ExportProfilesRequest { int64 after_id = 1; int32 limit = 2; }   // limit ≤ 1000
message ExportProfilesResponse { repeated PublicProfile profiles = 1; int64 next_after_id = 2; bool has_more = 3; }
message PublicProfile { string user_id = 1; string nickname = 2; int64 profile_version = 3; }

service SyncBackend {                                       // sync
  rpc Push(learninge.app.v1.PushRequest) returns (learninge.app.v1.PushResponse);
  rpc Pull(learninge.app.v1.PullRequest) returns (learninge.app.v1.PullResponse);
  rpc GetStreak(GetStreakRequest) returns (GetStreakResponse);
  rpc ExportXpEvents(ExportXpEventsRequest) returns (ExportXpEventsResponse);
  rpc ExportUserStats(ExportUserStatsRequest) returns (ExportUserStatsResponse);
}
message GetStreakRequest {}                                 // user lấy từ x-user-id
message GetStreakResponse { int64 total_xp = 1; int32 current_streak = 2; int32 longest_streak = 3; string last_study_date = 4; }
message ExportXpEventsRequest { int64 after_seq = 1; int32 limit = 2; }   // limit ≤ 1000
message ExportXpEventsResponse { repeated XpRecorded events = 1; int64 next_after_seq = 2; bool has_more = 3; }
message ExportUserStatsRequest { string after_user_id = 1; int32 limit = 2; }
message ExportUserStatsResponse { repeated ProgressChanged stats = 1; string next_after_user_id = 2; bool has_more = 3; }

service ContentBackend {                                    // content
  rpc GetManifest(learninge.app.v1.GetManifestRequest) returns (learninge.app.v1.GetManifestResponse);
  rpc GetDeck(learninge.app.v1.GetDeckRequest) returns (learninge.app.v1.GetDeckResponse);
  rpc GetExam(learninge.app.v1.GetExamRequest) returns (learninge.app.v1.GetExamResponse);
}

service GamificationBackend {                               // gamification
  rpc GetWeekly(learninge.app.v1.GetWeeklyRequest) returns (learninge.app.v1.GetWeeklyResponse);
  rpc GetStats(GetStatsRequest) returns (GetStatsResponse);
}
message GetStatsRequest {}                                  // user lấy từ x-user-id
message GetStatsResponse { int64 total_xp = 1; int32 week_xp = 2; string week_start = 3; }

service InsightBackend {                                    // insight
  rpc LogRequests(LogRequestsRequest) returns (LogRequestsResponse);
  rpc GetDailyStats(GetDailyStatsRequest) returns (GetDailyStatsResponse);
}
message LogRequestsRequest { repeated RequestLog logs = 1; }   // ≤ 100
message LogRequestsResponse { int32 accepted = 1; }
message RequestLog {
  string request_id = 1; string rpc = 2; int32 code = 3; int32 duration_ms = 4;
  string user_hmac = 5; int32 client_version = 6; int64 ts_ms = 7;   // không IP, không metadata authorization
}
message GetDailyStatsRequest { string from_date = 1; string to_date = 2; }   // ≤ 92 ngày
message GetDailyStatsResponse { repeated DailyStats days = 1; }
message DailyStats {
  string date = 1; int32 dau = 2; int32 new_users = 3; int32 cards_reviewed = 4; int64 xp_total = 5;
  int32 quiz_count = 6; int32 exam_count = 7; int32 push_count = 8; int32 push_records = 9;
  int32 login_failed = 10; int32 locks = 11;
}
```

### Bộ mặc định "Sổ tay của tôi" — `uuid` tất định (SYSTEM-BLUEPRINT §Bộ mặc định)
- Hằng namespace UUIDv5 (cố định vĩnh viễn, có trong `common::ids` của server và `SyncIds` của app): **`NS_LEARNINGE = a7039332-6c5e-4d97-8f86-2dc4e4e084cd`**.
- `uuid` của bộ = `UUIDv5(NS_LEARNINGE, "notebook:" + user_id)` với `user_id` là `User.id` dạng chữ thường có gạch nối; kết quả viết chữ thường. Rust: `Uuid::new_v5(&NS_LEARNINGE, format!("notebook:{user_id}").as_bytes())`; Java: tự hiện thực SHA-1 theo RFC 9562 §5.5 (JDK chỉ có `nameUUIDFromBytes` = v3/MD5, **không dùng được**) — test chéo bằng một cặp (user_id, uuid) cố định trong cả hai bộ test.
- `sync` tạo bộ khi nhận `user.registered` (`INSERT OR IGNORE`, `name = "Sổ tay của tôi"`, `is_default = 1`, cấp `server_seq`); app tạo cục bộ trước (nếu cần) với cùng `uuid` → Push về thành `APPLIED`/LWW trên cùng khóa, không sinh bản trùng.
