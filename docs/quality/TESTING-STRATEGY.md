# Testing Strategy — chiến lược kiểm thử

> **TỰ SINH** từ bộ phân tích thiết kế đã duyệt của nhóm — không sửa trực tiếp tệp này; đề xuất thay đổi qua Issue/PR để nhóm trưởng cập nhật nguồn và sinh lại. Danh mục tài liệu: [docs/README.md](../README.md).

**Mục đích:** phạm vi, rủi ro chất lượng, các mức kiểm thử và công cụ, test case phi chức năng, môi trường và dữ liệu test, tiêu chí bắt đầu/kết thúc nghiệm thu.

## Chiến lược kiểm thử
> Chiến lược test, môi trường, dữ liệu, tiêu chí vào/ra.

## Phạm vi (in/out)
**Trong phạm vi**
- 37 user story trong PRD (36 Must, 1 Should — sau thay đổi phạm vi) với 95 tiêu chí chấp nhận → 95 test case `TC-xxx.n` (§Truy vết).
- Quy tắc nghiệp vụ BR-001..BR-042 có logic tính toán (Leitner, chuỗi ngày, chấm điểm, sinh câu hỏi, băm mật khẩu, chuẩn hóa chữ).
- Yêu cầu phi chức năng NFR-P, A, S, X, D, R, E, M, L, Q (§Chiến lược, test case `TC-NFR-*`).
- REQ-013 dark mode (Should); REQ-015 widget (Could) chỉ khi T-071 được làm.
- Kiểm tra nội dung seed: số lượng, trường bắt buộc, trùng từ, nguồn/giấy phép.

**Ngoài phạm vi**
- Server, API, đồng bộ, đăng nhập mạng xã hội (Non-goal, không tồn tại).
- Phát hành Google Play, kiểm thử tương thích trên hàng loạt thiết bị thật, kiểm thử tải nhiều người dùng đồng thời.
- Chất lượng sư phạm của nội dung (nhóm tự rà soát chéo, không phải test case).
- Tấn công có quyền root trên máy (đọc trực tiếp file CSDL) — ngoài mô hình đe dọa của app offline cá nhân.

## Rủi ro chất lượng
| Rủi ro | Ảnh hưởng | Cách kiểm thử giảm rủi ro |
|---|---|---|
| Sai quy tắc Leitner / ngày ôn / chuỗi ngày (lệch 1 ngày, đổi ngày lúc nửa đêm) | Ôn sai thẻ, chuỗi về 0 vô lý — lỗi người dùng dễ thấy nhất | Unit test với `Clock` giả: ranh giới 23:59 → 00:00, bỏ 1 ngày, học 2 lần trong ngày, hộp 5 + Đã nhớ |
| Hiện thực PBKDF2 tự viết sai (DEC-003) | Không đăng nhập được hoặc băm yếu | Unit test bằng vector chuẩn RFC 7914 §11; so sánh thời gian băm trên máy yếu (AC-002.1 ≤ 1 giây — nếu vượt, giảm số vòng lặp qua DEC mới) |
| Truy vấn trên luồng giao diện gây giật/ANR | Trải nghiệm kém, ANR khi demo | StrictMode bản debug; Profiler khi cuộn danh sách và tra từ |
| Mất trạng thái khi xoay màn hình / app bị hệ thống hủy | Mất tiến độ phiên học, quiz | Bật "Don't keep activities", xoay ở từng bước SCR-04, SCR-09, SCR-13 |
| Xóa tài khoản không sạch / lộ dữ liệu giữa 2 tài khoản | Vi phạm NFR-R3, NFR-S5 | Instrumented test đếm bản ghi mọi bảng sau xóa; test 2 tài khoản trên 1 máy |
| Nhắc học không hiện / hiện khi đã học | Mất tính năng F4 | Kiểm thử thủ công: giờ nhắc + 2 phút; khởi động lại emulator; đổi múi giờ; Doze (`adb shell dumpsys deviceidle force-idle`) |
| Seed thiếu/sai định dạng (450 từ do 4 người soạn) | Màn hình trống, crash khi parse | Unit test kiểm tra seed chạy trên mọi commit đụng `assets/seed/` |
| Thiếu giọng TTS trên máy demo | Mất phát âm, câu nghe | Test trên emulator không có gói giọng: AC-010.2, AC-013.3 |

## Chiến lược (unit / integration / e2e / manual / perf / security / a11y)
| Mức | Công cụ | Phạm vi | Ai / khi nào | Ngưỡng |
|---|---|---|---|---|
| **Unit (U)** | JUnit 4 + JaCoCo, chạy `./gradlew testDebugUnitTest` | Lớp `domain.rules.*` (LeitnerScheduler, StreakCalculator, QuizGenerator, AnswerChecker, PasswordHasher, TextNormalizer), kiểm tra seed JSON | Người viết lớp, cùng PR với code | 100% pass; độ phủ nhánh ≥ 80% cho `domain.rules` (NFR-M2) |
| **Integration / instrumented (I)** | AndroidX Test trên emulator, `./gradlew connectedDebugAndroidTest` | DAO + Repository trên SQLite thật: tạo bảng, nạp seed, CASCADE, upsert tiến độ, truy vấn đến hạn, tra từ, giao dịch rollback | Nguyễn Tấn Thành (T-080), trước M4 | 100% pass |
| **E2E tự động (E)** | Espresso | 1 luồng chính: đăng ký → thiết lập → học 5 thẻ → quiz → kết quả (T-081) | Lê Văn Tấn Lực, trước M5 | Pass trên emulator API 34 |
| **Thủ công theo kịch bản (M)** | Bảng §Truy vết, ghi kết quả vào báo cáo kiểm thử | Mọi AC có giao diện; UAT toàn bộ US Must | Cả nhóm, mỗi người chạy tính năng của người khác (kiểm tra chéo), tuần M5 | 100% TC Cao đạt |
| **Hiệu năng** | `adb shell am start -W`, Android Studio Profiler, `dumpsys gfxinfo`, seed giả 5000 từ | TC-NFR-P1..P5, TC-NFR-E1 | người kiểm thử, M5 | Theo SRS (phi chức năng) |
| **Bảo mật** | Đọc manifest, DB Browser for SQLite mở `learninge.db`, Logcat, rà code `rawQuery/execSQL` | TC-NFR-S1..S6, checklist MASVS-STORAGE/CODE của người review | người review, trước G6 | 0 phát hiện mức cao |
| **Accessibility** | Accessibility Scanner, TalkBack, cỡ chữ 200% | TC-NFR-X1 trên SCR-02, 05, 07, 09, 13 | người kiểm thử, M5 | 0 lỗi vùng chạm/tương phản/thiếu nhãn ở 5 màn hình |
| **Offline** | Chế độ máy bay | TC-NFR-A1: toàn bộ kịch bản UAT | người kiểm thử, M5 | 100% chức năng chạy |

Test case cho SRS (phi chức năng):
| TC | Kiểm tra | Cách làm | Kết quả mong đợi |
|---|---|---|---|
| TC-NFR-P1 | Khởi động | `am start -W` 5 lần (lần đầu có seed, lần sau không) | Lần đầu ≤ 5 s; lần sau trung vị ≤ 2 s |
| TC-NFR-P2 | Không I/O trên luồng UI | Chạy UAT bản debug có StrictMode `penaltyLog` | Không có vi phạm DiskRead/DiskWrite trong Logcat |
| TC-NFR-P3 | Tra từ | Gõ "a", "mo", "con meo" với seed 5000 từ | Kết quả ≤ 300 ms sau khi ngừng gõ |
| TC-NFR-P4 | Mượt | Cuộn SCR-07, lật 10 thẻ, `dumpsys gfxinfo` | ≤ 5% khung hình chậm |
| TC-NFR-P5 | Kích thước | Build release | APK ≤ 30 MB; `learninge.db` sau seed ≤ 10 MB |
| TC-NFR-A1 | Offline | Chế độ máy bay, chạy UAT; kiểm tra manifest | Mọi chức năng chạy; không có quyền INTERNET |
| TC-NFR-A3 | Toàn vẹn | Kill tiến trình (`adb shell am kill`) giữa phiên học và giữa lưu quiz | Không có lượt quiz dở; tiến độ thẻ đã đánh giá còn nguyên |
| TC-NFR-A4 | Vòng đời | Xoay màn hình + "Don't keep activities" ở SCR-03, 04, 09, 13, 20 | Không mất thẻ/câu hiện tại, dữ liệu đang nhập |
| TC-NFR-A5 | Nhắc học | Đặt giờ +2 phút; khởi động lại máy; bật Doze | Thông báo trong ±15 phút; vẫn hoạt động sau khởi động lại |
| TC-NFR-S1 | Mật khẩu | Mở `learninge.db`, tìm trong SharedPreferences và Logcat | Không có mật khẩu/câu trả lời bảo mật dạng rõ |
| TC-NFR-S2..S4 | SQL tham số, exported, allowBackup | Rà code và manifest | Không nối chuỗi vào SQL; chỉ 2 thành phần exported; allowBackup=false |
| TC-NFR-S5 | Tách dữ liệu | Tạo 2 tài khoản, học/thêm sổ tay ở tài khoản A, đăng nhập B | B không thấy dữ liệu của A |
| TC-NFR-X1 | Accessibility | Scanner + TalkBack + cỡ chữ 200% | Như ngưỡng ở bảng trên |
| TC-NFR-X1-DARK | Dark mode (REQ-013) | Bật theme tối hệ thống, đi qua 24 màn hình | Không có chữ/nền màu cứng sai; tương phản đạt UI-UX-SPECIFICATION (hệ thống thiết kế) |
| TC-NFR-D3 | Thiết bị | Chạy UAT trên emulator API 24, API 34, 1 máy thật | Đạt trên cả 3 |
| TC-NFR-E1 | Mở rộng | Seed giả 5000 từ / 100 chủ đề, 10 tài khoản | Đạt TC-NFR-P2, P3 |
| TC-NFR-E2 | Thêm ngôn ngữ không sửa code | Thêm 1 dòng `languages` (vd `fr`) + 1 file seed nhỏ, build lại không sửa Java | Tạo được bộ tiếng Pháp, TTS đọc giọng fr-FR, nhãn cách đọc đúng |
| TC-NFR-IMP | Hiệu năng import | Import CSV 2000 dòng trên emulator API 34 | Xem trước ≤ 2 s; nhập ≤ 3 s; giao diện không treo |
| TC-NFR-E3 | Nâng cấp CSDL | Cài bản DB v1 có dữ liệu, cài đè bản v2 | Dữ liệu người dùng còn nguyên, seed mới được nạp |
| TC-NFR-M2 | Độ phủ | `./gradlew jacocoTestReport` | ≥ 80% nhánh ở `domain.rules` |
| TC-NFR-Q1 | Chất lượng build | `./gradlew lint assembleDebug` | Không lỗi; HardcodedText = 0 |
| TC-SEED | Nội dung seed | Unit test kiểm tra seed | 15 chủ đề, 450 từ (30/chủ đề), 450 câu ví dụ, 10 bài, 100 câu hỏi; X01 60, X02 60, X03 40, J01 20 từ; E01 30, E02 20 câu; 4 ngôn ngữ; không trùng từ trong chủ đề; mọi trường bắt buộc có giá trị |

## Môi trường & dữ liệu test
| Môi trường | Cấu hình | Mục đích |
|---|---|---|
| Emulator 1 | Pixel 6, API 34 (Android 14), 4 GB RAM, tiếng Việt | Môi trường chính; có Doze, quyền thông báo Android 13+ |
| Emulator 2 | Nexus 5, API 24 (Android 7.0), không cài gói giọng TTS tiếng Anh | minSdk; trường hợp thiếu giọng (AC-010.2, AC-013.3) |
| Máy thật | Điện thoại Android ≥ 10 của một thành viên | Hiệu năng thật, TTS thật, thông báo thật |
| Dữ liệu | (1) Seed thật 450 từ; (2) seed giả 5000 từ sinh bằng script (chỉ dùng cho TC-NFR-E1, không đưa vào APK); (3) tài khoản test `qa.user1`/`qa.user2` với mật khẩu test ghi trong `docs/testing/test-accounts.md` của repo code — chỉ là dữ liệu test cục bộ | — |
| Giả lập thời gian | Unit test dùng `Clock` giả; thủ công đổi ngày giờ máy để kiểm tra chuỗi ngày và ngày ôn | BR-012, BR-031 |

## Tiêu chí vào / ra
**Tiêu chí vào (bắt đầu UAT — M5, 14/12)**
- Đóng băng tính năng (M4) đã qua; mọi task P0 trong PROJECT-PLAN (công việc) ở trạng thái xong theo CODING-STANDARDS (định nghĩa hoàn thành).
- `./gradlew lint testDebugUnitTest assembleDebug` pass trên nhánh `main`; instrumented test (T-080) pass.
- Seed đủ số lượng (TC-SEED pass); APK debug cài được trên 3 cấu hình môi trường.

**Tiêu chí ra (qua G6 — nghiệm thu)**
- 100% test case ưu tiên Cao đạt; ≥ 90% test case ưu tiên Trung bình đạt.
- 0 lỗi mức Nghiêm trọng/Cao còn mở; lỗi Trung bình/Thấp còn mở được ghi vào danh sách lỗi đã biết kèm lý do chấp nhận và không thuộc luồng demo.
- 0 crash/ANR khi chạy toàn bộ kịch bản UAT trên 3 cấu hình (NFR-A2).
- Toàn bộ TC-NFR-* đạt, trừ những mục ghi rõ là chấp nhận rủi ro trong báo cáo kiểm thử và được Phan Anh Tuấn đồng ý.
- báo cáo kiểm thử có bằng chứng (ảnh chụp, log, báo cáo JaCoCo) và TEST-CASES cập nhật cột Trạng thái.

Mức lỗi: **Nghiêm trọng** = crash, mất dữ liệu, lộ mật khẩu; **Cao** = sai quy tắc BR hoặc không hoàn thành được luồng Must; **Trung bình** = sai hiển thị/thông báo, có cách làm khác; **Thấp** = lỗi chính tả, căn lề.
