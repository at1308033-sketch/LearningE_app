# Glossary — thuật ngữ nghiệp vụ và kỹ thuật

> **TỰ SINH** từ bộ phân tích thiết kế đã duyệt của nhóm — không sửa trực tiếp tệp này; đề xuất thay đổi qua Issue/PR để nhóm trưởng cập nhật nguồn và sinh lại. Danh mục tài liệu: [docs/README.md](../README.md).

**Mục đích:** từ điển thuật ngữ dùng thống nhất trong code, giao diện và tài liệu; kiến thức miền về học từ vựng, ôn tập ngắt quãng, dạng câu hỏi, ràng buộc Android.

## Thuật ngữ
> Từ điển thuật ngữ thống nhất. Code dùng tên tiếng Anh ở cột "Tên trong code"; tài liệu và giao diện dùng tên tiếng Việt.

## Thuật ngữ nghiệp vụ
| Thuật ngữ | Định nghĩa | Không nhầm với | Ghi chú |
|---|---|---|---|
| Người dùng (User) | Người có tài khoản cục bộ trong app; mọi dữ liệu học thuộc về một người dùng | Thành viên nhóm phát triển | Code: `User`. Chỉ 1 vai trò |
| Tài khoản cục bộ | **(Thay thế từ thay đổi phạm vi, giữ để truy vết thay đổi phạm vi..thay đổi phạm vi)** Tài khoản từng lưu trong SQLite trên máy, không đồng bộ. Từ thay đổi phạm vi tài khoản nằm trên máy chủ (xem "Tài khoản trên máy chủ") | Tài khoản Google/Firebase | Mật khẩu chỉ băm trên máy chủ |
| Chủ đề (Topic) | Nhóm từ vựng theo ngữ cảnh, có cấp độ CEFR | Bài ngữ pháp | Code: `Topic` |
| Từ vựng (Word) | Một mục từ seed sẵn: từ, phiên âm, từ loại, nghĩa, câu ví dụ | Từ trong sổ tay | Code: `Word` |
| Từ trong sổ tay (Notebook entry) | Từ người dùng tự thêm hoặc lưu từ kết quả tra | Từ yêu thích | Code: `NotebookEntry`; thuộc riêng 1 người dùng |
| Yêu thích (Favorite) | Đánh dấu một từ có sẵn để xem nhanh | Sổ tay | Chỉ là cờ đánh dấu, không tạo từ mới |
| Phiên âm (IPA) | Cách đọc viết bằng ký hiệu ngữ âm quốc tế, ví dụ /ˈwɔːtər/ | Phát âm (âm thanh) | Lưu dạng chuỗi Unicode |
| Phát âm (TTS) | Âm thanh đọc từ do Android TextToSpeech tạo | File âm thanh ghi sẵn | Không lưu file âm thanh |
| Flashcard (thẻ) | Thẻ 2 mặt: mặt trước từ tiếng Anh, mặt sau nghĩa + ví dụ | Câu hỏi quiz | Mỗi cặp người dùng–từ có 1 trạng thái thẻ |
| Trạng thái thẻ | Mới / Đang học / Đã thuộc, kèm hộp Leitner và ngày ôn tiếp | Điểm quiz | Code: `CardProgress` |
| Đã thuộc | Thẻ ở hộp Leitner cao nhất (hộp 5) hoặc người dùng tự đánh dấu | Đã học (đã xem qua) | Dùng cho thống kê "số từ đã thuộc" |
| Hộp Leitner (box) | Mức 1–5 quyết định khoảng cách ôn (1/2/4/7/14 ngày) | Cấp độ CEFR | Đúng → lên 1 hộp; sai → về hộp 1 |
| Ôn tập (Review) | Phiên học các thẻ đến hạn (`next_review_date` ≤ hôm nay) | Học từ mới | Theo mục tiêu hằng ngày |
| Học từ mới (Learn) | Phiên học các từ chưa từng học trong một chủ đề | Ôn tập | Số từ theo mục tiêu hằng ngày |
| Lặp lại ngắt quãng | Phương pháp ôn với khoảng cách tăng dần | Học dồn | Tiếng Anh: spaced repetition |
| Cấp độ CEFR | A1, A2, B1, B2, C1, C2 | Hộp Leitner | LearningE dùng A1–B1 |
| Bài ngữ pháp (Grammar lesson) | Một chủ điểm ngữ pháp: lý thuyết, ví dụ, bài tập | Chủ đề từ vựng | Code: `GrammarLesson` |
| Bài kiểm tra / Quiz | Một lượt làm nhiều câu hỏi về 1 chủ đề hoặc 1 bài ngữ pháp | Ôn tập flashcard | Code: `Quiz` (định nghĩa), `QuizAttempt` (lượt làm) |
| Câu hỏi (Question) | Một câu trong quiz, thuộc 1 dạng: trắc nghiệm, nghe chọn, điền từ, sắp xếp câu | Flashcard | Code: `Question`, `QuestionType` |
| Lượt làm bài (Attempt) | Một lần người dùng làm quiz: thời điểm, điểm, câu trả lời | Quiz | Lưu lịch sử |
| Điểm (Score) | Số câu đúng / tổng số câu × 100 của một lượt làm | Chuỗi ngày học | Không có điểm âm |
| Mục tiêu hằng ngày (Daily goal) | Số từ mới cần học mỗi ngày (mặc định 10) | Nhắc học | Người dùng chỉnh trong Cài đặt |
| Chuỗi ngày học (Streak) | Số ngày liên tiếp có ít nhất 1 hoạt động học hoàn thành | Tổng số ngày đã học | Bỏ 1 ngày → về 0; tính theo ngày giờ địa phương |
| Hoạt động học | Hoàn thành một phiên học từ mới, ôn tập, quiz hoặc bài ngữ pháp | Mở app | Chỉ mở app không tính |
| Nhắc học (Reminder) | Thông báo cục bộ hằng ngày vào giờ người dùng chọn | Thông báo đẩy (push) | Không cần mạng; bỏ qua nếu đã đạt mục tiêu |
| Tiến độ (Progress) | Thống kê: số từ đã thuộc theo chủ đề, điểm gần đây, chuỗi ngày học | Lịch sử quiz | Màn hình Tiến độ |
| Tra từ (Search) | Tìm theo từ tiếng Anh hoặc nghĩa tiếng Việt trong dữ liệu seed + sổ tay | Từ điển trực tuyến | Không gọi API ngoài |
| Dữ liệu seed | Nội dung học có sẵn nạp vào SQLite khi cài app | Dữ liệu người dùng | Nguồn và giấy phép: Glossary (kiến thức miền) |
| Offline | Học, ôn, quiz, thi thử chạy không cần Internet sau lần đăng nhập đầu (thay đổi phạm vi: xem "Offline-first"); đăng ký/đăng nhập lần đầu, bảng xếp hạng, tải nội dung cần mạng | — | Ràng buộc bắt buộc cho nhóm tính năng học |
| Tài khoản trên máy chủ | Tài khoản do máy chủ quản lý: email, biệt danh, mật khẩu băm PBKDF2 + salt, câu hỏi bảo mật; app chỉ giữ token | Tài khoản cục bộ (cũ) | REQ-001; máy không lưu mật khẩu |
| Offline-first | Ưu tiên chạy bằng dữ liệu SQLite trên máy; mạng chỉ dùng để đồng bộ, bảng xếp hạng, tải nội dung; ghi ở máy trước, đẩy sau | Chế độ chỉ-online | REQ-020, NFR-N1, BR-078 |
| XP (điểm kinh nghiệm) | Điểm thưởng theo hoạt động học: thẻ "Đã nhớ" +2, "Chưa nhớ" +1; quiz +1/câu đúng (+5 nếu 100%); thi thử +1/câu đúng; tối đa 500 XP/ngày | Điểm quiz (Score) | REQ-021; BR-065..BR-068; ghi trong `xp_events` |
| Bảng xếp hạng tuần | Danh sách top 50 người dùng theo tổng XP trong tuần ISO, kèm hạng của "Bạn"; hòa thì ai đạt sớm hơn xếp trên | Bảng xếp hạng toàn thời gian | REQ-021; BR-069; SCR-34; cần mạng, mất mạng hiện bản lưu gần nhất |
| Tuần ISO | Tuần bắt đầu thứ Hai 00:00:00 đến Chủ nhật 23:59:59 theo múi giờ Asia/Ho_Chi_Minh (UTC+7) | Tuần Chủ nhật–Thứ bảy | BR-069 |
| Rương Leitner | 5 rương pixel tương ứng 5 hộp Leitner, số trên rương là số thẻ đang ở hộp đó (mọi bộ) | Hộp Leitner (khái niệm); "rương" là cách hiển thị | BR-071; SCR-21, SCR-10 |
| Chế độ bản đồ | Cách hiển thị SCR-06 dạng bản đồ pixel: 15 công trình chủ đề, mở khóa theo cấp độ, hiện sao chủ đề; công tắc Lưới/Bản đồ, mặc định Lưới | Lưới chủ đề | REQ-024 (Could); BR-072, BR-074 |
| Sao chủ đề | 1–3 sao theo tỷ lệ từ Đã thuộc của chủ đề: 3★ khi 100%, 2★ khi ≥ 2/3, 1★ khi ≥ 1/3 | Điểm quiz | BR-072; `ThemeStars` |
| Lớp pixel (pixel layer) | Lớp trang trí kiểu trò chơi phủ lên thiết kế v2: ngọn lửa, huy hiệu, rương, màn ăn mừng, bản đồ | Thiết kế v2 (nền) | REQ-023, REQ-024 |
| Hết hạn phiên (`UNAUTHENTICATED`) | Máy chủ trả mã gRPC `UNAUTHENTICATED` (trước thay đổi phạm vi: HTTP 401) khi token sai/hết hạn/bị thu hồi; app dừng đồng bộ, xóa token, giữ dữ liệu chưa đẩy và đưa về đăng nhập | Đăng xuất chủ động | BR-059 |
| Ngoại tuyến (thanh "Đang ngoại tuyến") | Trạng thái mất kết nối mạng/máy chủ; app hiển thị thanh dùng chung và vẫn cho học | Lỗi máy chủ (`UNAVAILABLE`/`INTERNAL`) | BR-078, SCR-35 |

## Thuật ngữ kỹ thuật
| Thuật ngữ | Định nghĩa | Không nhầm với | Ghi chú |
|---|---|---|---|
| Máy chủ (server), Hệ thống máy chủ LearningE | **** Hệ thống phía server của nhóm gồm **6 microservice Rust** (`edge`, `auth`, `sync`, `content`, `gamification`, `insight`) giao tiếp gRPC; SQLite là CSDL chính (tài khoản, token, tiến độ), MongoDB là kho phụ (XP + bảng xếp hạng, nội dung, audit/log, bản sao phân tích); mã nguồn ở repo riêng `LearningE_Server`. Nơi chạy khi demo chưa quyết định (PQ-9). Thay "chương trình Java (Javalin) + SQLite, module `:server`" của thay đổi phạm vi | Firebase/dịch vụ đám mây; tác nhân "Hệ thống" (khởi tạo seed ở máy) | API ở API_CONTRACT (dịch vụ gRPC `learninge.app.v1`, EP-01..EP-16 là bí danh RPC); DEC-018..DEC-025 |
| Token (token opaque) | Chuỗi ngẫu nhiên 256 bit máy chủ cấp sau đăng nhập; máy chủ chỉ lưu băm SHA-256, hạn 30 ngày, thu hồi được; không phải JWT | JWT (có chữ ký, tự chứa dữ liệu) | BR-057, DEC-013; app lưu trong `TokenStore` |
| Đồng bộ (đẩy/kéo, push/pull) | Đẩy (push): gửi bản ghi `dirty` lên máy chủ theo lô ≤ 500; kéo (pull): lấy thay đổi mới hơn theo con trỏ `server_seq`; chạy tự động và qua nút "Đồng bộ ngay" | Thông báo đẩy (push notification), sao lưu một lần | REQ-020; BR-062..BR-064, BR-077 |
| LWW (Last-Write-Wins) | Quy tắc xung đột: bản ghi có `updated_at` mới hơn thắng, bản cũ bị trả về `stale` | Gộp thủ công/CRDT | BR-062; `updated_at` do máy chủ ghi nhận và kẹp khi lệch giờ (TM-17) |
| Tombstone (bản ghi xóa mềm) | Bản ghi đánh dấu `deleted = 1` (kèm `updated_at`) để lan truyền việc xóa sang máy khác; chỉ xóa cứng sau khi máy chủ xác nhận | Xóa hẳn dòng ngay | BR-063 |
| Outbox / cờ `dirty` | Cờ `dirty = 1` trên bản ghi đã đổi ở máy nhưng chưa đẩy; tập các bản ghi `dirty` là hàng đợi chờ đẩy | Bảng log riêng | Hiện ở SCR-35 "n thay đổi chờ đẩy"; BR-064 |
| Khóa tăng dần (progressive lockout) | Chống đoán mật khẩu/câu trả lời: sai 5 lần → khóa 60 giây, mỗi lần khóa kế tiếp gấp đôi (tối đa 1 giờ), tính cả email không tồn tại; về mức 0 sau 24 giờ không sai | Khóa cố định / chặn IP | Quyết định 11/11/2026; BR-003, BR-008, AC-002.8, TM-01, TM-02 |
| HMAC (HMAC-SHA256) | Mã xác thực băm có khóa bí mật; dùng để sinh câu hỏi bảo mật giả ổn định cho email không tồn tại, khóa từ biến môi trường `LEARNINGE_SERVER_KEY` | SHA-256 thường (không khóa) | BR-076, THREAT_MODEL D-03 |
| IDOR | Insecure Direct Object Reference: sửa mã trong yêu cầu để truy cập dữ liệu của người khác; chặn bằng cách lấy `user_id` từ token, không tin mã từ client | SQL injection | THREAT_MODEL TM-14 |
| Đường hầm (tunnel) | Dịch vụ công khai cổng gRPC của `edge` chạy trên laptop ra Internet để điện thoại thật kết nối khi demo; **thay đổi phạm vi: phải hỗ trợ HTTP/2 tới origin** (ứng viên ngrok, Cloudflare Tunnel — kết luận SPIKE S-02); thay "đường hầm HTTPS" của thay đổi phạm vi | VPN, hosting đám mây | DEC-024, NFR-E4; dự phòng LAN (gRPC không mã hóa, chỉ bản debug) và emulator `10.0.2.2:50051`; phương án hosting khác (Atlas/Railway/VPS) chưa quyết định — PQ-9 |
| `server_seq` (số thứ tự đồng bộ, khóa tăng dần) | Số thứ tự tăng dần máy chủ gán cho mỗi thay đổi, làm con trỏ kéo (`since`) | `updated_at` (thời điểm, có thể lệch) | EP-12; DATABASE-DESIGN B |
| FlameStage | Giai đoạn ngọn lửa pixel 0–4 theo chuỗi ngày: 0 tắt; 1–2 → 1; 3–6 → 2; 7–29 → 3; ≥ 30 → 4 | Chuỗi ngày (Streak, số ngày) | Code: `FlameStage.of(streak)`; BR-070; REQ-023 |
| Sprite / pixel art | Hình ảnh ngọn lửa, huy hiệu, rương, cúp vẽ theo lưới điểm ảnh, xuất VectorDrawable (≤ 30 path) hoặc PNG `filterBitmap=false` để không bị mờ | Minh họa vector thông thường (design v2) | REQ-023; T-131; UI-UX-SPECIFICATION (hệ thống thiết kế) "Pixel layer" |
| Javalin | **Đã thay bằng Rust (tokio + tonic) ở máy chủ ** (DEC-018 thay DEC-012). Thư viện web Java 6.7.0 từng dùng cho máy chủ monolith của thay đổi phạm vi; giữ để truy vết và làm phương án quay lui RB-1 | Spring Boot | thay đổi phạm vi DEC-012 (lưu ở `docs/changes/thay đổi phạm vi-snapshot/`) |
| Jackson | **Đã thay bằng protobuf (prost ở máy chủ, protobuf-lite ở app) **: giao tiếp app–máy chủ không còn JSON. Thư viện ánh xạ JSON Java 2.22.4 từng được duyệt cho máy chủ thay đổi phạm vi | `org.json` (app thay đổi phạm vi, nay cũng thay bằng protobuf-lite) | thay đổi phạm vi DEC-018, DEC-021; SYSTEM-BLUEPRINT (công nghệ) |
| Contract-first (hợp đồng-trước) | Chốt API_CONTRACT (thay đổi phạm vi: các file `.proto`) trước, rồi app (`GrpcClient`) và 6 dịch vụ máy chủ (T-140..T-154) làm song song theo hợp đồng; đổi hợp đồng phải qua CR | Code-first | Pipeline `contract-first`; proto v1 đóng băng ở M0b (16/11); chỉ thêm trường/RPC, không đổi số trường (DEC-022) |
| Rust | Ngôn ngữ lập trình hệ thống (edition 2021, toolchain ghim trong `rust-toolchain.toml`) dùng viết cả 6 dịch vụ máy chủ; an toàn bộ nhớ nhờ borrow checker | Java (ngôn ngữ của app Android) | thay đổi phạm vi, DEC-018; T-140 học Rust |
| tokio | Thư viện chạy bất đồng bộ (async runtime) của Rust, nền cho tonic; tác vụ chặn (SQLite) chạy trong `spawn_blocking` | Luồng (thread) Java | DEC-018 |
| tonic (và prost) | Thư viện gRPC của Rust (tonic) và bộ sinh/đọc protobuf (prost, tonic-build) dùng ở cả 6 dịch vụ; không dùng web framework HTTP | Javalin/axum (framework HTTP, không dùng) | DEC-018 |
| gRPC | Cách gọi hàm từ xa (RPC) dùng HTTP/2 và protobuf; mỗi lời gọi có tên RPC, deadline, metadata và mã trạng thái; mọi RPC của hệ thống ở bản này là unary (một yêu cầu — một phản hồi) | REST/JSON qua HTTP/1.1 (cách của thay đổi phạm vi) | thay đổi phạm vi, DEC-021, DEC-022; app dùng `grpc-okhttp` |
| protobuf, `.proto` | Định dạng thông điệp nhị phân có kiểu và file `.proto` mô tả dịch vụ + thông điệp; nguồn sự thật ở repo server (`proto/learninge/app/v1`, `proto/learninge/internal/v1`), app giữ bản sao gói công khai + `proto.lock` (SHA) | JSON | DEC-022; quy tắc tương thích: chỉ thêm trường/RPC, xóa thì `reserved` |
| Microservice | Kiến trúc chia hệ thống thành nhiều dịch vụ nhỏ độc lập, mỗi dịch vụ một trách nhiệm và kho dữ liệu riêng (database-per-service), giao tiếp qua gRPC; hệ thống LearningE có 6: `edge`, `auth`, `sync`, `content`, `gamification`, `insight` | Monolith (một chương trình, thay đổi phạm vi) | DEC-019; chế độ gộp `--all` chạy cả 6 trong 1 tiến trình để dự phòng demo |
| edge (cổng vào, API gateway) | Dịch vụ duy nhất app gọi trực tiếp (cổng 50051, chỉ nó lộ ra ngoài): xác thực token qua `auth`, xóa metadata `x-user-id` do client gửi rồi tự gắn lại, giới hạn tần suất, gắn `x-request-id`, chuyển lời gọi tới dịch vụ đích | Envoy hoặc gateway có sẵn (không dùng, thay đổi phạm vi Q2) | DEC-019; NFR-S9 |
| Outbox | Bảng trong SQLite của một dịch vụ, ghi **cùng giao dịch** với thay đổi nghiệp vụ, lưu sự kiện chờ phát sang dịch vụ/MongoDB khác; tránh lệch dữ liệu do ghi kép (dual-write) | Hàng đợi tin nhắn ngoài (không dùng) | DEC-020; NFR-N6 |
| Relay | Tác vụ nền của dịch vụ đọc outbox theo thứ tự, gọi `EventSink.Publish` của bên nhận, nhận ack thì đánh dấu đã phát; lỗi thì thử lại lùi dần 1 s → trần 15 s (thử ngay khi đích `SERVING`) | Đồng bộ app ↔ máy chủ (push/pull) | DEC-020 |
| Idempotent (bất biến khi lặp lại) | Thao tác thực hiện nhiều lần vẫn cho kết quả như một lần; sự kiện outbox giao ít nhất một lần nhưng bên nhận upsert theo `event_id` nên hiệu quả đúng một lần | Giao đúng một lần tuyệt đối | DEC-020; BR-062 (`duplicate`) |
| MongoDB | CSDL tài liệu (NoSQL) chạy song song SQLite làm **kho phụ**: XP + bảng xếp hạng (`learninge_game`), nội dung học (`learninge_content`), audit/log + bản sao phân tích (`learninge_insight`); không giữ email, băm mật khẩu, câu trả lời bảo mật; mất MongoDB không làm mất dữ liệu nghiệp vụ lõi | SQLite (CSDL chính, nguồn sự thật) | DEC-020; image `mongo:8.0`; NFR-N5 |
| Collection (tập tài liệu) | Nhóm tài liệu trong MongoDB, tương đương bảng ở SQLite (vd `weekly_xp`, `content_decks`, `audit_events`) | Bảng SQLite | DATABASE-DESIGN (mô hình MongoDB) |
| TTL index | Chỉ mục MongoDB tự xóa tài liệu sau một thời gian: `audit_events` 90 ngày, `request_logs` 7 ngày | Xóa tay theo lịch | DEC-020; NFR-R3 |
| Aggregation pipeline | Chuỗi bước xử lý tổng hợp trong MongoDB (lọc, nhóm, sắp xếp); dùng dựng top 50 bảng xếp hạng tuần với chỉ mục `{week:1, xp:-1, reached_at:1}` | Truy vấn SQL `GROUP BY` | `gamification`; BR-069 |
| Docker, Docker Compose | Docker chạy dịch vụ trong container; Compose khai báo `mongo:8.0` + 6 dịch vụ + mạng nội bộ (chỉ `edge` mở `127.0.0.1:50051`), khởi động bằng `docker compose up` | Máy ảo (VM) | DEC-024, NFR-E4; nơi chạy demo chưa quyết định (PQ-9) |
| Deadline (gRPC) | Hạn chót của một lời gọi RPC do bên gọi đặt; quá hạn thì lỗi `DEADLINE_EXCEEDED` và dịch vụ hủy việc; app: auth 10 s, Push 20 s, Pull 15 s, GetWeekly 5 s, content 15 s; edge truyền phần còn lại trừ 100 ms | Timeout kết nối/đọc của HTTP | NFR-N2; API_CONTRACT |
| Metadata (gRPC) | Cặp khóa–giá trị gửi kèm lời gọi (header) hoặc phản hồi (trailer): `authorization: Bearer <token>`, `x-request-id`, nội bộ `x-user-id`; trailer `error-code` (mã nghiệp vụ), `retry-after-seconds` | Dữ liệu trong thông điệp protobuf | DEC-022, DEC-025; BR-057 |
| Mã trạng thái gRPC | Kết quả mỗi RPC: `OK`, `INVALID_ARGUMENT`, `UNAUTHENTICATED`, `PERMISSION_DENIED`, `NOT_FOUND`, `ALREADY_EXISTS`, `RESOURCE_EXHAUSTED`, `UNAVAILABLE`, `INTERNAL`, `DEADLINE_EXCEEDED`; thay mã HTTP 400/401/403/404/409/413/429/503/500 của thay đổi phạm vi (bảng ánh xạ ở SRS §Ngoại lệ) | Mã HTTP | API_CONTRACT; app ánh xạ sang `ErrorCode` |
| Câu hỏi bảo mật giả | Câu hỏi máy chủ trả cho email không tồn tại, chọn ổn định bằng HMAC-SHA256 của email để không lộ email có tài khoản hay không | Câu hỏi bảo mật thật | BR-076, AC-027.3 |

## Viết tắt
| Viết tắt | Đầy đủ | Nghĩa |
|---|---|---|
| CEFR | Common European Framework of Reference for Languages | Khung trình độ ngôn ngữ A1–C2 |
| IPA | International Phonetic Alphabet | Ký hiệu phiên âm quốc tế |
| TTS | Text-To-Speech | Đọc văn bản thành giọng nói (Android TextToSpeech) |
| XP | Experience Points | Điểm kinh nghiệm theo hoạt động học (BR-065..BR-068) |
| LWW | Last-Write-Wins | Xung đột đồng bộ: bản ghi `updated_at` mới hơn thắng (BR-062) |
| API | Application Programming Interface | Giao diện lập trình; hợp đồng gRPC `learninge.app.v1` (EP-01..EP-16) ở API_CONTRACT |
| EP | Endpoint (thay đổi phạm vi: bí danh của RPC) | Mã thao tác ở API_CONTRACT, giữ để truy vết; vd EP-03 = RPC `AuthService.Login` (trước thay đổi phạm vi: `POST /api/v1/auth/login`) |
| HMAC | Hash-based Message Authentication Code | Mã xác thực băm có khóa; ở đây HMAC-SHA256 (BR-076) |
| PBKDF2 | Password-Based Key Derivation Function 2 | Hàm băm mật khẩu chậm, 120.000 vòng (DEC-003, DEC-013) |
| IDOR | Insecure Direct Object Reference | Lỗi truy cập dữ liệu người khác qua mã trong yêu cầu (TM-14) |
| TLS | Transport Layer Security | Mã hóa đường truyền cho gRPC; dùng qua tunnel khi demo (thay đổi phạm vi thay HTTPS của thay đổi phạm vi) |
| LAN | Local Area Network | Mạng cục bộ cùng Wi-Fi; đường dự phòng demo (gRPC không mã hóa, chỉ bản debug, host trong danh sách trắng) |
| gRPC / RPC | gRPC Remote Procedure Call / Remote Procedure Call | Gọi hàm từ xa; EP-xx là bí danh RPC ở API_CONTRACT |
| TTL | Time To Live | Thời gian sống của dữ liệu MongoDB trước khi tự xóa (TTL index) |
| NoSQL | Not only SQL | CSDL không quan hệ; ở đây MongoDB (kho phụ) |
| UUID | Universally Unique Identifier | Mã định danh 128 bit do app sinh cho bản ghi đồng bộ (`uuid`, BR-062) |
| ISO (tuần ISO) | International Organization for Standardization (ISO 8601) | Chuẩn tuần bắt đầu thứ Hai (BR-069) |
| DAO | Data Access Object | Lớp truy cập SQLite ở app (`UserDao`, `DeckDao`...) |
| SAF | Storage Access Framework | Chọn tệp import/export trên Android |
| CR | Change Request | Phiếu thay đổi yêu cầu (`docs/changes/CR-xxx.md`) |
| BR / REQ / US / AC / FLOW / TC / TM | Business Rule / Requirement / User Story / Acceptance Criteria / Flow / Test Case / Threat Model item | ID truy vết (TEST-CASES) |
| CVE | Common Vulnerabilities and Exposures | Mã định danh lỗ hổng bảo mật công khai |

## Khái niệm nghiệp vụ

### 1. Cấu trúc nội dung học
| Khái niệm | Mô tả | Áp dụng trong LearningE |
|---|---|---|
| Chủ đề (Topic) | Nhóm từ theo ngữ cảnh: gia đình, trường học, đồ ăn, du lịch, công việc... | 10–15 chủ đề seed sẵn; mỗi chủ đề có tên, ảnh/icon, cấp độ |
| Từ vựng (Word) | Đơn vị học nhỏ nhất: từ, phiên âm IPA, từ loại, nghĩa tiếng Việt, câu ví dụ (Anh + Việt) | Khoảng 30 từ/chủ đề, tổng ≥300 từ |
| Cấp độ CEFR | Khung tham chiếu châu Âu: A1, A2 (cơ bản), B1, B2 (trung cấp), C1, C2 (cao cấp) | Mỗi chủ đề và bài ngữ pháp gắn A1/A2/B1 để người dùng chọn đúng mức |
| Bài ngữ pháp (Grammar lesson) | Một chủ điểm: công thức, cách dùng, ví dụ, lỗi thường gặp, bài tập ngắn | 8–10 bài: các thì cơ bản, a/an/the, so sánh, câu điều kiện loại 1... |
| Sổ tay từ (Notebook) | Từ do người dùng tự thêm hoặc lưu từ kết quả tra | Thuộc riêng từng người dùng |

### 2. Ghi nhớ và ôn tập
- **Đường cong quên (Ebbinghaus, 1885):** lượng ghi nhớ giảm nhanh trong vài ngày đầu nếu không ôn; mỗi lần ôn đúng lúc làm khoảng cách đến lần quên tiếp theo dài ra.
- **Lặp lại ngắt quãng (spaced repetition):** ôn mỗi mục với khoảng cách tăng dần (ví dụ 1 → 3 → 7 → 14 ngày) thay vì học dồn.
- **Hệ thống Leitner:** thẻ nằm trong các "hộp" 1..n. Trả lời đúng → lên hộp kế tiếp (ôn thưa hơn); sai → về hộp 1. Đơn giản, dễ giải thích trong báo cáo.
- **SM-2 (SuperMemo 2):** mỗi thẻ có hệ số dễ (ease factor, khởi đầu 2,5) và khoảng cách ôn; người học tự chấm mức nhớ 0–5 sau mỗi lần ôn; dưới 3 thì học lại từ đầu. AnkiDroid dùng thuật toán này.
- **Truy hồi chủ động (active recall):** tự nhớ ra đáp án (lật thẻ, quiz) hiệu quả hơn đọc lại.

Đề xuất cho LearningE (quyết định cuối ở ARCHITECTURE-DECISIONS, phase 3): dùng **Leitner 5 hộp**, khoảng ôn 1/2/4/7/14 ngày. Lý do: đủ hiệu quả, dễ hiện thực bằng SQLite (1 cột `box`, 1 cột `next_review_date`), dễ trình bày bằng sơ đồ trạng thái. SM-2 để làm hướng phát triển.

### 3. Dạng câu hỏi kiểm tra
| Dạng | Mô tả | Chấm |
|---|---|---|
| Trắc nghiệm nghĩa (Anh → Việt, Việt → Anh) | 1 câu hỏi, 4 đáp án, 1 đúng; đáp án nhiễu lấy từ cùng chủ đề | Đúng/sai |
| Nghe chọn từ | Phát âm bằng TTS, chọn từ đúng | Đúng/sai |
| Điền từ vào câu | Câu ví dụ bị khuyết 1 từ, chọn hoặc gõ | So khớp không phân biệt hoa thường, bỏ khoảng trắng thừa |
| Sắp xếp từ thành câu | Các từ xáo trộn, chạm theo thứ tự | So khớp đúng thứ tự |
| Bài tập ngữ pháp | Trắc nghiệm theo chủ điểm của bài ngữ pháp | Đúng/sai, có giải thích |
Điểm bài kiểm tra = số câu đúng / tổng số câu × 100. Từ trả lời sai được đưa về hộp 1 (ôn lại sớm).

### 4. Động lực và thói quen
- **Mục tiêu hằng ngày:** số từ mới hoặc số phút mỗi ngày (mặc định 10 từ).
- **Chuỗi ngày học (streak):** số ngày liên tiếp có ít nhất 1 hoạt động học hoàn thành; bỏ 1 ngày thì về 0. Tính theo ngày lịch giờ địa phương của máy.
- **Nhắc học:** thông báo cục bộ mỗi ngày vào giờ người dùng chọn; không nhắc nếu hôm đó đã đạt mục tiêu.
- **Không phạt:** sai không trừ điểm tích lũy, không khóa bài (PRD (phân tích đối thủ)).

### 5. Ràng buộc kỹ thuật của miền trên Android
| Chủ đề | Ghi chú cho lập trình viên |
|---|---|
| TextToSpeech | `android.speech.tts.TextToSpeech`, locale lấy từ ngôn ngữ của bộ (`language.ttsLocale`: en-US, ja-JP, zh-CN, ko-KR…), không cố định một locale; phải kiểm tra `LANG_MISSING_DATA`/`LANG_NOT_SUPPORTED` và báo người dùng tải gói giọng; giọng offline tùy máy |
| Thông báo nhắc học | Android 13+ cần quyền runtime `POST_NOTIFICATIONS`; lên lịch bằng `AlarmManager` (inexact) hoặc `WorkManager`. Báo thức chính xác (`SCHEDULE_EXACT_ALARM`) bị hạn chế từ Android 14, không cần cho nhắc học. Cần đặt lại lịch sau khi máy khởi động lại (`BOOT_COMPLETED`) nếu dùng AlarmManager |
| Lưu mật khẩu cục bộ | Không lưu mật khẩu dạng rõ; băm có salt (ví dụ PBKDF2 qua `javax.crypto.SecretKeyFactory`) |
| Seed dữ liệu | Nạp lúc tạo CSDL (`SQLiteOpenHelper.onCreate`) từ file trong `assets/` (JSON/CSV/SQL); nâng phiên bản qua `onUpgrade` |
| Ngày giờ | Streak và lịch ôn tính theo ngày địa phương; lưu ngày dạng `yyyy-MM-dd` hoặc epoch millis thống nhất |

## Quy trình nghiệp vụ hiện tại
Quy trình mục tiêu của một người học (chi tiết hóa thành Use-Cases (luồng) ở phase 2):
1. Mở app lần đầu → đăng ký tài khoản cục bộ → chọn cấp độ và mục tiêu hằng ngày → chọn giờ nhắc học (cho phép thông báo).
2. Màn hình chính: hôm nay cần ôn bao nhiêu thẻ, còn bao nhiêu từ mới để đạt mục tiêu, chuỗi ngày học.
3. **Học từ mới:** chọn chủ đề → xem danh sách từ → học bằng flashcard (lật thẻ, nghe phát âm) → đánh dấu "đã thuộc"/"chưa thuộc" → từ vào hộp Leitner tương ứng.
4. **Ôn tập:** app gom các thẻ đến hạn (`next_review_date` ≤ hôm nay) → người dùng ôn → đúng lên hộp, sai về hộp 1.
5. **Kiểm tra:** chọn chủ đề/bài ngữ pháp → làm quiz → xem điểm, đáp án đúng → kết quả lưu lịch sử.
6. **Ngữ pháp:** chọn bài → đọc lý thuyết, ví dụ → làm bài tập ngắn.
7. **Tra từ:** tìm trong dữ liệu có sẵn + sổ tay → lưu vào sổ tay hoặc yêu thích; người dùng có thể tự thêm từ mới.
8. **Tiến độ:** xem số từ đã thuộc theo chủ đề, điểm quiz gần đây, chuỗi ngày học.
9. Hôm sau: nhận thông báo nhắc học vào giờ đã chọn (nếu chưa học) → quay lại bước 2.

Quy trình hiện tại khi không có app: xem PRD (nghiên cứu người dùng) §Hành trình hiện tại.

## Quy định / pháp lý liên quan
| Quy định | Liên quan đến LearningE | Cách xử lý |
|---|---|---|
| Bảo vệ dữ liệu cá nhân tại Việt Nam (Nghị định 13/2023/NĐ-CP; Luật Bảo vệ dữ liệu cá nhân 2025, hiệu lực từ 01/01/2026 — đối chiếu văn bản gốc khi viết báo cáo) | App lưu tên, email (nếu có), lịch sử học trên máy | Chỉ thu dữ liệu cần thiết, lưu cục bộ, không gửi ra ngoài; có chức năng xóa tài khoản và dữ liệu; mật khẩu băm |
| Chính sách Google Play về quyền thông báo | Quyền `POST_NOTIFICATIONS` | Xin quyền đúng lúc (khi bật nhắc học), có giải thích; app vẫn chạy nếu bị từ chối (dù không phát hành lên Play) |
| Bản quyền nội dung | Danh sách từ, câu ví dụ, bài ngữ pháp, ảnh/icon | Tự soạn hoặc dùng nguồn có giấy phép mở và ghi nguồn; **không** sao chép dữ liệu của app tham khảo hay danh sách từ có bản quyền (ví dụ Oxford 3000 thuộc Oxford University Press) |
| Giấy phép mở thường gặp | Wiktionary (CC BY-SA), Tatoeba câu ví dụ (CC BY 2.0 FR), Princeton WordNet (giấy phép WordNet), icon Material (Apache 2.0) | Ghi nguồn trong màn hình "Giới thiệu" và phụ lục báo cáo; CC BY-SA yêu cầu chia sẻ lại cùng giấy phép phần dữ liệu đã dùng |

## Nguồn tham khảo
- Ebbinghaus, H. (1885). *Über das Gedächtnis* (Memory: A Contribution to Experimental Psychology).
- Wozniak, P. A. — mô tả thuật toán SM-2: https://super-memory.com/english/ol/sm2.htm
- Leitner, S. (1972). *So lernt man lernen* — hệ thống hộp thẻ Leitner.
- Council of Europe — CEFR: https://www.coe.int/en/web/common-european-framework-reference-languages
- Android Developers — TextToSpeech: https://developer.android.com/reference/android/speech/tts/TextToSpeech
- Android Developers — Notification runtime permission: https://developer.android.com/develop/ui/views/notifications/notification-permission
- Android Developers — Schedule alarms: https://developer.android.com/develop/background-work/services/alarms/schedule
- Android Developers — Save data using SQLite: https://developer.android.com/training/data-storage/sqlite
- AnkiDroid (mã nguồn mở, tham khảo cách tổ chức thẻ và TTS): https://github.com/ankidroid/Anki-Android
- Tatoeba: https://tatoeba.org · Wiktionary: https://www.wiktionary.org
- Nghị định 13/2023/NĐ-CP về bảo vệ dữ liệu cá nhân (Cổng thông tin Chính phủ).
