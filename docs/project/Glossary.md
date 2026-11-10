# Glossary — thuật ngữ nghiệp vụ và kỹ thuật

> **TỰ SINH** từ bộ phân tích thiết kế đã duyệt của nhóm — không sửa trực tiếp tệp này; đề xuất thay đổi qua Issue/PR để nhóm trưởng cập nhật nguồn và sinh lại. Danh mục tài liệu: [docs/README.md](../README.md).

**Mục đích:** từ điển thuật ngữ dùng thống nhất trong code, giao diện và tài liệu; kiến thức miền về học từ vựng, ôn tập ngắt quãng, dạng câu hỏi, ràng buộc Android.

## Thuật ngữ
> Từ điển thuật ngữ thống nhất. Code dùng tên tiếng Anh ở cột "Tên trong code"; tài liệu và giao diện dùng tên tiếng Việt.

| Thuật ngữ | Định nghĩa | Không nhầm với | Ghi chú |
|---|---|---|---|
| Người dùng (User) | Người có tài khoản cục bộ trong app; mọi dữ liệu học thuộc về một người dùng | Thành viên nhóm phát triển | Code: `User`. Chỉ 1 vai trò |
| Tài khoản cục bộ | Tài khoản lưu trong SQLite trên máy, không đồng bộ | Tài khoản Google/Firebase | Mật khẩu lưu dạng băm có salt |
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
| Offline | Mọi tính năng chạy không cần Internet | — | Ràng buộc bắt buộc |

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
