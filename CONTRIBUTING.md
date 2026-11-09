# Quy ước đóng góp — LearningE_app

Áp dụng cho mọi thành viên. Đọc trước khi viết code hoặc mở Pull Request.

## Cấu trúc
- `app/src/main/java/com/learninge/app/`
  - `LearningEApp` (lớp Application); `core/` — `ServiceLocator`, `AppExecutors`
  - `data/db` (`DatabaseHelper`, `SeedLoader`, `DbContract`), `data/dao`, `data/repo`, `data/model`
  - `domain/` — Service; `domain/rules/` — quy tắc Java thuần (Leitner, chuỗi ngày, quiz, CSV, PBKDF2, chuẩn hóa chữ), **không import `android.*`**
  - `reminder/` — receiver nhắc học
  - `ui/<tính năng>/` — Activity/Fragment + ViewModel
- `app/src/main/assets/seed/` — dữ liệu học có sẵn dạng JSON
- `app/src/test/` — unit test JUnit; `app/src/androidTest/` — instrumented/Espresso
- `docs/` — tài liệu dự án theo 4 nhóm (project, engineering, quality, operations); danh mục ở `docs/README.md`. Tệp có dòng **TỰ SINH** không sửa trực tiếp

## Lệnh
```bash
./gradlew assembleDebug                 # build APK debug
./gradlew lint                          # Android Lint (0 lỗi)
./gradlew testDebugUnitTest             # unit test
./gradlew connectedDebugAndroidTest     # instrumented test (cần emulator)
```
Máy cần `JAVA_HOME` trỏ tới JDK 17+ (hoặc build trong Android Studio) và `local.properties` có `sdk.dir` (không commit).

## Nhánh, commit, Pull Request
- Nhánh: `feature/T-0xx-mo-ta`, `fix/T-0xx-…`, `seed/…`, `docs/…`; không push thẳng `main`.
- Commit: `feat(T-032): …`, `fix(…)`, `test(…)`, `seed(…)`, `docs(…)`, `refactor(…)`, `chore(…)`.
- PR cần 1 người review + CI xanh; squash merge; xóa nhánh sau khi merge.

## Quy tắc code
- Mọi chuỗi giao diện trong `strings.xml`; màu/kích thước dùng token (`@color/…`, `@dimen/…`, `?attr/…`).
- CSDL: chỉ SQL có tham số `?`; ghi nhiều bảng trong một giao dịch; không truy vấn trên luồng giao diện.
- Không commit `local.properties`, keystore, `*.db`, mật khẩu hay dữ liệu cá nhân thật.
- Công cụ cá nhân (cấu hình IDE riêng, trợ lý AI, ghi chú quy trình) để ở máy mình — repo chỉ chứa mã nguồn và tài liệu sản phẩm.

## Trước khi mở PR
1. `./gradlew lint testDebugUnitTest assembleDebug` pass.
2. Chạy app trên emulator, kiểm tra các test case của task, đính kèm ảnh chụp.
3. **Tự giải thích được luồng code của task trong mô tả PR** — vấn đáp chiếm 50/100 điểm; mỗi người phải hiểu và sửa được code mình phụ trách.
4. Thêm 1 dòng vào `docs/operations/CHANGELOG.md`; ghi file + tên hàm chính vào bảng minh chứng cho "File báo cáo kết quả dự án".
