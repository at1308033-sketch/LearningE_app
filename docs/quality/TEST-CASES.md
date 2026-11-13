# Test Cases — danh sách kịch bản kiểm thử

> **TỰ SINH** từ bộ phân tích thiết kế đã duyệt của nhóm — không sửa trực tiếp tệp này; đề xuất thay đổi qua Issue/PR để nhóm trưởng cập nhật nguồn và sinh lại. Danh mục tài liệu: [docs/README.md](../README.md).

**Mục đích:** mỗi tiêu chí chấp nhận có một test case TC-xxx (mức kiểm thử, kịch bản rút gọn, ưu tiên) và ma trận truy vết REQ → US → AC → BR → FLOW → TC → Task.

## Truy vết AC -> test case
(Xem TEST-CASES và TEST_CASES; AC chưa có TC ghi ở "Khoảng trống".) Mức: **U** unit · **SV** integration/hợp đồng server (TC-SRV, TC-RPC) · **I** instrumented · **SY** đồng bộ (1–2 máy) · **M** thủ công. Ưu tiên Cao = US Must, Trung bình = US Should, Could = chỉ khi làm, Won't = không chạy. Mỗi AC có đúng một TC cùng số (`AC-009.3` → `TC-009.3`); TC chạy theo đúng Given/When/Then của AC; bước chi tiết và bằng chứng (ảnh chụp) ghi trong báo cáo kiểm thử. **thay đổi phạm vi không đổi AC**: kỳ vọng lỗi trong cột "Kịch bản" đã đổi từ mã HTTP sang (mã gRPC · `error-code`); kịch bản phía server xem TC-SRV/TC-RPC. Bảng nối REQ/BR/FLOW/Task đầy đủ: TEST-CASES.

| TC | AC | Mức | Kịch bản rút gọn (bước → kết quả mong đợi theo AC) | Ưu tiên |
|---|---|---|---|---|
| TC-001.1 | AC-001.1 (US-001) | SV+M | Có mạng; email, biệt danh, mật khẩu + xác nhận hợp lệ → Đăng ký: `OK` + `AuthSession`, bảng `users` chỉ có hash + salt (không có mật khẩu rõ), app lưu token (SharedPreferences riêng tư) và không lưu mật khẩu, vào Thiết lập lần đầu; bộ "Sổ tay của tôi" được tạo. | Cao |
| TC-001.2 | AC-001.2 (US-001) | SV+M | Đăng ký "Minh@Gmail.com" khi "minh@gmail.com" đã có → `ALREADY_EXISTS` · `EMAIL_TAKEN`; app hiện "Email đã được sử dụng" dưới trường email, không tạo tài khoản. | Cao |
| TC-001.3 | AC-001.3 (US-001) | U+SV+M | Từng lỗi quy tắc (email thiếu `@`, mật khẩu 7 ký tự / không có số, biệt danh 2 hoặc 21 ký tự / có khoảng trắng): app báo đúng trường trước khi gửi; gửi thẳng RPC cùng dữ liệu → `INVALID_ARGUMENT` · `VALIDATION_ERROR` kèm `error-fields`; giữ dữ liệu đã nhập (trừ mật khẩu). | Cao |
| TC-001.4 | AC-001.4 (US-001) | U+SV+M | Chọn 1 trong 5 câu hỏi + trả lời 2–50 ký tự; " Hà Nội " và "ha noi" cho cùng băm (chỉ trên server, DB không có chữ rõ); thiếu câu hỏi hoặc trả lời 1 / 51 ký tự → không đăng ký. | Cao |
| TC-001.5 | AC-001.5 (US-001) | M | Ngắt server hoặc bật chế độ máy bay rồi Đăng ký → trong ≤ 26 s hiện "Không kết nối được máy chủ, vui lòng thử lại"; giữ dữ liệu đã nhập (trừ mật khẩu); server không có tài khoản dở dang. | Cao |
| TC-002.1 | AC-002.1 (US-002) | SV+M | Đúng email + mật khẩu, có mạng → `OK` kèm token hạn 30 ngày, vào Trang chủ; tắt mạng, đóng rồi mở lại app → vào Trang chủ ngay không cần mạng (NFR-N1). | Cao |
| TC-002.2 | AC-002.2 (US-002) | SV+M | Sai mật khẩu và email không tồn tại → cùng thông báo "Email hoặc mật khẩu không đúng" (`UNAUTHENTICATED` · `INVALID_CREDENTIALS`); thời gian phản hồi hai trường hợp chênh < 30%. | Cao |
| TC-002.3 | AC-002.3 (US-002) | U+SV+M | Sai mật khẩu lần 1–4 → `UNAUTHENTICATED` · `INVALID_CREDENTIALS`; lần 5 → `RESOURCE_EXHAUSTED` · `ACCOUNT_LOCKED` + `retry-after-seconds: 60`, app hiện "thử lại sau x giây"; đúng mật khẩu trong lúc khóa vẫn bị từ chối; email khác không bị khóa; khóa lũy tiến xem TC-002.8, TC-SRV-05, TC-RPC-04. | Cao |
| TC-002.4 | AC-002.4 (US-002) | SY+M | 0 bản ghi chờ: xác nhận → token bị thu hồi (gọi RPC cũ → `UNAUTHENTICATED`), về SCR-02, mở lại vẫn ở SCR-02. Còn 7 bản ghi chờ + có mạng: đẩy xong mới đăng xuất. Mất mạng: hộp cảnh báo, mặc định "Ở lại" (BR-073). | Cao |
| TC-002.5 | AC-002.5 (US-002) | SY | Máy A đã đồng bộ, đăng nhập cùng tài khoản trên máy B → dữ liệu máy A về máy B (kịch bản chi tiết ở TC-038.1). | Cao |
| TC-002.6 | AC-002.6 (US-002) | SV+SY+M | Thu hồi token (hoặc đặt `expires_at` quá khứ) rồi gọi RPC bất kỳ → `UNAUTHENTICATED`; app về SCR-02 với "Phiên đăng nhập đã hết hạn", điền sẵn email, giữ nguyên bản ghi chưa đẩy; đăng nhập lại cùng tài khoản → đẩy tiếp (BR-059). | Cao |
| TC-002.7 | AC-002.7 (US-002) | M | Máy chưa từng đăng nhập + chế độ máy bay, bấm Đăng nhập → "Cần kết nối mạng để đăng nhập lần đầu". | Cao |
| TC-002.8 | AC-002.8 (US-002) | U+SV+M | Email bị khóa 60 s; hết khóa, sai tiếp 5 lần liên tiếp → khóa lần 2 dài 2 phút (rồi 4, 8, 16, 32 phút, trần 1 giờ); lần thử trong lúc khóa bị từ chối và không tính thêm; đăng nhập đúng hoặc đặt lại mật khẩu thành công → đếm sai và bậc khóa về 0; app hiện "thử lại sau x giây" đúng số giây còn lại (BR-003). | Cao |
| TC-003.1 | AC-003.1 (US-003) | M | Mở tab Tôi → thấy email, biệt danh, cấp độ, mục tiêu hằng ngày, giờ nhắc, công tắc nhắc học, lối vào Trạng thái đồng bộ (SCR-35). | Cao |
| TC-003.2 | AC-003.2 (US-003) | SY+M | Đổi cấp độ/mục tiêu khi mất mạng → có hiệu lực ngay trên máy; có mạng lại → đồng bộ lên server và hiện ở máy thứ hai; đổi giờ nhắc đặt lại lịch (BR-030, giờ nhắc riêng từng máy). | Cao |
| TC-004.1 | AC-004.1 (US-004) | SV+M | Mật khẩu hiện tại đúng + mới hợp lệ khác cũ → máy gọi nhận token mới, token ở máy khác → `UNAUTHENTICATED`; đăng nhập bằng mật khẩu cũ thất bại, bằng mật khẩu mới thành công. | Cao |
| TC-004.2 | AC-004.2 (US-004) | SV+M | Given mật khẩu hiện tại sai, When lưu, Then báo "Mật khẩu hiện tại không đúng", không đổi gì. | Cao |
| TC-004.3 | AC-004.3 (US-004) | M | Chế độ máy bay, bấm Lưu → "Cần kết nối mạng để đổi mật khẩu"; không đổi gì. | Cao |
| TC-005.1 | AC-005.1 (US-005) | SV+I+M | Có mạng, nhập lại mật khẩu đúng + xác nhận → `OK`; đếm dòng mọi bảng server có cột `user_id` (lấy từ `sqlite_master`) = 0 và các bảng CSDL máy theo user_id = 0; token cũ → `UNAUTHENTICATED`; biến mất khỏi bảng xếp hạng ≤ 60 s (outbox `user.deleted` tới 3 dịch vụ); hủy lịch nhắc, về SCR-02; seed và người dùng khác nguyên vẹn. | Cao |
| TC-005.2 | AC-005.2 (US-005) | M | Given bấm Hủy ở hộp xác nhận, Then không xóa gì. | Cao |
| TC-005.3 | AC-005.3 (US-005) | M | Chế độ máy bay, chọn Xóa tài khoản → "Cần kết nối mạng để xóa tài khoản"; không xóa gì ở cả máy và server. | Cao |
| TC-006.1 | AC-006.1 (US-006) | M | Given vừa đăng ký, When hoàn thành 3 bước (cấp độ A1/A2/B1; mục tiêu 5/10/15/20 từ mỗi ngày; giờ nhắc), Then lưu cài đặt và vào Trang chủ. | Cao |
| TC-006.2 | AC-006.2 (US-006) | M | Given ở bước giờ nhắc trên Android 13+, When bật nhắc học, Then app xin quyền thông báo; nếu bị từ chối thì vẫn vào Trang chủ, nhắc học ở trạng thái tắt và có hướng dẫn bật lại trong Cài đặt. | Cao |
| TC-006.3 | AC-006.3 (US-006) | M | Given bấm "Bỏ qua", Then dùng mặc định: A1, 10 từ/ngày, nhắc 20:00 (BR-006). | Cao |
| TC-007.1 | AC-007.1 (US-007) | M | Given đã có dữ liệu seed, When mở tab Học, Then thấy ≥10 chủ đề, mỗi chủ đề hiện tên, icon, cấp độ, tiến độ "đã thuộc x/y từ". | Cao |
| TC-007.2 | AC-007.2 (US-007) | M | When lọc theo cấp độ (Tất cả/A1/A2/B1), Then chỉ hiện chủ đề đúng cấp độ; mặc định lọc theo cấp độ trong cài đặt. | Cao |
| TC-008.1 | AC-008.1 (US-008) | M | When mở một chủ đề, Then thấy danh sách từ với từ, phiên âm, nghĩa và nhãn trạng thái (Mới/Đang học/Đã thuộc). | Cao |
| TC-008.2 | AC-008.2 (US-008) | M | When chạm một từ, Then mở Chi tiết từ: từ, phiên âm IPA, từ loại, nghĩa, câu ví dụ Anh + Việt, nút phát âm, nút yêu thích, nút thêm vào sổ tay. | Cao |
| TC-009.1 | AC-009.1 (US-009) | U+M | Given chủ đề còn từ Mới, When bấm "Học từ mới", Then mở phiên gồm tối đa N thẻ (N = số từ mục tiêu còn lại trong ngày, tối thiểu 5 — BR-010). | Cao |
| TC-009.2 | AC-009.2 (US-009) | M | When chạm thẻ, Then lật thẻ (mặt trước: từ + phiên âm; mặt sau: nghĩa + ví dụ) có hiệu ứng ≤ 400 ms. | Cao |
| TC-009.3 | AC-009.3 (US-009) | U+M | When chọn "Đã nhớ" hoặc "Chưa nhớ", Then thẻ vào hộp Leitner theo BR-011 và hiện thẻ kế tiếp; hết phiên hiện màn hình Kết quả phiên (số từ đã nhớ/chưa nhớ). | Cao |
| TC-009.4 | AC-009.4 (US-009) | M | Given chủ đề không còn từ Mới, Then nút "Học từ mới" bị ẩn và hiện "Bạn đã học hết từ mới của chủ đề này", gợi ý Ôn tập hoặc Quiz. | Cao |
| TC-009.5 | AC-009.5 (US-009) | I+M | Given thoát giữa phiên, Then các thẻ đã đánh giá được lưu; thẻ chưa đánh giá vẫn là Mới. | Cao |
| TC-010.1 | AC-010.1 (US-010) | M | Given máy có giọng tiếng Anh cho TextToSpeech, When bấm nút loa, Then đọc từ (hoặc câu ví dụ) bằng giọng en-US trong ≤ 1 giây. | Cao |
| TC-010.2 | AC-010.2 (US-010) | M | Given máy thiếu dữ liệu giọng hoặc không hỗ trợ, When bấm nút loa, Then hiện "Máy chưa có giọng đọc tiếng Anh" kèm nút mở cài đặt giọng đọc; app không bị dừng. | Cao |
| TC-011.1 | AC-011.1 (US-011) | M | When bấm biểu tượng yêu thích ở Chi tiết từ hoặc danh sách từ, Then trạng thái đổi ngay và từ xuất hiện trong mục "Yêu thích" ở Bộ của tôi (SCR-26 → SCR-19 lọc Yêu thích — PQ-7, FN-005). | Cao |
| TC-011.2 | AC-011.2 (US-011) | M | When bấm lại, Then bỏ yêu thích. | Cao |
| TC-012.1 | AC-012.1 (US-012) | I+M | Given có thẻ với ngày ôn tiếp ≤ hôm nay, When bấm "Ôn tập" ở Trang chủ, Then mở phiên flashcard gồm các thẻ đến hạn, sắp theo ngày ôn sớm nhất trước, tối đa 50 thẻ/phiên (BR-013). | Cao |
| TC-012.2 | AC-012.2 (US-012) | U | When chọn "Đã nhớ", Then thẻ lên 1 hộp và ngày ôn tiếp = hôm nay + khoảng của hộp mới (1/2/4/7/14 ngày); When chọn "Chưa nhớ", Then thẻ về hộp 1, ôn lại ngày mai (BR-011, BR-012). | Cao |
| TC-012.3 | AC-012.3 (US-012) | M | Given không có thẻ đến hạn, Then nút Ôn tập hiện "Hôm nay không có thẻ cần ôn" và bị vô hiệu. | Cao |
| TC-013.1 | AC-013.1 (US-013) | U+M | Given chọn một chủ đề có ≥ 4 từ, When bấm "Bắt đầu quiz", Then tạo bài 10 câu ngẫu nhiên trộn 5 dạng: trắc nghiệm Anh→Việt, trắc nghiệm Việt→Anh, nghe chọn từ, điền từ, sắp xếp câu (BR-020). | Cao |
| TC-013.2 | AC-013.2 (US-013) | U+M | When trả lời một câu, Then hiện ngay đúng/sai và đáp án đúng; không trừ điểm, không khóa bài (BR-022). | Cao |
| TC-013.3 | AC-013.3 (US-013) | M | Given máy không có giọng TTS, Then không sinh câu dạng "nghe chọn từ" mà thay bằng trắc nghiệm nghĩa. | Cao |
| TC-013.4 | AC-013.4 (US-013) | M | Given thoát giữa bài, Then hỏi xác nhận; nếu thoát thì không lưu lượt làm. | Cao |
| TC-014.1 | AC-014.1 (US-014) | U+M | When trả lời xong câu cuối, Then hiện điểm = số câu đúng/tổng × 100 (làm tròn số nguyên), số câu đúng, thời gian làm, danh sách câu sai kèm đáp án đúng. | Cao |
| TC-014.2 | AC-014.2 (US-014) | I+M | Then các từ trả lời sai được đưa về hộp 1 (BR-021) và lượt làm được lưu lịch sử. | Cao |
| TC-015.1 | AC-015.1 (US-015) | SY+M | Mở Lịch sử quiz → danh sách mới nhất trước (chủ đề/bộ, ngày giờ, điểm); lượt làm trên máy khác của cùng tài khoản hiện sau khi đồng bộ. | Cao |
| TC-015.2 | AC-015.2 (US-015) | M | Given chưa làm quiz nào, Then hiện "Bạn chưa làm bài kiểm tra nào" và nút đến danh sách quiz. | Cao |
| TC-016.1 | AC-016.1 (US-016) | M | When mở Trang chủ, Then thấy: lời chào theo tên, chuỗi ngày học, số từ mới đã học hôm nay / mục tiêu, số thẻ cần ôn hôm nay, nút "Ôn tập", nút "Học tiếp" (chủ đề học gần nhất). | Cao |
| TC-016.2 | AC-016.2 (US-016) | M | Given người dùng mới chưa học gì, Then nút "Học tiếp" thay bằng "Chọn chủ đề đầu tiên". | Cao |
| TC-017.1 | AC-017.1 (US-017) | M | When mở màn hình Tiến độ, Then thấy tổng số từ đã thuộc / tổng số từ; số từ theo trạng thái Mới/Đang học/Đã thuộc; tiến độ từng chủ đề; điểm quiz trung bình 7 ngày gần nhất; biểu đồ số hoạt động học 7 ngày gần… | Cao |
| TC-017.2 | AC-017.2 (US-017) | I | Các số liệu khớp với dữ liệu trong CSDL (người kiểm thử đối chiếu bằng truy vấn — BR-033). | Cao |
| TC-018.1 | AC-018.1 (US-018) | U | Given hôm qua có hoạt động học, When hoàn thành hoạt động học đầu tiên hôm nay, Then chuỗi +1 (BR-031). | Cao |
| TC-018.2 | AC-018.2 (US-018) | U | Given hôm qua không có hoạt động học, When hoàn thành hoạt động học hôm nay, Then chuỗi = 1. | Cao |
| TC-018.3 | AC-018.3 (US-018) | U+M | Chuỗi dài nhất từng đạt được hiển thị cạnh chuỗi hiện tại. | Cao |
| TC-019.1 | AC-019.1 (US-019) | M | Given nhắc học bật và đã cấp quyền thông báo, When đến giờ nhắc (sai lệch cho phép ≤ 15 phút) và hôm nay chưa đạt mục tiêu, Then hiện thông báo "Đến giờ học rồi! Bạn còn x từ và y thẻ cần ôn"; chạm vào mở… | Cao |
| TC-019.2 | AC-019.2 (US-019) | M | Given hôm nay đã đạt mục tiêu, Then không hiện thông báo. | Cao |
| TC-019.3 | AC-019.3 (US-019) | M | Given máy khởi động lại, Then lịch nhắc vẫn còn hiệu lực. | Cao |
| TC-019.4 | AC-019.4 (US-019) | M | Given quyền thông báo bị thu hồi, When mở Cài đặt, Then công tắc nhắc học hiện trạng thái tắt kèm nút "Cấp quyền". | Cao |
| TC-020.1 | AC-020.1 (US-020) | — | **Won't (thay đổi phạm vi C1)** — ngữ pháp không làm vòng này; không thiết kế, không code, không chạy. Giữ dòng để truy vết. | Won't |
| TC-020.2 | AC-020.2 (US-020) | — | **Won't (thay đổi phạm vi C1)** — ngữ pháp không làm vòng này; không thiết kế, không code, không chạy. Giữ dòng để truy vết. | Won't |
| TC-021.1 | AC-021.1 (US-021) | — | **Won't (thay đổi phạm vi C1)** — ngữ pháp không làm vòng này; không thiết kế, không code, không chạy. Giữ dòng để truy vết. | Won't |
| TC-022.1 | AC-022.1 (US-022) | I+M | When gõ ≥ 1 ký tự vào ô tìm kiếm, Then sau ≤ 300 ms hiện kết quả khớp tiền tố từ tiếng Anh hoặc chứa chuỗi trong nghĩa tiếng Việt, không phân biệt hoa thường và dấu tiếng Việt (BR-040), gồm từ seed và từ trong… | Could |
| TC-022.2 | AC-022.2 (US-022) | M | Given không có kết quả, Then hiện "Không tìm thấy “[từ khóa đã gõ]”" và nút "Thêm vào sổ tay" điền sẵn từ khóa. | Could |
| TC-023.1 | AC-023.1 (US-023) | M | When nhập từ (bắt buộc), nghĩa (bắt buộc), phiên âm, ví dụ, ghi chú (tùy chọn) và lưu, Then từ xuất hiện đầu danh sách Sổ tay. | Cao |
| TC-023.2 | AC-023.2 (US-023) | I+M | Given từ đã có trong sổ tay của chính người dùng, When lưu, Then báo "Từ này đã có trong sổ tay" và mở mục đã có. | Cao |
| TC-023.3 | AC-023.3 (US-023) | M | When xóa một từ và xác nhận, Then từ biến mất khỏi sổ tay và kết quả tra. | Cao |
| TC-024.1 | AC-024.1 (US-024) | M | Given sổ tay có ≥ 1 từ, When bấm "Học sổ tay", Then mở phiên flashcard như US-009, tiến độ thẻ áp dụng Leitner như từ seed. | Cao |
| TC-025.1 | AC-025.1 (US-025) | U+I+M | Cài mới, mở app không có mạng → ≤ 5 s (có màn hình chờ): 15 chủ đề × 20 = 300 từ, 4 ngôn ngữ, X01 40 + X02 30 + X03 30 từ, J01 20 từ (tổng 420 từ), E01 20 câu; không có bài ngữ pháp (TC-SEED). | Cao |
| TC-025.2 | AC-025.2 (US-025) | I | Given nạp seed lỗi giữa chừng, Then không để CSDL dở dang (giao dịch rollback) và hiện "Không thể khởi tạo dữ liệu, vui lòng mở lại app". | Cao |
| TC-026.1 | AC-026.1 (US-026) | M | When mở Tôi → Giới thiệu, Then thấy tên app, phiên bản, 4 thành viên, môn học, danh sách nguồn dữ liệu và giấy phép. | Trung bình |
| TC-027.1 | AC-027.1 (US-027) | SV+M | Có mạng, "Quên mật khẩu?" + email có thật → hiện câu hỏi bảo mật của tài khoản; email không tồn tại → vẫn hiện một câu hỏi giả, ổn định giữa các lần gọi (BR-076). | Cao |
| TC-027.2 | AC-027.2 (US-027) | SV+M | Trả lời đúng ("Hà Nội" ≡ "ha noi") + mật khẩu mới hợp lệ → đổi mật khẩu, mọi token cũ → `UNAUTHENTICATED`, bộ đếm sai đăng nhập về 0, về Đăng nhập với "Đã đặt lại mật khẩu". | Cao |
| TC-027.3 | AC-027.3 (US-027) | U+SV+M | Sai câu trả lời hoặc email không tồn tại → "Thông tin không khớp" (cùng thông báo); sai lần thứ 5 liên tiếp → `RESOURCE_EXHAUSTED` · `RESET_LOCKED` + `retry-after-seconds: 60`, đợt sau gấp đôi (120, 240 … tối đa 1 giờ, BR-008), mở lại khi hết khóa; email giả bị đếm/khóa như email thật (BR-008, BR-076). | Cao |
| TC-027.4 | AC-027.4 (US-027) | M | Chế độ máy bay, bấm "Quên mật khẩu?" → "Cần kết nối mạng để đặt lại mật khẩu"; app không gửi email. | Cao |
| TC-028.1 | AC-028.1 (US-028) | M | Given ở tab Học → "Bộ của tôi", When bấm "Tạo bộ", nhập tên (1–50 ký tự), mô tả (tùy chọn), chọn ngôn ngữ (mặc định Tiếng Anh) và lưu, Then bộ mới xuất hiện đầu danh sách "Bộ của tôi" với 0 từ. | Cao |
| TC-028.2 | AC-028.2 (US-028) | I+M | Given đã có bộ cùng tên (không phân biệt hoa thường) của chính người dùng, When lưu, Then báo "Bạn đã có bộ tên này" và không tạo (BR-044). | Cao |
| TC-028.3 | AC-028.3 (US-028) | I+M | Given vừa đăng ký, When mở "Bộ của tôi", Then đã có sẵn bộ "Sổ tay của tôi" (BR-046). | Cao |
| TC-029.1 | AC-029.1 (US-029) | M | When đổi tên hợp lệ và lưu, Then tên mới hiển thị ở mọi nơi. | Cao |
| TC-029.2 | AC-029.2 (US-029) | I+M | When chọn "Xóa bộ" và xác nhận, Then bộ, các từ trong bộ và tiến độ học của các từ đó bị xóa (BR-045); bộ "Sổ tay của tôi" không có nút xóa. | Cao |
| TC-029.3 | AC-029.3 (US-029) | M | Given bộ có sẵn (15 chủ đề, bộ luyện thi), Then không có nút sửa/xóa (BR-043). | Cao |
| TC-030.1 | AC-030.1 (US-030) | M | When mở bộ → "Thêm từ", nhập từ và nghĩa (bắt buộc), cách đọc, ví dụ, bản dịch ví dụ (tùy chọn) rồi lưu, Then từ xuất hiện trong bộ; số từ của bộ tăng 1. | Cao |
| TC-030.2 | AC-030.2 (US-030) | I+M | Given từ đã có trong bộ, When lưu, Then báo "Từ này đã có trong bộ" (BR-047). | Cao |
| TC-030.3 | AC-030.3 (US-030) | M | When sửa hoặc xóa (có xác nhận) một từ, Then thay đổi hiện ngay; xóa từ thì xóa luôn tiến độ học của từ. | Cao |
| TC-030.4 | AC-030.4 (US-030) | I+M | Given đang xem một từ có sẵn (Chi tiết từ), When bấm "Lưu vào bộ", chọn một bộ của tôi, Then từ được chép vào bộ đó (dữ liệu có sẵn không bị sửa). | Cao |
| TC-031.1 | AC-031.1 (US-031) | I+M | Given bộ có ≥ 1 từ, When bấm "Học", Then mở phiên flashcard như US-009 với các từ của bộ; tiến độ theo Leitner (BR-011, BR-012) và thẻ đến hạn xuất hiện trong "Ôn tập" chung (US-012). | Cao |
| TC-031.2 | AC-031.2 (US-031) | M | Given bộ có ≥ 4 từ, When bấm "Làm quiz", Then tạo quiz như US-013 từ các từ của bộ. | Cao |
| TC-032.1 | AC-032.1 (US-032) | U+M | Chọn file CSV hợp lệ bằng trình chọn file → màn xem trước: tổng dòng, hợp lệ, trùng, lỗi (kèm số dòng, lý do), 5 dòng đầu; chọn file `.json` → bị từ chối vì chỉ hỗ trợ CSV (JSON là Won't — C6). | Cao |
| TC-032.2 | AC-032.2 (US-032) | I+M | When chọn bộ đích (bộ mới hoặc bộ có sẵn của tôi) và bấm "Nhập", Then các dòng hợp lệ được thêm trong một giao dịch ≤ 3 giây với 2000 dòng, dòng trùng/lỗi bị bỏ qua, và hiện "Đã nhập x từ, bỏ qua y" (BR-050). | Cao |
| TC-032.3 | AC-032.3 (US-032) | U+M | Given file > 1 MB, > 2000 dòng, sai định dạng hoặc không có dòng hợp lệ, Then không nhập gì và báo lý do cụ thể. | Cao |
| TC-033.1 | AC-033.1 (US-033) | I+M | Export bộ có ≥ 1 từ → file `<tên bộ>_<yyyyMMdd>.csv` (chỉ CSV); import lại cho đúng số từ và nội dung (BR-051). | Cao |
| TC-033.2 | AC-033.2 (US-033) | M | Given người dùng hủy chọn nơi lưu hoặc ghi file lỗi, Then không có file dở dang và báo "Không thể xuất file". | Cao |
| TC-034.1 | AC-034.1 (US-034) | M | Given tạo bộ mới, When chọn ngôn ngữ Tiếng Nhật / Tiếng Trung / Tiếng Hàn / Tiếng Anh, Then nhãn trường "cách đọc" đổi theo ngôn ngữ (BR-052) và phát âm dùng giọng của ngôn ngữ đó. | Cao |
| TC-034.2 | AC-034.2 (US-034) | I+M | Given cài app lần đầu, Then có sẵn bộ mẫu "Tiếng Nhật cơ bản" 20 từ (từ, kana/romaji, nghĩa, ví dụ) học được bằng flashcard và quiz như bộ tiếng Anh. | Cao |
| TC-034.3 | AC-034.3 (US-034) | M | Thêm một ngôn ngữ mới chỉ cần thêm 1 dòng vào dữ liệu `languages` và (tùy chọn) 1 file seed — không sửa mã Java (kiểm chứng trong TESTING-STRATEGY TC-NFR-E2). | Cao |
| TC-034.4 | AC-034.4 (US-034) | M | Given máy thiếu giọng đọc của ngôn ngữ đó, Then áp dụng như AC-010.2 và quiz không sinh câu dạng nghe. | Cao |
| TC-035.1 | AC-035.1 (US-035) | M | Tab Luyện tập → "Luyện thi": 3 kỳ thi TOEIC/IELTS/Aptis, mỗi kỳ có bộ từ riêng (40 / 30 / 30 từ — C5) học được bằng flashcard, ôn tập, quiz. | Cao |
| TC-036.1 | AC-036.1 (US-036) | M | Chọn đề E01 (TOEIC Reading Part 5, 20 câu, 15 phút — thời lượng là ASSUMPTION) → đồng hồ đếm ngược, thanh tiến độ câu, chuyển qua lại và đánh dấu câu để xem lại; chỉ có 1 đề (E02 đã bỏ). | Cao |
| TC-036.2 | AC-036.2 (US-036) | U+M | When bấm "Nộp bài" (có xác nhận nếu còn câu chưa làm) hoặc hết giờ (tự nộp), Then hiện điểm = số câu đúng/tổng × 100, thời gian làm, từng câu với đáp án đúng và giải thích; ghi chú rõ "Điểm luyện tập, không… | Cao |
| TC-036.3 | AC-036.3 (US-036) | I+M | Then lượt thi được lưu vào lịch sử thi thử; từ vựng gắn với câu sai được đưa về hộp 1 để ôn (BR-055). | Cao |
| TC-036.4 | AC-036.4 (US-036) | I+M | Given thoát app giữa bài, When mở lại trong thời gian còn lại, Then tiếp tục đúng câu và đồng hồ; quá giờ thì bài được tự nộp với các câu đã trả lời. | Cao |
| TC-037.1 | AC-037.1 (US-037) | M | When mở "Luyện thi" → "Tài nguyên chính thức", Then thấy danh sách liên kết tới trang chính thức của TOEIC (ETS), IELTS và British Council Aptis kèm mô tả ngắn; chạm vào mở trình duyệt của máy (app không tự… | Cao |
| TC-037.2 | AC-037.2 (US-037) | M | Given máy không có trình duyệt, Then báo "Không tìm thấy trình duyệt để mở liên kết". | Cao |
| TC-038.1 | AC-038.1 (US-038) | SY | Máy A đã đồng bộ xong; đăng nhập cùng tài khoản trên máy B (Wi-Fi) → trong ≤ 10 s thấy đúng tiến độ thẻ, chuỗi ngày, bộ từ + từ của tôi, yêu thích, cài đặt; số thẻ hộp 1–5 trên B = trên A (so 5 con số); dữ liệu mẫu 2.000 bản ghi (NFR-N3). | Cao |
| TC-038.2 | AC-038.2 (US-038) | SY | Mất mạng giữa lúc kéo (trang 2/3) → giữ trang đã ghi, thanh "Đang ngoại tuyến"; có mạng lại kéo tiếp từ cursor đã lưu, không tạo bản ghi trùng (BR-063). | Cao |
| TC-039.1 | AC-039.1 (US-039) | M | Chế độ máy bay: học/ôn 20 thẻ → 20 thẻ ghi vào CSDL máy, `dirty = 1`, không có lỗi chặn việc học (NFR-N1); SCR-35 hiện "chờ đẩy: 20" (cộng bản ghi XP/nhật ký nếu có). | Cao |
| TC-039.2 | AC-039.2 (US-039) | SY | Còn 20 thay đổi chờ; bật mạng và mở app → tự đồng bộ; server có đúng 20 bản ghi `card_progress` mới của người dùng (đếm trước–sau), 0 bản ghi mất, số chờ đẩy về 0. | Cao |
| TC-039.3 | AC-039.3 (US-039) | U+SY | Tự đồng bộ đúng 3 thời điểm: ngay sau đăng nhập, khi mở app, sau mỗi phiên học/quiz/thi thử; 1.230 thay đổi → chia 3 lô 500/500/230 (BR-064). | Cao |
| TC-040.1 | AC-040.1 (US-040) | SV+SY | Thẻ T01-0007: A đánh giá t1 (hộp 3, ngoại tuyến), B đánh giá t2 > t1 (hộp 1), B đẩy rồi A đẩy → server trả `stale` cho A; cả hai máy giữ hộp 1; server có đúng 1 dòng `card_progress` của thẻ. | Cao |
| TC-040.2 | AC-040.2 (US-040) | SY | Xóa bộ tự tạo trên máy A → máy B đồng bộ xong, bộ (và từ, tiến độ) biến mất khỏi B; máy A không xóa cứng trước khi server xác nhận (tombstone, BR-063). | Cao |
| TC-041.1 | AC-041.1 (US-041) | M | Tôi → Trạng thái đồng bộ (SCR-35): thời điểm đồng bộ thành công gần nhất (hh:mm dd/MM), số thay đổi chờ đẩy, lỗi gần nhất bằng tiếng Việt; chưa đồng bộ lần nào → "Chưa đồng bộ". | Cao |
| TC-041.2 | AC-041.2 (US-041) | SY | Có mạng, x > 0 thay đổi chờ (≤ 500) → "Đồng bộ ngay" → ≤ 5 s (Wi-Fi), số chờ về 0, thời điểm cuối cập nhật; nút vô hiệu "Đang đồng bộ…" trong lúc chạy. | Cao |
| TC-041.3 | AC-041.3 (US-041) | M | Chế độ máy bay, "Đồng bộ ngay" → "Không có kết nối mạng", số chờ đẩy giữ nguyên, không mất dữ liệu. | Cao |
| TC-042.1 | AC-042.1 (US-042) | SV+M | Có mạng, mở SCR-34 → tối đa 50 người, XP tuần giảm dần, mỗi dòng có hạng + biệt danh + XP; tuần thứ Hai 00:00 → Chủ nhật 23:59 giờ Asia/Ho_Chi_Minh; không có email/id của người khác. | Cao |
| TC-042.2 | AC-042.2 (US-042) | SV+M | Tài khoản hạng 51 (ngoài top 50) → dòng "Bạn" vẫn hiện với hạng, XP tuần, chuỗi ngày; tài khoản 0 XP → "Chưa có hạng tuần này — học để nhận XP". | Cao |
| TC-042.3 | AC-042.3 (US-042) | U+SV | Hai người cùng 120 XP: An đạt lúc 09:00, Bình lúc 10:00 → An xếp trên; hòa cả thời điểm → `user_id` nhỏ hơn trước (BR-069). | Cao |
| TC-042.4 | AC-042.4 (US-042) | M | Mất mạng, mở SCR-34 → bản đã lưu kèm "Cập nhật lúc hh:mm dd/MM"; chưa từng tải → "Cần kết nối mạng để xem bảng xếp hạng". | Cao |
| TC-043.1 | AC-043.1 (US-043) | U+SV | Phiên 10 thẻ: 7 "Đã nhớ" + 3 "Chưa nhớ" → +17 XP (server tính lại, `xp_awarded` = 17); thoát giữa phiên vẫn tính thẻ đã đánh giá (BR-065). | Cao |
| TC-043.2 | AC-043.2 (US-043) | U+SV | Quiz 10/10 → +15; 7/10 → +7; thi thử E01 đúng 16/20 → +16 (không thưởng 100%) (BR-066, BR-067). | Cao |
| TC-043.3 | AC-043.3 (US-043) | U+SV | Đã có 490 XP trong ngày, quiz 10/10 (+15) → chỉ +10, tổng 500, app hiện "Bạn đã đạt tối đa 500 XP hôm nay"; hoạt động tiếp theo +0; sang ngày mới (00:00 giờ VN) cộng lại bình thường. | Cao |
| TC-043.4 | AC-043.4 (US-043) | SV | Client gửi `xp: 999` / tổng XP sai cho phiên 10 thẻ 7/3 → server bỏ qua, ghi 17; XP kiếm khi ngoại tuyến hiện tạm tính rồi được thay bằng số của server sau đồng bộ. | Cao |
| TC-044.1 | AC-044.1 (US-044) | SV+SY | [Should — thay đổi phạm vi] Tăng `content_version` bằng `content-import` + thêm 1 bộ trên server → mở app lần kế tiếp (có mạng, có token) bộ mới hiện ở SCR-06; tiến độ học hiện có không đổi; không token → `UNAUTHENTICATED`; Mongo/`content` tắt → `UNAVAILABLE`, app dùng seed + bản đã tải. | Trung bình |
| TC-044.2 | AC-044.2 (US-044) | SY | [Should — thay đổi phạm vi] Ngắt mạng ở từ thứ 25/40 khi tải bộ → giữ nguyên nội dung và version cũ (không có bộ dở dang); thử lại ở lần mở sau. | Trung bình |
| TC-045.1 | AC-045.1 (US-045) | U | `FlameStage.of(s)`: 0 → tắt; 1, 2 → giai đoạn 1; 3, 6 → 2; 7, 29 → 3; 30 → 4 (unit test 8 biên); SCR-21 hiện đúng hình theo chuỗi thật. | Cao |
| TC-045.2 | AC-045.2 (US-045) | M | Trang Chủ (SCR-05): huy hiệu chuỗi ngày pixel cùng giai đoạn với SCR-21 và hiện số ngày s. | Cao |
| TC-046.1 | AC-046.1 (US-046) | U+M | SCR-21: 5 rương, số trên rương k = số thẻ chưa xóa ở hộp k; tổng 5 số = tổng thẻ đã học (unit `ChestCounts` + đối chiếu truy vấn). | Cao |
| TC-046.2 | AC-046.2 (US-046) | U+M | Kết thúc phiên (SCR-10): cúp + confetti + chênh lệch từng rương; 3 thẻ hộp 1 → hộp 2 hiện rương 1 "−3", rương 2 "+3". | Cao |
| TC-047.1 | AC-047.1 (US-047) | M | [Could] SCR-06: công tắc Lưới/Bản đồ, mặc định Lưới; chọn Bản đồ, thoát và mở lại app → vẫn Bản đồ. | Could |
| TC-047.2 | AC-047.2 (US-047) | M | [Could] Bản đồ: 15 công trình; cấp A2 → 12 mở, 3 chủ đề B1 khóa, chạm công trình khóa báo điều kiện mở (BR-074). | Could |
| TC-047.3 | AC-047.3 (US-047) | U+M | [Could] Chủ đề 20 từ: 6 thuộc → 0★, 7 → 1★, 13 → 1★, 14 → 2★, 19 → 2★, 20 → 3★ (BR-072). | Could |

### Khoảng trống và điểm cần xác nhận
- **AC chưa có TC:** 0 (130 AC ↔ 130 dòng TC; 3 dòng Won't giữ để truy vết). Mọi REQ-020..REQ-024 có TC (REQ-024 là Could; REQ-022  là Should). Mọi RPC công khai EP-01..EP-16 có ≥ 1 TC-RPC và TC-SRV; mọi RPC nội bộ có TC-SVC-01.
- **KG-1 (ĐÃ ĐÓNG 11/11 — NFR-N2 và BR-077, giữ ở thay đổi phạm vi):** thử lại tối đa 3 lần, chờ 2 s, 4 s, 8 s (±20%) cho RPC idempotent khi `UNAVAILABLE`/`DEADLINE_EXCEEDED`; `RESOURCE_EXHAUSTED` chờ `retry-after-seconds`, không tự thử lại; không thử lại Register/Login/…; `edge` không thử lại. TC-AND-08, TC-UNI-16, TC-SYN-21, TC-SVC-11 kiểm theo bản này.
- **KG-2 (ĐÃ ĐÓNG 11/11 — khóa tăng dần, AC-002.8, BR-003/BR-008):** 5 lần sai → 60 s, gấp đôi mỗi lần (60/120/240/480/960/1.920/3.600 s), trần 1 giờ, tính cả email không tồn tại, mật khẩu và câu trả lời bảo mật dùng cùng quy tắc (bộ đếm riêng); bậc về 0 sau 24 giờ không sai. TC-UNI-21, TC-SRV-05, 12, TC-RPC-04, 10, TC-SEC-02, 03 kiểm theo bản này. **Đã sửa ở bản này:** TC-027.3 và TC-SRV-19 cũ còn ghi "sai 3 lần → khóa 300 s", mâu thuẫn BR-008 — nay thống nhất 5 lần/60 s lũy tiến.
- **KG-3 (ĐÃ ĐÓNG 11/11 — BR-076 và D-03):** HMAC-SHA256 với khóa `LEARNINGE_SERVER_KEY` (`APP_ENV=prod` thiếu khóa → từ chối khởi động; dev → khóa tạm + WARN). TC-UNI-25, TC-SRV-11, TC-SEC-04 kiểm.
- **KG-4 (ĐÃ ĐÓNG 11/11 — tên bộ "(2)" vượt 50 ký tự):** BR-044 cắt phần tên gốc để tổng ≤ 50 ký tự; TC-UNI-14 kiểm.
- **KG-5 (`REJECTED`/`DUPLICATE` và AC):** AC-040.1/BR-062 mô tả `STALE`; `DUPLICATE` cho bảng chỉ-thêm không có AC riêng — kiểm ở TC-SRV-17, TC-SYN-15, TC-SYN-26 (truy vết về BR-064).
- **KG-6 (ngưỡng độ phủ server):** NFR-M2 chỉ yêu cầu ≥ 80% nhánh cho `domain.rules` ở app; ngưỡng ≥ 80% dòng cho `auth`, `sync`, `gamification` của server (đo bằng `cargo llvm-cov`) là đề xuất của người kiểm thử, cần nhóm trưởng/nhóm trưởng xác nhận.
- **KG-7 (Q3/DEC-023 chưa chốt — nơi triển khai P1..P4):** TC-SEC-38, TC-SEC-27, TC-DEMO-02, TC-PERF-19, TC-AND-17 chỉ chạy được khi đã chọn phương án; các ca này ghi theo "lớp lộ ra" chung. Nếu chọn P3 (Railway) mà proxy công khai chỉ có TCP thì TLS phải kết thúc ở `edge` (tonic TLS — crate/feature mới cần duyệt) và sẽ cần thêm TC kiểm cấu hình TLS của `edge` (người kiểm thử bổ sung khi DEC-023 chốt). Phương án P2 (Atlas) thêm TC-SEC-28 phần IP allowlist.
- **KG-8 (công cụ/dependency chỉ dùng cho test — cần duyệt):** Q1 của thay đổi phạm vi chỉ duyệt thư viện runtime. người kiểm thử đề xuất duyệt thêm **công cụ dev/test**, không vào image/APK: Rust `tempfile`, `testcontainers` (hoặc dịch vụ Mongo của CI), `cargo-llvm-cov`, `cargo deny`; app `testImplementation` `grpc-inprocess`/`grpc-testing`; tải `ghz` (Go, binary dev) hoặc `tools/loadtest` tự viết; `buf` (chỉ nếu không dùng test descriptor ở TC-PROTO-01). Chưa duyệt → không thêm; ngoài danh sách này không thêm gì mới.
- **KG-9 (NFR-S11..S15 chưa có trong SRS (phi chức năng).md):** THREAT_MODEL đề xuất (11 khóa nội bộ, 12 chặn plaintext, 13 Mongo không PII, 14 container không root, 15 xóa tài khoản ≤ 60 s). TC-SEC-24, 26, 33, 36, 32 và ma trận SRS (phi chức năng) → TC giữ theo đề xuất; lập trình viên/nhóm trưởng cần thêm vào SRS (phi chức năng) để truy vết chính thức.
- **KG-10 (ưu tiên REQ-022):** thay đổi phạm vi §3 nâng REQ-022 (US-044) Could → Should; PRD hiện còn ghi Could. TC-044.1/044.2 tạm tính **Trung bình**; nhóm trưởng đồng bộ PRD/PROJECT-PLAN (công việc). Nếu PRD giữ Could thì hai TC này chuyển lại Could (không ảnh hưởng điều kiện ra).
- **KG-11 (ngưỡng SRS (phi chức năng) là mục tiêu chưa đo):** NFR-N4, N6, E4, E5, P7 chưa đo; có thể cần sửa qua CR sau S-01/M1/M2. TC-PERF ghi số thật, không chỉnh ngưỡng để khớp. Đặc biệt TC-OUT-06 phụ thuộc `OUTBOX_MAX_BACKOFF_SECS` ≤ 15 hoặc relay thử ngay khi đích `SERVING` (NFR-N6).
- **KG-12 (ResetPassword và cache token 60 s):** NFR-S7 viết "ResetPassword… chậm nhất khi cache hết hạn (≤ 60 s)", còn API_CONTRACT §Xác thực và THREAT_MODEL TC-SEC-29 yêu cầu `edge` xóa cache **ngay** (lấy user từ `AuthSession.user.id`). Test đặt kỳ vọng chặt hơn (ngay lập tức — TC-RPC-10, TC-SVC-07, TC-SEC-29); lập trình viên thống nhất lại NFR-S7.
- **KG-13 (thay đổi hợp đồng so với thay đổi phạm vi):** `x-client-version` thiếu → `UPGRADE_REQUIRED` (API_CONTRACT) khác ca "thiếu header" của thay đổi phạm vi (TC-SRV-47 cũ) — đã đổi ở TC-RPC-30, TC-SRV-51.
- **Thay đổi so với bản thay đổi phạm vi:** TC-SRV (51 ca REST/Javalin) viết lại thành 56 ca Rust theo dịch vụ + 34 ca TC-RPC; thêm TC-SVC, TC-OUT, TC-DEG, TC-PROTO, TC-AND, TC-PERF; TC-SEC 23 → 40; TC-UNI-16 và TC-UNI-19..26 viết lại cho Rust/gRPC, thêm TC-UNI-27..29; TC-SYN thêm 26..28; TC-MAN-09 đổi, thêm TC-MAN-11; TC-SRS (phi chức năng): bỏ N3/N4/E1/E4 (sang TC-PERF), thêm O1; TC-DEMO 8 → 10; KG-7 (Jackson) bỏ vì Javalin/Jackson bị hủy (thay đổi phạm vi §3). Giữ: mức E (Espresso) đã bỏ từ thay đổi phạm vi; `TC-NFR-X1-DARK`, `TC-SEED` cũ bỏ/thay; `TC-020.1`, `TC-020.2`, `TC-021.1` Won't; AC-032.1/033.1 chỉ CSV; seed 20 từ/chủ đề và 1 đề E01.

## Ma trận truy vết
> Truy vết từ yêu cầu đến user story, acceptance criteria, business rule, flow, RPC, test case và task: không yêu cầu nào không có test, không test nào không có yêu cầu.
> Bảng sinh bằng script từ PRD (REQ/US/AC), SRS (BR/FN/UC), Use-Cases (luồng) (FLOW), API_CONTRACT (EP-01..EP-16), SRS (phi chức năng), THREAT_MODEL và cột "Truy vết" của từng TC trong TESTING-STRATEGY, nên số liệu ở §7 là số đếm thật, không nhập tay. Mỗi AC có đúng một test case cùng số (`AC-009.3` → `TC-009.3`); cột TC liệt kê thêm các test bổ sung trỏ tới AC đó: `TC-UNI` unit, `TC-INS` instrumented DAO, `TC-SRV` integration theo dịch vụ Rust, `TC-RPC` hợp đồng RPC công khai qua `edge`, `TC-SVC` liên dịch vụ, `TC-OUT` outbox/relay, `TC-DEG` suy giảm có kiểm soát, `TC-PROTO` hợp đồng proto, `TC-AND` app gọi gRPC, `TC-SYN` đồng bộ, `TC-MAN` thủ công, `TC-SEC` bảo mật, `TC-SRS (phi chức năng)`/`TC-PERF` phi chức năng, `TC-DEMO` diễn tập. Nội dung từng TC: TESTING-STRATEGY §Truy vết AC → test case. người kiểm thử cập nhật cột Trạng thái khi chạy (Chưa chạy / Đạt / Lỗi #id).
> Giữ nguyên ID cũ, không đánh số lại: REQ/US/AC/BR của phần Won't (ngữ pháp REQ-008, dark mode REQ-013, widget REQ-015, import JSON BR-049, bài tập ngữ pháp BR-056) vẫn nằm trong bảng để truy vết, đánh dấu "Won't". REQ-009, REQ-022, REQ-024 là Could (theo PRD).

## 1. Ma trận
Quy ước ID: `REQ-xxx` (yêu cầu) → `US-xxx` (user story, trong PRD) → `AC-xxx.n` (tiêu chí chấp nhận) → `BR-xxx` (SRS) → `FLOW-xxx` (Use-Cases (luồng)) → `EP-xx` + RPC (API_CONTRACT, gói công khai `learninge.app.v1`, ghi `Dịch vụ.Phương thức`) → `TC-xxx` (test case, TESTING-STRATEGY) → `T-xxx` (task trong PROJECT-PLAN (công việc)).
Mỗi `US-xxx` trong PRD phải xuất hiện ít nhất một lần trong bảng (lint kiểm tra). Cột BR ở mức AC = BR viện dẫn trong chính AC (PRD) cộng BR trong các TC trỏ tới AC đó; nếu AC không viện dẫn BR nào thì lấy BR của US nguồn (SRS §Business rules, cột "Nguồn (US)"); BR chưa được AC/TC nào viện dẫn được gắn vào mọi AC của US nguồn để không BR nào rơi khỏi ma trận (như bản trước). Cột RPC = EP mà AC hoặc TC-RPC/TC-SRV/TC-AND… viện dẫn; AC thuần cục bộ (không gọi máy chủ) ghi `—`; với AC có từ khóa mạng/máy chủ nhưng không TC nào nêu EP thì lấy EP của FN tương ứng (13 AC, người kiểm thử đối chiếu khi rà TESTING-STRATEGY).
**Cột Task:** đã khớp PROJECT-PLAN (công việc) thay đổi phạm vi (Rust gRPC, T-140..T-155, cập nhật 12/11). Ma trận dùng ID task của **app** (T-001..T-107, T-120..T-133) và của **server** (T-146..T-150, T-153, chọn theo cột US/RPC/TC của từng dòng). Task server cũ theo Javalin/REST được thay bằng T-141, T-145, T-147, T-148, T-149, T-150, T-152, T-153, T-154. Không còn nhãn chờ; ma trận không tự đánh số task.

| REQ | US | AC | BR (SRS) | FLOW | RPC (API_CONTRACT) | TC | Task | Trạng thái |
|---|---|---|---|---|---|---|---|---|
| REQ-001 | US-001 | AC-001.1 | BR-002, BR-057, BR-061 | FLOW-001 | EP-02 `Auth.Register` | TC-001.1, TC-RPC-02, TC-SRV-01 | T-006, T-020, T-021, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-001 | US-001 | AC-001.2 | BR-060, BR-061 | FLOW-001 | EP-02 `Auth.Register` | TC-001.2, TC-RPC-02, TC-SRV-02 | T-006, T-020, T-021, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-001 | US-001 | AC-001.3 | BR-060, BR-061 | FLOW-001 | EP-02 `Auth.Register` | TC-001.3, TC-RPC-02, TC-SRV-03, TC-UNI-11 | T-006, T-020, T-021, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-001 | US-001 | AC-001.4 | BR-007, BR-009 | FLOW-001 | EP-02 `Auth.Register` | TC-001.4, TC-RPC-02, TC-SRV-03 | T-006, T-020, T-021, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-001 | US-001 | AC-001.5 | BR-001, BR-002, BR-007, BR-009, BR-057, BR-060, BR-061 | FLOW-001 | EP-02 `Auth.Register` | TC-001.5 | T-006, T-020, T-021, T-121, T-146, T-147 | Chưa chạy |
| REQ-001 | US-002 | AC-002.1 | BR-057, BR-076 | FLOW-001, FLOW-018 | EP-03 `Auth.Login` | TC-002.1, TC-RPC-03, TC-SRV-04 | T-020, T-021, T-024, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-001 | US-002 | AC-002.2 | BR-057, BR-076 | FLOW-001, FLOW-018 | EP-03 `Auth.Login` | TC-002.2, TC-RPC-03, TC-SRV-04 | T-020, T-021, T-024, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-001 | US-002 | AC-002.3 | BR-003 | FLOW-001, FLOW-018 | EP-03 `Auth.Login` | TC-002.3, TC-RPC-04, TC-RPC-27, TC-SRV-05 | T-020, T-021, T-024, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-001 | US-002 | AC-002.4 | BR-058, BR-073 | FLOW-001, FLOW-018 | EP-04 `Auth.Logout` | TC-002.4, TC-RPC-05, TC-SRV-08, TC-SYN-18 | T-020, T-021, T-024, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-001 | US-002 | AC-002.5 | BR-003, BR-004, BR-057, BR-058, BR-059, BR-060, BR-073, BR-076 | FLOW-001, FLOW-018 | EP-12 `Sync.Pull` | TC-002.5 | T-020, T-021, T-024, T-121, T-146, T-147, T-148 | Chưa chạy |
| REQ-001 | US-002 | AC-002.6 | BR-059, BR-073 | FLOW-001, FLOW-018 | EP-03 `Auth.Login` | TC-002.6, TC-SYN-12 | T-020, T-021, T-024, T-121, T-146, T-147 | Chưa chạy |
| REQ-001 | US-002 | AC-002.7 | BR-078 | FLOW-001, FLOW-018 | EP-03 `Auth.Login` | TC-002.7, TC-MAN-02 | T-020, T-021, T-024, T-121, T-146, T-147 | Chưa chạy |
| REQ-001 | US-002 | AC-002.8 | BR-003, BR-008 | FLOW-001, FLOW-018 | EP-03 `Auth.Login` | TC-002.8, TC-RPC-04, TC-SRV-05, TC-UNI-21 | T-020, T-021, T-024, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-001 | US-003 | AC-003.1 | BR-030, BR-061 | FLOW-009 | EP-05 `Auth.GetMe` | TC-003.1, TC-RPC-06 | T-024, T-052, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-001 | US-003 | AC-003.2 | BR-030, BR-061 | FLOW-009 | EP-06 `Auth.UpdateMe` | TC-003.2, TC-RPC-07 | T-024, T-052, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-001 | US-004 | AC-004.1 | BR-058 | FLOW-009 | EP-07 `Auth.ChangePassword` | TC-004.1, TC-RPC-08, TC-SRV-10 | T-024, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-001 | US-004 | AC-004.2 | BR-058, BR-059 | FLOW-009 | EP-07 `Auth.ChangePassword`, EP-10 `Auth.DeleteAccount` | TC-004.2, TC-AND-10, TC-RPC-08, TC-SRV-10 | T-024, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-001 | US-004 | AC-004.3 | BR-078 | FLOW-009 | EP-06 `Auth.UpdateMe`, EP-07 `Auth.ChangePassword`, EP-10 `Auth.DeleteAccount` | TC-004.3, TC-MAN-02 | T-024, T-121, T-146, T-147 | Chưa chạy |
| REQ-001 | US-005 | AC-005.1 | BR-005 | FLOW-009 | EP-10 `Auth.DeleteAccount`, EP-13 `Leaderboard.GetWeekly` | TC-005.1, TC-INS-05, TC-RPC-11, TC-SRV-13 | T-024, T-080, T-121, T-146, T-147, T-149, T-153 | Chưa chạy |
| REQ-001 | US-005 | AC-005.2 | BR-005 | FLOW-009 | — | TC-005.2 | T-024, T-080, T-121, T-147 | Chưa chạy |
| REQ-001 | US-005 | AC-005.3 | BR-078 | FLOW-009 | EP-06 `Auth.UpdateMe`, EP-07 `Auth.ChangePassword`, EP-10 `Auth.DeleteAccount` | TC-005.3, TC-MAN-02 | T-024, T-080, T-121, T-146, T-147 | Chưa chạy |
| REQ-002 | US-006 | AC-006.1 | BR-006 | FLOW-002 | — | TC-006.1 | T-022 | Chưa chạy |
| REQ-002 | US-006 | AC-006.2 | BR-006 | FLOW-002 | — | TC-006.2 | T-022 | Chưa chạy |
| REQ-002 | US-006 | AC-006.3 | BR-006 | FLOW-002 | — | TC-006.3 | T-022 | Chưa chạy |
| REQ-003 | US-007 | AC-007.1 | — | FLOW-003 | — | TC-007.1 | T-030 | Chưa chạy |
| REQ-003 | US-007 | AC-007.2 | — | FLOW-003 | — | TC-007.2 | T-030 | Chưa chạy |
| REQ-003 | US-008 | AC-008.1 | BR-014 | FLOW-003 | — | TC-008.1 | T-030, T-031 | Chưa chạy |
| REQ-003 | US-008 | AC-008.2 | BR-014 | FLOW-003 | — | TC-008.2 | T-030, T-031 | Chưa chạy |
| REQ-003 | US-009 | AC-009.1 | BR-010 | FLOW-004, FLOW-012 | — | TC-009.1 | T-007, T-032 | Chưa chạy |
| REQ-003 | US-009 | AC-009.2 | BR-010, BR-011, BR-015 | FLOW-004, FLOW-012 | — | TC-009.2 | T-007, T-032 | Chưa chạy |
| REQ-003 | US-009 | AC-009.3 | BR-011 | FLOW-004, FLOW-012 | — | TC-009.3 | T-007, T-032 | Chưa chạy |
| REQ-003 | US-009 | AC-009.4 | BR-010, BR-011, BR-015 | FLOW-004, FLOW-012 | — | TC-009.4 | T-007, T-032 | Chưa chạy |
| REQ-003 | US-009 | AC-009.5 | BR-010, BR-011, BR-015 | FLOW-004, FLOW-012 | — | TC-009.5 | T-007, T-032 | Chưa chạy |
| REQ-003 | US-010 | AC-010.1 | — | FLOW-003 | — | TC-010.1 | T-031 | Chưa chạy |
| REQ-003 | US-010 | AC-010.2 | — | FLOW-003 | — | TC-010.2 | T-031 | Chưa chạy |
| REQ-003 | US-011 | AC-011.1 | — | FLOW-003 | — | TC-011.1 | T-031, T-061 | Chưa chạy |
| REQ-003 | US-011 | AC-011.2 | — | FLOW-003 | — | TC-011.2 | T-031, T-061 | Chưa chạy |
| REQ-004 | US-012 | AC-012.1 | BR-011, BR-012, BR-013, BR-033 | FLOW-004, FLOW-012 | — | TC-012.1, TC-INS-09, TC-UNI-08 | T-007, T-033 | Chưa chạy |
| REQ-004 | US-012 | AC-012.2 | BR-011, BR-012, BR-013 | FLOW-004, FLOW-012 | — | TC-012.2, TC-UNI-08 | T-007, T-033 | Chưa chạy |
| REQ-004 | US-012 | AC-012.3 | BR-011, BR-012, BR-013, BR-042 | FLOW-004, FLOW-012 | — | TC-012.3 | T-007, T-033 | Chưa chạy |
| REQ-005 | US-013 | AC-013.1 | BR-020, BR-021, BR-022, BR-023, BR-024, BR-025 | FLOW-005 | — | TC-013.1, TC-UNI-10 | T-008, T-040 | Chưa chạy |
| REQ-005 | US-013 | AC-013.2 | BR-022 | FLOW-005 | — | TC-013.2 | T-008, T-040 | Chưa chạy |
| REQ-005 | US-013 | AC-013.3 | BR-020, BR-022, BR-024, BR-025 | FLOW-005 | — | TC-013.3 | T-008, T-040 | Chưa chạy |
| REQ-005 | US-013 | AC-013.4 | BR-020, BR-022, BR-024, BR-025 | FLOW-005 | — | TC-013.4, TC-INS-11 | T-008, T-040 | Chưa chạy |
| REQ-005 | US-014 | AC-014.1 | BR-020, BR-021, BR-022, BR-023, BR-024, BR-025 | FLOW-005 | — | TC-014.1, TC-UNI-10 | T-008, T-041 | Chưa chạy |
| REQ-005 | US-014 | AC-014.2 | BR-021 | FLOW-005 | — | TC-014.2, TC-INS-11 | T-008, T-041 | Chưa chạy |
| REQ-005 | US-015 | AC-015.1 | — | FLOW-005 | — | TC-015.1 | T-041 | Chưa chạy |
| REQ-005 | US-015 | AC-015.2 | — | FLOW-005 | — | TC-015.2 | T-041 | Chưa chạy |
| REQ-006 | US-016 | AC-016.1 | BR-032 | FLOW-008 | — | TC-016.1 | T-050 | Chưa chạy |
| REQ-006 | US-016 | AC-016.2 | BR-032 | FLOW-008 | — | TC-016.2 | T-050 | Chưa chạy |
| REQ-006 | US-017 | AC-017.1 | BR-014, BR-033 | FLOW-008 | — | TC-017.1 | T-051 | Chưa chạy |
| REQ-006 | US-017 | AC-017.2 | BR-033 | FLOW-008 | — | TC-017.2, TC-INS-09 | T-051 | Chưa chạy |
| REQ-006 | US-018 | AC-018.1 | BR-031, BR-042 | FLOW-008 | — | TC-018.1, TC-UNI-09 | T-007, T-050 | Chưa chạy |
| REQ-006 | US-018 | AC-018.2 | BR-031, BR-042 | FLOW-008 | — | TC-018.2, TC-UNI-09 | T-007, T-050 | Chưa chạy |
| REQ-006 | US-018 | AC-018.3 | BR-031, BR-042 | FLOW-008 | — | TC-018.3, TC-UNI-09 | T-007, T-050 | Chưa chạy |
| REQ-007 | US-019 | AC-019.1 | BR-030 | FLOW-007 | — | TC-019.1 | T-052 | Chưa chạy |
| REQ-007 | US-019 | AC-019.2 | BR-030 | FLOW-007 | — | TC-019.2 | T-052 | Chưa chạy |
| REQ-007 | US-019 | AC-019.3 | BR-030 | FLOW-007 | — | TC-019.3 | T-052 | Chưa chạy |
| REQ-007 | US-019 | AC-019.4 | BR-030 | FLOW-007 | — | TC-019.4 | T-052 | Chưa chạy |
| REQ-008 | US-020 | AC-020.1 | — | FLOW-006 | — | TC-020.1 | T-014, T-060 | Won't — không chạy |
| REQ-008 | US-020 | AC-020.2 | — | FLOW-006 | — | TC-020.2 | T-014, T-060 | Won't — không chạy |
| REQ-008 | US-021 | AC-021.1 | BR-023, BR-056 | FLOW-006 | — | TC-021.1 | T-014, T-060 | Won't — không chạy |
| REQ-009 | US-022 | AC-022.1 | BR-040 | FLOW-011 | — | TC-022.1 | T-061 | Chưa chạy (Could) |
| REQ-009 | US-022 | AC-022.2 | BR-040, BR-053 | FLOW-011 | — | TC-022.2 | T-061 | Chưa chạy (Could) |
| REQ-010 | US-023 | AC-023.1 | BR-041, BR-046, BR-047 | FLOW-011 | — | TC-023.1 | T-090 | Chưa chạy |
| REQ-010 | US-023 | AC-023.2 | BR-041, BR-046, BR-047 | FLOW-011 | — | TC-023.2 | T-090 | Chưa chạy |
| REQ-010 | US-023 | AC-023.3 | BR-041, BR-046, BR-047 | FLOW-011 | — | TC-023.3 | T-090 | Chưa chạy |
| REQ-010 | US-024 | AC-024.1 | BR-015 | FLOW-004 | — | TC-024.1 | T-090 | Chưa chạy |
| REQ-011 | US-025 | AC-025.1 | — | FLOW-010 | — | TC-025.1, TC-INS-02, TC-UNI-18 | T-003, T-004, T-010, T-011, T-012, T-013, T-021, T-080 | Chưa chạy |
| REQ-011 | US-025 | AC-025.2 | — | FLOW-010 | — | TC-025.2, TC-INS-02 | T-003, T-004, T-010, T-011, T-012, T-013, T-021, T-080 | Chưa chạy |
| REQ-012 | US-026 | AC-026.1 | — | — | — | TC-026.1 | T-063 | Chưa chạy (Should) |
| REQ-013 | — | — | — | — | — | — | T-070 (Hủy) | Won't — không làm, không kiểm thử |
| REQ-014 | US-027 | AC-027.1 | BR-076 | FLOW-013 | EP-08 `Auth.GetResetQuestion` | TC-027.1, TC-RPC-09, TC-SRV-11 | T-006, T-023, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-014 | US-027 | AC-027.2 | BR-007, BR-008, BR-058 | FLOW-013 | EP-09 `Auth.ResetPassword` | TC-027.2, TC-RPC-10, TC-SRV-12 | T-006, T-023, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-014 | US-027 | AC-027.3 | BR-007, BR-008, BR-076 | FLOW-013 | EP-09 `Auth.ResetPassword` | TC-027.3, TC-RPC-10, TC-SRV-12 | T-006, T-023, T-121, T-146, T-147, T-153 | Chưa chạy |
| REQ-014 | US-027 | AC-027.4 | BR-078 | FLOW-013 | EP-08 `Auth.GetResetQuestion`, EP-09 `Auth.ResetPassword` | TC-027.4, TC-MAN-02 | T-006, T-023, T-121, T-146, T-147 | Chưa chạy |
| REQ-015 | — | — | — | — | — | — | T-071 (Won't) | Won't — không làm, không kiểm thử |
| REQ-016 | US-028 | AC-028.1 | BR-043, BR-044, BR-046 | FLOW-014 | — | TC-028.1 | T-090 | Chưa chạy |
| REQ-016 | US-028 | AC-028.2 | BR-044 | FLOW-014 | — | TC-028.2, TC-UNI-14 | T-090 | Chưa chạy |
| REQ-016 | US-028 | AC-028.3 | BR-043, BR-046 | FLOW-014 | — | TC-028.3, TC-SYN-28 | T-090 | Chưa chạy |
| REQ-016 | US-029 | AC-029.1 | BR-043, BR-044, BR-045 | FLOW-014 | — | TC-029.1 | T-090 | Chưa chạy |
| REQ-016 | US-029 | AC-029.2 | BR-045 | FLOW-014 | — | TC-029.2 | T-090 | Chưa chạy |
| REQ-016 | US-029 | AC-029.3 | BR-043 | FLOW-014 | — | TC-029.3 | T-090 | Chưa chạy |
| REQ-016 | US-030 | AC-030.1 | BR-043, BR-047 | FLOW-014 | — | TC-030.1 | T-090 | Chưa chạy |
| REQ-016 | US-030 | AC-030.2 | BR-047 | FLOW-014 | — | TC-030.2 | T-090 | Chưa chạy |
| REQ-016 | US-030 | AC-030.3 | BR-043, BR-047 | FLOW-014 | — | TC-030.3 | T-090 | Chưa chạy |
| REQ-016 | US-030 | AC-030.4 | BR-043, BR-047 | FLOW-014 | — | TC-030.4 | T-090 | Chưa chạy |
| REQ-016 | US-031 | AC-031.1 | BR-011, BR-012 | FLOW-014 | — | TC-031.1 | T-090 | Chưa chạy |
| REQ-016 | US-031 | AC-031.2 | BR-043 | FLOW-014 | — | TC-031.2 | T-090 | Chưa chạy |
| REQ-017 | US-032 | AC-032.1 | BR-048, BR-049 | FLOW-015 | — | TC-032.1 | T-092, T-093 | Chưa chạy |
| REQ-017 | US-032 | AC-032.2 | BR-050 | FLOW-015 | — | TC-032.2, TC-INS-10 | T-092, T-093 | Chưa chạy |
| REQ-017 | US-032 | AC-032.3 | BR-048, BR-050, BR-051 | FLOW-015 | — | TC-032.3, TC-UNI-17 | T-092, T-093 | Chưa chạy |
| REQ-017 | US-033 | AC-033.1 | BR-048, BR-050, BR-051 | FLOW-016 | — | TC-033.1, TC-SYN-22, TC-UNI-17 | T-092, T-093 | Chưa chạy |
| REQ-017 | US-033 | AC-033.2 | BR-051 | FLOW-016 | — | TC-033.2 | T-092, T-093 | Chưa chạy |
| REQ-018 | US-034 | AC-034.1 | BR-052 | FLOW-014 | — | TC-034.1 | T-091, T-099 | Chưa chạy |
| REQ-018 | US-034 | AC-034.2 | BR-052, BR-053 | FLOW-014 | — | TC-034.2 | T-091, T-099 | Chưa chạy |
| REQ-018 | US-034 | AC-034.3 | BR-052, BR-053 | FLOW-014 | — | TC-034.3 | T-091, T-099 | Chưa chạy |
| REQ-018 | US-034 | AC-034.4 | BR-052, BR-053 | FLOW-014 | — | TC-034.4 | T-091, T-099 | Chưa chạy |
| REQ-019 | US-035 | AC-035.1 | — | FLOW-017 | — | TC-035.1, TC-UNI-18 | T-095, T-096, T-097, T-098 | Chưa chạy |
| REQ-019 | US-036 | AC-036.1 | BR-054, BR-055 | FLOW-017 | — | TC-036.1 | T-094, T-095, T-096 | Chưa chạy |
| REQ-019 | US-036 | AC-036.2 | BR-054 | FLOW-017 | — | TC-036.2 | T-094, T-095, T-096 | Chưa chạy |
| REQ-019 | US-036 | AC-036.3 | BR-055 | FLOW-017 | — | TC-036.3, TC-INS-11 | T-094, T-095, T-096 | Chưa chạy |
| REQ-019 | US-036 | AC-036.4 | BR-054, BR-055 | FLOW-017 | — | TC-036.4, TC-INS-11 | T-094, T-095, T-096 | Chưa chạy |
| REQ-019 | US-037 | AC-037.1 | — | FLOW-017 | — | TC-037.1 | T-095 | Chưa chạy |
| REQ-019 | US-037 | AC-037.2 | — | FLOW-017 | — | TC-037.2 | T-095 | Chưa chạy |
| REQ-020 | US-038 | AC-038.1 | BR-063 | FLOW-018 | EP-12 `Sync.Pull` | TC-038.1, TC-RPC-18, TC-SRV-26, TC-SYN-09 | T-003, T-106, T-120, T-122, T-126, T-146, T-148, T-153 | Chưa chạy |
| REQ-020 | US-038 | AC-038.2 | BR-063 | FLOW-018 | EP-03 `Auth.Login`, EP-11 `Sync.Push`, EP-12 `Sync.Pull` | TC-038.2, TC-INS-07, TC-SYN-11 | T-003, T-106, T-120, T-122, T-126, T-146, T-147, T-148 | Chưa chạy |
| REQ-020 | US-039 | AC-039.1 | BR-059, BR-064, BR-077, BR-078 | FLOW-019 | — | TC-039.1, TC-SYN-10 | T-003, T-106, T-120, T-122, T-126, T-148 | Chưa chạy |
| REQ-020 | US-039 | AC-039.2 | BR-062 | FLOW-019 | EP-11 `Sync.Push` | TC-039.2, TC-DEG-01, TC-RPC-12, TC-SRV-15, TC-SYN-10 | T-003, T-106, T-120, T-122, T-126, T-146, T-148, T-153 | Chưa chạy |
| REQ-020 | US-039 | AC-039.3 | BR-064, BR-077 | FLOW-019 | EP-11 `Sync.Push`, EP-12 `Sync.Pull` | TC-039.3, TC-SYN-01, TC-SYN-14, TC-UNI-12 | T-003, T-106, T-120, T-122, T-126, T-146, T-148 | Chưa chạy |
| REQ-020 | US-040 | AC-040.1 | BR-062, BR-064 | FLOW-019 | EP-11 `Sync.Push`, EP-12 `Sync.Pull` | TC-040.1, TC-RPC-13, TC-SRV-16, TC-SYN-04 | T-003, T-106, T-120, T-122, T-126, T-146, T-148, T-153 | Chưa chạy |
| REQ-020 | US-040 | AC-040.2 | BR-045, BR-063 | FLOW-019 | EP-11 `Sync.Push`, EP-12 `Sync.Pull` | TC-040.2, TC-SYN-06 | T-003, T-106, T-120, T-122, T-126, T-146, T-148 | Chưa chạy |
| REQ-020 | US-041 | AC-041.1 | BR-077 | FLOW-019 | — | TC-041.1, TC-INS-12 | T-003, T-106, T-120, T-122, T-125, T-126, T-148 | Chưa chạy |
| REQ-020 | US-041 | AC-041.2 | BR-077 | FLOW-019 | — | TC-041.2 | T-003, T-106, T-120, T-122, T-125, T-126, T-148 | Chưa chạy |
| REQ-020 | US-041 | AC-041.3 | BR-077 | FLOW-019 | — | TC-041.3 | T-003, T-106, T-120, T-122, T-125, T-126, T-148 | Chưa chạy |
| REQ-021 | US-042 | AC-042.1 | BR-042, BR-069 | FLOW-020 | EP-13 `Leaderboard.GetWeekly` | TC-042.1, TC-MAN-04, TC-OUT-02, TC-RPC-20, TC-SRV-32, TC-SYN-25, TC-UNI-06 | T-103, T-106, T-124, T-146, T-149, T-153 | Chưa chạy |
| REQ-021 | US-042 | AC-042.2 | BR-069 | FLOW-020 | EP-13 `Leaderboard.GetWeekly` | TC-042.2, TC-MAN-04, TC-SRV-32, TC-UNI-07 | T-103, T-106, T-124, T-146, T-149, T-153 | Chưa chạy |
| REQ-021 | US-042 | AC-042.3 | BR-069 | FLOW-020 | EP-13 `Leaderboard.GetWeekly` | TC-042.3, TC-MAN-04, TC-SRV-32, TC-UNI-07 | T-103, T-106, T-124, T-146, T-149, T-153 | Chưa chạy |
| REQ-021 | US-042 | AC-042.4 | BR-031, BR-061, BR-069, BR-078 | FLOW-020 | EP-13 `Leaderboard.GetWeekly`, EP-14 `Content.GetManifest` | TC-042.4, TC-AND-16, TC-DEG-05, TC-INS-12, TC-MAN-04, TC-SYN-27 | T-103, T-106, T-124, T-146, T-149, T-150, T-153 | Chưa chạy |
| REQ-021 | US-043 | AC-043.1 | BR-065, BR-066, BR-067 | FLOW-008, FLOW-020 | — | TC-043.1, TC-UNI-04 | T-106, T-124, T-149 | Chưa chạy |
| REQ-021 | US-043 | AC-043.2 | BR-065, BR-066, BR-067 | FLOW-008, FLOW-020 | — | TC-043.2, TC-UNI-04 | T-106, T-124, T-149 | Chưa chạy |
| REQ-021 | US-043 | AC-043.3 | BR-068 | FLOW-008, FLOW-020 | — | TC-043.3, TC-UNI-05 | T-106, T-124, T-149 | Chưa chạy |
| REQ-021 | US-043 | AC-043.4 | BR-068 | FLOW-008, FLOW-020 | EP-13 `Leaderboard.GetWeekly` | TC-043.4, TC-SYN-17 | T-106, T-124, T-146, T-149 | Chưa chạy |
| REQ-022 | US-044 | AC-044.1 | BR-075 | FLOW-021 | EP-14 `Content.GetManifest`, EP-15 `Content.GetDeck`, EP-16 `Content.GetExam` | TC-044.1, TC-RPC-21, TC-RPC-22, TC-SRV-37, TC-SRV-38 | T-106, T-123, T-146, T-150, T-153 | Chưa chạy (Could) |
| REQ-022 | US-044 | AC-044.2 | BR-075 | FLOW-021 | EP-13 `Leaderboard.GetWeekly`, EP-14 `Content.GetManifest` | TC-044.2, TC-AND-16, TC-DEG-05 | T-106, T-123, T-146, T-149, T-150, T-153 | Chưa chạy (Could) |
| REQ-023 | US-045 | AC-045.1 | BR-070 | FLOW-008 | — | TC-045.1, TC-MAN-06, TC-UNI-01 | T-103, T-106, T-130, T-131, T-132 | Chưa chạy |
| REQ-023 | US-045 | AC-045.2 | BR-070 | FLOW-008 | — | TC-045.2, TC-MAN-07 | T-103, T-106, T-130, T-131, T-132 | Chưa chạy |
| REQ-023 | US-046 | AC-046.1 | BR-071 | FLOW-004, FLOW-012 | — | TC-046.1, TC-MAN-06, TC-UNI-03 | T-103, T-106, T-130, T-131, T-132 | Chưa chạy |
| REQ-023 | US-046 | AC-046.2 | BR-071 | FLOW-004, FLOW-012 | — | TC-046.2, TC-MAN-05, TC-UNI-03 | T-103, T-106, T-130, T-131, T-132 | Chưa chạy |
| REQ-024 | US-047 | AC-047.1 | BR-072, BR-074 | FLOW-022 | — | TC-047.1 | T-106, T-133 | Chưa chạy (Could) |
| REQ-024 | US-047 | AC-047.2 | BR-074 | FLOW-022 | — | TC-047.2 | T-106, T-133 | Chưa chạy (Could) |
| REQ-024 | US-047 | AC-047.3 | BR-072 | FLOW-022 | — | TC-047.3, TC-UNI-02 | T-106, T-133 | Chưa chạy (Could) |

Tổng: 47 user story, 130 AC, 130 test case `TC-xxx.n` (1 AC = 1 TC) trong bảng; 24 REQ (REQ-001..REQ-024), trong đó REQ-013 và REQ-015 không có US.

## 2. Chỉ mục US → FN → UC
FN (SRS §Chức năng chi tiết) và UC (§Danh sách use case) theo từng US; FLOW lặp lại để tra nhanh.

| US | Ưu tiên | FN | UC | FLOW |
|---|---|---|---|---|
| US-001 | Must | FN-001 | UC-001 | FLOW-001 |
| US-002 | Must | FN-002 | UC-002 | FLOW-001, FLOW-018 |
| US-003 | Must | FN-004 | UC-004 | FLOW-009 |
| US-004 | Must | FN-004 | UC-004 | FLOW-009 |
| US-005 | Must | FN-004 | UC-004 | FLOW-009 |
| US-006 | Must | FN-003 | UC-003 | FLOW-002 |
| US-007 | Must | FN-005 | UC-005 | FLOW-003 |
| US-008 | Must | FN-005 | UC-005 | FLOW-003 |
| US-009 | Must | FN-006 | UC-006 | FLOW-004, FLOW-012 |
| US-010 | Must | FN-005 | UC-005 | FLOW-003 |
| US-011 | Must | FN-005 | UC-005 | FLOW-003 |
| US-012 | Must | FN-007 | UC-007 | FLOW-004, FLOW-012 |
| US-013 | Must | FN-008 | UC-008 | FLOW-005 |
| US-014 | Must | FN-008 | UC-008 | FLOW-005 |
| US-015 | Must | FN-009 | UC-009 | FLOW-005 |
| US-016 | Must | FN-010 | UC-010 | FLOW-008 |
| US-017 | Must | FN-010 | UC-010 | FLOW-008 |
| US-018 | Must | FN-010 | UC-010 | FLOW-008 |
| US-019 | Must | FN-011 | UC-011 | FLOW-007 |
| US-020 | Won't | FN-012 | UC-012 | FLOW-006 |
| US-021 | Won't | FN-012 | UC-012 | FLOW-006 |
| US-022 | Could | FN-013 | UC-013 | FLOW-011 |
| US-023 | Must | FN-014 | UC-014 | FLOW-011 |
| US-024 | Must | FN-014 | UC-014 | FLOW-004 |
| US-025 | Must | FN-015 | UC-015 | FLOW-010 |
| US-026 | Should | FN-016 | UC-016 | — |
| US-027 | Must | FN-017 | UC-017 | FLOW-013 |
| US-028 | Must | FN-018 | UC-018 | FLOW-014 |
| US-029 | Must | FN-018 | UC-018 | FLOW-014 |
| US-030 | Must | FN-018 | UC-018 | FLOW-014 |
| US-031 | Must | FN-018 | UC-018 | FLOW-014 |
| US-032 | Must | FN-019 | UC-019 | FLOW-015 |
| US-033 | Must | FN-020 | UC-020 | FLOW-016 |
| US-034 | Must | FN-021 | UC-021 | FLOW-014 |
| US-035 | Must | FN-022 | UC-022 | FLOW-017 |
| US-036 | Must | FN-022 | UC-022 | FLOW-017 |
| US-037 | Must | FN-022 | UC-022 | FLOW-017 |
| US-038 | Must | FN-002, FN-023 | UC-002, UC-023 | FLOW-018 |
| US-039 | Must | FN-023 | UC-023 | FLOW-019 |
| US-040 | Must | FN-023 | UC-023 | FLOW-019 |
| US-041 | Must | FN-027 | UC-027 | FLOW-019 |
| US-042 | Must | FN-024 | UC-024 | FLOW-020 |
| US-043 | Must | FN-024 | UC-024 | FLOW-008, FLOW-020 |
| US-044 | Could | FN-025 | UC-025 | FLOW-021 |
| US-045 | Must | FN-010 | UC-010 | FLOW-008 |
| US-046 | Must | FN-010 | UC-010 | FLOW-004, FLOW-012 |
| US-047 | Could | FN-026 | UC-026 | FLOW-022 |

## 3. Test bổ sung theo business rule (BR → TC)
Cột "TC" = `TC-xxx.n` của các AC viện dẫn BR + TC bổ sung viện dẫn BR (cột "Truy vết" hoặc nội dung TC). BR không được AC/TC nào viện dẫn trực tiếp được kiểm bằng `TC-xxx.n` của các AC thuộc US nguồn (ghi "gián tiếp"). BR Won't giữ để truy vết, không có TC chạy.

| BR | Ưu tiên | US nguồn | AC | TC |
|---|---|---|---|---|
| BR-001 | Must/Should | US-001 | AC-001.5 | gián tiếp: TC-001.1..5 |
| BR-002 | Must/Should | US-001, US-004, US-027 | AC-001.1, AC-001.5 | TC-001.1, TC-UNI-19 |
| BR-003 | Must/Should | US-002 | AC-002.3, AC-002.5, AC-002.8 | TC-002.3, TC-002.8, TC-SRV-05, TC-UNI-21 |
| BR-004 | Must/Should | US-002 | AC-002.5 | gián tiếp: TC-002.1..8 |
| BR-005 | Must/Should | US-005 | AC-005.1, AC-005.2 | TC-005.1, TC-INS-05, TC-RPC-11, TC-SRV-13, TC-SRV-28, TC-SYN-24 |
| BR-006 | Must/Should | US-006 | AC-006.1, AC-006.2, AC-006.3 | TC-006.3 |
| BR-007 | Must/Should | US-001, US-027 | AC-001.4, AC-001.5, AC-027.2, AC-027.3 | TC-001.4, TC-027.2, TC-SRV-12 |
| BR-008 | Must/Should | US-027 | AC-002.8, AC-027.2, AC-027.3 | TC-027.3, TC-RPC-10, TC-SRV-12, TC-UNI-21 |
| BR-009 | Must/Should | US-001 | AC-001.4, AC-001.5 | TC-001.4 |
| BR-010 | Must/Should | US-009 | AC-009.1, AC-009.2, AC-009.4, AC-009.5 | TC-009.1 |
| BR-011 | Must/Should | US-009, US-012 | AC-009.2..5, AC-012.1, AC-012.2, AC-012.3, AC-031.1 | TC-009.3, TC-012.2, TC-031.1, TC-UNI-08 |
| BR-012 | Must/Should | US-012 | AC-012.1, AC-012.2, AC-012.3, AC-031.1 | TC-012.2, TC-031.1, TC-UNI-08 |
| BR-013 | Must/Should | US-012 | AC-012.1, AC-012.2, AC-012.3 | TC-012.1, TC-UNI-08 |
| BR-014 | Must/Should | US-008, US-017 | AC-008.1, AC-008.2, AC-017.1 | gián tiếp: TC-008.1, TC-008.2, TC-017.1, TC-017.2 |
| BR-015 | Must/Should | US-009, US-024, US-040 | AC-009.2, AC-009.4, AC-009.5, AC-024.1 | TC-INS-03, TC-SYN-23, TC-UNI-15 |
| BR-020 | Won't | US-013 | AC-013.1, AC-013.3, AC-013.4, AC-014.1 | — (Won't: không có TC riêng; TC-xxx.n của US nguồn chỉ kiểm phần CSV còn làm) |
| BR-021 | Must/Should | US-014 | AC-013.1, AC-014.1, AC-014.2 | TC-014.2, TC-UNI-10 |
| BR-022 | Must/Should | US-013 | AC-013.1..4, AC-014.1 | TC-013.2, TC-UNI-10 |
| BR-023 | Must/Should | US-014 | AC-013.1, AC-014.1, AC-021.1 | TC-021.1, TC-UNI-10 |
| BR-024 | Must/Should | US-013 | AC-013.1, AC-013.3, AC-013.4, AC-014.1 | TC-UNI-10 |
| BR-025 | Must/Should | US-013 | AC-013.1, AC-013.3, AC-013.4, AC-014.1 | TC-UNI-10 |
| BR-030 | Must/Should | US-019, US-003 | AC-003.1, AC-003.2, AC-019.1..4 | TC-003.2, TC-019.1 |
| BR-031 | Must/Should | US-018, US-042 | AC-018.1, AC-018.2, AC-018.3, AC-042.4 | TC-018.1, TC-UNI-09 |
| BR-032 | Won't | US-016, US-018 | AC-016.1, AC-016.2 | — (Won't: không có TC riêng; TC-xxx.n của US nguồn chỉ kiểm phần CSV còn làm) |
| BR-033 | Must/Should | US-017 | AC-012.1, AC-017.1, AC-017.2 | TC-017.2, TC-INS-09 |
| BR-040 | Could | US-022 | AC-022.1, AC-022.2 | TC-022.1 |
| BR-041 | Must/Should | US-023 | AC-023.1, AC-023.2, AC-023.3 | gián tiếp: TC-023.1, TC-023.2, TC-023.3 |
| BR-042 | Must/Should | US-012, US-018 | AC-012.3, AC-018.1, AC-018.2, AC-018.3, AC-042.1 | TC-UNI-06, TC-UNI-09 |
| BR-043 | Must/Should | US-028, US-029, US-030, US-031 | AC-028.1, AC-028.3, AC-029.1, AC-029.3, AC-030.1, AC-030.3, AC-030.4, AC-031.2 | TC-029.3, TC-DEG-07, TC-SRV-29, TC-SYN-28 |
| BR-044 | Must/Should | US-028, US-029 | AC-028.1, AC-028.2, AC-029.1 | TC-028.2, TC-SYN-07, TC-UNI-14 |
| BR-045 | Must/Should | US-029 | AC-029.1, AC-029.2, AC-040.2 | TC-029.2, TC-INS-04, TC-SYN-06 |
| BR-046 | Must/Should | US-028, US-023 | AC-023.1, AC-023.2, AC-023.3, AC-028.1, AC-028.3 | TC-028.3 |
| BR-047 | Must/Should | US-030, US-023, US-032, US-040 | AC-023.1, AC-023.2, AC-023.3, AC-030.1..4 | TC-030.2, TC-INS-08, TC-SYN-08, TC-UNI-15 |
| BR-048 | Must/Should | US-032 | AC-032.1, AC-032.3, AC-033.1 | TC-032.1, TC-UNI-17 |
| BR-049 | Won't | US-032 | AC-032.1 | — (Won't: không có TC riêng; TC-xxx.n của US nguồn chỉ kiểm phần CSV còn làm) |
| BR-050 | Must/Should | US-032 | AC-032.2, AC-032.3, AC-033.1 | TC-032.2, TC-INS-10, TC-SYN-22, TC-UNI-17 |
| BR-051 | Won't | US-033 | AC-032.3, AC-033.1, AC-033.2 | — (Won't: không có TC riêng; TC-xxx.n của US nguồn chỉ kiểm phần CSV còn làm) |
| BR-052 | Must/Should | US-034 | AC-034.1..4 | TC-034.1 |
| BR-053 | Could | US-022, US-034 | AC-022.2, AC-034.2, AC-034.3, AC-034.4 | gián tiếp: TC-022.1, TC-022.2, TC-034.1..4 |
| BR-054 | Must/Should | US-036 | AC-036.1, AC-036.2, AC-036.4 | TC-036.2 |
| BR-055 | Must/Should | US-036 | AC-036.1, AC-036.3, AC-036.4 | TC-036.3 |
| BR-056 | Won't | US-021 | AC-021.1 | TC-021.1 (không chạy) |
| BR-057 | Must/Should | US-001, US-002 | AC-001.1, AC-001.5, AC-002.1, AC-002.2, AC-002.5 | TC-001.1, TC-002.1, TC-SRV-04, TC-UNI-20 |
| BR-058 | Must/Should | US-002, US-004, US-027 | AC-002.4, AC-002.5, AC-004.1, AC-004.2, AC-027.2 | TC-002.4, TC-004.1, TC-027.2, TC-RPC-05, TC-SRV-08, TC-SRV-10, TC-SVC-08, TC-UNI-20 |
| BR-059 | Must/Should | US-002, US-039 | AC-002.5, AC-002.6, AC-004.2, AC-039.1 | TC-002.6, TC-004.2, TC-AND-09, TC-AND-10, TC-DEG-08, TC-RPC-16, TC-SYN-12, TC-SYN-13, TC-SYN-24, TC-UNI-16 |
| BR-060 | Must/Should | US-001, US-002 | AC-001.2, AC-001.3, AC-001.5, AC-002.5 | TC-001.2, TC-SRV-02, TC-UNI-11, TC-UNI-25 |
| BR-061 | Must/Should | US-001, US-003, US-042 | AC-001.1, AC-001.2, AC-001.3, AC-001.5, AC-003.1, AC-003.2, AC-042.4 | TC-001.1, TC-RPC-07, TC-SRV-02, TC-SRV-09, TC-UNI-11, TC-UNI-25, TC-UNI-28 |
| BR-062 | Must/Should | US-040 | AC-039.2, AC-040.1 | TC-040.1, TC-RPC-12, TC-SRV-15, TC-SRV-16, TC-SYN-03, TC-SYN-04, TC-SYN-20, TC-UNI-13, TC-UNI-24 |
| BR-063 | Must/Should | US-038, US-040 | AC-038.1, AC-038.2, AC-040.2 | TC-038.2, TC-040.2, TC-INS-04, TC-INS-07, TC-SRV-26, TC-SYN-06, TC-SYN-11, TC-UNI-13 |
| BR-064 | Must/Should | US-039 | AC-039.1, AC-039.3, AC-040.1 | TC-039.3, TC-AND-13, TC-RPC-13, TC-SRV-17, TC-SRV-18, TC-SRV-19, TC-SYN-01, TC-SYN-05, TC-SYN-15, TC-SYN-16, TC-SYN-26, TC-UNI-12, TC-UNI-13 |
| BR-065 | Must/Should | US-043 | AC-043.1, AC-043.2 | TC-043.1, TC-UNI-04 |
| BR-066 | Must/Should | US-043 | AC-043.1, AC-043.2 | TC-043.2, TC-UNI-04 |
| BR-067 | Must/Should | US-043 | AC-043.1, AC-043.2 | TC-043.2, TC-UNI-04 |
| BR-068 | Must/Should | US-043 | AC-043.3, AC-043.4 | TC-043.3, TC-043.4, TC-SRV-22, TC-SRV-31, TC-SYN-17, TC-UNI-05, TC-UNI-23 |
| BR-069 | Must/Should | US-042 | AC-042.1..4 | TC-042.1, TC-042.3, TC-SRV-32, TC-SRV-33, TC-UNI-06, TC-UNI-07 |
| BR-070 | Must/Should | US-045 | AC-045.1, AC-045.2 | TC-045.1, TC-UNI-01 |
| BR-071 | Must/Should | US-046 | AC-046.1, AC-046.2 | TC-046.1, TC-MAN-05, TC-UNI-03 |
| BR-072 | Could | US-047 | AC-047.1, AC-047.3 | TC-047.3, TC-UNI-02 |
| BR-073 | Must/Should | US-002 | AC-002.4, AC-002.5, AC-002.6 | TC-002.4, TC-SYN-12, TC-SYN-18 |
| BR-074 | Could | US-047 | AC-047.1, AC-047.2 | TC-047.2 |
| BR-075 | Could | US-044 | AC-044.1, AC-044.2 | gián tiếp: TC-044.1, TC-044.2 |
| BR-076 | Must/Should | US-002, US-027 | AC-002.1, AC-002.2, AC-002.5, AC-027.1, AC-027.3 | TC-027.1, TC-027.3, TC-RPC-09, TC-SRV-04, TC-SRV-06, TC-SRV-11, TC-UNI-25 |
| BR-077 | Must/Should | US-039, US-041 | AC-039.1, AC-039.3, AC-041.1, AC-041.2, AC-041.3 | TC-AND-08, TC-SVC-11, TC-SYN-02, TC-SYN-14, TC-SYN-21, TC-SYN-26, TC-UNI-16 |
| BR-078 | Must/Should | US-039, US-042 | AC-002.7, AC-004.3, AC-005.3, AC-027.4, AC-039.1, AC-042.4 | TC-MAN-01, TC-MAN-02 |

## 4. Phi chức năng (SRS (phi chức năng) → TC)
Nguồn: SRS (phi chức năng).md (các `NFR-xxx`), chỉ mục "Ma trận SRS (phi chức năng) → test case" của TESTING-STRATEGY và cột "Truy vết"/"Ngưỡng (SRS (phi chức năng))" của từng TC. SRS (phi chức năng) về độ trễ, RAM, `compose up`, kích thước APK, tốc độ đồng bộ nằm ở `TC-PERF`; suy giảm có kiểm soát ở `TC-DEG`; outbox ở `TC-OUT`; hợp đồng ở `TC-PROTO`/`TC-RPC`; bảo mật S9..S15 ở `TC-SEC`. Dấu † = liên kết gián tiếp (SRS (phi chức năng).md ghi cách kiểm chứng là UAT, TESTING-STRATEGY không nêu SRS (phi chức năng) đó); SRS (phi chức năng) chưa có TC ghi ở §8.

| SRS (phi chức năng) | Nội dung (rút gọn từ SRS (phi chức năng).md) | TC |
|---|---|---|
| NFR-A1 | Mọi chức năng học… | TC-NFR-A1 |
| NFR-A2 | 0 crash/ANR khi chạy toàn bộ kịch bản UAT trên 2 thiết bị chuẩn / báo cáo kiểm thử | TC-DEMO-06, TC-NFR-A2 |
| NFR-A3 | Mọi thao tác ghi nhiều bảng chạy trong một giao dịch | TC-AND-18, TC-NFR-A3 |
| NFR-A4 | Xoay màn hình, đưa app xuống nền, đổi theme hệ thống không mất trạng thái màn hình đang làm… | TC-AND-18, TC-NFR-A4 |
| NFR-A5 | Thông báo nhắc học hiện trong khoảng ±15 phút so với giờ đặt | TC-NFR-A5 |
| NFR-D1 | Android 10 (API 29) đến Android 17 (API 37) (A5, thay đổi phạm vi): minSdk = 29, targetSdk/compileSdk… | TC-NFR-D3 † |
| NFR-D2 | Điện thoại 5–6,9 inch, chỉ hướng dọc cho màn hình flashcard/quiz… | TC-NFR-D3 † |
| NFR-D3 | Kiểm thử trên emulator API 29 (tối thiểu), API 33 (quyền thông báo), API 37 + ít nhất 1 máy… | TC-NFR-D3 |
| NFR-E1 | Đáp ứng NFR-P2 với 5000 từ/100 chủ đề/10 người dùng trên máy; server đạt NFR-N4 với 50 tài khoản × 20.000 bản ghi | TC-PERF-17 |
| NFR-E2 | Thêm chủ đề/từ mới chỉ cần sửa file JSON ở content/ của repo server (tăng version) rồi chạy… | TC-034.3, TC-NFR-E2, TC-SRV-37 |
| NFR-E3 | Lược đồ có cơ chế nâng cấp giữ dữ liệu người dùng — app onUpgrade | TC-NFR-E3, TC-SRV-14, TC-SRV-55 |
| NFR-E4 | Một lệnh `docker compose up -d --wait` đưa MongoDB + 6 dịch vụ về `healthy` (≤ 60 s) | TC-DEMO-01, TC-PERF-13, TC-RPC-01, TC-SRV-48, TC-SRV-55, TC-SRV-56 |
| NFR-E5 | Ngân sách RAM từng container khi rảnh/dưới tải (edge ≤ 64 MB, auth ≤ 96, sync ≤ 128, content/gamification/insight ≤ 64) | TC-PERF-12 |
| NFR-L1 | Giao diện tiếng Việt | — (chưa có TC, xem §8) |
| NFR-M1 | Tách tầng giao diện / nghiệp vụ / truy cập dữ liệu ở app… | — (chưa có TC, xem §8) |
| NFR-M2 | Quy tắc nghiệp vụ có unit test, độ phủ nhánh ≥ 80% cho các lớp này — app (JUnit): Leitner,… | TC-NFR-M2 |
| NFR-M3 | Không có chuỗi giao diện viết cứng trong code/layout — tất cả nằm trong strings.xml /… | — (chưa có TC, xem §8) |
| NFR-N1 | Sau lần đăng nhập đầu, 100% kịch bản UAT nhóm học (FLOW-004..FLOW-006, FLOW-014..FLOW-017)… | TC-002.1, TC-039.1, TC-AND-19, TC-DEG-09, TC-DEMO-05, TC-MAN-01, TC-NFR-N1, TC-SYN-10 |
| NFR-N2 | Mọi lời gọi gRPC từ app đặt withDeadlineAfter (không có deadline = lỗi review): Health 3 s… | TC-001.5, TC-AND-06, TC-AND-07, TC-AND-08, TC-NFR-N2, TC-RPC-19, TC-RPC-28, TC-SVC-10, TC-SVC-11, TC-SYN-02, TC-SYN-21, TC-SYN-26, TC-UNI-16 |
| NFR-N3 | Đẩy một lô 500 bản ghi + kéo 500 bản ghi hoàn tất ≤ 5 s qua Wi-Fi LAN hoặc emulator tới… | TC-038.1, TC-041.2, TC-PERF-16, TC-SYN-01, TC-SYN-09 |
| NFR-N4 | p95 thời gian xử lý đo tại edge… | TC-DEG-03, TC-PERF-01..11, TC-PERF-17, TC-SRV-25, TC-SRV-47, TC-SVC-06 |
| NFR-N5 | Khi MongoDB hoặc một dịch vụ phụ ngừng, lõi vẫn chạy và app thấy đúng hành vi suy giảm: (a)… | TC-042.4, TC-AND-16, TC-DEG-01..13, TC-DEMO-09, TC-OUT-05, TC-RPC-20, TC-SRV-40, TC-SRV-47, TC-SVC-14, TC-SYN-27 |
| NFR-N6 | Sự kiện outbox (XP, hồ sơ, xóa tài khoản, tiến độ…) tới đích p95 ≤ 5 s kể từ commit khi… | TC-OUT-01, TC-OUT-02, TC-OUT-03, TC-OUT-05, TC-OUT-06, TC-OUT-08, TC-OUT-12, TC-PERF-18, TC-SYN-27, TC-UNI-28 |
| NFR-N7 | Message vào edge ≤ 1 MiB… | TC-AND-13, TC-AND-20, TC-RPC-14, TC-RPC-26, TC-SEC-16 |
| NFR-O1 | x-request-id đầu–cuối: app sinh UUID mỗi lời gọi → edge nhận lại nếu hợp lệ… | TC-AND-05, TC-NFR-O1, TC-RPC-29, TC-SEC-20, TC-SEC-39, TC-SRV-41, TC-SVC-09 |
| NFR-P1 | Khởi động lạnh đến khi hiện Trang chủ ≤ 2 giây (lần mở thứ 2 trở đi) | TC-AND-01, TC-NFR-P1, TC-PERF-14, TC-PERF-15 |
| NFR-P2 | Mọi truy vấn CSDL trên luồng giao diện bị cấm | TC-AND-11, TC-NFR-P2, TC-PERF-17 |
| NFR-P3 | Kết quả tra hiện ≤ 300 ms sau khi ngừng gõ (debounce 250 ms) | TC-NFR-P3 |
| NFR-P4 | Cuộn danh sách từ (RecyclerView) và lật thẻ mượt, không quá 5% khung hình chậm (jank) /… | TC-NFR-P4 |
| NFR-P5 | APK bản release ≤ 30 MB | TC-NFR-P5, TC-PERF-14, TC-PERF-15 |
| NFR-P6 | Sprite pixel (ngọn lửa, rương, cúp, bản đồ) không làm rớt khung hình: ở SCR-05, SCR-10,… | TC-NFR-P6 |
| NFR-P7 | Thêm grpc-okhttp, grpc-protobuf-lite, grpc-stub, protobuf-javalite (+ okio) làm APK release… | TC-AND-22, TC-PERF-14, TC-PERF-15 |
| NFR-Q1 | App: ./gradlew assembleDebug và ./gradlew lint không lỗi, không cảnh báo mức Error,… | TC-NFR-Q1 |
| NFR-R1 | Chỉ thu email (đăng nhập, không gửi thư), biệt danh công khai (bảng xếp hạng), tên hiển thị… | TC-MAN-04, TC-SRV-42 |
| NFR-R2 | Dữ liệu chỉ lưu trên máy và ở nơi chạy docker compose của nhóm (nơi triển khai chốt ở DEC-023) | — (chưa có TC, xem §8) |
| NFR-R3 | Xóa tài khoản xóa ngay users/token/câu trả lời ở auth.db (CASCADE, BR-005) và dữ liệu trên máy | TC-NFR-R3 |
| NFR-R4 | Quyền hệ thống chỉ gồm INTERNET, ACCESS_NETWORK_STATE, POST_NOTIFICATIONS,… | TC-MAN-10 |
| NFR-S1 | Mật khẩu và câu trả lời bảo mật chỉ được băm trong dịch vụ auth bằng PBKDF2-HMAC-SHA256… | TC-NFR-S1, TC-PERF-02, TC-SEC-01..07, TC-SEC-09, TC-SEC-19, TC-SRV-01, TC-UNI-19..22 |
| NFR-S2 | 100% truy vấn có tham số — app: selectionArgs/SQLiteStatement | TC-NFR-S2, TC-SEC-01..07, TC-SEC-09, TC-SEC-19, TC-UNI-19..22 |
| NFR-S3 | Chỉ Activity khởi chạy và BroadcastReceiver khởi động máy exported | TC-AND-02, TC-AND-03, TC-AND-15, TC-AND-17, TC-DEMO-03, TC-MAN-09, TC-NFR-S3, TC-SEC-10, TC-SEC-26, TC-SEC-38 |
| NFR-S4 | App: android:allowBackup="false" và android:dataExtractionRules… | TC-AND-14, TC-NFR-S4, TC-SEC-08, TC-SEC-37 |
| NFR-S5 | Mọi truy vấn dữ liệu người dùng ở dịch vụ lọc theo user_id lấy từ metadata x-user-id do… | TC-INS-06, TC-NFR-S5, TC-RPC-15, TC-SRV-20, TC-SRV-27, TC-SVC-03, TC-SYN-19 |
| NFR-S6 | Đạt các mục áp dụng mức cơ bản của OWASP MASVS (MASVS-STORAGE, CRYPTO, AUTH, NETWORK, CODE)… | TC-NFR-S6 |
| NFR-S7 | Token = 32 byte từ nguồn ngẫu nhiên của hệ điều hành (OsRng), mã hóa base64url không đệm 43… | TC-AND-04, TC-NFR-S7, TC-RPC-23, TC-SEC-01..07, TC-SEC-09, TC-SEC-19, TC-SRV-08, TC-SRV-46, TC-SVC-05, TC-SVC-06, TC-UNI-16, TC-UNI-19..22 |
| NFR-S8 | Login tối đa 5 yêu cầu/phút/IP tại edge… | TC-002.3, TC-NFR-S8, TC-RPC-02, TC-RPC-27, TC-SEC-01..07, TC-SEC-09, TC-SEC-19, TC-SRV-49, TC-UNI-19..22 |
| NFR-S9 | Chỉ edge lộ cổng — compose chỉ publish 127.0.0.1:50051… | TC-SEC-25, TC-SEC-27, TC-SEC-28, TC-SRV-14, TC-SRV-36, TC-SVC-15 |
| NFR-S10 | cargo audit (RustSec) 0 lỗ hổng mức cao/nghiêm trọng chưa xử lý trên Cargo.lock tại mỗi PR… | TC-SEC-35, TC-SEC-37 |
| NFR-S11 | Mọi RPC nội bộ (5 dịch vụ sau edge, kể cả EventSink, Export*, GetDailyStats) yêu cầu… | TC-SEC-24, TC-SEC-30, TC-SEC-40, TC-SVC-01..04, TC-UNI-27 |
| NFR-S12 | Bản release không thể gọi gRPC plaintext: GrpcChannelFactory mặc định TLS, usePlaintext()… | TC-AND-02, TC-AND-03, TC-AND-17, TC-MAN-09, TC-SEC-10, TC-SEC-26, TC-SEC-38 |
| NFR-S13 | Dữ liệu MongoDB không chứa email, hash mật khẩu, token, câu trả lời bảo mật, tên hiển thị | TC-OUT-14, TC-SEC-33, TC-SRV-42 |
| NFR-S14 | Mọi container dịch vụ chạy không root (USER 10001:10001), read_only: true (+ tmpfs: /tmp),… | TC-SEC-36 |
| NFR-S15 | Sau DeleteAccount, dữ liệu của user ở sync.db (9 bảng), learninge_game… | TC-OUT-13, TC-SEC-18, TC-SEC-32, TC-SRV-43 |
| NFR-V1 | Tương thích ngược proto v1: chỉ cộng thêm… | TC-PROTO-01..09, TC-RPC-31, TC-RPC-32 |
| NFR-X1 | Vùng chạm ≥ 48 × 48 dp | TC-MAN-08, TC-NFR-X1 |

## 5. Bảo mật (TM → TC-SEC)
Nguồn: THREAT_MODEL (TM-01..TM-50; ca TC-SEC-01..TC-SEC-40) đối chiếu cột "Mối đe dọa" của TESTING-STRATEGY §TC-SEC, thêm các TC khác (TC-RPC/SRV/SVC/OUT/AND…) viện dẫn mã TM. Dấu † = liên kết gián tiếp theo nội dung TC (TESTING-STRATEGY không ghi mã TM), người kiểm thử xác nhận ở §8.

| TM | Mối đe dọa (rút gọn) | Mức | TC-SEC | TC khác |
|---|---|---|---|---|
| TM-01 | S: Dò mật khẩu / credential stuffing | Cao | TC-SEC-01, TC-SEC-02 | — |
| TM-02 | S: Dò câu trả lời bảo mật để chiếm tài khoản | Cao | TC-SEC-03 | — |
| TM-03 | I: Dò email đã đăng ký | TB | TC-SEC-04, TC-SEC-05 | TC-SRV-06, TC-UNI-25 |
| TM-04 | S/D: Giả `x-forwarded-for` né giới hạn tần suất; sai cấu hình proxy tin cậy | Cao | TC-SEC-06 | TC-SRV-49, TC-UNI-22 |
| TM-05 | D: Làm cạn CPU bằng PBKDF2 | Cao | TC-SEC-01, TC-SEC-07 | TC-SRV-07 |
| TM-06 | I: Lộ token trên máy | TB | TC-SEC-08 | TC-AND-14 |
| TM-07 | S: Dùng lại token bị đánh cắp | TB | TC-SEC-09 | — |
| TM-08 | I: Lộ file SQLite / sao lưu / mongodump | Cao | TC-SEC-08, TC-SEC-33 † | — |
| TM-09 | E: Token cũ còn dùng được sau đổi mật khẩu/xóa tài khoản | TB | TC-SEC-09, TC-SEC-29 | — |
| TM-10 | I: Lớp lộ ra thấy bản rõ | TB | TC-SEC-38 | TC-DEMO-02 |
| TM-11 | I/T: Nghe lén h2c (LAN, cấu hình sai) | Cao | TC-SEC-10, TC-SEC-26 | TC-DEMO-03 |
| TM-12 | S: Server giả / host bị đổi | TB | TC-SEC-26 † | — |
| TM-13 | E: Vượt lớp lộ ra, gọi thẳng edge/cổng nội bộ | TB | TC-SEC-06, TC-SEC-27 | TC-DEMO-02, TC-SVC-15 |
| TM-14 | I/T: IDOR/BOLA trong đồng bộ | Cao | TC-SEC-11, TC-SEC-12 | TC-RPC-15, TC-SRV-20, TC-SVC-16 |
| TM-15 | T/E: Gán hàng loạt qua trường do server quản lý | Cao | TC-SEC-13 | TC-SRV-21 |
| TM-16 | T: Gian lận XP | Cao | TC-SEC-14 | TC-UNI-23 |
| TM-17 | T: Thao túng LWW bằng `updated_at_ms` tương lai | Thấp | TC-SEC-15 | TC-SRV-23, TC-SYN-20, TC-UNI-24 |
| TM-18 | D: Message lớn, chuỗi dài, quá nhiều bản ghi | TB | TC-SEC-16 | TC-RPC-26, TC-SRV-19 |
| TM-19 | D: Nghẽn khóa ghi SQLite | TB | TC-SEC-17 | TC-RPC-17, TC-SRV-25 |
| TM-20 | T/E: SQL injection / NoSQL injection | Cao | TC-SEC-19 | TC-RPC-22, TC-SRV-39 |
| TM-21 | T/R: Chèn dòng log giả | Thấp | TC-SEC-20 | TC-SRV-51, TC-SVC-09, TC-UNI-26 |
| TM-22 | I: Lỗi lộ chi tiết nội bộ | TB | TC-SEC-21 | TC-SRV-53, TC-UNI-26 |
| TM-23 | I: Bí mật/CSDL bị commit lên repo | Cao | TC-SEC-37 | — |
| TM-24 | I: Log chứa PII/token/mật khẩu | TB | TC-SEC-22 | — |
| TM-25 | T/D: Hỏng/mất file SQLite | TB | TC-SEC-23 | TC-DEG-12, TC-SRV-14, TC-SRV-24, TC-SRV-56 |
| TM-26 | R: Chối bỏ thao tác tài khoản | Thấp | — | TC-OUT-03, TC-SRV-41, TC-SRV-43, TC-SRV-45 † |
| TM-27 | I: Xóa tài khoản không đầy đủ trong `auth.db`/`sync.db` | TB | TC-SEC-18, TC-SEC-32 | — |
| TM-28 | I: Quyền riêng tư (PDPL) | TB | TC-SEC-18, TC-SEC-32 | — |
| TM-29 | T: Chuỗi cung ứng dependency **app** (Gradle) | TB | TC-SEC-35 | TC-PROTO-04 |
| TM-30 | T: Nội dung server bị sửa/độc hại | Thấp | — | — (chưa có TC, xem §8) |
| TM-31 | S: Tài khoản demo mật khẩu biết trước trên URL công khai | Thấp | — | TC-DEMO-01 † |
| TM-32 | S: Token còn hiệu lực sau đăng xuất ngoại tuyến | Thấp | TC-SEC-09 | — |
| TM-33 | S/E: Giả mạo giữa dịch vụ — gọi thẳng dịch vụ nội bộ với `x-user-id` tùy ý | Cao | TC-SEC-24 | TC-RPC-25, TC-SVC-02, TC-SVC-03, TC-SVC-04, TC-UNI-27 |
| TM-34 | I: gRPC reflection / health liệt kê dịch vụ, RPC nội bộ | Thấp | TC-SEC-25 | TC-RPC-01, TC-RPC-25 |
| TM-35 | I/T: App release gọi gRPC plaintext (`grpc-okhttp` bỏ qua… | Cao | TC-SEC-26 | TC-AND-02, TC-AND-03 |
| TM-36 | I/T: Kết thúc TLS và h2c ở lớp lộ ra theo từng phương án | TB | TC-SEC-38 | — |
| TM-37 | E: Token đã thu hồi vẫn dùng được nhờ cache 60 s ở edge | TB | TC-SEC-29 | TC-SVC-07 |
| TM-38 | S/T: Đầu độc outbox / `EventSink.Publish` giả (XP giả, `user.deleted` giả) | TB | TC-SEC-30 | TC-SVC-13, TC-UNI-28 |
| TM-39 | T: Phát lại / trùng / lệch thứ tự sự kiện làm "hồi sinh" dữ liệu người đã xóa | TB | TC-SEC-31 | TC-OUT-03, TC-OUT-07, TC-SRV-28, TC-SRV-34 |
| TM-40 | D: Outbox phình khi bên nhận ngừng lâu | Thấp | — | TC-OUT-09, TC-OUT-11, TC-UNI-28 † |
| TM-41 | I: Xóa tài khoản lan sang Mongo chậm/thiếu | TB | TC-SEC-32 | TC-OUT-13, TC-SRV-43 |
| TM-42 | I: Gỡ bút danh dữ liệu insight | Thấp | TC-SEC-33 | TC-OUT-14, TC-SRV-42, TC-UNI-29 |
| TM-43 | I/E: MongoDB lộ cổng/không xác thực/quyền rộng | Cao | TC-SEC-27, TC-SEC-28 | TC-SRV-36, TC-SVC-15 |
| TM-44 | D: DoS bằng protobuf/HTTP/2 (message lớn, nhiều phần tử, header lớn, nhiều… | Cao | TC-SEC-34 | TC-DEG-13, TC-RPC-26, TC-RPC-28, TC-SRV-44, TC-SRV-50 |
| TM-45 | T: Chuỗi cung ứng **server** (crate, `protoc` vendored, image Docker) | TB | TC-SEC-35 | — |
| TM-46 | E: Container chạy root, FS ghi được, quyền thừa | TB | TC-SEC-36 | — |
| TM-47 | I: Bí mật lộ qua repo, image, log, `docker inspect`, nền tảng | Cao | TC-SEC-33, TC-SEC-37 | — |
| TM-48 | I: PII/token lọt log do in nguyên message proto (`Debug`) | TB | TC-SEC-39 | — |
| TM-49 | I: Lạm dụng RPC vận hành/xuất hàng loạt (`Export*`, `GetDailyStats`, `requeue`) | TB | TC-SEC-40 | TC-SRV-30, TC-SRV-44 |
| TM-50 | E: Chiếm bề mặt quản trị hạ tầng (SSH VPS, tài khoản Railway/Atlas/tunnel,… | TB | TC-SEC-27, TC-SEC-38 | — |

## 6. RPC → TC-RPC / TC-SRV
**RPC công khai** (`learninge.app.v1`, do `edge` phục vụ): mọi EP có ≥ 1 `TC-RPC` (qua `edge`) và ≥ 1 `TC-SRV` (dịch vụ đích). Cột "TC khác" gồm TC-AND (app), TC-SYN, TC-SVC, TC-DEG, TC-PERF, TC-SEC… viện dẫn EP đó.

| EP | RPC | Quyền | AC phục vụ | TC-RPC | TC-SRV | TC khác |
|---|---|---|---|---|---|---|
| EP-01 | `grpc.health.v1.Health/Check` | Công khai | — | TC-RPC-01, TC-RPC-24, TC-RPC-33 | TC-SRV-48 | TC-AND-04, TC-AND-21, TC-PERF-01 |
| EP-02 | `AuthService/Register` | Công khai | AC-001.1..5 | TC-RPC-02 | TC-SRV-01, TC-SRV-07 | TC-PERF-02 |
| EP-03 | `AuthService/Login` | Công khai | AC-002.1, AC-002.2, AC-002.3, AC-002.6, AC-002.7, AC-002.8, AC-038.2 | TC-RPC-03 | TC-SRV-04 | TC-PERF-02, TC-SEC-05 |
| EP-04 | `AuthService/Logout` | Token | AC-002.4 | TC-RPC-05, TC-RPC-23 | TC-SRV-08 | — |
| EP-05 | `AuthService/GetMe` | Token | AC-003.1 | TC-RPC-06 | TC-SRV-47 | TC-PERF-04 |
| EP-06 | `AuthService/UpdateMe` | Token | AC-003.2, AC-004.3, AC-005.3 | TC-RPC-07 | TC-SRV-09 | — |
| EP-07 | `AuthService/ChangePassword` | Token | AC-004.1, AC-004.2, AC-004.3, AC-005.3 | TC-RPC-08 | TC-SRV-10 | TC-AND-10 |
| EP-08 | `AuthService/GetResetQuestion` | Công khai | AC-027.1, AC-027.4 | TC-RPC-09 | TC-SRV-11 | TC-SEC-04 |
| EP-09 | `AuthService/ResetPassword` | Công khai | AC-027.2, AC-027.3, AC-027.4 | TC-RPC-10 | TC-SRV-12 | TC-SEC-03, TC-SEC-05 |
| EP-10 | `AuthService/DeleteAccount` | Token | AC-004.2, AC-004.3, AC-005.1, AC-005.3 | TC-RPC-11 | TC-SRV-13 | TC-AND-10 |
| EP-11 | `SyncService/Push` | Token | AC-038.2, AC-039.2, AC-039.3, AC-040.1, AC-040.2 | TC-RPC-12 | TC-SRV-15 | TC-PERF-06 |
| EP-12 | `SyncService/Pull` | Token | AC-002.5, AC-038.1, AC-038.2, AC-039.3, AC-040.1, AC-040.2 | TC-RPC-18 | TC-SRV-26 | TC-PERF-07 |
| EP-13 | `LeaderboardService/GetWeekly` | Token | AC-005.1, AC-042.1..4, AC-043.4, AC-044.2 | TC-RPC-11, TC-RPC-20 | TC-SRV-32 | TC-AND-16, TC-OUT-02, TC-OUT-13, TC-PERF-08, TC-SEC-32 |
| EP-14 | `ContentService/GetManifest` | Token | AC-042.4, AC-044.1, AC-044.2 | TC-RPC-21 | TC-SRV-38, TC-SRV-40 | TC-AND-16 |
| EP-15 | `ContentService/GetDeck` | Token | AC-044.1 | TC-RPC-22 | TC-SRV-39 | — |
| EP-16 | `ContentService/GetExam` | Token | AC-044.1 | TC-RPC-22, TC-RPC-33 | TC-SRV-39 | — |

**RPC nội bộ** (`learninge.internal.v1`, app không có stub): kiểm ở ranh giới tin cậy TB-4 bằng `TC-SVC` (khóa `x-internal-auth` ở **mọi** RPC nội bộ — TC-SVC-01 sinh ca từ descriptor), `TC-SRV` (dịch vụ đích), `TC-OUT` (`EventSink`) và `TC-DEG`.

| RPC nội bộ (API_CONTRACT) | Mục đích | TC-SVC | TC-SRV / TC-OUT / TC-DEG liên quan |
|---|---|---|---|
| `TokenValidator/ValidateToken` | edge → auth | TC-SVC-01, TC-SVC-03, TC-SVC-05, TC-SVC-06, TC-SVC-10 | TC-SRV-08 |
| `AuthBackend/Register`, `Login`, `UpdateMe`, `ChangePassword`, `GetResetQuestion`, `ResetPassword`, `DeleteAccount` | edge → auth | TC-SVC-01 | TC-SRV-01..06, TC-SRV-09..13 |
| `AuthBackend/Logout` | edge → auth | TC-SVC-01 | TC-SRV-08 |
| `AuthBackend/GetProfile` | edge → auth | TC-SVC-01 | TC-SRV-09 |
| `AuthBackend/ExportProfiles` | gamification (lệnh `rebuild`) → auth | TC-SVC-01 | — |
| `SyncBackend/Push`, `Pull` | edge → sync | TC-SVC-01, TC-SVC-04, TC-SVC-16 | TC-SRV-15..28 |
| `SyncBackend/GetStreak` | edge → sync | TC-SVC-01 | TC-SRV-30 |
| `SyncBackend/ExportXpEvents`, `ExportUserStats` | gamification (`rebuild`) → sync | TC-SVC-01 | TC-SRV-30 |
| `ContentBackend/GetManifest`, `GetDeck`, `GetExam` | edge → content | TC-SVC-01 | TC-SRV-38, TC-SRV-39 |
| `GamificationBackend/GetWeekly` | edge → gamification | TC-SVC-01, TC-SVC-16 | TC-SRV-32, TC-SRV-33 |
| `GamificationBackend/GetStats` | edge → gamification | TC-SVC-01, TC-SVC-16 | — |
| `EventSink/Publish` | relay của auth, sync → sync, gamification, insight | TC-SVC-01, TC-SVC-13 | TC-SRV-28, TC-SRV-29, TC-SRV-31, TC-SRV-34, TC-SRV-41 |
| `InsightBackend/LogRequests` | edge → insight | TC-SVC-01 | TC-SRV-44 |
| `InsightBackend/GetDailyStats` | người vận hành (`grpcurl`) → insight | TC-SVC-01 | TC-SRV-44 |
| `grpc.health.v1.Health/Check` | edge, relay → mọi dịch vụ | TC-SVC-01, TC-SVC-14 | TC-SRV-48 |

## 7. Tóm tắt độ phủ
Số liệu đếm từ chính các tài liệu nguồn ở ngày cập nhật.

| Chỉ số | Giá trị |
|---|---|
| REQ | 24 (Must 17, Should 1, Could 3: REQ-009, REQ-022, REQ-024; Won't 3: REQ-008, REQ-013, REQ-015) |
| User story có trong ma trận | 47 / 47 (Must 41, Should 1, Could 3, Won't 2) |
| AC có trong ma trận | 130 / 130 (trong đó 3 AC Won't: AC-020.1, AC-020.2, AC-021.1; 7 AC Could) |
| **AC không có TC** | **0** |
| TC `TC-xxx.n` không có AC | 0 (130 TC ↔ 130 AC) |
| AC có thêm ≥ 1 TC bổ sung | 65 / 130 |
| BR đã định nghĩa | 64 (trong BR-001..BR-078, một số số không dùng); Won't: BR-049, BR-056; Could: BR-040, BR-053, BR-075 |
| BR gắn ít nhất một AC | 64 / 64 |
| BR được TC viện dẫn trực tiếp | 57 / 64; 7 BR còn lại kiểm gián tiếp qua `TC-xxx.n` của US nguồn |
| BR không có AC lẫn TC | 0 |
| FLOW có US | 22 / 22 (FLOW-001..FLOW-022); US không có FLOW: US-026 |
| FN và UC có US | FN 27 / 27; UC 27 / 27 |
| RPC công khai có TC-RPC và TC-SRV | 16 / 16 (EP-01..EP-16) |
| RPC nội bộ có TC-SVC | 15 / 15 dòng RPC nội bộ (TC-SVC-01 sinh ca từ descriptor `learninge.internal.v1`) |
| SRS (phi chức năng) có TC | 48 / 54 trực tiếp; +2 gián tiếp † (NFR-D1, NFR-D2); 4 chưa có (NFR-L1, NFR-M1, NFR-M3, NFR-R2) |
| TM có TC | 44 / 50 trực tiếp; +5 gián tiếp † (TM-08, TM-12, TM-26, TM-31, TM-40); 1 chưa có (TM-30) |
| TC bổ sung | 345 (UNI 29, INS 12, SRV 56, RPC 34, SVC 16, OUT 16, DEG 13, PROTO 9, AND 22, SYN 28, MAN 11, SRS (phi chức năng) 30, PERF 19, SEC 40, DEMO 10) |
| Tổng TC | 475 = 130 `TC-xxx.n` + 345 bổ sung; **chạy 472** (không chạy 3 TC Won't của ngữ pháp: TC-020.1, TC-020.2, TC-021.1) — khớp TESTING-STRATEGY §Số lượng test case (472) |
| TC bổ sung trỏ tới ≥ 1 AC | 80 / 345; phần còn lại trỏ tới BR, SRS (phi chức năng), TM, EP, FLOW hoặc tài liệu thiết kế (DATABASE-DESIGN, SYSTEM-BLUEPRINT, DEC, rủi ro) |
| TC không truy vết tới AC/BR/SRS (phi chức năng)/TM/EP/FLOW/US | 20 (chỉ trỏ tới tài liệu thiết kế, DEC, rủi ro hoặc mốc: TC-AND-12, TC-DEMO-07, TC-DEMO-08, TC-DEMO-10, TC-INS-01, TC-MAN-03, TC-MAN-11, TC-OUT-04, TC-OUT-09, TC-OUT-10, TC-OUT-11, TC-OUT-15, TC-OUT-16, TC-PERF-19, TC-RPC-30, TC-SRV-35, TC-SRV-45, TC-SRV-52, TC-SRV-54, TC-SVC-12) |
| Task trong ma trận | 53 ID task app + 6 ID task server (T-146, T-147, T-148, T-149, T-150, T-153) |

### REQ → số US, AC, TC

| REQ | Ưu tiên | US | AC | TC `TC-xxx.n` | TC bổ sung viện dẫn AC của REQ |
|---|---|---|---|---|---|
| REQ-001 | Must | 5 | 21 | 21 | 24 |
| REQ-002 | Must | 1 | 3 | 3 | 0 |
| REQ-003 | Must | 5 | 13 | 13 | 0 |
| REQ-004 | Must | 1 | 3 | 3 | 2 |
| REQ-005 | Must | 3 | 8 | 8 | 2 |
| REQ-006 | Must | 3 | 7 | 7 | 2 |
| REQ-007 | Must | 1 | 4 | 4 | 0 |
| REQ-008 | Won't | 2 | 3 | 3 | 0 |
| REQ-009 | Could | 1 | 2 | 2 | 0 |
| REQ-010 | Must | 2 | 4 | 4 | 0 |
| REQ-011 | Must | 1 | 2 | 2 | 2 |
| REQ-012 | Should | 1 | 1 | 1 | 0 |
| REQ-013 | Won't | 0 | 0 | 0 | 0 |
| REQ-014 | Must | 1 | 4 | 4 | 5 |
| REQ-015 | Won't | 0 | 0 | 0 | 0 |
| REQ-016 | Must | 4 | 12 | 12 | 2 |
| REQ-017 | Must | 2 | 5 | 5 | 3 |
| REQ-018 | Must | 1 | 4 | 4 | 0 |
| REQ-019 | Must | 3 | 7 | 7 | 2 |
| REQ-020 | Must | 4 | 10 | 10 | 17 |
| REQ-021 | Must | 2 | 8 | 8 | 14 |
| REQ-022 | Could | 1 | 2 | 2 | 6 |
| REQ-023 | Must | 2 | 4 | 4 | 5 |
| REQ-024 | Could | 1 | 3 | 3 | 1 |

## 8. Khoảng trống phát hiện
| Loại | ID | Cách xử lý |
|---|---|---|
| Yêu cầu không có US | REQ-013 Dark mode, REQ-015 Widget | **Won't** (thay đổi phạm vi C2; thay đổi phạm vi); task T-070 Hủy, T-071 Won't; không TC |
| Yêu cầu Won't (có US giữ để truy vết) | REQ-008 Ngữ pháp → US-020, US-021 (AC-020.1, AC-020.2, AC-021.1), BR-056, FLOW-006, FN-012, T-014/T-060 Hủy | Không làm; 3 TC (TC-020.1, TC-020.2, TC-021.1) không chạy; không thuộc rubric |
| Yêu cầu Could | REQ-009 (US-022), REQ-022 (US-044), REQ-024 (US-047) | Chỉ làm khi còn dư (T-061, T-123 + phần server, T-133); TC chạy khi làm |
| Lệch ưu tiên REQ-022 | PRD ghi **Could**; thay đổi phạm vi §3 và TESTING-STRATEGY (KG-10) ghi Should cho US-044 (TC-044.1/044.2 tạm tính Trung bình) | Ma trận theo PRD (Could). nhóm trưởng đồng bộ PRD/PROJECT-PLAN (công việc); không ảnh hưởng truy vết |
| BR Won't / Could | Won't: BR-049 (import JSON, thay đổi phạm vi C6), BR-056 (bài tập ngữ pháp); Could: BR-040, BR-053 (tra từ), BR-075 (nội dung từ server) | BR Won't không có TC chạy, gắn US chỉ để truy vết |
| US không có BR | US-015, US-025, US-026 | Hành vi đơn giản, đủ mô tả bằng AC (SRS không có BR riêng) |
| US không có FLOW | US-026 Giới thiệu | Màn hình tĩnh |
| AC lấy RPC theo FN | 13 AC có từ khóa mạng/máy chủ nhưng không TC nào nêu EP (cột RPC lấy EP của FN) | người kiểm thử đối chiếu khi rà TESTING-STRATEGY; không đổi độ phủ |
| SRS (phi chức năng) chưa có TC | NFR-L1, NFR-M1, NFR-M3, NFR-R2 (NFR-L1 bản địa hóa, NFR-M1 tách tầng, NFR-M3 không chuỗi cứng, NFR-R2 nơi lưu dữ liệu) | SRS (phi chức năng).md ghi cách kiểm chứng là kiểm tay/lint/review (M3 qua Android Lint `HardcodedText` trong TC-NFR-Q1; M1 qua review phụ thuộc gói). người kiểm thử xác nhận hoặc thêm `TC-NFR-L1/M1/M3/R2`; không chặn G4 |
| SRS (phi chức năng) liên kết gián tiếp (†) | NFR-D1, NFR-D2 | SRS (phi chức năng).md: "Chạy kịch bản UAT trên các cấu hình"; TESTING-STRATEGY chỉ có TC-NFR-D3 (UAT API 29/33/37 + máy thật). người kiểm thử ghi rõ NFR-D1/D2 vào TC-NFR-D3 |
| TM chưa có TC | TM-30 (nội dung `content/*.json` bị sửa/độc hại; rủi ro còn lại RC-10) | Giảm nhẹ bằng quy trình (nội dung qua PR) và validator Mongo; người kiểm thử thêm ca kiểm validator/`content-import` vào TC-SRV hoặc nhóm trưởng chấp nhận RC-10 |
| TM liên kết gián tiếp (†) | TM-08, TM-12, TM-26, TM-31, TM-40 | Liên kết theo nội dung TC (TC-SEC-08/33, TC-SEC-26, TC-SRV-41/43/45 + TC-OUT-03, TC-DEMO-01, TC-OUT-09/11 + TC-UNI-28) vì TESTING-STRATEGY không ghi mã TM ở các ca này; người kiểm thử bổ sung mã TM vào cột "Truy vết" |
| TC chỉ trỏ tới thiết kế | 20 TC bổ sung (xem §7) trỏ tới DATABASE-DESIGN/SYSTEM-BLUEPRINT/API_CONTRACT/DEC/rủi ro/mốc, chưa nêu AC/BR/SRS (phi chức năng)/TM | Không phải TC mồ côi; người kiểm thử có thể thêm mã BR/SRS (phi chức năng)/TM khi rà |
| TC-NFR-IMP | Hiệu năng import CSV 2.000 dòng (xem trước ≤ 2 s, nhập ≤ 3 s) không có SRS (phi chức năng) tương ứng trong SRS (phi chức năng).md | Gắn tạm với US-032/US-033 (BR-048, BR-050); nhóm trưởng/lập trình viên thêm SRS (phi chức năng) import vào SRS (phi chức năng).md hoặc người kiểm thử đổi tên TC |
| TESTING-STRATEGY KG-9 và KG-12 đã cũ | KG-9 ("NFR-S11..S15 chưa có trong SRS (phi chức năng).md") và KG-12 (cache token sau ResetPassword): SRS (phi chức năng).md hiện đã có NFR-S11..S15 và NFR-S7 đã chốt "edge xóa cache ngay" | người kiểm thử đóng KG-9, KG-12 ở TESTING-STRATEGY; ma trận này dùng SRS (phi chức năng).md hiện hành |
| Phụ thuộc quyết định chưa chốt | KG-7 (DEC-023, nơi triển khai P1..P4): TC-SEC-38, TC-SEC-27, TC-DEMO-02, TC-PERF-19, TC-AND-17; KG-8: công cụ dev/test (`testcontainers`, `cargo-llvm-cov`, `cargo deny`, `grpc-testing`…) chưa duyệt | Không chặn truy vết; các TC giữ ở "Chưa chạy" tới khi quyết định chốt |
| Task (đã giải quyết) | PROJECT-PLAN (công việc) thay đổi phạm vi đã chốt (T-140..T-155). Nhãn chờ đã thay bằng ID thật; task server Javalin/REST cũ (đã hủy) thay bằng T-141, T-145, T-147, T-148, T-149, T-150, T-152, T-153, T-154 | **Đã giải quyết** (12/11): 37 dòng AC có task server, tra theo US/RPC/TC (T-146, T-147, T-148, T-149, T-150, T-153); không đánh lại số ID |
| Task chưa gắn US/AC | T-001, T-002, T-005, T-082, T-083, T-085, T-101, T-102, T-104, T-105 và các task hạ tầng server thay đổi phạm vi | Task nền tảng/hạ tầng, không sinh AC riêng; được kiểm bằng TC-DEMO/TC-SRS (phi chức năng)/TC-PERF |
| Mục rubric | 10 mục chức năng của "File báo cáo kết quả dự án" | Cơ bản → US-028..US-031, US-009, US-012; nâng cao → US-001..US-005, US-032/US-033, US-012 (Leitner), US-016..US-018, US-019, US-034, US-035..US-037, US-038..US-043 (đồng bộ, XP) |
