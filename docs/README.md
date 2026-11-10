# Tài liệu dự án LearningE

Danh mục đầy đủ tài liệu của dự án, chia 4 nhóm. **Thành viên mới bắt đầu tại [SYSTEM-BLUEPRINT](engineering/SYSTEM-BLUEPRINT.md).**

- Tệp có dòng **TỰ SINH** ở đầu được tạo từ bộ phân tích thiết kế đã duyệt — không sửa trực tiếp, đề xuất thay đổi qua Issue/PR.
- Tệp còn lại viết tay — sửa qua PR như code.

## A. Project Documentation — phân tích dự án (`project/`)
| Tệp | Nội dung | Loại |
|---|---|---|
| [PRD.md](project/PRD.md) | Product Requirements Document — tầm nhìn, mục tiêu, người dùng, phạm vi, 19 yêu cầu MoSCoW, 37 user story + tiêu chí chấp nhận, nội dung học, nghiên cứu người dùng, phân tích đối thủ | Tự sinh |
| [SRS.md](project/SRS.md) | Software Requirements Specification — phân quyền, quy tắc kiểm tra dữ liệu, trạng thái đối tượng, xử lý ngoại lệ, dữ liệu vào/ra, yêu cầu phi chức năng có số đo | Tự sinh |
| [Use-Cases.md](project/Use-Cases.md) | 22 use case + sơ đồ, đặc tả từng chức năng, 17 luồng người dùng (User Flow) và luồng hệ thống (System Flow, sequence) | Tự sinh |
| [Business-Rules.md](project/Business-Rules.md) | 55 quy tắc nghiệp vụ BR-001..BR-055: điều kiện, kết quả, ví dụ số | Tự sinh |
| [Glossary.md](project/Glossary.md) | Từ điển thuật ngữ nghiệp vụ & kỹ thuật, kiến thức miền (Leitner, CEFR, dạng câu hỏi, ràng buộc Android, pháp lý) | Tự sinh |
| [PROJECT-PLAN.md](project/PROJECT-PLAN.md) | Mốc M0–M6, danh sách task T-xxx, sổ rủi ro | Tự sinh |

## B. Engineering Documentation — phát triển kỹ thuật (`engineering/`)
| Tệp | Nội dung | Loại |
|---|---|---|
| [SYSTEM-BLUEPRINT.md](engineering/SYSTEM-BLUEPRINT.md) | Bản thiết kế tổng thể: tính năng, bản đồ gói mã, quy trình làm task, rubric; kiến trúc 3 tầng, lược đồ lớp, luồng dữ liệu, công nghệ | Tự sinh |
| [DATABASE-DESIGN.md](engineering/DATABASE-DESIGN.md) | ERD, 18 bảng SQLite, ràng buộc, index, lưu giữ; định dạng dữ liệu nạp sẵn và import/export | Tự sinh |
| [API-SPECIFICATION.md](engineering/API-SPECIFICATION.md) | App không có REST API: hợp đồng Service (phương thức, đầu vào/ra, mã lỗi), quy ước DAO, tham số điều hướng, định dạng tệp | Viết tay |
| [CODING-STANDARDS.md](engineering/CODING-STANDARDS.md) | Cấu trúc mã, đặt tên, quy tắc Java/XML/CSDL, kiểm thử, Git/PR, Definition of Done | Viết tay |
| [ARCHITECTURE-DECISIONS.md](engineering/ARCHITECTURE-DECISIONS.md) | Nhật ký quyết định (ADR): quyết định sản phẩm ban đầu, DEC-001..DEC-010 | Tự sinh |
| [UI-UX-SPECIFICATION.md](engineering/UI-UX-SPECIFICATION.md) | Sơ đồ màn hình & điều hướng, design system, đặc tả 32 màn hình + link Figma, bố cục màn hình mới, ánh xạ token → code | Tự sinh |
| [INTEGRATION-DOCUMENTATION.md](engineering/INTEGRATION-DOCUMENTATION.md) | TextToSpeech, AlarmManager/thông báo, Storage Access Framework, trình duyệt, thư viện & giấy phép, GitHub Actions | Viết tay |

## C. Quality Assurance — kiểm thử (`quality/`)
| Tệp | Nội dung | Loại |
|---|---|---|
| [TESTING-STRATEGY.md](quality/TESTING-STRATEGY.md) | Phạm vi, rủi ro chất lượng, các mức kiểm thử, test case phi chức năng, môi trường, tiêu chí vào/ra | Tự sinh |
| [TEST-CASES.md](quality/TEST-CASES.md) | 95 test case theo tiêu chí chấp nhận + ma trận truy vết REQ → US → AC → BR → FLOW → TC → Task | Tự sinh |
| [BUG-REPORT-TEMPLATE.md](quality/BUG-REPORT-TEMPLATE.md) | Mức độ lỗi, mẫu báo lỗi, vòng đời lỗi (kèm mẫu Issue "Báo lỗi" trên GitHub) | Viết tay |
| [RELEASE-CHECKLIST.md](quality/RELEASE-CHECKLIST.md) | Checklist build, chất lượng, bảo mật, APK, hồ sơ nộp đồ án | Viết tay |
| [test-accounts.md](quality/test-accounts.md) | Tài khoản dùng khi kiểm thử | Viết tay |

## D. Operations — triển khai & bảo trì (`operations/`)
| Tệp | Nội dung | Loại |
|---|---|---|
| [INSTALLATION.md](operations/INSTALLATION.md) | Cài Android Studio, JDK, SDK, emulator; clone, build, chạy; cài APK lên máy thật | Viết tay |
| [DEPLOYMENT.md](operations/DEPLOYMENT.md) | Phiên bản, keystore, quy trình phát hành APK release, đóng gói nộp bài, rollback | Viết tay |
| [ENVIRONMENT.md](operations/ENVIRONMENT.md) | Biến môi trường, tệp cấu hình (commit / không commit), build type, hằng số cấu hình, môi trường test | Viết tay |
| [BACKUP-RESTORE.md](operations/BACKUP-RESTORE.md) | Sao lưu dữ liệu người dùng (export/import), lấy/khôi phục CSDL khi phát triển, dữ liệu demo, tài sản dự án | Viết tay |
| [MONITORING.md](operations/MONITORING.md) | Quy tắc log, StrictMode, Profiler, CI, cảnh báo và phản ứng, thu thập log | Viết tay |
| [TROUBLESHOOTING.md](operations/TROUBLESHOOTING.md) | Sự cố thường gặp: môi trường/build, chạy app, TTS, nhắc học, import/export, Git | Viết tay |
| [CHANGELOG.md](operations/CHANGELOG.md) | Lịch sử thay đổi phiên bản | Viết tay |
| [HANDOVER.md](operations/HANDOVER.md) | Gói bàn giao, ngày đầu cho người mới, trạng thái hiện tại, lỗi đã biết, bảo trì, liên hệ | Viết tay |

## Sơ đồ UML (`diagrams/`)
48 sơ đồ PlantUML (mã nguồn `.puml` + ảnh `.png`/`.svg`), đã gắn vào đúng mục của các tài liệu trên:

| Thư mục | Nội dung | Gắn vào |
|---|---|---|
| `usecase/` | Use case tổng quát + 6 nhóm chức năng | Use-Cases, PRD |
| `activity/` | 12 sơ đồ hoạt động có swimlane (FLOW-001…017) | Use-Cases |
| `state/` | Trạng thái thẻ Leitner, tài khoản, lượt thi thử | SRS |
| `sequence/` | Đăng nhập, ôn tập, import, thi thử, nhắc học | Use-Cases, SYSTEM-BLUEPRINT, API, INTEGRATION |
| `class/` | Lớp miền, lớp theo tầng MVVM, domain.rules, hạ tầng, service | SYSTEM-BLUEPRINT, API |
| `erd/` | ERD tổng quan + 4 nhóm bảng | DATABASE-DESIGN |
| `architecture/` | Thành phần, gói, triển khai, bản đồ màn hình | SYSTEM-BLUEPRINT, UI-UX, PRD |

Sửa sơ đồ: sửa tệp `.puml` → `python docs/diagrams/render.py` (cần Java 17+ và biến `PLANTUML_JAR` trỏ tới plantuml.jar) → `python docs/diagrams/embed.py` (gắn lại ảnh theo `manifest.json`). Xuất bản Word có kèm sơ đồ: `python docs/tools/build_docx.py` → `docs/export/*.docx` (không commit, sinh lại được).

## Thiết kế (`design/`)
| Tệp | Nội dung |
|---|---|
| [mockups/index.html](design/mockups/index.html) | Mockup độ trung thực cao v2: hệ thống thiết kế (màu, chữ, component, minh họa) + 46 khung cho 32 màn hình và trạng thái, 2 bản tối. Mở bằng trình duyệt; đưa vào Figma bằng plugin nhập HTML hoặc dựng lại theo token |
| [mockups/screens/](design/mockups/screens/) | Ảnh PNG từng màn hình (tên tệp = mã màn hình, vd `scr-05.png`) |
| [icons/topics/](design/icons/topics/) | 15 icon chủ đề SVG tự vẽ (`t01-family.svg` … `t15-health.svg`), thay emoji — nhập bằng Vector Asset, màu nền ô theo DESIGN-SYSTEM |
| [illustrations/](design/illustrations/) | 13 minh họa SVG gốc (nhân vật, linh vật, trạng thái rỗng, luyện thi…) — nhập vào Android Studio bằng Vector Asset |
