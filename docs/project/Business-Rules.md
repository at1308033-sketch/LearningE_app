# Business Rules — quy tắc nghiệp vụ

> **TỰ SINH** từ bộ phân tích thiết kế đã duyệt của nhóm — không sửa trực tiếp tệp này; đề xuất thay đổi qua Issue/PR để nhóm trưởng cập nhật nguồn và sinh lại. Danh mục tài liệu: [docs/README.md](../README.md).

**Mục đích:** toàn bộ quy tắc nghiệp vụ BR-xxx (điều kiện → kết quả), nguồn user story và ví dụ số. Mọi logic nghiệp vụ trong code phải dẫn được về một BR.

## Quy tắc nghiệp vụ
| ID | Quy tắc (nếu [điều kiện] thì [kết quả]) | Nguồn (US) | Ví dụ số |
|---|---|---|---|
| BR-001 | Nếu tên đăng nhập đã tồn tại (so sánh không phân biệt hoa thường) thì không cho đăng ký | US-001 | "Minh01" trùng "minh01" |
| BR-002 | Mật khẩu chỉ lưu dạng băm PBKDF2-HMAC-SHA256 với salt ngẫu nhiên 16 byte riêng từng tài khoản; không bao giờ lưu hay ghi log mật khẩu rõ | US-001, US-004 | 120.000 vòng lặp (lập trình viên chốt ở ARCHITECTURE-DECISIONS) |
| BR-003 | Nếu đăng nhập sai 5 lần liên tiếp cho một tên đăng nhập thì khóa đăng nhập tên đó 60 giây; đăng nhập đúng thì đếm về 0 | US-002 | Sai lần 5 lúc 10:00:00 → thử lại được từ 10:01:00 |
| BR-004 | Nếu đăng nhập thành công thì lưu mã người dùng hiện tại trong SharedPreferences; mở app có mã hợp lệ thì vào thẳng Trang chủ; đăng xuất hoặc xóa tài khoản thì xóa mã | US-002 | — |
| BR-005 | Nếu xóa tài khoản thì xóa mọi bản ghi gắn với người dùng đó (tiến độ thẻ, lượt quiz và câu trả lời, sổ tay, yêu thích, nhật ký học, trạng thái bài ngữ pháp, cài đặt) trong một giao dịch; dữ liệu seed không bị ảnh hưởng | US-005 | — |
| BR-006 | Nếu bỏ qua Thiết lập lần đầu thì dùng mặc định: cấp độ A1, mục tiêu 10 từ/ngày, nhắc học bật lúc 20:00 (nếu có quyền thông báo) | US-006 | — |
| BR-007 | Câu trả lời bảo mật được chuẩn hóa (bỏ khoảng trắng đầu/cuối, chữ thường, bỏ dấu tiếng Việt) rồi băm PBKDF2 với salt riêng như BR-002; so sánh bằng băm | US-001, US-027 | " Hà Nội " ≡ "ha noi" |
| BR-008 | Nếu trả lời sai câu hỏi bảo mật 3 lần liên tiếp thì khóa đặt lại mật khẩu của tài khoản đó 5 phút; trả lời đúng thì đếm về 0 | US-027 | Sai lần 3 lúc 10:00 → thử lại từ 10:05 |
| BR-009 | 5 câu hỏi bảo mật cố định: tên trường tiểu học đầu tiên; tên thú cưng đầu tiên; thành phố nơi bạn sinh ra; món ăn yêu thích; tên người bạn thân thời nhỏ | US-001 | — |
| BR-010 | Số thẻ trong phiên học từ mới = max(5, mục tiêu − số từ mới đã học hôm nay), không vượt số từ Mới còn lại của chủ đề | US-009 | Mục tiêu 10, đã học 7 → phiên 5 thẻ; chủ đề còn 3 từ Mới → 3 thẻ |
| BR-011 | Leitner: chọn "Đã nhớ" → hộp = min(hộp + 1, 5); "Chưa nhớ" → hộp = 1. Thẻ Mới khi được đánh giá lần đầu: "Đã nhớ" → hộp 2, "Chưa nhớ" → hộp 1 | US-009, US-012 | Hộp 3 + Đã nhớ → hộp 4 |
| BR-012 | Ngày ôn tiếp = ngày đánh giá + khoảng của hộp: hộp 1 = 1 ngày, 2 = 2, 3 = 4, 4 = 7, 5 = 14 ngày | US-012 | Đánh giá 10/11, lên hộp 4 → ôn 17/11 |
| BR-013 | Thẻ đến hạn ôn khi ngày ôn tiếp ≤ hôm nay; phiên ôn tối đa 50 thẻ, sắp ngày ôn tiếp tăng dần rồi hộp tăng dần | US-012 | 63 thẻ đến hạn → phiên đầu 50, phiên sau 13 |
| BR-014 | Trạng thái thẻ: chưa có bản ghi tiến độ = **Mới**; hộp 1–4 = **Đang học**; hộp 5 = **Đã thuộc** | US-008, US-017 | — |
| BR-015 | Một từ seed hoặc một từ sổ tay có tối đa 1 bản ghi tiến độ cho mỗi người dùng | US-009, US-024 | — |
| BR-020 | Quiz chủ đề gồm 10 câu (hoặc 2 × số từ nếu chủ đề có < 5 từ đã học), chọn ngẫu nhiên từ các từ của chủ đề, ưu tiên từ Đang học; dạng câu hỏi xoay vòng: trắc nghiệm Anh→Việt, trắc nghiệm Việt→Anh, nghe chọn từ, điền từ, sắp xếp câu | US-013 | — |
| BR-021 | Nếu một câu quiz về từ X sai thì thẻ của X (nếu đã có tiến độ) về hộp 1, ngày ôn tiếp = ngày mai; nếu X là Mới thì tạo tiến độ ở hộp 1 | US-014 | — |
| BR-022 | Trả lời sai không trừ điểm, không giới hạn số lần sai, không khóa bài | US-013 | — |
| BR-023 | Điểm lượt làm = round(số câu đúng / tổng số câu × 100); lượt làm chỉ lưu khi trả lời hết các câu | US-014, US-021 | 7/10 → 70 |
| BR-024 | Đáp án nhiễu của câu trắc nghiệm: 3 từ khác cùng chủ đề (thiếu thì lấy cùng cấp độ), không trùng nghĩa với đáp án đúng | US-013 | — |
| BR-025 | Câu "điền từ" chấm đúng khi chuỗi nhập (bỏ khoảng trắng đầu/cuối, không phân biệt hoa thường) bằng từ đáp án | US-013 | " Apple " = "apple" |
| BR-030 | Nhắc học: mỗi ngày đúng giờ đã chọn (±15 phút), chỉ hiện nếu nhắc học bật, có quyền thông báo và hôm nay chưa đạt mục tiêu (số từ mới học hôm nay < mục tiêu **hoặc** còn thẻ đến hạn); đổi giờ thì hủy lịch cũ, đặt lịch mới | US-019, US-003 | — |
| BR-031 | Chuỗi ngày học: khi ghi hoạt động học đầu tiên của ngày D, nếu ngày học gần nhất = D − 1 thì chuỗi + 1; nếu < D − 1 thì chuỗi = 1; chuỗi dài nhất = max(chuỗi dài nhất, chuỗi) | US-018 | Học 8/11, 9/11, nghỉ 10/11, học 11/11 → chuỗi = 1 |
| BR-032 | Hoạt động học = hoàn thành phiên học từ mới, phiên ôn tập, lượt quiz, hoặc đọc xong bài ngữ pháp. Mở app hay xem danh sách không tính | US-016, US-018 | — |
| BR-033 | Thống kê tính trực tiếp từ dữ liệu, không lưu số tổng hợp riêng (tránh lệch số) | US-017 | — |
| BR-040 | Tra từ: khớp tiền tố từ tiếng Anh hoặc chứa chuỗi trong nghĩa tiếng Việt; chuẩn hóa: chữ thường, bỏ dấu tiếng Việt; tối đa 50 kết quả, từ khớp chính xác lên đầu | US-022 | "an" → "and", "animal"…; "con meo" khớp "con mèo" |
| BR-041 | Sổ tay không cho 2 mục cùng từ (không phân biệt hoa thường) trong cùng một người dùng | US-023 | — |
| BR-042 | Ngày trong app luôn là ngày lịch theo múi giờ của máy | US-012, US-018 | — |
| BR-043 | Bộ có sẵn (`owner` rỗng: 15 chủ đề, bộ luyện thi, bộ tiếng Nhật) chỉ đọc; bộ của người dùng chỉ chủ sở hữu thấy, sửa, xóa | US-028..US-031 | — |
| BR-044 | Tên bộ 1–50 ký tự sau khi bỏ khoảng trắng đầu/cuối, không trùng (không phân biệt hoa thường) trong các bộ của cùng người dùng | US-028, US-029 | "Từ mới" trùng "từ mới" |
| BR-045 | Xóa bộ xóa mọi từ của bộ và tiến độ thẻ của các từ đó trong một giao dịch; bộ mặc định "Sổ tay của tôi" không xóa được | US-029 | — |
| BR-046 | Khi đăng ký, tạo sẵn 1 bộ mặc định "Sổ tay của tôi" (ngôn ngữ en) cho người dùng | US-028, US-023 | — |
| BR-047 | Trong một bộ không có 2 từ cùng `term` sau chuẩn hóa (bỏ khoảng trắng đầu/cuối, chữ thường) — thay và mở rộng BR-041 | US-030, US-023, US-032 | "Apple" trùng " apple" |
| BR-048 | Import CSV: UTF-8 (chấp nhận BOM), dòng đầu là tiêu đề đúng thứ tự `term,reading,meaning,example,example_translation`; theo RFC 4180 (ô chứa dấu phẩy, dấu nháy, xuống dòng phải bọc nháy kép; nháy trong ô viết `""`); `term`, `meaning` bắt buộc; độ dài trường như §Validation | US-032 | — |
| BR-049 | Import JSON: cùng định dạng file seed (CODING-STANDARDS) — đối tượng có mảng `words`, mỗi phần tử có `term`, `meaning` bắt buộc | US-032 | — |
| BR-050 | File ≤ 1 MB và ≤ 2000 dòng dữ liệu; dòng thiếu trường bắt buộc/quá dài → lỗi kèm số dòng; dòng trùng trong file hoặc trong bộ đích → bỏ qua; mọi dòng hợp lệ chèn trong một giao dịch; không có dòng hợp lệ → không tạo/sửa gì; bộ đích mới lấy tên file (bỏ đuôi), trùng tên thì thêm " (2)" | US-032 | 100 dòng: 90 hợp lệ, 6 trùng, 4 lỗi → nhập 90 |
| BR-051 | Export một bộ (kể cả bộ có sẵn) ra CSV (định dạng BR-048) hoặc JSON (BR-049), tên file `<tên bộ>_<yyyyMMdd>`; chỉ xuất nội dung từ, không xuất tiến độ học hay dữ liệu tài khoản | US-033 | `Từ mới_20261120.csv` |
| BR-052 | Ngôn ngữ nội dung lấy từ danh mục `languages` (seed: en, ja, zh, ko); mỗi bộ đúng 1 ngôn ngữ, chỉ đổi được khi bộ chưa có từ; TTS dùng `tts_locale` của ngôn ngữ; nhãn trường cách đọc: en "Phiên âm (IPA)", ja "Cách đọc (kana/romaji)", zh "Pinyin", ko "Romaja" | US-034 | — |
| BR-053 | Tra từ (mở rộng BR-040) áp dụng cho mọi ngôn ngữ: khớp tiền tố trên `term` đã chuẩn hóa, chứa chuỗi trong `reading` và nghĩa tiếng Việt đã bỏ dấu | US-022, US-034 | "ねこ" khớp "ねこ"; "neko" khớp cách đọc |
| BR-054 | Đề thi thử có số câu và thời gian cố định (E01: 30 câu/20 phút; E02: 20 câu/15 phút); hết giờ tự nộp, câu chưa làm tính sai; điểm = round(đúng/tổng × 100); không quy đổi sang thang điểm TOEIC/IELTS/Aptis | US-036 | 24/30 → 80 |
| BR-055 | Câu thi thử sai có gắn từ vựng → thẻ của từ về hộp 1 như BR-021; lượt thi lưu lịch sử riêng (không lẫn với quiz chủ đề) và tính là một hoạt động học (BR-032) | US-036 | — |
