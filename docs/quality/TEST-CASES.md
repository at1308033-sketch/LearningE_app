# Test Cases — danh sách kịch bản kiểm thử

> **TỰ SINH** từ bộ phân tích thiết kế đã duyệt của nhóm — không sửa trực tiếp tệp này; đề xuất thay đổi qua Issue/PR để nhóm trưởng cập nhật nguồn và sinh lại. Danh mục tài liệu: [docs/README.md](../README.md).

**Mục đích:** mỗi tiêu chí chấp nhận có một test case TC-xxx (mức kiểm thử, kịch bản rút gọn, ưu tiên) và ma trận truy vết REQ → US → AC → BR → FLOW → TC → Task.

## Truy vết AC -> test case
Mức: **U** unit · **I** instrumented · **E** Espresso · **M** thủ công. Ưu tiên Cao = US Must, Trung bình = US Should. Bảng nối REQ/BR/FLOW/Task đầy đủ: TEST-CASES. Mỗi TC thủ công chạy theo đúng Given/When/Then của AC; bước chi tiết và bằng chứng (ảnh chụp) ghi trong báo cáo kiểm thử.

| TC | AC | Mức | Kịch bản rút gọn (bước → kết quả mong đợi theo AC) | Ưu tiên |
|---|---|---|---|---|
| TC-001.1 | AC-001.1 (US-001) | E+M | Given chưa có tài khoản, When nhập tên hiển thị, tên đăng nhập, mật khẩu, xác nhận mật khẩu hợp lệ và bấm Đăng ký, Then tài khoản được tạo, mật khẩ… | Cao |
| TC-001.2 | AC-001.2 (US-001) | M | Given tên đăng nhập đã tồn tại, When bấm Đăng ký, Then không tạo tài khoản và hiện "Tên đăng nhập đã được sử dụng" dưới trường đó. | Cao |
| TC-001.3 | AC-001.3 (US-001) | M | Given một trường vi phạm quy tắc kiểm tra (SRS §Validation), When bấm Đăng ký, Then hiện lỗi tại đúng trường sai, giữ nguyên dữ liệu đã… | Cao |
| TC-001.4 | AC-001.4 (US-001) | U+M | Given đang đăng ký, When chọn 1 trong 5 câu hỏi bảo mật có sẵn (BR-009) và nhập câu trả lời 2–50 ký tự, Then câu trả lời được chuẩn hóa và lưu dạng… | Cao |
| TC-002.1 | AC-002.1 (US-002) | E+M | Given đúng tên đăng nhập và mật khẩu, When bấm Đăng nhập, Then vào Trang chủ trong ≤ 1 giây và phiên đăng nhập được ghi nhớ đến khi đăng xuất (BR-0… | Cao |
| TC-002.2 | AC-002.2 (US-002) | M | Given sai tên đăng nhập hoặc mật khẩu, When bấm Đăng nhập, Then hiện thông báo chung "Tên đăng nhập hoặc mật khẩu không đúng" (không nói rõ sai phầ… | Cao |
| TC-002.3 | AC-002.3 (US-002) | U+M | Given một tên đăng nhập đã sai mật khẩu 4 lần liên tiếp, When nhập sai lần thứ 5, Then khóa ngay đăng nhập tên đó 60 giây và hiện thời gian còn lại (BR-003); lần 1–4 chưa khóa; tên đăng nhập khác không bị khóa. | Cao |
| TC-002.4 | AC-002.4 (US-002) | M | Given đang đăng nhập, When bấm Đăng xuất và xác nhận, Then quay về màn hình Đăng nhập; mở lại app vẫn ở màn hình Đăng nhập. | Cao |
| TC-003.1 | AC-003.1 (US-003) | M | Given đã đăng nhập, When mở tab Tôi, Then thấy tên hiển thị, cấp độ, mục tiêu hằng ngày, giờ nhắc học, công tắc nhắc học. | Cao |
| TC-003.2 | AC-003.2 (US-003) | M | When đổi một giá trị hợp lệ và lưu, Then giá trị mới có hiệu lực ngay (lịch nhắc được đặt lại nếu đổi giờ nhắc — BR-030). | Cao |
| TC-004.1 | AC-004.1 (US-004) | U+M | Given nhập đúng mật khẩu hiện tại và mật khẩu mới hợp lệ (khác mật khẩu cũ), When lưu, Then mật khẩu mới có hiệu lực ở lần đăng nhập sau. | Cao |
| TC-004.2 | AC-004.2 (US-004) | M | Given mật khẩu hiện tại sai, When lưu, Then báo "Mật khẩu hiện tại không đúng", không đổi gì. | Cao |
| TC-005.1 | AC-005.1 (US-005) | I+M | Given đã đăng nhập, When chọn Xóa tài khoản, nhập lại mật khẩu đúng và xác nhận, Then xóa tài khoản và mọi dữ liệu thuộc người dùng đó (BR-005); truy vấn từng bảng theo user_id (cài đặt, tiến độ thẻ, yêu thích, bộ từ riêng và từ, lượt quiz/thi và câu trả lời, nhật ký học) đều trả về 0 dòng; dữ liệu seed còn nguyên… | Cao |
| TC-005.2 | AC-005.2 (US-005) | M | Given bấm Hủy ở hộp xác nhận, Then không xóa gì. | Cao |
| TC-006.1 | AC-006.1 (US-006) | E+M | Given vừa đăng ký, When hoàn thành 3 bước (cấp độ A1/A2/B1; mục tiêu 5/10/15/20 từ mỗi ngày; giờ nhắc), Then lưu cài đặt và vào Trang chủ. | Cao |
| TC-006.2 | AC-006.2 (US-006) | M | Given ở bước giờ nhắc trên Android 13+, When bật nhắc học, Then app xin quyền thông báo; nếu bị từ chối thì vẫn vào Trang chủ, nhắc học ở trạng thá… | Cao |
| TC-006.3 | AC-006.3 (US-006) | M | Given bấm "Bỏ qua", Then dùng mặc định: A1, 10 từ/ngày, nhắc 20:00 (BR-006). | Cao |
| TC-007.1 | AC-007.1 (US-007) | M | Given đã có dữ liệu seed, When mở tab Học, Then thấy ≥10 chủ đề, mỗi chủ đề hiện tên, icon, cấp độ, tiến độ "đã thuộc x/y từ". | Cao |
| TC-007.2 | AC-007.2 (US-007) | M | When lọc theo cấp độ (Tất cả/A1/A2/B1), Then chỉ hiện chủ đề đúng cấp độ; mặc định lọc theo cấp độ trong cài đặt. | Cao |
| TC-008.1 | AC-008.1 (US-008) | M | When mở một chủ đề, Then thấy danh sách từ với từ, phiên âm, nghĩa và nhãn trạng thái (Mới/Đang học/Đã thuộc). | Cao |
| TC-008.2 | AC-008.2 (US-008) | M | When chạm một từ, Then mở Chi tiết từ: từ, phiên âm IPA, từ loại, nghĩa, câu ví dụ Anh + Việt, nút phát âm, nút yêu thích, nút thêm vào sổ tay. | Cao |
| TC-009.1 | AC-009.1 (US-009) | U+M | Given chủ đề còn từ Mới, When bấm "Học từ mới", Then mở phiên gồm tối đa N thẻ (N = số từ mục tiêu còn lại trong ngày, tối thiểu 5 — BR-010). | Cao |
| TC-009.2 | AC-009.2 (US-009) | E+M | When chạm thẻ, Then lật thẻ (mặt trước: từ + phiên âm; mặt sau: nghĩa + ví dụ) có hiệu ứng ≤ 400 ms. | Cao |
| TC-009.3 | AC-009.3 (US-009) | U+M | When chọn "Đã nhớ" hoặc "Chưa nhớ", Then thẻ vào hộp Leitner theo BR-011 và hiện thẻ kế tiếp; hết phiên hiện màn hình Kết quả phiên (số từ đã nhớ/c… | Cao |
| TC-009.4 | AC-009.4 (US-009) | M | Given chủ đề không còn từ Mới, Then nút "Học từ mới" bị ẩn và hiện "Bạn đã học hết từ mới của chủ đề này", gợi ý Ôn tập hoặc Quiz. | Cao |
| TC-009.5 | AC-009.5 (US-009) | I+M | Given thoát giữa phiên, Then các thẻ đã đánh giá được lưu; thẻ chưa đánh giá vẫn là Mới. | Cao |
| TC-010.1 | AC-010.1 (US-010) | M | Given máy có giọng tiếng Anh cho TextToSpeech, When bấm nút loa, Then đọc từ (hoặc câu ví dụ) bằng giọng en-US trong ≤ 1 giây. | Cao |
| TC-010.2 | AC-010.2 (US-010) | M | Given máy thiếu dữ liệu giọng hoặc không hỗ trợ, When bấm nút loa, Then hiện "Máy chưa có giọng đọc tiếng Anh" kèm nút mở cài đặt giọng đọc; app kh… | Cao |
| TC-011.1 | AC-011.1 (US-011) | M | When bấm biểu tượng yêu thích ở Chi tiết từ hoặc danh sách từ, Then trạng thái đổi ngay và từ xuất hiện trong mục Yêu thích (tab Tra từ). | Cao |
| TC-011.2 | AC-011.2 (US-011) | M | When bấm lại, Then bỏ yêu thích. | Cao |
| TC-012.1 | AC-012.1 (US-012) | I+M | Given có thẻ với ngày ôn tiếp ≤ hôm nay, When bấm "Ôn tập" ở Trang chủ, Then mở phiên flashcard gồm các thẻ đến hạn, sắp theo ngày ôn sớm nhất trướ… | Cao |
| TC-012.2 | AC-012.2 (US-012) | U | When chọn "Đã nhớ", Then thẻ lên 1 hộp và ngày ôn tiếp = hôm nay + khoảng của hộp mới (1/2/4/7/14 ngày); When chọn "Chưa nhớ", Then thẻ về hộp 1, ô… | Cao |
| TC-012.3 | AC-012.3 (US-012) | M | Given không có thẻ đến hạn, Then nút Ôn tập hiện "Hôm nay không có thẻ cần ôn" và bị vô hiệu. | Cao |
| TC-013.1 | AC-013.1 (US-013) | U+M | Given chọn một chủ đề có ≥ 4 từ, When bấm "Bắt đầu quiz", Then tạo bài 10 câu ngẫu nhiên trộn 5 dạng: trắc nghiệm Anh→Việt, trắc nghiệm Việt→Anh, nghe chọn từ, điền từ, sắp xếp câu (BR-020)… | Cao |
| TC-013.2 | AC-013.2 (US-013) | U+M | When trả lời một câu, Then hiện ngay đúng/sai và đáp án đúng; không trừ điểm, không khóa bài (BR-022). | Cao |
| TC-013.3 | AC-013.3 (US-013) | M | Given máy không có giọng TTS, Then không sinh câu dạng "nghe chọn từ" mà thay bằng trắc nghiệm nghĩa. | Cao |
| TC-013.4 | AC-013.4 (US-013) | M | Given thoát giữa bài, Then hỏi xác nhận; nếu thoát thì không lưu lượt làm. | Cao |
| TC-014.1 | AC-014.1 (US-014) | U+E | When trả lời xong câu cuối, Then hiện điểm = số câu đúng/tổng × 100 (làm tròn số nguyên), số câu đúng, thời gian làm, danh sách câu sai kèm đáp án… | Cao |
| TC-014.2 | AC-014.2 (US-014) | I+M | Then các từ trả lời sai được đưa về hộp 1 (BR-021) và lượt làm được lưu lịch sử. | Cao |
| TC-015.1 | AC-015.1 (US-015) | M | When mở Lịch sử quiz, Then thấy danh sách lượt làm mới nhất trước: chủ đề/bài ngữ pháp, ngày giờ, điểm. | Cao |
| TC-015.2 | AC-015.2 (US-015) | M | Given chưa làm quiz nào, Then hiện "Bạn chưa làm bài kiểm tra nào" và nút đến danh sách quiz. | Cao |
| TC-016.1 | AC-016.1 (US-016) | M | When mở Trang chủ, Then thấy: lời chào theo tên, chuỗi ngày học, số từ mới đã học hôm nay / mục tiêu, số thẻ cần ôn hôm nay, nút "Ôn tập", nút "Học… | Cao |
| TC-016.2 | AC-016.2 (US-016) | M | Given người dùng mới chưa học gì, Then nút "Học tiếp" thay bằng "Chọn chủ đề đầu tiên". | Cao |
| TC-017.1 | AC-017.1 (US-017) | M | When mở màn hình Tiến độ, Then thấy tổng số từ đã thuộc / tổng số từ; số từ theo trạng thái Mới/Đang học/Đã thuộc; tiến độ từng chủ đề; điểm quiz t… | Cao |
| TC-017.2 | AC-017.2 (US-017) | I | Các số liệu khớp với dữ liệu trong CSDL (người kiểm thử đối chiếu bằng truy vấn — BR-033). | Cao |
| TC-018.1 | AC-018.1 (US-018) | U | Given hôm qua có hoạt động học, When hoàn thành hoạt động học đầu tiên hôm nay, Then chuỗi +1 (BR-031). | Cao |
| TC-018.2 | AC-018.2 (US-018) | U | Given hôm qua không có hoạt động học, When hoàn thành hoạt động học hôm nay, Then chuỗi = 1. | Cao |
| TC-018.3 | AC-018.3 (US-018) | U+M | Chuỗi dài nhất từng đạt được hiển thị cạnh chuỗi hiện tại. | Cao |
| TC-019.1 | AC-019.1 (US-019) | M | Given nhắc học bật và đã cấp quyền thông báo, When đến giờ nhắc (sai lệch cho phép ≤ 15 phút) và hôm nay chưa đạt mục tiêu, Then hiện thông báo "Đế… | Cao |
| TC-019.2 | AC-019.2 (US-019) | M | Given hôm nay đã đạt mục tiêu, Then không hiện thông báo. | Cao |
| TC-019.3 | AC-019.3 (US-019) | M | Given máy khởi động lại, Then lịch nhắc vẫn còn hiệu lực. | Cao |
| TC-019.4 | AC-019.4 (US-019) | M | Given quyền thông báo bị thu hồi, When mở Cài đặt, Then công tắc nhắc học hiện trạng thái tắt kèm nút "Cấp quyền". | Cao |
| TC-020.1 | AC-020.1 (US-020) | M | When mở tab Luyện tập → Ngữ pháp, Then thấy ≥ 8 bài, mỗi bài có tên, cấp độ, trạng thái đã đọc/chưa đọc. | Cao |
| TC-020.2 | AC-020.2 (US-020) | M | When mở một bài, Then thấy công thức, cách dùng, ví dụ (có nút phát âm), lỗi thường gặp; đọc đến cuối bài thì bài được đánh dấu "đã đọc". | Cao |
| TC-021.1 | AC-021.1 (US-021) | M | Given bài có ≥ 5 câu hỏi (BR-056), When bấm "Làm bài tập", Then làm 5–10 câu trắc nghiệm (loại GRAMMAR), mỗi câu sai có giải thích; kết quả lưu vào lịch sử quiz (BR-023). Bài có < 5 câu thì ẩn nút "Làm bài tập". | Cao |
| TC-022.1 | AC-022.1 (US-022) | I+M | When gõ ≥ 1 ký tự vào ô tìm kiếm, Then sau ≤ 300 ms hiện kết quả khớp tiền tố từ tiếng Anh hoặc chứa chuỗi trong nghĩa tiếng Việt, không phân biệt… | Cao |
| TC-022.2 | AC-022.2 (US-022) | M | Given không có kết quả, Then hiện "Không tìm thấy “[từ khóa đã gõ]”" và nút "Thêm vào sổ tay" điền sẵn từ khóa. | Cao |
| TC-023.1 | AC-023.1 (US-023) | M | When nhập từ (bắt buộc), nghĩa (bắt buộc), phiên âm, ví dụ, ghi chú (tùy chọn) và lưu, Then từ xuất hiện đầu danh sách Sổ tay. | Cao |
| TC-023.2 | AC-023.2 (US-023) | I+M | Given từ đã có trong sổ tay của chính người dùng, When lưu, Then báo "Từ này đã có trong sổ tay" và mở mục đã có. | Cao |
| TC-023.3 | AC-023.3 (US-023) | M | When xóa một từ và xác nhận, Then từ biến mất khỏi sổ tay và kết quả tra. | Cao |
| TC-024.1 | AC-024.1 (US-024) | M | Given sổ tay có ≥ 1 từ, When bấm "Học sổ tay", Then mở phiên flashcard như US-009, tiến độ thẻ áp dụng Leitner như từ seed. | Cao |
| TC-025.1 | AC-025.1 (US-025) | I+M | Given cài app lần đầu, When mở app, Then CSDL được tạo và nạp 15 chủ đề, 450 từ (30 từ/chủ đề, mỗi từ 1 câu ví dụ Anh–Việt), 10 bài ngữ pháp, 100 c… | Cao |
| TC-025.2 | AC-025.2 (US-025) | I | Given nạp seed lỗi giữa chừng, Then không để CSDL dở dang (giao dịch rollback) và hiện "Không thể khởi tạo dữ liệu, vui lòng mở lại app". | Cao |
| TC-026.1 | AC-026.1 (US-026) | M | When mở Tôi → Giới thiệu, Then thấy tên app, phiên bản, 4 thành viên, môn học, danh sách nguồn dữ liệu và giấy phép. | Trung bình |
| TC-027.1 | AC-027.1 (US-027) | M | Given ở màn hình Đăng nhập, When bấm "Quên mật khẩu?" và nhập tên đăng nhập có tồn tại, Then hiện câu hỏi bảo mật của tài khoản đó. | Cao |
| TC-027.2 | AC-027.2 (US-027) | U+M | Given trả lời đúng (so sánh sau chuẩn hóa — BR-007), When nhập mật khẩu mới hợp lệ + xác nhận, Then mật khẩu được đổi, bộ đếm sai đăng nhập về 0, q… | Cao |
| TC-027.3 | AC-027.3 (US-027) | U+M | Given trả lời sai, Then báo "Câu trả lời không đúng"; sai 3 lần liên tiếp thì khóa đặt lại mật khẩu 5 phút (BR-008). Given tên đăng nhập không tồn… | Cao |
| TC-028.1 | AC-028.1 (US-028) | M | Given ở tab Học → "Bộ của tôi", When bấm "Tạo bộ", nhập tên (1–50 ký tự), mô tả (tùy chọn), chọn ngôn ngữ (mặc định Tiếng Anh) và lưu, Then bộ mới… | Cao |
| TC-028.2 | AC-028.2 (US-028) | I+M | Given đã có bộ cùng tên (không phân biệt hoa thường) của chính người dùng, When lưu, Then báo "Bạn đã có bộ tên này" và không tạo (BR-044). | Cao |
| TC-028.3 | AC-028.3 (US-028) | I+M | Given vừa đăng ký, When mở "Bộ của tôi", Then đã có sẵn bộ "Sổ tay của tôi" (BR-046). | Cao |
| TC-029.1 | AC-029.1 (US-029) | M | When đổi tên hợp lệ và lưu, Then tên mới hiển thị ở mọi nơi. | Cao |
| TC-029.2 | AC-029.2 (US-029) | I+M | When chọn "Xóa bộ" và xác nhận, Then bộ, các từ trong bộ và tiến độ học của các từ đó bị xóa (BR-045); bộ "Sổ tay của tôi" không có nút xóa. | Cao |
| TC-029.3 | AC-029.3 (US-029) | M | Given bộ có sẵn (15 chủ đề, bộ luyện thi), Then không có nút sửa/xóa (BR-043). | Cao |
| TC-030.1 | AC-030.1 (US-030) | M | When mở bộ → "Thêm từ", nhập từ và nghĩa (bắt buộc), cách đọc, ví dụ, bản dịch ví dụ (tùy chọn) rồi lưu, Then từ xuất hiện trong bộ; số từ của bộ t… | Cao |
| TC-030.2 | AC-030.2 (US-030) | I+M | Given từ đã có trong bộ, When lưu, Then báo "Từ này đã có trong bộ" (BR-047). | Cao |
| TC-030.3 | AC-030.3 (US-030) | M | When sửa hoặc xóa (có xác nhận) một từ, Then thay đổi hiện ngay; xóa từ thì xóa luôn tiến độ học của từ. | Cao |
| TC-030.4 | AC-030.4 (US-030) | I+M | Given đang xem một từ có sẵn (Chi tiết từ), When bấm "Lưu vào bộ", chọn một bộ của tôi, Then từ được chép vào bộ đó (dữ liệu có sẵn không bị sửa). | Cao |
| TC-031.1 | AC-031.1 (US-031) | I+M | Given bộ có ≥ 1 từ, When bấm "Học", Then mở phiên flashcard như US-009 với các từ của bộ; tiến độ theo Leitner (BR-011, BR-012) và thẻ đến hạn xuất… | Cao |
| TC-031.2 | AC-031.2 (US-031) | M | Given bộ có ≥ 4 từ, When bấm "Làm quiz", Then tạo quiz như US-013 từ các từ của bộ. | Cao |
| TC-032.1 | AC-032.1 (US-032) | U+M | Given có file CSV hoặc JSON đúng định dạng (BR-048, BR-049), When chọn "Import" → chọn file bằng trình chọn file của Android, Then hiện màn hình xe… | Cao |
| TC-032.2 | AC-032.2 (US-032) | I+M | When chọn bộ đích (bộ mới hoặc bộ có sẵn của tôi) và bấm "Nhập", Then các dòng hợp lệ được thêm trong một giao dịch ≤ 3 giây với 2000 dòng, dòng tr… | Cao |
| TC-032.3 | AC-032.3 (US-032) | U+M | Given file > 1 MB, > 2000 dòng, sai định dạng hoặc không có dòng hợp lệ, Then không nhập gì và báo lý do cụ thể. | Cao |
| TC-033.1 | AC-033.1 (US-033) | I+M | Given một bộ bất kỳ có ≥ 1 từ, When chọn "Export", chọn CSV hoặc JSON và nơi lưu, Then file `<tên bộ>_<yyyyMMdd>.csv/json` được ghi, mở lại bằng Im… | Cao |
| TC-033.2 | AC-033.2 (US-033) | M | Given người dùng hủy chọn nơi lưu hoặc ghi file lỗi, Then không có file dở dang và báo "Không thể xuất file". | Cao |
| TC-034.1 | AC-034.1 (US-034) | M | Given tạo bộ mới, When chọn ngôn ngữ Tiếng Nhật / Tiếng Trung / Tiếng Hàn / Tiếng Anh, Then nhãn trường "cách đọc" đổi theo ngôn ngữ (BR-052) và ph… | Cao |
| TC-034.2 | AC-034.2 (US-034) | I+M | Given cài app lần đầu, Then có sẵn bộ mẫu "Tiếng Nhật cơ bản" 20 từ (từ, kana/romaji, nghĩa, ví dụ) học được bằng flashcard và quiz như bộ tiếng Anh. | Cao |
| TC-034.3 | AC-034.3 (US-034) | M | Thêm một ngôn ngữ mới chỉ cần thêm 1 dòng vào dữ liệu `languages` và (tùy chọn) 1 file seed — không sửa mã Java (kiểm chứng trong TESTING-STRATEGY TC-NFR-… | Cao |
| TC-034.4 | AC-034.4 (US-034) | M | Given máy thiếu giọng đọc của ngôn ngữ đó, Then áp dụng như AC-010.2 và quiz không sinh câu dạng nghe. | Cao |
| TC-035.1 | AC-035.1 (US-035) | M | When mở tab Luyện tập → "Luyện thi", Then thấy 3 kỳ thi TOEIC, IELTS, Aptis, mỗi kỳ thi có bộ từ riêng (TOEIC 60 từ, IELTS 60 từ, Aptis 40 từ) học… | Cao |
| TC-036.1 | AC-036.1 (US-036) | M | When chọn một đề thi thử (ví dụ "TOEIC Reading Part 5 — Đề 1: 30 câu, 20 phút"), Then bắt đầu bài có đồng hồ đếm ngược, thanh tiến độ câu, cho chuy… | Cao |
| TC-036.2 | AC-036.2 (US-036) | U+M | When bấm "Nộp bài" (có xác nhận nếu còn câu chưa làm) hoặc hết giờ (tự nộp), Then hiện điểm = số câu đúng/tổng × 100, thời gian làm, từng câu với đ… | Cao |
| TC-036.3 | AC-036.3 (US-036) | I+M | Then lượt thi được lưu vào lịch sử thi thử; từ vựng gắn với câu sai được đưa về hộp 1 để ôn (BR-055). | Cao |
| TC-036.4 | AC-036.4 (US-036) | I+M | Given thoát app giữa bài, When mở lại trong thời gian còn lại, Then tiếp tục đúng câu và đồng hồ; quá giờ thì bài được tự nộp với các câu đã trả lời. | Cao |
| TC-037.1 | AC-037.1 (US-037) | M | When mở "Luyện thi" → "Tài nguyên chính thức", Then thấy danh sách liên kết tới trang chính thức của TOEIC (ETS), IELTS và British Council Aptis kè… | Cao |
| TC-037.2 | AC-037.2 (US-037) | M | Given máy không có trình duyệt, Then báo "Không tìm thấy trình duyệt để mở liên kết". | Cao |

## Ma trận truy vết
> Truy vết từ yêu cầu đến user story, acceptance criteria, test case và task: không yêu cầu nào không có test, không test nào không có yêu cầu.
> Bảng sinh từ PRD (đã gồm thay đổi phạm vi, thay đổi phạm vi); mỗi AC có đúng một test case cùng số (`AC-009.3` → `TC-009.3`). Nội dung từng TC và mức kiểm thử: TESTING-STRATEGY §Truy vết. người kiểm thử cập nhật cột Trạng thái khi chạy (Chưa chạy / Đạt / Lỗi #id).

## 1. Ma trận
Quy ước ID: `REQ-xxx` (yêu cầu) → `US-xxx` (user story, trong PRD) → `AC-xxx.n` (tiêu chí chấp nhận) → `TC-xxx` (test case) → `T-xxx` (task trong PROJECT-PLAN (công việc)).
Mỗi `US-xxx` trong PRD phải xuất hiện ít nhất một lần trong bảng (lint kiểm tra).

| REQ | US | AC | BR (SRS) | FLOW | TC | Task | Trạng thái |
|---|---|---|---|---|---|---|---|
| REQ-001 | US-001 | AC-001.1 | BR-001, BR-002, BR-004, BR-009, BR-046 | FLOW-001 | TC-001.1 | T-006, T-020, T-021 | Chưa chạy |
| REQ-001 | US-001 | AC-001.2 | BR-001, BR-002, BR-004, BR-009, BR-046 | FLOW-001 | TC-001.2 | T-006, T-020, T-021 | Chưa chạy |
| REQ-001 | US-001 | AC-001.3 | BR-001, BR-002, BR-004, BR-009, BR-046 | FLOW-001 | TC-001.3 | T-006, T-020, T-021 | Chưa chạy |
| REQ-001 | US-001 | AC-001.4 | BR-001, BR-002, BR-004, BR-009, BR-046 | FLOW-001 | TC-001.4 | T-006, T-020, T-021 | Chưa chạy |
| REQ-001 | US-002 | AC-002.1 | BR-002, BR-003, BR-004 | FLOW-001 | TC-002.1 | T-020, T-021 | Chưa chạy |
| REQ-001 | US-002 | AC-002.2 | BR-002, BR-003, BR-004 | FLOW-001 | TC-002.2 | T-020, T-021 | Chưa chạy |
| REQ-001 | US-002 | AC-002.3 | BR-002, BR-003, BR-004 | FLOW-001 | TC-002.3 | T-020, T-021 | Chưa chạy |
| REQ-001 | US-002 | AC-002.4 | BR-002, BR-003, BR-004 | FLOW-001 | TC-002.4 | T-020, T-021 | Chưa chạy |
| REQ-001 | US-003 | AC-003.1 | BR-030 | FLOW-009 | TC-003.1 | T-024, T-052 | Chưa chạy |
| REQ-001 | US-003 | AC-003.2 | BR-030 | FLOW-009 | TC-003.2 | T-024, T-052 | Chưa chạy |
| REQ-001 | US-004 | AC-004.1 | BR-002 | FLOW-009 | TC-004.1 | T-024 | Chưa chạy |
| REQ-001 | US-004 | AC-004.2 | BR-002 | FLOW-009 | TC-004.2 | T-024 | Chưa chạy |
| REQ-001 | US-005 | AC-005.1 | BR-005 | FLOW-009 | TC-005.1 | T-024, T-080 | Chưa chạy |
| REQ-001 | US-005 | AC-005.2 | BR-005 | FLOW-009 | TC-005.2 | T-024, T-080 | Chưa chạy |
| REQ-002 | US-006 | AC-006.1 | BR-006, BR-030 | FLOW-002 | TC-006.1 | T-022 | Chưa chạy |
| REQ-002 | US-006 | AC-006.2 | BR-006, BR-030 | FLOW-002 | TC-006.2 | T-022 | Chưa chạy |
| REQ-002 | US-006 | AC-006.3 | BR-006, BR-030 | FLOW-002 | TC-006.3 | T-022 | Chưa chạy |
| REQ-003 | US-007 | AC-007.1 | BR-014, BR-043 | FLOW-003 | TC-007.1 | T-030 | Chưa chạy |
| REQ-003 | US-007 | AC-007.2 | BR-014, BR-043 | FLOW-003 | TC-007.2 | T-030 | Chưa chạy |
| REQ-003 | US-008 | AC-008.1 | BR-014 | FLOW-003 | TC-008.1 | T-030, T-031 | Chưa chạy |
| REQ-003 | US-008 | AC-008.2 | BR-014 | FLOW-003 | TC-008.2 | T-030, T-031 | Chưa chạy |
| REQ-003 | US-009 | AC-009.1 | BR-010, BR-011, BR-012, BR-015 | FLOW-004, FLOW-012 | TC-009.1 | T-007, T-032 | Chưa chạy |
| REQ-003 | US-009 | AC-009.2 | BR-010, BR-011, BR-012, BR-015 | FLOW-004, FLOW-012 | TC-009.2 | T-007, T-032 | Chưa chạy |
| REQ-003 | US-009 | AC-009.3 | BR-010, BR-011, BR-012, BR-015 | FLOW-004, FLOW-012 | TC-009.3 | T-007, T-032 | Chưa chạy |
| REQ-003 | US-009 | AC-009.4 | BR-010, BR-011, BR-012, BR-015 | FLOW-004, FLOW-012 | TC-009.4 | T-007, T-032 | Chưa chạy |
| REQ-003 | US-009 | AC-009.5 | BR-010, BR-011, BR-012, BR-015 | FLOW-004, FLOW-012 | TC-009.5 | T-007, T-032 | Chưa chạy |
| REQ-003 | US-010 | AC-010.1 | BR-052 | FLOW-003 | TC-010.1 | T-031, T-091 | Chưa chạy |
| REQ-003 | US-010 | AC-010.2 | BR-052 | FLOW-003 | TC-010.2 | T-031, T-091 | Chưa chạy |
| REQ-003 | US-011 | AC-011.1 | — | FLOW-003 | TC-011.1 | T-031, T-061 | Chưa chạy |
| REQ-003 | US-011 | AC-011.2 | — | FLOW-003 | TC-011.2 | T-031, T-061 | Chưa chạy |
| REQ-004 | US-012 | AC-012.1 | BR-011, BR-012, BR-013 | FLOW-004 | TC-012.1 | T-007, T-033 | Chưa chạy |
| REQ-004 | US-012 | AC-012.2 | BR-011, BR-012, BR-013 | FLOW-004 | TC-012.2 | T-007, T-033 | Chưa chạy |
| REQ-004 | US-012 | AC-012.3 | BR-011, BR-012, BR-013 | FLOW-004 | TC-012.3 | T-007, T-033 | Chưa chạy |
| REQ-005 | US-013 | AC-013.1 | BR-020, BR-022, BR-024, BR-025 | FLOW-005 | TC-013.1 | T-008, T-040 | Chưa chạy |
| REQ-005 | US-013 | AC-013.2 | BR-020, BR-022, BR-024, BR-025 | FLOW-005 | TC-013.2 | T-008, T-040 | Chưa chạy |
| REQ-005 | US-013 | AC-013.3 | BR-020, BR-022, BR-024, BR-025 | FLOW-005 | TC-013.3 | T-008, T-040 | Chưa chạy |
| REQ-005 | US-013 | AC-013.4 | BR-020, BR-022, BR-024, BR-025 | FLOW-005 | TC-013.4 | T-008, T-040 | Chưa chạy |
| REQ-005 | US-014 | AC-014.1 | BR-021, BR-023 | FLOW-005 | TC-014.1 | T-008, T-041 | Chưa chạy |
| REQ-005 | US-014 | AC-014.2 | BR-021, BR-023 | FLOW-005 | TC-014.2 | T-008, T-041 | Chưa chạy |
| REQ-005 | US-015 | AC-015.1 | — | FLOW-005 | TC-015.1 | T-041 | Chưa chạy |
| REQ-005 | US-015 | AC-015.2 | — | FLOW-005 | TC-015.2 | T-041 | Chưa chạy |
| REQ-006 | US-016 | AC-016.1 | BR-032, BR-033 | FLOW-008 | TC-016.1 | T-050 | Chưa chạy |
| REQ-006 | US-016 | AC-016.2 | BR-032, BR-033 | FLOW-008 | TC-016.2 | T-050 | Chưa chạy |
| REQ-006 | US-017 | AC-017.1 | BR-014, BR-033 | FLOW-008 | TC-017.1 | T-051 | Chưa chạy |
| REQ-006 | US-017 | AC-017.2 | BR-014, BR-033 | FLOW-008 | TC-017.2 | T-051 | Chưa chạy |
| REQ-006 | US-018 | AC-018.1 | BR-031, BR-032, BR-042 | FLOW-008 | TC-018.1 | T-007, T-050 | Chưa chạy |
| REQ-006 | US-018 | AC-018.2 | BR-031, BR-032, BR-042 | FLOW-008 | TC-018.2 | T-007, T-050 | Chưa chạy |
| REQ-006 | US-018 | AC-018.3 | BR-031, BR-032, BR-042 | FLOW-008 | TC-018.3 | T-007, T-050 | Chưa chạy |
| REQ-007 | US-019 | AC-019.1 | BR-030 | FLOW-007 | TC-019.1 | T-052 | Chưa chạy |
| REQ-007 | US-019 | AC-019.2 | BR-030 | FLOW-007 | TC-019.2 | T-052 | Chưa chạy |
| REQ-007 | US-019 | AC-019.3 | BR-030 | FLOW-007 | TC-019.3 | T-052 | Chưa chạy |
| REQ-007 | US-019 | AC-019.4 | BR-030 | FLOW-007 | TC-019.4 | T-052 | Chưa chạy |
| REQ-008 | US-020 | AC-020.1 | BR-032 | FLOW-006 | TC-020.1 | T-014, T-060 | Chưa chạy |
| REQ-008 | US-020 | AC-020.2 | BR-032 | FLOW-006 | TC-020.2 | T-014, T-060 | Chưa chạy |
| REQ-008 | US-021 | AC-021.1 | BR-022, BR-023, BR-056 | FLOW-006 | TC-021.1 | T-014, T-060 | Chưa chạy |
| REQ-009 | US-022 | AC-022.1 | BR-040, BR-053 | FLOW-011 | TC-022.1 | T-061, T-091 | Chưa chạy |
| REQ-009 | US-022 | AC-022.2 | BR-040, BR-053 | FLOW-011 | TC-022.2 | T-061, T-091 | Chưa chạy |
| REQ-010 | US-023 | AC-023.1 | BR-041, BR-046, BR-047 | FLOW-011, FLOW-014 | TC-023.1 | T-090 | Chưa chạy |
| REQ-010 | US-023 | AC-023.2 | BR-041, BR-046, BR-047 | FLOW-011, FLOW-014 | TC-023.2 | T-090 | Chưa chạy |
| REQ-010 | US-023 | AC-023.3 | BR-041, BR-046, BR-047 | FLOW-011, FLOW-014 | TC-023.3 | T-090 | Chưa chạy |
| REQ-010 | US-024 | AC-024.1 | BR-011, BR-012, BR-015 | FLOW-004 | TC-024.1 | T-090 | Chưa chạy |
| REQ-011 | US-025 | AC-025.1 | — | FLOW-010 | TC-025.1 | T-003, T-004, T-010..T-014, T-096..T-099, T-080 | Chưa chạy |
| REQ-011 | US-025 | AC-025.2 | — | FLOW-010 | TC-025.2 | T-003, T-004, T-010..T-014, T-096..T-099, T-080 | Chưa chạy |
| REQ-012 | US-026 | AC-026.1 | — | — | TC-026.1 | T-063 | Chưa chạy |
| REQ-014 | US-027 | AC-027.1 | BR-007, BR-008 | FLOW-013 | TC-027.1 | T-006, T-023 | Chưa chạy |
| REQ-014 | US-027 | AC-027.2 | BR-007, BR-008 | FLOW-013 | TC-027.2 | T-006, T-023 | Chưa chạy |
| REQ-014 | US-027 | AC-027.3 | BR-007, BR-008 | FLOW-013 | TC-027.3 | T-006, T-023 | Chưa chạy |
| REQ-016 | US-028 | AC-028.1 | BR-043, BR-044, BR-046 | FLOW-014 | TC-028.1 | T-090 | Chưa chạy |
| REQ-016 | US-028 | AC-028.2 | BR-043, BR-044, BR-046 | FLOW-014 | TC-028.2 | T-090 | Chưa chạy |
| REQ-016 | US-028 | AC-028.3 | BR-043, BR-044, BR-046 | FLOW-014 | TC-028.3 | T-090 | Chưa chạy |
| REQ-016 | US-029 | AC-029.1 | BR-043, BR-044, BR-045 | FLOW-014 | TC-029.1 | T-090 | Chưa chạy |
| REQ-016 | US-029 | AC-029.2 | BR-043, BR-044, BR-045 | FLOW-014 | TC-029.2 | T-090 | Chưa chạy |
| REQ-016 | US-029 | AC-029.3 | BR-043, BR-044, BR-045 | FLOW-014 | TC-029.3 | T-090 | Chưa chạy |
| REQ-016 | US-030 | AC-030.1 | BR-047 | FLOW-014 | TC-030.1 | T-090 | Chưa chạy |
| REQ-016 | US-030 | AC-030.2 | BR-047 | FLOW-014 | TC-030.2 | T-090 | Chưa chạy |
| REQ-016 | US-030 | AC-030.3 | BR-047 | FLOW-014 | TC-030.3 | T-090 | Chưa chạy |
| REQ-016 | US-030 | AC-030.4 | BR-047 | FLOW-014 | TC-030.4 | T-090 | Chưa chạy |
| REQ-016 | US-031 | AC-031.1 | BR-011, BR-012, BR-020 | FLOW-004, FLOW-005 | TC-031.1 | T-090 | Chưa chạy |
| REQ-016 | US-031 | AC-031.2 | BR-011, BR-012, BR-020 | FLOW-004, FLOW-005 | TC-031.2 | T-090 | Chưa chạy |
| REQ-017 | US-032 | AC-032.1 | BR-048, BR-049, BR-050 | FLOW-015 | TC-032.1 | T-092, T-093 | Chưa chạy |
| REQ-017 | US-032 | AC-032.2 | BR-048, BR-049, BR-050 | FLOW-015 | TC-032.2 | T-092, T-093 | Chưa chạy |
| REQ-017 | US-032 | AC-032.3 | BR-048, BR-049, BR-050 | FLOW-015 | TC-032.3 | T-092, T-093 | Chưa chạy |
| REQ-017 | US-033 | AC-033.1 | BR-051 | FLOW-016 | TC-033.1 | T-092, T-093 | Chưa chạy |
| REQ-017 | US-033 | AC-033.2 | BR-051 | FLOW-016 | TC-033.2 | T-092, T-093 | Chưa chạy |
| REQ-018 | US-034 | AC-034.1 | BR-052, BR-053 | FLOW-014 | TC-034.1 | T-091, T-099 | Chưa chạy |
| REQ-018 | US-034 | AC-034.2 | BR-052, BR-053 | FLOW-014 | TC-034.2 | T-091, T-099 | Chưa chạy |
| REQ-018 | US-034 | AC-034.3 | BR-052, BR-053 | FLOW-014 | TC-034.3 | T-091, T-099 | Chưa chạy |
| REQ-018 | US-034 | AC-034.4 | BR-052, BR-053 | FLOW-014 | TC-034.4 | T-091, T-099 | Chưa chạy |
| REQ-019 | US-035 | AC-035.1 | BR-043 | FLOW-017 | TC-035.1 | T-095, T-096..T-098 | Chưa chạy |
| REQ-019 | US-036 | AC-036.1 | BR-054, BR-055 | FLOW-017 | TC-036.1 | T-094, T-095, T-096, T-097 | Chưa chạy |
| REQ-019 | US-036 | AC-036.2 | BR-054, BR-055 | FLOW-017 | TC-036.2 | T-094, T-095, T-096, T-097 | Chưa chạy |
| REQ-019 | US-036 | AC-036.3 | BR-054, BR-055 | FLOW-017 | TC-036.3 | T-094, T-095, T-096, T-097 | Chưa chạy |
| REQ-019 | US-036 | AC-036.4 | BR-054, BR-055 | FLOW-017 | TC-036.4 | T-094, T-095, T-096, T-097 | Chưa chạy |
| REQ-019 | US-037 | AC-037.1 | — | FLOW-017 | TC-037.1 | T-095 | Chưa chạy |
| REQ-019 | US-037 | AC-037.2 | — | FLOW-017 | TC-037.2 | T-095 | Chưa chạy |

Tổng: 37 user story, 95 AC, 95 test case.

## 2. Khoảng trống phát hiện
| Loại | ID | Cách xử lý |
|---|---|---|
| Yêu cầu không có US | REQ-013 Dark mode (Should) | Kiểm thử bằng TC-NFR-X1-DARK trong TESTING-STRATEGY, task T-070 |
| Yêu cầu đã hủy | REQ-015 Widget (Won't ) | Không kiểm thử |
| US không có BR | US-011, US-015, US-025, US-026, US-037 | Hành vi đơn giản, đủ mô tả bằng AC |
| US không có FLOW | US-026 Giới thiệu | Màn hình tĩnh |
| Mục rubric | 10 mục chức năng của "File báo cáo kết quả dự án" | Cơ bản → US-028..US-031, US-009, US-012; nâng cao → US-001..US-005, US-032/033, US-012 (Leitner), US-016..US-018, US-019, US-034, US-035..US-037 |
| SRS (phi chức năng) không gắn AC | NFR-P1..NFR-Q1 | Kiểm thử bằng TC-NFR-* trong TESTING-STRATEGY §Chiến lược |
