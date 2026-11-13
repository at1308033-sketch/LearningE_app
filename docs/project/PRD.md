# PRD — Product Requirements Document

> **TỰ SINH** từ bộ phân tích thiết kế đã duyệt của nhóm — không sửa trực tiếp tệp này; đề xuất thay đổi qua Issue/PR để nhóm trưởng cập nhật nguồn và sinh lại. Danh mục tài liệu: [docs/README.md](../README.md).

**Mục đích:** mục tiêu, người dùng, phạm vi, yêu cầu ưu tiên MoSCoW, user story và tiêu chí chấp nhận, nội dung học có sẵn, nghiên cứu người dùng và đối thủ.

## Tôn chỉ dự án
> Tầm nhìn, mục tiêu, phạm vi sơ bộ, ràng buộc, chỉ số thành công. Chi tiết và nguồn: PRD.

## Tầm nhìn (1-2 câu)
Một app Android gọn, offline-first (học được khi không có mạng, đồng bộ qua hệ thống máy chủ LearningE của nhóm — thay đổi phạm vi, thay đổi phạm vi), giúp người mới học tiếng Anh duy trì thói quen học 10–15 phút mỗi ngày: học từ bằng flashcard, tự kiểm tra bằng quiz và thấy rõ tiến bộ của mình.

## Vấn đề cần giải quyết
Người học cơ bản thiếu một công cụ ôn tập ngắn, không cần mạng, nhắc họ học đều và cho biết đã thuộc bao nhiêu (PRD §3, A1). Về mặt đồ án: nhóm cần một sản phẩm demo được và bộ tài liệu phân tích thiết kế hệ thống đầy đủ để nộp.

## Mục tiêu & chỉ số thành công
| Mục tiêu | Chỉ số | Mục tiêu đo |
|---|---|---|
| Sản phẩm demo được | Số nhóm tính năng Must chạy end-to-end (F1–F4, sổ tay/bộ từ; học/ôn/quiz chạy được cả khi offline — thay đổi phạm vi) | 100% trên emulator và ít nhất 1 máy thật; kịch bản 2 máy cùng tài khoản đồng bộ đúng |
| Ổn định khi demo | Số crash ở luồng chính trong buổi kiểm thử cuối | 0 |
| Nội dung đủ để học | Số từ / chủ đề / đề thi thử seed sẵn | ≥300 từ (15 chủ đề × 20 từ), 1 đề thi thử 20 câu; ngữ pháp → Won't (thay đổi phạm vi C1, C4, C5) |
| Tài liệu phân tích thiết kế | Sơ đồ use case, activity, sequence, class, ERD + đặc tả chức năng có trong báo cáo | Đủ 100% theo danh sách A8 |
| Nộp đúng hạn | Báo cáo + slide + mã nguồn trên GitHub | Trước ngày nộp (mốc tạm ≈ 2027-01-03 ; xem A6 và mục Mốc thời gian) |

## Phạm vi sơ bộ

<!-- diagram:usecase/uc-00-overview.puml -->
![Sơ đồ use case tổng quát — phạm vi chức năng của LearningE (online, backend 6 microservice gRPC; Won't/Could đánh dấu)](../diagrams/usecase/uc-00-overview.png)
<!-- /diagram -->
### In-scope
- F1 Tài khoản (đăng ký, đăng nhập, đăng xuất, hồ sơ cơ bản) — thay đổi phạm vi: tài khoản trên server của nhóm thay tài khoản cục bộ.
- F2 Từ vựng theo chủ đề + flashcard (thuộc/chưa thuộc, yêu thích, phát âm TextToSpeech).
- F3 Quiz: trắc nghiệm, sắp xếp từ thành câu, điền từ; chấm điểm, lịch sử.
- F4 Tiến độ (thống kê, chuỗi ngày học) + nhắc học hằng ngày.
- F5 Sổ tay từ do người dùng thêm; tra từ → Could, ngữ pháp → Won't.
- Tài liệu: phân tích thiết kế, báo cáo theo mẫu HCMUTE, slide bảo vệ.
- Online theo hướng Duolingo, offline-first: hệ thống máy chủ LearningE của nhóm (thay đổi phạm vi: **6 microservice Rust giao tiếp gRPC**, SQLite là CSDL chính + MongoDB là kho phụ, repo server riêng `LearningE_Server`) thay server Java (Javalin) của thay đổi phạm vi; tài khoản trên server, đồng bộ tiến độ đa thiết bị, XP + bảng xếp hạng tuần; lớp game pixel-art. Chi tiết và ưu tiên (ngữ pháp, dark mode → Won't; tra từ → Could): PRD.
### Out-of-scope
Đăng nhập mạng xã hội, vai trò admin/trang quản trị web, giải đấu/tim/đá quý, bạn bè/chat/realtime, hosting cloud (ASSUMPTION: nơi chạy server demo — laptop + Docker Compose, hay MongoDB Atlas / Railway / VPS — **chưa quyết định**, Q3 thay đổi phạm vi, người quyết định Phan Anh Tuấn, chốt trước khi code server), đa ngôn ngữ giao diện, chấm phát âm, quảng cáo/thanh toán, phát hành Google Play, iOS (PRD §5; bỏ "Server/API, đồng bộ đám mây" ).

## Người dùng / khách hàng chính
- Người học tiếng Anh trình độ A1–B1, giao diện tiếng Việt (A2).
- Người chấm: giảng viên môn Lập trình Android.

## Ràng buộc (ngân sách, thời gian, pháp lý, công nghệ bắt buộc)

<!-- diagram:architecture/arc-deployment.puml -->
![Sơ đồ triển khai: app ngoại tuyến trên Android, đồng bộ qua gRPC/TLS với backend docker compose (nơi chạy chưa chốt, DEC-023)](../diagrams/architecture/arc-deployment.png)
<!-- /diagram -->
- App: Java native, XML + Material Components, SQLite bằng `SQLiteOpenHelper` (D2); offline-first; gọi gRPC trực tiếp tới hệ thống máy chủ.
- Máy chủ: **Rust** (tokio + tonic), **6 microservice** (edge, auth, sync, content, gamification, insight) giao tiếp **gRPC** (hợp đồng protobuf `learninge.app.v1`); **SQLite là CSDL chính** (nguồn sự thật), **MongoDB chạy song song làm kho phụ**; mã nguồn ở **repo riêng** `LearningE_Server` (repo app chỉ giữ bản sao proto). Nơi triển khai demo: **chưa quyết định** (Q3 thay đổi phạm vi).
- Figma dựng trước khi code giao diện.
- 4 thành viên, 0đ, **8 tuần kể cả tuần 09/11 (từ 11/11/2026 tới ≈ 03/01/2027)**  (trước đó khoảng 6 tuần, hạn 20/12/2026).
- Nội dung seed lấy từ nguồn mở phải ghi nguồn và kiểm tra giấy phép; không sao chép dữ liệu của app tham khảo.

## Giả định (ASSUMPTION)
A1–A8 trong PRD §7. Quan trọng nhất: A3 (khối lượng nội dung seed), A6 (mốc nộp bài: 2026-12-20 → ≈ **2027-01-03** ; nhóm nêu "khoảng tuần 04/01/2027" — người dùng xác nhận ngày chính thức), A8 (cấu trúc báo cáo HCMUTE).

## Rủi ro lớn ban đầu
| Rủi ro | Ảnh hưởng | Giảm thiểu |
|---|---|---|
| Soạn nội dung seed (từ, câu hỏi, ngữ pháp) tốn nhiều thời gian hơn dự kiến | Thiếu dữ liệu khi demo | Chốt định dạng dữ liệu sớm (phase 3), chia việc soạn cho cả nhóm từ tuần 2 |
| 4 người cùng sửa một project Android dễ xung đột | Mất code, chậm tiến độ | Git branch theo tính năng, PR + review, quy ước trong CODING-STANDARDS |
| Làm tài liệu dồn cuối kỳ | Báo cáo thiếu, lệch với code | Viết tài liệu theo phase; báo cáo tổng hợp từ tài liệu đã duyệt |
| TextToSpeech không có giọng tiếng Anh offline trên một số máy | Mất tính năng phát âm khi demo | Kiểm tra gói giọng trên máy demo; báo lỗi thân thiện khi không có |
| Đường cong học Rust và vận hành 6 microservice + MongoDB (R-41, R-45 trong thay đổi phạm vi) | Server trễ, ảnh hưởng đường găng đồng bộ | T-140 + SPIKE ngay 12/11; go/no-go 29/11 (RB-1 quay về thay đổi phạm vi); chế độ gộp `--all` dự phòng (RB-3) |
| gRPC qua tunnel và nơi triển khai server chưa quyết định (R-42, Q3) | Điện thoại thật không gọi được server khi demo | SPIKE S-02 hạn 20/11; dự phòng cùng Wi-Fi LAN + emulator + video demo quay trước |

## Mốc thời gian dự kiến
| Tuần | Khoảng ngày | Mốc |
|---|---|---|
| 1–2 | 09/11–22/11 | G0–G2: khởi tạo, khám phá, yêu cầu (PRD, use case, flow); **M0a 12/11** quyết định thay đổi phạm vi; **M0b 16/11** duyệt tài liệu, **proto v1 đóng băng**; **M1a 22/11** nền tảng app. SPIKE S-01..S-03, học Rust (T-140) |
| 3 | 23/11–29/11 | **M1 29/11**: 6 dịch vụ chạy được (Docker Compose hoặc chế độ gộp); Đăng ký/Đăng nhập từ app qua gRPC; **go/no-go Rust (RB-1)** |
| 4–5 | 30/11–13/12 | **M2 13/12**: học từ + đăng nhập online + đồng bộ `card_progress` 2 máy + đường XP (outbox → MongoDB); go/no-go đồng bộ |
| 6 | 14/12–20/12 | **M3 20/12**: quiz, tiến độ, bộ từ, đồng bộ đủ bảng, pixel, bảng xếp hạng, insight, nội dung |
| 7 | 21/12–27/12 | **M4 27/12**: luyện thi, import/export; **đóng băng tính năng** |
| 8 | 28/12–03/01/2027 | **M5 31/12** kiểm thử 2 máy, MongoDB tắt giữa phiên, checklist bảo mật, diễn tập demo; **M6 03/01/2027** nộp bài (báo cáo, slide, APK, 2 link GitHub: app + server). Nếu hạn thật rơi tuần 04/01 thì giữ M6 và dùng phần dư làm đệm |

Nguồn mốc: `docs/changes/thay đổi phạm vi.md` §5. Nơi triển khai server (laptop + Compose / Atlas / Railway / VPS) chưa quyết định — ảnh hưởng mốc M1, M5 (OPEN, chủ: Phan Anh Tuấn).

## Yêu cầu sản phẩm
> Tính năng, user story, tiêu chí chấp nhận, ưu tiên. Mỗi story có ID để truy vết (REQ → US → AC → TC).
> Thuật ngữ theo Glossary. Quy tắc nghiệp vụ chi tiết (BR-xxx) ở SRS; luồng (FLOW-xxx) ở Use-Cases (luồng).

## Bối cảnh & mục tiêu (liên kết PRD)
LearningE (LearningE) là app Android (Java, SQLite) học tiếng Anh **online theo hướng Duolingo** cho người mới học (persona chính P1 — PRD (nghiên cứu người dùng)), có **hệ thống máy chủ LearningE riêng** của nhóm (thay đổi phạm vi; thay đổi phạm vi: 6 microservice Rust giao tiếp gRPC, SQLite chính + MongoDB phụ). Mục tiêu sản phẩm: giúp người dùng học đều mỗi ngày và nhớ lâu, nhờ flashcard + ôn tập ngắt quãng (Leitner 5 hộp), quiz, nhắc học, theo dõi tiến độ, và **vòng lặp động lực kiểu Duolingo**: XP → bảng xếp hạng tuần → chuỗi ngày; học được trên nhiều máy bằng một tài khoản (đồng bộ tiến độ), vẫn chạy khi mất mạng (offline-first). Một **lớp game pixel-art** (ngọn lửa chuỗi ngày, 5 rương Leitner, màn ăn mừng, chế độ Bản đồ) phủ lên phần tử trò chơi, giữ thiết kế v2 làm nền. Mục tiêu đồ án và chỉ số: PRD §Mục tiêu.

### Ràng buộc
- Client: Java native (Java 17, minSdk 29) + SQLite (`SQLiteOpenHelper`) trên Android, gọi **gRPC trực tiếp** tới hệ thống máy chủ (DEC-021). Máy chủ: **Rust** (tokio + tonic), **6 microservice** `edge`, `auth`, `sync`, `content`, `gamification`, `insight`; hợp đồng protobuf gói công khai `learninge.app.v1` (dịch vụ gRPC của app); **SQLite là CSDL chính** (tài khoản, token, tiến độ, bộ từ), **MongoDB chạy song song làm kho phụ** (sự kiện XP + bảng xếp hạng, nội dung học, audit/log, bản sao phân tích). Mã nguồn máy chủ ở **repo riêng** `LearningE_Server`; repo app giữ bản sao proto công khai.
- **Offline-first**: đăng ký/đăng nhập lần đầu cần mạng; sau đó học, ôn, quiz, thi thử chạy ngoại tuyến, tự đồng bộ khi có mạng. Bảng xếp hạng và tải nội dung cần mạng.
- Demo: phương án đề xuất trong thay đổi phạm vi là chạy trên **laptop của nhóm** bằng `docker compose up` (hoặc chế độ gộp `learninge-server --all`) + tunnel hỗ trợ HTTP/2 cho điện thoại thật (kết luận SPIKE S-02); dự phòng cùng Wi-Fi LAN (gRPC không mã hóa, chỉ bản debug), emulator dùng `10.0.2.2:50051`. Các phương án khác đang cân nhắc: MongoDB Atlas, Railway, VPS (ASSUMPTION — chưa chốt; quyết định trước khi code server).
- Xác thực bằng token opaque (ngẫu nhiên 256 bit, server lưu băm, hạn 30 ngày); mật khẩu chỉ băm PBKDF2 trên server, máy chỉ giữ token.
- Dependency (thay đổi phạm vi §6, Q1 đã duyệt): app thêm `grpc-okhttp`, `grpc-protobuf-lite`, `grpc-stub`, `protobuf-javalite` + plugin Gradle protobuf (thay `HttpURLConnection` + `org.json` của thay đổi phạm vi); máy chủ dùng các crate Rust (tokio, tonic, prost, rusqlite, mongodb, …) và image `mongo:8.0`; phiên bản chốt ở SPIKE S-01/S-03 (SYSTEM-BLUEPRINT (công nghệ)). Dependency Java của máy chủ thay đổi phạm vi (Javalin, Jackson, sqlite-jdbc, slf4j, JUnit 5) **đã bị thay**.
- Lớp pixel: VectorDrawable + PNG `drawable-nodpi`, không thư viện game; vẽ mỗi khung ≤ 16 ms (NFR-P6).

## Phạm vi
### In-scope
F1 Tài khoản **trên server** (dùng được trên nhiều máy) · F2 Từ vựng + flashcard + ôn tập · F3 Quiz · F4 Tiến độ + nhắc học · F5 Sổ tay (tra từ: Could; ngữ pháp: Won't — thay đổi phạm vi) · **F6 Bộ từ vựng tự tạo + import/export CSV** · **F7 Ngôn ngữ nội dung cấu hình được + luyện thi TOEIC/IELTS/Aptis** (F6, F7  để đáp ứng rubric môn học) · **F8 Online: đồng bộ đa thiết bị, XP + bảng xếp hạng tuần, nội dung từ server (Could)** · **F9 Lớp game pixel-art** (F8, F9 ). Giao diện tiếng Việt; nội dung chính Anh–Việt cấp độ A1–B1, có bộ mẫu tiếng Nhật.
### Non-goals
Đăng nhập Google/Facebook; vai trò admin, **trang quản trị web** và quản trị nội dung trong app (nội dung server do nhóm cập nhật trực tiếp); giải đấu/hạng đấu (league), tim/đá quý và tiền ảo; bạn bè, mạng xã hội, chat; tính năng thời gian thực (realtime, push từ server); gửi email (kể cả đặt lại mật khẩu); hosting cloud cho server (ASSUMPTION: nơi triển khai demo chưa quyết định — PQ-9); đa ngôn ngữ **giao diện** (UI chỉ tiếng Việt — ngôn ngữ nội dung học thì cấu hình được, REQ-018); đề thi chính thức của ETS/IDP/British Council; chấm phát âm/nhận diện giọng nói; quảng cáo, thanh toán; phát hành Google Play; iOS. Đã hạ xuống Won't : ngữ pháp (REQ-008), chế độ tối (REQ-013), import/export JSON, kiểm thử UI Espresso.

## Danh sách yêu cầu (ưu tiên MoSCoW)
| REQ | Yêu cầu / tính năng | Ưu tiên | Nguồn (ai yêu cầu) | Ghi chú |
|---|---|---|---|---|
| REQ-001 | Tài khoản trên server: đăng ký, đăng nhập, đăng xuất, đổi mật khẩu, xóa tài khoản; mật khẩu chỉ băm PBKDF2 trên server, máy chỉ giữ token | Must | Nhóm (PRD F1) | US-001..US-005; đăng nhập được trên máy thứ hai bằng cùng tài khoản |
| REQ-002 | Thiết lập lần đầu: cấp độ, mục tiêu hằng ngày, giờ nhắc học | Must | nhóm trưởng (PRD (nghiên cứu người dùng) I1) | US-006 |
| REQ-003 | Từ vựng theo chủ đề + flashcard + phát âm + yêu thích | Must | Nhóm (F2) | US-007..US-011 |
| REQ-004 | Ôn tập ngắt quãng theo Leitner 5 hộp | Must | nhóm trưởng (Glossary (kiến thức miền) §2) | US-012 |
| REQ-005 | Quiz theo chủ đề: 5 dạng câu hỏi (BR-020), chấm điểm, lịch sử | Must | Nhóm (F3) | US-013..US-015 |
| REQ-006 | Trang chủ hôm nay, thống kê tiến độ, chuỗi ngày học (kèm XP hôm nay/tuần — REQ-021) | Must | Nhóm (F4); thay đổi phạm vi | US-016..US-018 |
| REQ-007 | Nhắc học hằng ngày bằng thông báo cục bộ | Must | Nhóm (F4) | US-019 |
| REQ-008 | Bài ngữ pháp + bài tập ngữ pháp | **Won't (vòng này)** | Nhóm (F5) (C1) | US-020, US-021 giữ để truy vết, không làm; không thuộc rubric |
| REQ-009 | Tra từ trong dữ liệu có sẵn và sổ tay | **Could** | Nhóm (F5) (C7) | US-022 |
| REQ-010 | Sổ tay từ cá nhân: thêm, sửa, xóa, học bằng flashcard | Must | Nhóm (F5); thay đổi phạm vi | US-023, US-024; sổ tay là bộ mặc định "Sổ tay của tôi" của REQ-016 |
| REQ-011 | Nạp dữ liệu seed khi cài app | Must | Nhóm (PRD A3) | US-025 |
| REQ-012 | Màn hình Giới thiệu: thông tin nhóm, nguồn dữ liệu, giấy phép | Should | Glossary (kiến thức miền) §Pháp lý | US-026 |
| REQ-013 | Chế độ tối (dark mode) | **Won't (vòng này)** | nhóm trưởng; thay đổi phạm vi (PQ-4) (C2) | Không làm vòng này; giao diện chỉ chế độ sáng |
| REQ-014 | Đặt lại mật khẩu bằng câu hỏi bảo mật, kiểm tra trên server (không gửi email) | Must | Phan Anh Tuấn | US-027; cần mạng |
| REQ-015 | Widget màn hình chính hiện số thẻ cần ôn | Won't (vòng này) | thay đổi phạm vi | Nhường thời gian cho REQ-016..019 |
| REQ-016 | Bộ từ vựng do người dùng tạo: tạo/đổi tên/xóa bộ; thêm/sửa/xóa từ; học/ôn/quiz trên bộ | Must | Rubric "chức năng cơ bản" | US-028..US-031 |
| REQ-017 | Import/export bộ từ vựng (chỉ CSV; JSON → Won't  C6) | Must | Rubric "nâng cao"; thay đổi phạm vi | US-032, US-033 |
| REQ-018 | Ngôn ngữ nội dung cấu hình được (Anh, Nhật, Trung, Hàn…) | Must | Rubric "nâng cao" | US-034 |
| REQ-019 | Luyện thi TOEIC/IELTS/Aptis: bộ từ theo kỳ thi, 1 đề thi thử có giờ, tài nguyên chính thức | Must | Rubric "nâng cao"; giảm  (C5) | US-035..US-037 |
| REQ-020 | Đồng bộ tiến độ đa thiết bị: card_progress, chuỗi ngày/study_log, lịch sử quiz/thi thử (tóm tắt lượt), bộ từ + từ do người dùng tạo, yêu thích, cài đặt học; offline-first, tự đồng bộ khi đăng nhập, khi mở app, sau mỗi phiên học, và nút "Đồng bộ ngay" | Must | Phan Anh Tuấn | US-038..US-041 |
| REQ-021 | XP và bảng xếp hạng tuần kiểu Duolingo: XP cho mỗi thẻ ôn, quiz, thi thử; bảng top 50 theo XP tuần (thứ Hai 00:00 → Chủ nhật 23:59, giờ Asia/Ho_Chi_Minh) + hạng của tôi + chuỗi ngày | Must | Phan Anh Tuấn | US-042, US-043 |
| REQ-022 | Nội dung từ server: app kiểm tra `content_version`, tải bộ từ/đề thi mới hoặc sửa; seed trong APK vẫn là bản nền để dùng ngoại tuyến | **Could** | Phan Anh Tuấn | US-044; thay đổi phạm vi §3 ghi Should, hạ Could theo cắt C8 đã duyệt (Q4) |
| REQ-023 | Lớp pixel: ngọn lửa theo chuỗi ngày (SCR-21) + huy hiệu (SCR-05) + 5 rương Leitner (SCR-21) + màn ăn mừng (SCR-10) | Must | Phan Anh Tuấn; concept Figma Nguyễn Đức Quang Vinh | US-045, US-046 |
| REQ-024 | Chế độ Bản đồ SCR-06: 15 công trình chủ đề trên địa hình pixel, mở khóa theo cấp độ, hiện sao chủ đề | Could | thay đổi phạm vi | US-047 |

### Thay đổi ưu tiên  (cắt C1–C8, Gói A đã duyệt)
| Cắt | Nội dung | Ảnh hưởng PRD |
|---|---|---|
| C1 | Ngữ pháp → Won't | REQ-008 Won't; US-020, US-021 không làm; seed không có bài/câu hỏi ngữ pháp |
| C2 | Dark mode → Won't | REQ-013 Won't |
| C3 | Kiểm thử UI Espresso → Won't (giữ unit test + integration test server) | Không đổi AC; ghi ở TESTING-STRATEGY |
| C4 | Seed 15 chủ đề × 20 từ (thay 30) | AC-025.1, §Nội dung seed: 300 từ |
| C5 | Luyện thi: X01 40 từ + 1 đề E01 20 câu; X02/X03 30 từ; bỏ đề E02 | AC-035.1, AC-036.1, §Nội dung seed |
| C6 | Import/export chỉ CSV, bỏ JSON | REQ-017, AC-032.1, AC-033.1 |
| C7 | Tra từ → Could | REQ-009 Could; US-022 |
| C8 | Nội dung từ server → Could | REQ-022 Could; US-044 |

Thứ tự cắt tiếp nếu trễ mốc M2 (13/12 ; trước đó 29/11), chưa áp dụng: X1 bảng xếp hạng → Could, X2 màn ăn mừng + rương, X3 quiz 5 dạng → 3 dạng, X4 bộ X02/X03 → 20 từ (thay đổi phạm vi §5).

## User stories & Acceptance criteria
Vai trò duy nhất: **Người học** (đã hoặc chưa đăng nhập).

### F1 — Tài khoản (REQ-001)
> Tài khoản nằm trên server; mọi kiểm tra đăng ký/đăng nhập/đổi/đặt lại mật khẩu chạy trên server, quy tắc độ dài mật khẩu kiểm ở cả hai phía (BR-001..BR-008 sửa, BR-057..BR-061). Các thao tác tài khoản cần mạng; mất mạng thì hiện "Cần kết nối mạng để …" và không đổi gì.
#### US-001 (REQ-001): Là người học mới, tôi muốn đăng ký tài khoản trên server để dữ liệu học được lưu riêng cho tôi và dùng được trên nhiều máy
- AC-001.1: Given chưa có tài khoản và có mạng, When nhập email, biệt danh công khai (3–20 ký tự, hiện trên bảng xếp hạng — BR-061), mật khẩu, xác nhận mật khẩu hợp lệ và bấm Đăng ký, Then server tạo tài khoản, mật khẩu chỉ lưu dạng băm PBKDF2 trên server (BR-002), app nhận token phiên (BR-057) và chuyển sang Thiết lập lần đầu (US-006); máy không lưu mật khẩu.
- AC-001.2 (ngoại lệ): Given email đã được đăng ký (không phân biệt hoa thường — BR-060), When bấm Đăng ký, Then không tạo tài khoản và hiện "Email đã được sử dụng" dưới trường đó.
- AC-001.3 (ngoại lệ): Given một trường vi phạm quy tắc kiểm tra (SRS §Validation), When bấm Đăng ký, Then app báo lỗi tại đúng trường sai trước khi gửi (server kiểm lại cùng quy tắc), giữ nguyên dữ liệu đã nhập (trừ mật khẩu).
- AC-001.4: Given đang đăng ký, When chọn 1 trong 5 câu hỏi bảo mật có sẵn (BR-009) và nhập câu trả lời 2–50 ký tự, Then câu trả lời được chuẩn hóa và lưu dạng băm trên server (BR-007); thiếu một trong hai thì không cho đăng ký.
- AC-001.5 (ngoại lệ, thay đổi phạm vi): Given mất mạng hoặc hệ thống máy chủ không phản hồi trong 10 s (deadline gRPC, lỗi `UNAVAILABLE`/`DEADLINE_EXCEEDED` — NFR-N2), When bấm Đăng ký, Then hiện "Không kết nối được máy chủ, vui lòng thử lại", giữ dữ liệu đã nhập (trừ mật khẩu), không tạo tài khoản dở dang.
#### US-002 (REQ-001): Là người học, tôi muốn đăng nhập và đăng xuất để dùng đúng dữ liệu của mình trên bất kỳ máy nào
- AC-002.1: Given đúng email và mật khẩu, có mạng, When bấm Đăng nhập, Then server cấp token (hạn 30 ngày — BR-057), app vào Trang chủ và phiên được ghi nhớ đến khi đăng xuất hoặc token hết hạn; lần mở app sau không cần mạng để vào Trang chủ (NFR-N1).
- AC-002.2 (ngoại lệ): Given sai email hoặc mật khẩu, When bấm Đăng nhập, Then hiện thông báo chung "Email hoặc mật khẩu không đúng" (không nói rõ sai phần nào).
- AC-002.3 (ngoại lệ): Given một email (kể cả email chưa đăng ký) đã bị sai mật khẩu 5 lần liên tiếp, When thử đăng nhập tiếp, Then server từ chối kể cả khi mật khẩu đúng (giới hạn tần suất NFR-S8, BR-003 sửa; gRPC `RESOURCE_EXHAUSTED` + `retry-after-seconds`) và app hiện "Bạn thử quá nhiều lần, vui lòng thử lại sau x giây" (x theo thời gian server báo); lần sai thứ 1–4 chưa bị chặn.
- AC-002.4: Given đang đăng nhập, When bấm Đăng xuất và xác nhận, Then server thu hồi token (BR-058), app quay về màn hình Đăng nhập; mở lại app vẫn ở màn hình Đăng nhập. Given còn thay đổi chưa đồng bộ, Then app đồng bộ trước khi đăng xuất; nếu mất mạng thì cảnh báo "Còn x thay đổi chưa đồng bộ sẽ mất trên máy này" và chỉ đăng xuất khi người dùng xác nhận rõ (nút mặc định "Ở lại" — BR-073, PQ-6).
- AC-002.5: Given tài khoản đã có dữ liệu trên máy A, When đăng nhập cùng tài khoản trên máy B, Then dữ liệu của máy A được kéo về máy B theo US-038.
- AC-002.6 (ngoại lệ, thay đổi phạm vi): Given token hết hạn hoặc bị thu hồi, When app gọi máy chủ và nhận gRPC `UNAUTHENTICATED`, Then app về màn hình Đăng nhập với thông báo "Phiên đăng nhập đã hết hạn", giữ nguyên dữ liệu chưa đẩy để đồng bộ sau khi đăng nhập lại (BR-059).
- AC-002.7 (ngoại lệ, thay đổi phạm vi): Given chưa từng đăng nhập trên máy này và mất mạng, When bấm Đăng nhập, Then hiện "Cần kết nối mạng để đăng nhập lần đầu".
- AC-002.8 (ngoại lệ, khóa tăng dần — quyết định 11/11): Given email đang bị khóa lần đầu, When hết 60 giây và lại sai mật khẩu 5 lần liên tiếp, Then server khóa lần kế tiếp **gấp đôi** (60 s → 2 phút → 4 phút → 8 phút …, tối đa 1 giờ — BR-003); đăng nhập đúng hoặc đặt lại mật khẩu thành công thì đếm sai và bậc khóa về 0.
#### US-003 (REQ-001): Là người học, tôi muốn xem và sửa hồ sơ, cài đặt để điều chỉnh việc học
- AC-003.1: Given đã đăng nhập, When mở tab Tôi, Then thấy email, biệt danh, cấp độ, mục tiêu hằng ngày, giờ nhắc học, công tắc nhắc học, lối vào Trạng thái đồng bộ (SCR-35).
- AC-003.2: When đổi một giá trị hợp lệ và lưu, Then giá trị mới có hiệu lực ngay trên máy (lịch nhắc được đặt lại nếu đổi giờ nhắc — BR-030), kể cả khi mất mạng, và được đồng bộ lên server ở lần đồng bộ kế tiếp (REQ-020).
#### US-004 (REQ-001): Là người học, tôi muốn đổi mật khẩu để bảo vệ tài khoản
- AC-004.1: Given có mạng, nhập đúng mật khẩu hiện tại và mật khẩu mới hợp lệ (khác mật khẩu cũ), When lưu, Then server đổi mật khẩu, thu hồi mọi token của tài khoản trên các máy khác (BR-058) và máy hiện tại nhận token mới; lần đăng nhập sau dùng mật khẩu mới.
- AC-004.2 (ngoại lệ): Given mật khẩu hiện tại sai, When lưu, Then báo "Mật khẩu hiện tại không đúng", không đổi gì và vẫn giữ đăng nhập (server trả `PERMISSION_DENIED` `INVALID_CREDENTIALS`, khác `UNAUTHENTICATED` nên không bị coi là hết phiên — BR-059).
- AC-004.3 (ngoại lệ, thay đổi phạm vi): Given mất mạng, When lưu, Then báo "Cần kết nối mạng để đổi mật khẩu", không đổi gì.
#### US-005 (REQ-001): Là người học, tôi muốn xóa tài khoản để xóa toàn bộ dữ liệu của tôi khỏi server và máy
- AC-005.1: Given đã đăng nhập và có mạng, When chọn Xóa tài khoản, nhập lại mật khẩu đúng và xác nhận, Then server xóa tài khoản, mọi token và mọi dữ liệu thuộc người dùng (cài đặt, tiến độ thẻ, yêu thích, bộ từ riêng và các từ trong bộ, lượt quiz, lượt thi thử, nhật ký học, sự kiện XP — BR-005), người dùng biến mất khỏi bảng xếp hạng; app xóa dữ liệu người dùng trên máy, hủy lịch nhắc, về màn hình Đăng nhập.
- AC-005.2 (ngoại lệ): Given bấm Hủy ở hộp xác nhận, Then không xóa gì.
- AC-005.3 (ngoại lệ, thay đổi phạm vi): Given mất mạng, When chọn Xóa tài khoản, Then báo "Cần kết nối mạng để xóa tài khoản", không xóa gì ở cả máy và server.

### F1 — Thiết lập lần đầu (REQ-002)
#### US-006 (REQ-002): Là người học vừa đăng ký, tôi muốn chọn cấp độ, mục tiêu và giờ nhắc để app phù hợp với tôi
- AC-006.1: Given vừa đăng ký, When hoàn thành 3 bước (cấp độ A1/A2/B1; mục tiêu 5/10/15/20 từ mỗi ngày; giờ nhắc), Then lưu cài đặt và vào Trang chủ.
- AC-006.2: Given ở bước giờ nhắc trên Android 13+, When bật nhắc học, Then app xin quyền thông báo; nếu bị từ chối thì vẫn vào Trang chủ, nhắc học ở trạng thái tắt và có hướng dẫn bật lại trong Cài đặt.
- AC-006.3: Given bấm "Bỏ qua", Then dùng mặc định: A1, 10 từ/ngày, nhắc 20:00 (BR-006).

### F2 — Từ vựng (REQ-003)
#### US-007 (REQ-003): Là người học, tôi muốn xem danh sách chủ đề để chọn nội dung muốn học
- AC-007.1: Given đã có dữ liệu seed, When mở tab Học, Then thấy ≥10 chủ đề, mỗi chủ đề hiện tên, icon, cấp độ, tiến độ "đã thuộc x/y từ".
- AC-007.2: When lọc theo cấp độ (Tất cả/A1/A2/B1), Then chỉ hiện chủ đề đúng cấp độ; mặc định lọc theo cấp độ trong cài đặt.
#### US-008 (REQ-003): Là người học, tôi muốn xem danh sách từ và chi tiết từng từ
- AC-008.1: When mở một chủ đề, Then thấy danh sách từ với từ, phiên âm, nghĩa và nhãn trạng thái (Mới/Đang học/Đã thuộc).
- AC-008.2: When chạm một từ, Then mở Chi tiết từ: từ, phiên âm IPA, từ loại, nghĩa, câu ví dụ Anh + Việt, nút phát âm, nút yêu thích, nút thêm vào sổ tay.
#### US-009 (REQ-003): Là người học, tôi muốn học từ mới bằng flashcard để ghi nhớ chủ động
- AC-009.1: Given chủ đề còn từ Mới, When bấm "Học từ mới", Then mở phiên gồm tối đa N thẻ (N = số từ mục tiêu còn lại trong ngày, tối thiểu 5 — BR-010).
- AC-009.2: When chạm thẻ, Then lật thẻ (mặt trước: từ + phiên âm; mặt sau: nghĩa + ví dụ) có hiệu ứng ≤ 400 ms.
- AC-009.3: When chọn "Đã nhớ" hoặc "Chưa nhớ", Then thẻ vào hộp Leitner theo BR-011 và hiện thẻ kế tiếp; hết phiên hiện màn hình Kết quả phiên (số từ đã nhớ/chưa nhớ).
- AC-009.4 (ngoại lệ): Given chủ đề không còn từ Mới, Then nút "Học từ mới" bị ẩn và hiện "Bạn đã học hết từ mới của chủ đề này", gợi ý Ôn tập hoặc Quiz.
- AC-009.5: Given thoát giữa phiên, Then các thẻ đã đánh giá được lưu; thẻ chưa đánh giá vẫn là Mới.
#### US-010 (REQ-003): Là người học, tôi muốn nghe phát âm từ và câu ví dụ
- AC-010.1: Given máy có giọng tiếng Anh cho TextToSpeech, When bấm nút loa, Then đọc từ (hoặc câu ví dụ) bằng giọng en-US trong ≤ 1 giây.
- AC-010.2 (ngoại lệ): Given máy thiếu dữ liệu giọng hoặc không hỗ trợ, When bấm nút loa, Then hiện "Máy chưa có giọng đọc tiếng Anh" kèm nút mở cài đặt giọng đọc; app không bị dừng.
#### US-011 (REQ-003): Là người học, tôi muốn đánh dấu từ yêu thích để xem lại nhanh
- AC-011.1: When bấm biểu tượng yêu thích ở Chi tiết từ hoặc danh sách từ, Then trạng thái đổi ngay và từ xuất hiện trong mục "Yêu thích" ở Bộ của tôi (SCR-26 → SCR-19 lọc Yêu thích — PQ-7, FN-005).
- AC-011.2: When bấm lại, Then bỏ yêu thích.

### F2 — Ôn tập (REQ-004)
#### US-012 (REQ-004): Là người học, tôi muốn ôn các thẻ đến hạn để không quên từ đã học
- AC-012.1: Given có thẻ với ngày ôn tiếp ≤ hôm nay, When bấm "Ôn tập" ở Trang chủ, Then mở phiên flashcard gồm các thẻ đến hạn, sắp theo ngày ôn sớm nhất trước, tối đa 50 thẻ/phiên (BR-013).
- AC-012.2: When chọn "Đã nhớ", Then thẻ lên 1 hộp và ngày ôn tiếp = hôm nay + khoảng của hộp mới (1/2/4/7/14 ngày); When chọn "Chưa nhớ", Then thẻ về hộp 1, ôn lại ngày mai (BR-011, BR-012).
- AC-012.3 (ngoại lệ): Given không có thẻ đến hạn, Then nút Ôn tập hiện "Hôm nay không có thẻ cần ôn" và bị vô hiệu.

### F3 — Quiz (REQ-005)
#### US-013 (REQ-005): Là người học, tôi muốn làm quiz theo chủ đề để tự kiểm tra
- AC-013.1: Given chọn một chủ đề có ≥ 4 từ, When bấm "Bắt đầu quiz", Then tạo bài 10 câu ngẫu nhiên trộn 5 dạng: trắc nghiệm Anh→Việt, trắc nghiệm Việt→Anh, nghe chọn từ, điền từ, sắp xếp câu (BR-020).
- AC-013.2: When trả lời một câu, Then hiện ngay đúng/sai và đáp án đúng; không trừ điểm, không khóa bài (BR-022).
- AC-013.3 (ngoại lệ): Given máy không có giọng TTS, Then không sinh câu dạng "nghe chọn từ" mà thay bằng trắc nghiệm nghĩa.
- AC-013.4 (ngoại lệ): Given thoát giữa bài, Then hỏi xác nhận; nếu thoát thì không lưu lượt làm.
#### US-014 (REQ-005): Là người học, tôi muốn xem kết quả quiz để biết mình sai ở đâu
- AC-014.1: When trả lời xong câu cuối, Then hiện điểm = số câu đúng/tổng × 100 (làm tròn số nguyên), số câu đúng, thời gian làm, danh sách câu sai kèm đáp án đúng.
- AC-014.2: Then các từ trả lời sai được đưa về hộp 1 (BR-021) và lượt làm được lưu lịch sử.
#### US-015 (REQ-005): Là người học, tôi muốn xem lịch sử quiz để thấy mình tiến bộ
- AC-015.1: When mở Lịch sử quiz, Then thấy danh sách lượt làm mới nhất trước: chủ đề/bộ từ, ngày giờ, điểm (gồm cả lượt làm trên máy khác của cùng tài khoản sau khi đồng bộ — REQ-020).
- AC-015.2 (trạng thái rỗng): Given chưa làm quiz nào, Then hiện "Bạn chưa làm bài kiểm tra nào" và nút đến danh sách quiz.

### F4 — Tiến độ (REQ-006)
#### US-016 (REQ-006): Là người học, tôi muốn Trang chủ cho biết hôm nay cần làm gì
- AC-016.1: When mở Trang chủ, Then thấy: lời chào theo tên, chuỗi ngày học, số từ mới đã học hôm nay / mục tiêu, số thẻ cần ôn hôm nay, nút "Ôn tập", nút "Học tiếp" (chủ đề học gần nhất).
- AC-016.2: Given người dùng mới chưa học gì, Then nút "Học tiếp" thay bằng "Chọn chủ đề đầu tiên".
#### US-017 (REQ-006): Là người học, tôi muốn xem thống kê tiến độ
- AC-017.1: When mở màn hình Tiến độ, Then thấy tổng số từ đã thuộc / tổng số từ; số từ theo trạng thái Mới/Đang học/Đã thuộc; tiến độ từng chủ đề; điểm quiz trung bình 7 ngày gần nhất; biểu đồ số hoạt động học 7 ngày gần nhất.
- AC-017.2: Các số liệu khớp với dữ liệu trong CSDL (người kiểm thử đối chiếu bằng truy vấn — BR-033).
#### US-018 (REQ-006): Là người học, tôi muốn thấy chuỗi ngày học để có động lực học đều
- AC-018.1: Given hôm qua có hoạt động học, When hoàn thành hoạt động học đầu tiên hôm nay, Then chuỗi +1 (BR-031).
- AC-018.2: Given hôm qua không có hoạt động học, When hoàn thành hoạt động học hôm nay, Then chuỗi = 1.
- AC-018.3: Chuỗi dài nhất từng đạt được hiển thị cạnh chuỗi hiện tại.

### F4 — Nhắc học (REQ-007)
#### US-019 (REQ-007): Là người học, tôi muốn được nhắc học mỗi ngày vào giờ tôi chọn
- AC-019.1: Given nhắc học bật và đã cấp quyền thông báo, When đến giờ nhắc (sai lệch cho phép ≤ 15 phút) và hôm nay chưa đạt mục tiêu, Then hiện thông báo "Đến giờ học rồi! Bạn còn x từ và y thẻ cần ôn"; chạm vào mở Trang chủ (BR-030).
- AC-019.2: Given hôm nay đã đạt mục tiêu, Then không hiện thông báo.
- AC-019.3: Given máy khởi động lại, Then lịch nhắc vẫn còn hiệu lực.
- AC-019.4 (ngoại lệ): Given quyền thông báo bị thu hồi, When mở Cài đặt, Then công tắc nhắc học hiện trạng thái tắt kèm nút "Cấp quyền".

### F5 — Ngữ pháp (REQ-008) — Won't vòng này (thay đổi phạm vi C1)
> US-020, US-021 giữ nguyên để truy vết; không thiết kế, không code, không test ở vòng này.
#### US-020 (REQ-008): Là người học, tôi muốn đọc bài ngữ pháp theo chủ điểm
- AC-020.1: When mở tab Luyện tập → Ngữ pháp, Then thấy ≥ 8 bài, mỗi bài có tên, cấp độ, trạng thái đã đọc/chưa đọc.
- AC-020.2: When mở một bài, Then thấy công thức, cách dùng, ví dụ (có nút phát âm), lỗi thường gặp; đọc đến cuối bài thì bài được đánh dấu "đã đọc".
#### US-021 (REQ-008): Là người học, tôi muốn làm bài tập sau mỗi bài ngữ pháp
- AC-021.1: Given bài có ≥ 5 câu hỏi (BR-056), When bấm "Làm bài tập", Then làm 5–10 câu trắc nghiệm (loại câu GRAMMAR), mỗi câu sai có giải thích; kết quả lưu vào lịch sử quiz (BR-023). Bài có < 5 câu thì ẩn nút và báo "Bài này chưa có bài tập".

### F5 — Tra từ & sổ tay (REQ-009, REQ-010)
#### US-022 (REQ-009, Could  C7): Là người học, tôi muốn tra từ nhanh
- AC-022.1: When gõ ≥ 1 ký tự vào ô tìm kiếm, Then sau ≤ 300 ms hiện kết quả khớp tiền tố từ tiếng Anh hoặc chứa chuỗi trong nghĩa tiếng Việt, không phân biệt hoa thường và dấu tiếng Việt (BR-040), gồm từ seed và từ trong sổ tay (có nhãn "Sổ tay").
- AC-022.2 (trạng thái rỗng): Given không có kết quả, Then hiện "Không tìm thấy “[từ khóa đã gõ]”" và nút "Thêm vào sổ tay" điền sẵn từ khóa.
#### US-023 (REQ-010): Là người học, tôi muốn thêm, sửa, xóa từ trong sổ tay của tôi
- AC-023.1: When nhập từ (bắt buộc), nghĩa (bắt buộc), phiên âm, ví dụ, ghi chú (tùy chọn) và lưu, Then từ xuất hiện đầu danh sách Sổ tay.
- AC-023.2 (ngoại lệ): Given từ đã có trong sổ tay của chính người dùng, When lưu, Then báo "Từ này đã có trong sổ tay" và mở mục đã có.
- AC-023.3: When xóa một từ và xác nhận, Then từ biến mất khỏi sổ tay và kết quả tra.
#### US-024 (REQ-010): Là người học, tôi muốn học các từ trong sổ tay bằng flashcard
- AC-024.1: Given sổ tay có ≥ 1 từ, When bấm "Học sổ tay", Then mở phiên flashcard như US-009, tiến độ thẻ áp dụng Leitner như từ seed.

#### US-027 (REQ-014, thay đổi phạm vi): Là người học quên mật khẩu, tôi muốn đặt lại mật khẩu bằng câu hỏi bảo mật, được kiểm tra trên server
- AC-027.1: Given ở màn hình Đăng nhập và có mạng, When bấm "Quên mật khẩu?" và nhập email, Then server trả về câu hỏi bảo mật của tài khoản đó và app hiển thị (email không tồn tại vẫn nhận một câu hỏi giả cố định — BR-076).
- AC-027.2: Given trả lời đúng (server so sánh băm sau chuẩn hóa — BR-007), When nhập mật khẩu mới hợp lệ + xác nhận, Then server đổi mật khẩu, thu hồi mọi token cũ (BR-058), xóa bộ đếm sai đăng nhập; app quay về Đăng nhập với thông báo "Đã đặt lại mật khẩu".
- AC-027.3 (ngoại lệ): Given trả lời sai hoặc email không tồn tại, Then báo chung "Thông tin không khớp" (không có "Không tìm thấy tài khoản", không tiết lộ email có tồn tại hay không — BR-076, đã được người review xác nhận: THREAT_MODEL D-03); sai 5 lần liên tiếp thì server khóa đặt lại mật khẩu của email đó 60 giây, mỗi lần khóa tiếp theo gấp đôi, tối đa 1 giờ (khóa tăng dần — BR-008); email không tồn tại cũng bị đếm và khóa như email thật.
- AC-027.4 (ngoại lệ, thay đổi phạm vi): Given mất mạng, When bấm "Quên mật khẩu?", Then báo "Cần kết nối mạng để đặt lại mật khẩu"; app không gửi email.

### F6 — Bộ từ vựng & import/export (REQ-016, REQ-017 — thay đổi phạm vi)
#### US-028 (REQ-016): Là người học, tôi muốn tạo bộ từ vựng của riêng mình để gom các từ tôi cần học
- AC-028.1: Given ở tab Học → "Bộ của tôi", When bấm "Tạo bộ", nhập tên (1–50 ký tự), mô tả (tùy chọn), chọn ngôn ngữ (mặc định Tiếng Anh) và lưu, Then bộ mới xuất hiện đầu danh sách "Bộ của tôi" với 0 từ.
- AC-028.2 (ngoại lệ): Given đã có bộ cùng tên (không phân biệt hoa thường) của chính người dùng, When lưu, Then báo "Bạn đã có bộ tên này" và không tạo (BR-044).
- AC-028.3: Given vừa đăng ký, When mở "Bộ của tôi", Then đã có sẵn bộ "Sổ tay của tôi" (BR-046; nếu chưa kéo về từ server thì dùng bộ cục bộ có cùng uuid tất định, không sinh bản trùng).
#### US-029 (REQ-016): Là người học, tôi muốn đổi tên hoặc xóa bộ từ của tôi
- AC-029.1: When đổi tên hợp lệ và lưu, Then tên mới hiển thị ở mọi nơi.
- AC-029.2: When chọn "Xóa bộ" và xác nhận, Then bộ, các từ trong bộ và tiến độ học của các từ đó bị xóa (BR-045); bộ "Sổ tay của tôi" không có nút xóa.
- AC-029.3 (ngoại lệ): Given bộ có sẵn (15 chủ đề, bộ luyện thi), Then không có nút sửa/xóa (BR-043).
#### US-030 (REQ-016): Là người học, tôi muốn thêm, sửa, xóa từ trong bộ của tôi
- AC-030.1: When mở bộ → "Thêm từ", nhập từ và nghĩa (bắt buộc), cách đọc, ví dụ, bản dịch ví dụ (tùy chọn) rồi lưu, Then từ xuất hiện trong bộ; số từ của bộ tăng 1.
- AC-030.2 (ngoại lệ): Given từ đã có trong bộ, When lưu, Then báo "Từ này đã có trong bộ" (BR-047).
- AC-030.3: When sửa hoặc xóa (có xác nhận) một từ, Then thay đổi hiện ngay; xóa từ thì xóa luôn tiến độ học của từ.
- AC-030.4: Given đang xem một từ có sẵn (Chi tiết từ), When bấm "Lưu vào bộ", chọn một bộ của tôi, Then từ được chép vào bộ đó (dữ liệu có sẵn không bị sửa).
#### US-031 (REQ-016): Là người học, tôi muốn học, ôn và làm quiz trên bộ tôi tự tạo
- AC-031.1: Given bộ có ≥ 1 từ, When bấm "Học", Then mở phiên flashcard như US-009 với các từ của bộ; tiến độ theo Leitner (BR-011, BR-012) và thẻ đến hạn xuất hiện trong "Ôn tập" chung (US-012).
- AC-031.2: Given bộ có ≥ 4 từ, When bấm "Làm quiz", Then tạo quiz như US-013 từ các từ của bộ.
#### US-032 (REQ-017): Là người học, tôi muốn nhập (import) từ vựng từ file để không phải gõ tay
- AC-032.1: Given có file CSV đúng định dạng (BR-048; JSON — BR-049 — là Won't  C6), When chọn "Import" → chọn file bằng trình chọn file của Android, Then hiện màn hình xem trước: tổng số dòng, số dòng hợp lệ, số dòng trùng, số dòng lỗi kèm số dòng và lý do, 5 dòng đầu.
- AC-032.2: When chọn bộ đích (bộ mới hoặc bộ có sẵn của tôi) và bấm "Nhập", Then các dòng hợp lệ được thêm trong một giao dịch ≤ 3 giây với 2000 dòng, dòng trùng/lỗi bị bỏ qua, và hiện "Đã nhập x từ, bỏ qua y" (BR-050).
- AC-032.3 (ngoại lệ): Given file > 1 MB, > 2000 dòng, sai định dạng hoặc không có dòng hợp lệ, Then không nhập gì và báo lý do cụ thể.
#### US-033 (REQ-017): Là người học, tôi muốn xuất (export) một bộ từ ra file để sao lưu hoặc chia sẻ
- AC-033.1: Given một bộ bất kỳ có ≥ 1 từ, When chọn "Export" và nơi lưu, Then file CSV `<tên bộ>_<yyyyMMdd>.csv` được ghi (chỉ CSV — thay đổi phạm vi C6), mở lại bằng Import cho ra đúng số từ và nội dung (BR-051).
- AC-033.2 (ngoại lệ): Given người dùng hủy chọn nơi lưu hoặc ghi file lỗi, Then không có file dở dang và báo "Không thể xuất file".

### F7 — Đa ngôn ngữ nội dung & luyện thi (REQ-018, REQ-019 — thay đổi phạm vi)
#### US-034 (REQ-018): Là người học ngôn ngữ khác (Nhật, Trung, Hàn), tôi muốn tạo và học bộ từ của ngôn ngữ đó trong cùng app
- AC-034.1: Given tạo bộ mới, When chọn ngôn ngữ Tiếng Nhật / Tiếng Trung / Tiếng Hàn / Tiếng Anh, Then nhãn trường "cách đọc" đổi theo ngôn ngữ (BR-052) và phát âm dùng giọng của ngôn ngữ đó.
- AC-034.2: Given cài app lần đầu, Then có sẵn bộ mẫu "Tiếng Nhật cơ bản" 20 từ (từ, kana/romaji, nghĩa, ví dụ) học được bằng flashcard và quiz như bộ tiếng Anh.
- AC-034.3: Thêm một ngôn ngữ mới chỉ cần thêm 1 dòng vào dữ liệu `languages` và (tùy chọn) 1 file seed — không sửa mã Java (kiểm chứng trong TESTING-STRATEGY TC-NFR-E2).
- AC-034.4 (ngoại lệ): Given máy thiếu giọng đọc của ngôn ngữ đó, Then áp dụng như AC-010.2 và quiz không sinh câu dạng nghe.
#### US-035 (REQ-019): Là người ôn thi TOEIC/IELTS/Aptis, tôi muốn có bộ từ vựng theo từng kỳ thi
- AC-035.1: When mở tab Luyện tập → "Luyện thi", Then thấy 3 kỳ thi TOEIC, IELTS, Aptis, mỗi kỳ thi có bộ từ riêng (TOEIC 40 từ, IELTS 30 từ, Aptis 30 từ — thay đổi phạm vi C5) học được bằng flashcard, ôn tập, quiz.
#### US-036 (REQ-019): Là người ôn thi, tôi muốn làm bài thi thử có giới hạn thời gian để làm quen áp lực phòng thi
- AC-036.1: When chọn đề thi thử (vòng này có 1 đề: "E01 TOEIC Reading Part 5 — Đề 1: 20 câu, 15 phút" — thay đổi phạm vi C5; thời lượng 15 phút là ASSUMPTION, Lê Văn Tấn Lực chốt khi soạn đề), Then bắt đầu bài có đồng hồ đếm ngược, thanh tiến độ câu, cho chuyển qua lại giữa các câu và đánh dấu câu để xem lại.
- AC-036.2: When bấm "Nộp bài" (có xác nhận nếu còn câu chưa làm) hoặc hết giờ (tự nộp), Then hiện điểm = số câu đúng/tổng × 100, thời gian làm, từng câu với đáp án đúng và giải thích; ghi chú rõ "Điểm luyện tập, không quy đổi sang thang điểm chính thức" (BR-054).
- AC-036.3: Then lượt thi được lưu vào lịch sử thi thử; từ vựng gắn với câu sai được đưa về hộp 1 để ôn (BR-055).
- AC-036.4 (ngoại lệ): Given thoát app giữa bài, When mở lại trong thời gian còn lại, Then tiếp tục đúng câu và đồng hồ; quá giờ thì bài được tự nộp với các câu đã trả lời.
#### US-037 (REQ-019): Là người ôn thi, tôi muốn biết nguồn chính thức để luyện thêm
- AC-037.1: When mở "Luyện thi" → "Tài nguyên chính thức", Then thấy danh sách liên kết tới trang chính thức của TOEIC (ETS), IELTS và British Council Aptis kèm mô tả ngắn; chạm vào mở trình duyệt của máy (app không tự tải trang).
- AC-037.2 (ngoại lệ): Given máy không có trình duyệt, Then báo "Không tìm thấy trình duyệt để mở liên kết".

### F8 — Đồng bộ đa thiết bị (REQ-020 — thay đổi phạm vi)
#### US-038 (REQ-020): Là người học, tôi đăng nhập trên máy thứ hai và thấy đúng tiến độ, chuỗi ngày, bộ từ của mình
- AC-038.1: Given máy A đã đồng bộ xong dữ liệu của tài khoản, When đăng nhập cùng tài khoản trên máy B qua Wi-Fi, Then trong ≤ 10 s sau khi đăng nhập thành công, máy B hiện đúng tiến độ thẻ, chuỗi ngày, bộ từ và từ do người dùng tạo, yêu thích, cài đặt học của máy A; số thẻ ở mỗi hộp Leitner 1–5 trên máy B khớp máy A.
- AC-038.2 (ngoại lệ): Given mất mạng giữa lúc kéo dữ liệu, Then dữ liệu đã kéo được giữ, thanh "Đang ngoại tuyến" hiện, và lần đồng bộ sau tiếp tục từ cursor đã lưu (BR-063); không tạo bản ghi trùng.
#### US-039 (REQ-020): Là người học, tôi học khi mất mạng và dữ liệu tự đẩy lên khi có mạng
- AC-039.1: Given đã đăng nhập trên máy, When bật chế độ máy bay và học/ôn 20 thẻ, Then cả 20 thẻ được ghi vào CSDL máy và đánh dấu chờ đẩy; app không báo lỗi chặn việc học (NFR-N1).
- AC-039.2: Given còn 20 thay đổi chờ đẩy, When bật mạng và mở app, Then app tự đồng bộ và server có đúng 20 bản ghi mới của người dùng; không mất bản ghi nào, số thay đổi chờ đẩy về 0.
- AC-039.3: Given có mạng, Then app tự đồng bộ ở 3 thời điểm: ngay sau đăng nhập, khi mở app, sau mỗi phiên học/quiz/thi thử kết thúc; một lần đẩy ≤ 500 bản ghi (BR-064), nhiều hơn thì chia nhiều lần.
#### US-040 (REQ-020): Là người học, khi cùng một thẻ bị sửa trên 2 máy, bản mới hơn thắng
- AC-040.1: Given cùng một thẻ được đánh giá trên máy A (lúc t1) và máy B (lúc t2 > t1, theo `updated_at` của lần đánh giá, server kẹp không vượt giờ server + 5 phút — BR-062), When cả hai máy đồng bộ xong, Then cả hai máy giữ trạng thái thẻ của bản t2 (last-write-wins — BR-062) và server chỉ có 1 bản ghi tiến độ cho thẻ đó của người dùng (không tạo bản ghi trùng).
- AC-040.2: Given xóa một bộ từ tự tạo trên máy A, When máy B đồng bộ, Then bộ đó không còn hiển thị trên máy B (xóa dạng tombstone — BR-063).
#### US-041 (REQ-020): Là người học, tôi xem trạng thái đồng bộ và bấm "Đồng bộ ngay"
- AC-041.1: When mở Tôi → Trạng thái đồng bộ (SCR-35), Then thấy thời điểm đồng bộ thành công gần nhất (hh:mm dd/MM), số thay đổi đang chờ đẩy, lỗi đồng bộ gần nhất (nếu có, bằng tiếng Việt).
- AC-041.2: Given có mạng và có x > 0 thay đổi chờ đẩy, When bấm "Đồng bộ ngay", Then trong ≤ 5 s với ≤ 500 bản ghi qua Wi-Fi (NFR-N3) số chờ đẩy về 0 và thời điểm đồng bộ cuối được cập nhật.
- AC-041.3 (ngoại lệ): Given mất mạng, When bấm "Đồng bộ ngay", Then báo "Không có kết nối mạng", số chờ đẩy giữ nguyên, không mất dữ liệu.

### F8 — XP & bảng xếp hạng tuần (REQ-021 — thay đổi phạm vi)
#### US-042 (REQ-021): Là người học, tôi thấy bảng xếp hạng XP tuần và hạng của mình
- AC-042.1: Given có mạng, When mở Bảng xếp hạng (SCR-34), Then thấy tối đa 50 người có XP tuần cao nhất, xếp giảm dần, mỗi dòng có hạng, biệt danh, XP tuần; tuần tính từ thứ Hai 00:00 đến Chủ nhật 23:59 giờ Asia/Ho_Chi_Minh (BR-069).
- AC-042.2: Then dòng "Bạn" luôn hiện (kể cả khi ngoài top 50) với hạng, XP tuần và chuỗi ngày hiện tại của tôi.
- AC-042.3: Given hai người bằng XP tuần, Then người đạt mức XP đó sớm hơn xếp trên (BR-069).
- AC-042.4 (ngoại lệ): Given mất mạng hoặc dịch vụ bảng xếp hạng/MongoDB không sẵn sàng (gRPC `UNAVAILABLE`, NFR-N5), When mở SCR-34, Then hiện bảng đã lưu gần nhất kèm "Cập nhật lúc hh:mm dd/MM"; chưa từng tải thì hiện "Cần kết nối mạng để xem bảng xếp hạng".
#### US-043 (REQ-021): Là người học, tôi nhận XP sau mỗi phiên/quiz/thi thử
- AC-043.1: Given hoàn thành một phiên học/ôn, When kết thúc phiên, Then mỗi thẻ đánh giá "Đã nhớ" được +2 XP, "Chưa nhớ" +1 XP (BR-065); ví dụ phiên 10 thẻ, 7 nhớ 3 chưa nhớ → +17 XP.
- AC-043.2: Given hoàn thành quiz, Then +1 XP mỗi câu đúng và +5 XP nếu đúng 100% (BR-066); quiz 10 câu đúng cả 10 → +15 XP. Given nộp bài thi thử, Then +1 XP mỗi câu đúng (BR-067).
- AC-043.3: Given XP trong ngày đã đạt 500, When hoàn thành hoạt động tiếp theo, Then không cộng thêm XP trong ngày đó và hiện "Bạn đã đạt tối đa 500 XP hôm nay" (BR-068).
- AC-043.4: Given client gửi tổng XP khác với tổng tính từ sự kiện, When đồng bộ, Then server bỏ qua tổng của client và dùng tổng tự tính lại từ sự kiện XP (BR-068); XP kiếm được khi mất mạng được tính sau khi đồng bộ.

### F8 — Nội dung từ server (REQ-022, Could — thay đổi phạm vi)
#### US-044 (REQ-022): Là người học, tôi nhận bộ từ/đề thi mới mà không cần cài lại app
- AC-044.1: Given server tăng `content_version` và thêm một bộ từ mới, When mở app lần kế tiếp có mạng, Then app tải nội dung mới và bộ mới hiện trong SCR-06; tiến độ học hiện có không bị thay đổi.
- AC-044.2 (ngoại lệ): Given mất mạng hoặc tải lỗi giữa chừng, Then app giữ nguyên nội dung seed/phiên bản trước (không để nội dung dở dang) và thử lại ở lần mở app sau.

### F9 — Lớp game pixel-art (REQ-023, REQ-024 — thay đổi phạm vi)
#### US-045 (REQ-023): Là người học, tôi thấy ngọn lửa lớn dần theo chuỗi ngày
- AC-045.1: Given chuỗi ngày học hiện tại = s, When mở Tiến độ (SCR-21), Then ngọn lửa hiện theo `FlameStage.of(s)`: s = 0 → tắt; 1–2 → giai đoạn 1; 3–6 → giai đoạn 2; 7–29 → giai đoạn 3; ≥ 30 → giai đoạn 4 (BR-070); unit test bao các biên 0, 1, 2, 3, 6, 7, 29, 30.
- AC-045.2: When mở Trang chủ (SCR-05), Then huy hiệu chuỗi ngày pixel hiện cùng giai đoạn với SCR-21 và số ngày s.
#### US-046 (REQ-023): Là người học, tôi thấy 5 rương Leitner và màn ăn mừng sau phiên
- AC-046.1: When mở SCR-21, Then thấy 5 rương; số trên rương k (k = 1..5) bằng số thẻ của người dùng đang ở hộp Leitner k (BR-071); tổng 5 số bằng tổng số thẻ đã học.
- AC-046.2: Given kết thúc một phiên học/ôn, When hiện màn kết quả (SCR-10), Then thấy cúp, hiệu ứng confetti và số thẻ thay đổi của từng rương (+/−); ví dụ 3 thẻ lên từ hộp 1 sang hộp 2 → rương 1 "−3", rương 2 "+3".
#### US-047 (REQ-024, Could): Là người học, tôi xem chủ đề dạng bản đồ
- AC-047.1: Given ở tab Học (SCR-06), Then có công tắc Lưới/Bản đồ, mặc định Lưới; chọn Bản đồ rồi thoát và mở lại app thì vẫn là Bản đồ.
- AC-047.2: Given chế độ Bản đồ, Then 15 công trình tương ứng 15 chủ đề hiện trên địa hình pixel; công trình của chủ đề chưa mở khóa theo cấp độ hiện trạng thái khóa và chạm vào báo điều kiện mở khóa (BR-074: chủ đề có cấp độ ≤ cấp độ trong cài đặt thì mở, cao hơn thì khóa — PQ-8).
- AC-047.3: Then mỗi công trình hiện sao chủ đề theo BR-072: 1★ khi ≥ 1/3 số từ đã thuộc (hộp 5), 2★ khi ≥ 2/3, 3★ khi 100%; với chủ đề 20 từ: 6 từ thuộc → 0★, 7 → 1★, 14 → 2★, 20 → 3★.

### Hệ thống (REQ-011, REQ-012)
#### US-025 (REQ-011): Là người học, tôi muốn có sẵn nội dung ngay khi cài app để học được luôn mà không cần mạng
- AC-025.1: Given cài app lần đầu, When mở app, Then CSDL máy được tạo và nạp 15 chủ đề, 300 từ (20 từ/chủ đề, mỗi từ 1 câu ví dụ Anh–Việt), 4 ngôn ngữ (en, ja, zh, ko), 3 bộ luyện thi (100 từ), 1 bộ tiếng Nhật (20 từ), 1 đề thi thử (20 câu) trong ≤ 5 giây, có màn hình chờ; không cần mạng. Seed là bản nền ngoại tuyến; nội dung từ server (REQ-022, Could) chỉ bổ sung/sửa. Không có bài ngữ pháp (REQ-008 Won't). Danh sách: §Nội dung seed.
- AC-025.2 (ngoại lệ): Given nạp seed lỗi giữa chừng, Then không để CSDL dở dang (giao dịch rollback) và hiện "Không thể khởi tạo dữ liệu, vui lòng mở lại app".
#### US-026 (REQ-012): Là người học hoặc giảng viên, tôi muốn xem Giới thiệu để biết nhóm làm app và nguồn dữ liệu
- AC-026.1: When mở Tôi → Giới thiệu, Then thấy tên app, phiên bản, 4 thành viên, môn học, danh sách nguồn dữ liệu và giấy phép.

## Chỉ số thành công
| Chỉ số | Hiện tại | Mục tiêu | Cách đo |
|---|---|---|---|
| Số US Must đạt toàn bộ AC | 0 | 100% (41 US Must sau thay đổi phạm vi) | báo cáo kiểm thử |
| Mục rubric có minh chứng file/hàm | 0 | 10/10 mục chức năng trong "File báo cáo kết quả dự án" | Bảng tiến độ trong file báo cáo |
| Crash ở luồng chính trong kiểm thử cuối | Chưa có | 0 | Chạy kịch bản UAT trên emulator + 1 máy thật |
| Nội dung seed | 0 | 15 chủ đề, 300 từ, 300 câu ví dụ; + 3 bộ luyện thi (100 từ), 1 bộ tiếng Nhật (20 từ), 1 đề thi thử (20 câu) | Truy vấn đếm bảng trong CSDL |
| Đồng bộ 2 máy | Chưa có | Đăng nhập máy B thấy dữ liệu máy A trong ≤ 10 s (Wi-Fi); số thẻ mỗi hộp Leitner khớp 5/5 | Kịch bản 2 máy (TESTING-STRATEGY), bấm giờ |
| Mất dữ liệu khi học ngoại tuyến | Chưa có | 0 bản ghi mất sau kịch bản 20 thẻ ở chế độ máy bay | So số bản ghi trên server trước/sau |
| Hiệu năng đồng bộ & gRPC | Chưa đo | 500 bản ghi ≤ 5 s qua Wi-Fi (NFR-N3); gRPC p95 ≤ 300 ms đo tại edge trên máy demo (NFR-N4) | Log thời gian ở app; đo p95 bằng test tải đơn giản (grpcurl/script) trên hệ thống máy chủ |
| Vòng lặp động lực | Chưa đo | ≥ 3/4 người thử nói bảng xếp hạng/ngọn lửa làm họ muốn học tiếp ngày sau | Phỏng vấn nhanh sau buổi thử (ASSUMPTION: thử với 4–8 bạn cùng lớp) |
| Mục "Sáng tạo & mở rộng" của rubric | Chưa có | Demo được đồng bộ 2 máy + bảng xếp hạng + lớp pixel trong buổi bảo vệ | Kịch bản demo, video dự phòng |
| Thời gian một phiên học 10 từ | Chưa đo | 3–7 phút | Thử với 3–4 người (PRD (nghiên cứu người dùng) H2) |
| Tỷ lệ người thử bật nhắc học | Chưa đo | ≥ 5/8 | Phỏng vấn nhanh H1 |

## Nội dung seed
Mỗi chủ đề **20 từ** thông dụng nhất của chủ đề ở cấp độ đó (15 × 20 = **300 từ**; thay đổi phạm vi C4, trước đây 30 từ/chủ đề). Nhóm có thể đổi tên chủ đề trước 18/11. Seed nằm trong APK làm bản nền ngoại tuyến; server giữ cùng nội dung (REQ-022).
| # | Chủ đề (tiếng Việt / English) | Cấp độ | Người soạn đề xuất |
|---|---|---|---|
| T01 | Gia đình / Family | A1 | Phan Anh Tuấn |
| T02 | Số đếm & thời gian / Numbers & Time | A1 | Phan Anh Tuấn |
| T03 | Màu sắc & hình dạng / Colors & Shapes | A1 | Phan Anh Tuấn |
| T04 | Cơ thể người / The Body | A1 | Phan Anh Tuấn |
| T05 | Đồ ăn & thức uống / Food & Drinks | A1 | Nguyễn Đức Quang Vinh |
| T06 | Động vật / Animals | A1 | Nguyễn Đức Quang Vinh |
| T07 | Nhà cửa & đồ dùng / House & Home | A1 | Nguyễn Đức Quang Vinh |
| T08 | Trường học / School | A2 | Nguyễn Đức Quang Vinh |
| T09 | Quần áo & mua sắm / Clothes & Shopping | A2 | Nguyễn Tấn Thành |
| T10 | Thời tiết & các mùa / Weather & Seasons | A2 | Nguyễn Tấn Thành |
| T11 | Thành phố & phương tiện / City & Transport | A2 | Nguyễn Tấn Thành |
| T12 | Sở thích & thể thao / Hobbies & Sports | A2 | Nguyễn Tấn Thành |
| T13 | Công việc & nghề nghiệp / Work & Jobs | B1 | Lê Văn Tấn Lực |
| T14 | Du lịch / Travel | B1 | Lê Văn Tấn Lực |
| T15 | Sức khỏe / Health | B1 | Lê Văn Tấn Lực |

Bảng bài ngữ pháp dưới đây **không nạp ở vòng này** (REQ-008 Won't — thay đổi phạm vi C1); giữ để truy vết và làm vòng sau.

| # | Bài ngữ pháp | Cấp độ | Số câu bài tập |
|---|---|---|---|
| G01 | Động từ "to be" (am/is/are) | A1 | 10 |
| G02 | Thì hiện tại đơn | A1 | 10 |
| G03 | Thì hiện tại tiếp diễn | A1 | 10 |
| G04 | Mạo từ a/an/the | A1 | 10 |
| G05 | Danh từ số ít/số nhiều; đếm được/không đếm được | A1 | 10 |
| G06 | Thì quá khứ đơn | A2 | 10 |
| G07 | Tương lai: will và be going to | A2 | 10 |
| G08 | So sánh hơn và so sánh nhất | A2 | 10 |
| G09 | Thì hiện tại hoàn thành | B1 | 10 |
| G10 | Câu điều kiện loại 1 và loại 2 | B1 | 10 |

Bài ngữ pháp do Lê Văn Tấn Lực điều phối, mỗi người soạn 2–3 bài. Nguồn soạn và giấy phép: Glossary (kiến thức miền) §Pháp lý.

Bổ sung :
| # | Bộ có sẵn | Ngôn ngữ | Nhãn kỳ thi | Số từ | Người soạn đề xuất |
|---|---|---|---|---|---|
| X01 | Từ vựng TOEIC thông dụng (văn phòng, kinh doanh) | en | TOEIC | 40 (thay đổi phạm vi C5) | Lê Văn Tấn Lực |
| X02 | Từ vựng IELTS học thuật | en | IELTS | 30 (thay đổi phạm vi C5) | Nguyễn Tấn Thành |
| X03 | Từ vựng Aptis | en | Aptis | 30 (thay đổi phạm vi C5) | Nguyễn Đức Quang Vinh |
| J01 | Tiếng Nhật cơ bản (mẫu chứng minh đa ngôn ngữ) | ja | — | 20 | Phan Anh Tuấn |

| # | Đề thi thử (tự soạn theo định dạng, không chép đề thật) | Số câu | Thời gian | Người soạn đề xuất |
|---|---|---|---|---|
| E01 | TOEIC Reading Part 5 — Đề 1 (điền từ vào câu) | 20 (thay đổi phạm vi C5) | 15 phút (ASSUMPTION) | Lê Văn Tấn Lực |
| E02 | IELTS/Aptis — Từ vựng & ngữ pháp — Đề 1 | — | — | **Bỏ (Won't)  C5** |

## Câu hỏi mở
| ID | Câu hỏi | Ảnh hưởng nếu chưa rõ | Ai trả lời | Hạn |
|---|---|---|---|---|
| PQ-1 | Figma hiện có những màn hình nào? | **Đã trả lời:** Figma rỗng, người thiết kế thiết kế toàn bộ 32 màn hình (33 mã SCR, sau thay đổi phạm vi/thay đổi phạm vi) ở phase 3 | Phan Anh Tuấn | Đóng 09/11 |
| PQ-2 | Danh sách chủ đề và bài ngữ pháp cụ thể | **Đã chốt:** §Nội dung seed | nhóm trưởng/người phân tích theo ủy quyền | Đóng 09/11 |
| PQ-3 | Có làm REQ-014 (quên mật khẩu bằng câu hỏi bảo mật) không? | **Đã trả lời:** có, REQ-014 thành Must, US-027 | Phan Anh Tuấn | Đóng 09/11 |
| PQ-4 | Giảng viên có yêu cầu số màn hình / chức năng tối thiểu không? | **Đã trả lời:** không có mức tối thiểu, làm càng đủ càng tốt → REQ-013 lên Should, REQ-015 lên Could | Phan Anh Tuấn | Đóng 09/11 |
| PQ-5 | Định danh đăng nhập là email (theo BR-060 của thay đổi phạm vi) thay cho tên đăng nhập — có giữ "tên đăng nhập" không? | **người phân tích đề xuất (SRS BR-060, BR-061):** đăng nhập bằng email; bỏ tên đăng nhập; biệt danh công khai duy nhất + tên hiển thị riêng (Ảnh hưởng cũ: PRD đã viết theo email + biệt danh công khai; ảnh hưởng BR-001..BR-004, SCR-01/02/03, API `auth`) | người phân tích (SRS) → Phan Anh Tuấn xác nhận | 12/11 — chờ Phan Anh Tuấn xác nhận khi duyệt lại |
| PQ-6 | Đăng xuất khi còn thay đổi chưa đồng bộ và mất mạng: cho đăng xuất kèm cảnh báo (AC-002.4, ASSUMPTION) hay chặn? | **người phân tích đề xuất (BR-073):** đẩy lần cuối rồi mới đăng xuất; không đẩy được → cảnh báo + bắt xác nhận rõ, mặc định "Ở lại" (Ảnh hưởng cũ: Có thể mất dữ liệu học trên máy) | người phân tích (BR) | 12/11 — chờ Phan Anh Tuấn xác nhận khi duyệt lại |
| PQ-7 | REQ-009 Tra từ hạ Could: mục "Yêu thích" (AC-011.1) và tìm từ trong sổ tay đặt ở đâu nếu không làm tab Tra từ? | **người phân tích đề xuất (FN-005):** nút ♥ ở SCR-07/SCR-08; danh sách ở mục "Yêu thích" trong SCR-26 → SCR-19 lọc Yêu thích; người thiết kế cập nhật UI-UX-SPECIFICATION (sơ đồ màn hình)/UI-UX-SPECIFICATION (Ảnh hưởng cũ: Ảnh hưởng UI-UX-SPECIFICATION (sơ đồ màn hình), UI-UX-SPECIFICATION) | người phân tích + người thiết kế | 12/11 — chờ Phan Anh Tuấn xác nhận khi duyệt lại |
| PQ-8 | Quy tắc "mở khóa theo cấp độ" ở chế độ Bản đồ (AC-047.2, ASSUMPTION) | **người phân tích đề xuất (BR-074):** cấp độ chủ đề ≤ cấp độ cài đặt → mở, cao hơn → khóa (Ảnh hưởng cũ: Chỉ ảnh hưởng REQ-024 (Could)) | người phân tích (BR-072 hoặc BR mới) | 13/11 — chờ Phan Anh Tuấn xác nhận khi duyệt lại |
| PQ-9 | (thay đổi phạm vi Q3) Nơi triển khai máy chủ khi demo/chấm: laptop + Docker Compose + tunnel (đề xuất thay đổi phạm vi), MongoDB Atlas, Railway hay VPS? (OPEN) | Ảnh hưởng ràng buộc "hosting cloud" (Non-goal), NFR-N4, mốc M1/M5, rủi ro R-42; chưa chốt thì mọi tài liệu ghi "nơi triển khai chưa quyết định" | Phan Anh Tuấn | Trước khi bắt đầu code server (sau 16/11) |
| PQ-10 | (thay đổi phạm vi Q6) Ai phụ trách dịch vụ nào trong 6 microservice và app `GrpcClient`? (OPEN; thay đổi phạm vi chỉ đề xuất) | Không đổi nghiệp vụ; ảnh hưởng PROJECT-PLAN (công việc)/PROJECT-PLAN và người vấn đáp từng dịch vụ | Phan Anh Tuấn | Trước khi bắt đầu code server |

## Nghiên cứu người dùng
> Persona, nhu cầu, nỗi đau, hành trình hiện tại.

## Phương pháp & nguồn
| Nguồn | Cách làm | Độ tin cậy |
|---|---|---|
| Trang Google Play của 4 app (học tiếng Anh InnovationApps, Duolingo, Quizlet, AnkiDroid), đọc 2026-11-09 | Đọc mô tả tính năng, mô hình giá, các bài đánh giá hiển thị đầu trang | Trung bình: mô tả do nhà phát triển viết; chỉ đọc vài bài đánh giá nổi bật |
| Kiến thức miền về ghi nhớ và học ngôn ngữ | Xem Glossary (kiến thức miền) | Cao (nguồn học thuật/chuẩn quốc tế) |
| Phỏng vấn đầu vào với nhóm (PRD) | Câu trả lời của nhóm | Cao cho ràng buộc; thấp cho nhu cầu người dùng thật |
| Phỏng vấn người dùng thật | **Chưa làm.** Đề xuất: mỗi thành viên hỏi nhanh 2 bạn cùng lớp (tổng 8 người) bằng 5 câu ở §Cơ hội | — |

Giới hạn: chưa có dữ liệu người dùng thật, nên persona và insight dưới đây là giả thuyết có căn cứ, cần kiểm chứng (H1–H5).

## Persona
| Persona | Mục tiêu | Nỗi đau | Bối cảnh dùng |
|---|---|---|---|
| **P1 — Minh, sinh viên năm 2 (20 tuổi)**, trình độ A2, cần chuẩn đầu ra tiếng Anh của trường | Tăng vốn từ cơ bản để thi chứng chỉ; học đều mỗi ngày | Học vài hôm rồi bỏ; quên từ sau 1–2 ngày; app nhiều quảng cáo, nhiều tính năng tốn phí | Trên xe buýt, giờ nghỉ giữa tiết; mạng di động yếu hoặc hết dung lượng; mỗi lần 5–15 phút |
| **P2 — Chị Lan, nhân viên văn phòng (28 tuổi)**, trình độ A1–A2, mất gốc ngữ pháp | Giao tiếp cơ bản, đọc email đơn giản | Không biết bắt đầu từ đâu; ngại app phức tạp; bị app phạt khi sai thấy nản | Buổi tối trước khi ngủ; cần được nhắc vì hay quên |
| **P3 — Học sinh lớp 10–12 (16–18 tuổi)**, trình độ A2–B1 | Ôn từ và ngữ pháp theo chủ đề cho bài kiểm tra | Cần tra nghĩa nhanh, tự ghi lại từ mới gặp | Ở nhà, điện thoại dùng chung, không có gói cước |

P1 là persona chính để thiết kế; P2, P3 dùng để kiểm tra giao diện đủ đơn giản và tính năng tra từ/sổ tay.

## Hành trình hiện tại (as-is)
Persona P1, không có LearningE:
1. **Quyết tâm** — tải một app học tiếng Anh hoặc mua sổ từ vựng.
2. **Học vài ngày đầu** — học được 20–30 từ; app chen quảng cáo, một số tính năng đòi trả phí hoặc cần mạng.
3. **Gián đoạn** — bận thi giữa kỳ, không ai nhắc, bỏ 3–4 ngày.
4. **Quên** — mở lại thấy đã quên phần lớn từ cũ, không biết từ nào cần ôn.
5. **Bỏ cuộc** — cảm giác không tiến bộ, chuyển app khác hoặc ngừng học.
Điểm đau lớn nhất: bước 3 → 4 (không có nhắc học và lịch ôn lại).

## Insight chính (kèm bằng chứng)
| # | Insight | Bằng chứng |
|---|---|---|
| I1 | Thói quen hằng ngày là yếu tố giữ chân chính | Duolingo đặt streak, mốc thành tích, nhắc học làm tính năng cốt lõi (mô tả Play); Duolingo vừa đổi tính năng streak ("Streak của bạn đang tiến hóa…") |
| I2 | Ôn lại đúng lúc sắp quên giúp nhớ lâu hơn học dồn | AnkiDroid dựa hoàn toàn vào lặp lại ngắt quãng (SM-2); Quizlet có chế độ "ôn luyện lặp lại, định kỳ"; đường cong quên của Ebbinghaus (Glossary (kiến thức miền)) |
| I3 | Offline là nhu cầu thật, đủ quan trọng để được bán thành tính năng trả phí | Quizlet đưa "truy cập ngoại tuyến" vào gói Plus; app tham khảo ghi rõ "Không cần kết nối Internet" |
| I4 | Cơ chế phạt khi sai gây khó chịu | Bài đánh giá Duolingo 05/11/2026 (27 lượt thấy hữu ích): bị trừ tim khi không nói được |
| I5 | Người mới cần lộ trình sẵn, không muốn tự tạo nội dung | App tham khảo (9000 từ chia sẵn thể loại) có 500N+ lượt tải dù tính năng đơn giản; AnkiDroid/Quizlet phải dựa vào kho bộ thẻ có sẵn |
| I6 | Phát âm là phần không thể thiếu khi học từ | Cả 4 app đều có âm thanh/TTS; AnkiDroid tích hợp text-to-speech |

## Cơ hội & giả thuyết cần kiểm chứng
| # | Giả thuyết | Liên quan | Cách kiểm chứng | Ngưỡng đạt |
|---|---|---|---|---|
| H1 | Người dùng sẽ bật nhắc học nếu chỉ phải chọn 1 giờ | F4 | Hỏi 8 bạn: "Bạn có bật thông báo nhắc học không?" | ≥5/8 trả lời có |
| H2 | Phiên học 10 từ (khoảng 5 phút) là vừa | F2 | Thử bản Figma prototype với 3–4 bạn, bấm giờ | Hoàn thành trong 3–7 phút |
| H3 | Người dùng thích "đánh dấu đã thuộc" + ôn lại từ sai hơn là bị trừ điểm | F2, F3 | Hỏi lựa chọn giữa 2 cách | ≥5/8 chọn không phạt |
| H4 | 10–15 chủ đề × 30 từ đủ cho đồ án và đủ hữu ích | A3 | Hỏi các chủ đề muốn học, đối chiếu danh sách dự kiến | ≥70% chủ đề được nhắc đến nằm trong danh sách |
| H5 | Sổ tay từ tự thêm được dùng thường xuyên | F5 | Hỏi "Bạn có hay ghi lại từ mới gặp không?" | ≥4/8 có |

5 câu hỏi nhanh cho mỗi người được hỏi:
1. Bạn đang học tiếng Anh bằng gì? Đã bỏ app nào chưa, vì sao?
2. Bạn học lúc nào, ở đâu, có mạng không?
3. Bạn có bật thông báo nhắc học nếu app cho chọn giờ không?
4. Khi trả lời sai, bạn muốn app làm gì?
5. Bạn muốn học từ theo những chủ đề nào?

## Phân tích đối thủ
> Đối thủ/giải pháp thay thế, điểm khác biệt.
> Nguồn: trang Google Play (tiếng Việt) của từng app, đọc ngày 2026-11-09. Số liệu (sao, lượt tải) có thể thay đổi theo thời gian.

## Đối thủ / giải pháp thay thế
| Tên | Điểm mạnh | Điểm yếu | Giá | Ghi chú |
|---|---|---|---|---|
| **Học tiếng Anh** — InnovationApps (`com.language.learnenglish`) — app tham khảo chính | ~9000 từ và cụm từ có âm thanh, chia thể loại → tiểu thể loại; quiz chọn đáp án + sắp xếp từ thành câu; tìm kiếm, yêu thích; chạy offline. 4,7★, 500N+ lượt tải | Không thấy flashcard có trạng thái thuộc/chưa thuộc, không thống kê tiến độ, không nhắc học, không có bài ngữ pháp (theo mô tả trên Play) | Miễn phí, có quảng cáo + mua trong app | Gần LearningE nhất về phạm vi và cách chạy offline. Lấy cấu trúc "chủ đề → bài → từ" và 2 dạng quiz |
| **Duolingo** (`com.duolingo`) | Bài học ngắn kiểu trò chơi; đủ nghe–nói–đọc–viết; streak, mốc thành tích, bảng xếp hạng tạo thói quen hằng ngày. 4,7★, 500Tr+ lượt tải | Cơ chế "tim" bị trừ khi sai/không nói được gây khó chịu (bài đánh giá 05/11/2026); cần mạng cho phần lớn tính năng; nhiều tính năng ngoài ngôn ngữ (cờ vua, toán) | Miễn phí có quảng cáo; Super Duolingo trả phí (dùng thử 14 ngày) | Mẫu tham khảo cho **chuỗi ngày học (streak)** và nhắc học; tránh cơ chế phạt |
| **Quizlet** (`com.quizlet.quizletandroid`) | Tự tạo bộ thẻ hoặc dùng 500Tr+ học phần có sẵn; chế độ Học lặp lại định kỳ, chấm điểm; bài kiểm tra thử; chia sẻ thẻ. 4,6★, 50Tr+ lượt tải | Truy cập offline chỉ có trong gói trả phí Quizlet Plus; nội dung do cộng đồng tạo, chất lượng không đều; không chuyên tiếng Anh | Miễn phí có quảng cáo; Quizlet Plus trả phí (tháng/năm) | Mẫu tham khảo cho **flashcard + chế độ kiểm tra** và sổ tay từ tự tạo |
| **AnkiDroid** (`com.ichi2.anki`) | Lặp lại ngắt quãng theo thuật toán SuperMemo 2 (SM-2); TextToSpeech; thống kê chi tiết; widget tiến độ; >6000 bộ thẻ có sẵn; mã nguồn mở. 4,8★, 10Tr+ lượt tải | Giao diện và cấu hình phức tạp với người mới; người dùng phải tự chọn/tạo bộ thẻ, không có lộ trình bài học | Miễn phí, mã nguồn mở, không quảng cáo | Mẫu tham khảo cho **thuật toán ôn tập** và **phát âm bằng TextToSpeech** (đúng hướng A4) |
| **Giải pháp thay thế không phải app**: sổ tay từ giấy, flashcard giấy, nhóm học | Không tốn tiền, quen thuộc | Không tự nhắc, không thống kê, không phát âm, dễ bỏ dở | 0đ | Đối thủ thật của người mới học: thói quen cũ |

## Điểm khác biệt của chúng ta
LearningE ghép những điểm mạnh nằm rải rác ở 4 app trên thành một app **gọn, miễn phí, offline 100%, không quảng cáo** cho người Việt mới học:
1. **Offline đầy đủ ở mọi tính năng** (Quizlet khóa offline sau gói trả phí; Duolingo cần mạng).
2. **Flashcard có trạng thái + lịch ôn đơn giản** (học theo ý tưởng SM-2/Leitner như AnkiDroid, nhưng người dùng không phải cấu hình gì).
3. **Lộ trình có sẵn theo chủ đề** như app tham khảo, thay vì bắt người dùng tự tìm bộ thẻ như AnkiDroid/Quizlet.
4. **Động lực không phạt**: chuỗi ngày học + nhắc học hằng ngày như Duolingo, nhưng **không trừ "tim"**, không khóa bài khi sai.
5. **Ngữ pháp + sổ tay từ cá nhân** trong cùng app, Anh–Việt, giao diện tiếng Việt.

## Bài học / mẫu nên tránh
| Mẫu | Thấy ở | Vì sao tránh | LearningE làm gì |
|---|---|---|---|
| Phạt khi sai (trừ tim, khóa bài) | Duolingo (bài đánh giá người dùng) | Làm người mới nản; lỗi kỹ thuật (micro) cũng bị phạt | Sai chỉ hiện đáp án đúng + đưa từ vào danh sách ôn lại |
| Khóa tính năng cốt lõi sau trả phí (offline) | Quizlet | Đi ngược mục tiêu "học mọi lúc" | Mọi tính năng đều offline, miễn phí |
| Cấu hình phức tạp, nhiều tùy chọn | AnkiDroid | Người mới không biết bắt đầu từ đâu | Chỉ 1 thiết lập: giờ nhắc học + mục tiêu số từ/ngày |
| Mở rộng sang lĩnh vực khác (cờ vua, toán) | Duolingo | Loãng trọng tâm | Chỉ tiếng Anh, 5 nhóm tính năng F1–F5 |
| Quảng cáo trong lúc học | App tham khảo, Duolingo, Quizlet (bản miễn phí) | Ngắt mạch học | Không quảng cáo (Non-goal) |
| Sao chép dữ liệu/nội dung của app khác | — | Vi phạm bản quyền | Nội dung seed tự soạn hoặc từ nguồn mở có giấy phép phù hợp (Glossary (kiến thức miền) §Nguồn) |
