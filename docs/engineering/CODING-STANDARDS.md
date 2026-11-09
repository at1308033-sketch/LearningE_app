# Coding Standards — quy chuẩn code

**Mục đích:** một cách viết code thống nhất cho 4 thành viên để code dễ review, dễ giải thích khi vấn đáp và dễ bảo trì. Áp dụng cho mọi PR. Quy trình nhánh/PR rút gọn ở [CONTRIBUTING.md](../../CONTRIBUTING.md).

## 1. Cấu trúc mã nguồn
```text
app/src/main/java/com/learninge/app/
├── LearningEApp.java          # Application: khởi tạo core.ServiceLocator
├── core/                      # ServiceLocator, AppExecutors, Clock
├── data/
│   ├── db/                    # DatabaseHelper, SeedLoader, DbContract (tên bảng/cột)
│   ├── dao/                   # một DAO mỗi bảng chính
│   ├── repo/                  # gom DAO theo nghiệp vụ, mở giao dịch
│   └── model/                 # POJO: User, Deck, Word, CardProgress…
├── domain/                    # Service: AuthService, DeckService, StudyService…
│   └── rules/                 # Java thuần: LeitnerScheduler, StreakCalculator, QuizGenerator, CsvCodec…
├── reminder/                  # ReminderReceiver, BootReceiver
└── ui/
    ├── auth/ main/ home/ learn/ study/ practice/ quiz/ exam/ deck/ search/ me/
    └── common/                # TtsHelper, EmptyStateView, BarChartView, tiện ích
app/src/main/res/              # layout, values (strings, colors, dimens, themes), values-night, drawable, font, navigation, xml
app/src/main/assets/seed/      # dữ liệu nạp sẵn (JSON)
app/src/test/java/…            # unit test JUnit (bắt buộc cho domain/rules)
app/src/androidTest/java/…     # instrumented (DAO) và Espresso
```
**Quy tắc phụ thuộc:** `ui` → `domain` → `data`. `domain.rules` không import `android.*`. DAO không chứa quy tắc nghiệp vụ. ViewModel không giữ `Context` của Activity. Hợp đồng giữa các tầng: [API-SPECIFICATION](API-SPECIFICATION.md).

## 2. Quy ước đặt tên
| Loại | Quy ước | Ví dụ |
|---|---|---|
| Gói | chữ thường, theo tầng/tính năng | `com.learninge.app.ui.study` |
| Lớp | `PascalCase` + hậu tố vai trò | `FlashcardFragment`, `FlashcardViewModel`, `WordDao`, `StudyService`, `WordAdapter` |
| Phương thức, biến | `camelCase`, động từ cho phương thức | `rateCard()`, `nextReviewDate` |
| Hằng số | `UPPER_SNAKE_CASE`, `static final` | `MAX_REVIEW_CARDS = 50` |
| Biến boolean | tiền tố `is/has/can/should` | `isReadOnly`, `hasMetGoal` |
| Layout | `<loại>_<màn hình>.xml` | `fragment_flashcard.xml`, `item_word.xml`, `dialog_delete_account.xml`, `view_empty_state.xml` |
| ID view | `snake_case` + tiền tố loại | `btn_login`, `til_username`, `et_username`, `tv_title`, `rv_words`, `iv_speaker` |
| Chuỗi | `<màn hình>_<nội dung>` | `login_error_wrong_credentials`, `deck_create_title` |
| Màu, kích thước | theo token | `@color/on_surface_variant`, `@dimen/space_4`, `@dimen/radius_m` |
| Icon | `ic_<tên>` | `ic_volume`, `ic_flame` |
| Bảng/cột CSDL | `snake_case`, khai báo một lần trong `DbContract` | `card_progress.next_review_date` |
| Test | `<Lớp>Test`, phương thức `tênHàm_điềuKiện_kếtQuả` | `LeitnerSchedulerTest.rate_rememberedAtBox5_staysAtBox5` |

## 3. Quy tắc viết code Java
- Java 17, định dạng mặc định Android Studio (4 dấu cách, dòng ≤ 120 ký tự); chạy *Reformat Code* + *Optimize Imports* trước khi commit.
- Một lớp một trách nhiệm; phương thức ≤ ~40 dòng; tách hàm khi lồng quá 3 cấp.
- Không dùng số "ma thuật" — đặt hằng số có tên, ghi mã quy tắc trong comment: `// BR-012: khoảng ôn theo hộp`.
- Comment giải thích **vì sao**, không lặp lại code làm gì. Lớp public có Javadoc 1–3 dòng.
- Không bắt `Exception` chung để im lặng; bắt đúng loại, ghi log (không ghi dữ liệu cá nhân) và trả `Result.error(...)`.
- Không để code chết, `System.out.println`, `printStackTrace()`.
- Đóng `Cursor`, stream bằng try-with-resources.
- Không dùng thư viện ngoài khi chưa thống nhất trong nhóm (ghi vào [ARCHITECTURE-DECISIONS](ARCHITECTURE-DECISIONS.md)).

## 4. Giao diện (XML, Material 3)
- Mọi chuỗi trong `strings.xml`; mọi màu/khoảng cách/bo góc dùng token (`@color/…`, `@dimen/…`, `?attr/…`) — Android Lint `HardcodedText` phải bằng 0.
- Dùng component Material (`MaterialButton`, `TextInputLayout`, `MaterialCardView`, `Chip`…) đúng như [UI-UX-SPECIFICATION](UI-UX-SPECIFICATION.md).
- Vùng chạm ≥ 48 dp; nút chỉ có icon phải có `contentDescription`; cỡ chữ dùng `sp`.
- Danh sách dùng `RecyclerView` + `ListAdapter`/`DiffUtil`.
- Kiểm tra layout ở 360 dp và 412 dp, xoay màn hình, theme tối, cỡ chữ 200%.

## 5. Dữ liệu và luồng
- Không truy cập CSDL trên luồng giao diện (StrictMode bản debug sẽ báo); dùng `AppExecutors`.
- Chỉ SQL có tham số `?`; không nối chuỗi người dùng nhập vào câu SQL.
- Ghi nhiều bảng trong một giao dịch.
- Không lưu mật khẩu dạng rõ; không ghi mật khẩu, câu trả lời bảo mật, tên người dùng vào log.

## 6. Kiểm thử
- Lớp trong `domain.rules` bắt buộc có unit test; độ phủ nhánh gói này ≥ 80%.
- DAO quan trọng có instrumented test trên SQLite thật.
- Test dùng `Clock` giả và `Random` có seed để kết quả lặp lại được.
- Kịch bản và mã test case: [TEST-CASES](../quality/TEST-CASES.md).

## 7. Git, commit, Pull Request
- Nhánh: `feature/T-0xx-mo-ta`, `fix/T-0xx-…`, `seed/…`, `docs/…`; không push thẳng `main`; nhánh sống ≤ 3 ngày, `git pull --rebase origin main` mỗi ngày.
- Commit theo Conventional Commits: `feat(T-032): lật thẻ flashcard`, `fix(…)`, `test(…)`, `seed(…)`, `docs(…)`, `refactor(…)`, `chore(…)`.
- PR: tiêu đề = commit chính; mô tả có mã task, US/AC, ảnh chụp, test case đã chạy; 1 người review; CI xanh; squash merge; xóa nhánh sau merge.
- Không commit: `local.properties`, keystore, `*.db`, thư mục `build/`, cấu hình công cụ cá nhân.

## 8. Định nghĩa hoàn thành (Definition of Done)
Một task chỉ xong khi đủ các mục sau:
- [ ] Đạt tiêu chí chấp nhận của US liên quan, có ảnh chụp/GIF trên emulator API 34 trong PR.
- [ ] Một thành viên khác review và duyệt PR (không tự merge).
- [ ] `./gradlew lint testDebugUnitTest assembleDebug` pass trên CI.
- [ ] Quy tắc mới/sửa trong `domain.rules` có unit test, độ phủ gói không giảm dưới 80%.
- [ ] Không truy vấn trên luồng giao diện; SQL có tham số; ghi nhiều bảng trong giao dịch.
- [ ] Chuỗi, màu, kích thước dùng resource/token; giao diện khớp Figma ở 360 và 412 dp; không vỡ khi xoay và cỡ chữ 200%.
- [ ] Xử lý đủ trạng thái rỗng / lỗi / đang tải theo [SRS](../project/SRS.md).
- [ ] Không có mật khẩu, keystore, dữ liệu cá nhân thật trong code, log, commit.
- [ ] **Người phụ trách tự giải thích được luồng dữ liệu của task trong PR** và đã thử sửa nhanh một tham số hoặc thêm log rồi chạy lại (vấn đáp 50/100 điểm).
- [ ] Một dòng trong [CHANGELOG](../operations/CHANGELOG.md); ghi file + tên hàm chính vào bảng minh chứng cho "File báo cáo kết quả dự án".

Task soạn dữ liệu (từ vựng, ngữ pháp, đề thi): đúng định dạng trong [DATABASE-DESIGN](DATABASE-DESIGN.md), unit test kiểm tra dữ liệu pass, mỗi tệp có trường `source` ghi nguồn hợp lệ, một người khác rà chính tả/nghĩa/phiên âm.
