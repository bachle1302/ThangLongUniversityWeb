# 📋 Bộ Tài Liệu BA Report — Hệ Thống Quản Lý Đại Học Thăng Long

> **Dự án:** ThangLongUniversityWeb — University Management System (UMS)  
> **Phiên bản tài liệu:** 1.0  
> **Ngày tạo:** 28/05/2026  
> **Người tổng hợp:** Business Analyst / Technical Writer  

---

## 📖 Mục Lục Tổng Quan

| # | File | Tiêu đề | Mô tả ngắn |
|---|------|---------|------------|
| 00 | [00_TONG_QUAN_DU_AN.md](./00_TONG_QUAN_DU_AN.md) | Tổng Quan Dự Án | Mục tiêu, phạm vi, công nghệ, cấu trúc tổng thể |
| 01 | [01_PHAN_TICH_NGHIEP_VU.md](./01_PHAN_TICH_NGHIEP_VU.md) | Phân Tích Nghiệp Vụ | Tác nhân, vai trò, quyền hạn, quy trình nghiệp vụ |
| 02 | [02_YEU_CAU_CHUC_NANG.md](./02_YEU_CAU_CHUC_NANG.md) | Yêu Cầu Chức Năng | Danh sách FR với mã hóa FR-001… |
| 03 | [03_YEU_CAU_PHI_CHUC_NANG.md](./03_YEU_CAU_PHI_CHUC_NANG.md) | Yêu Cầu Phi Chức Năng | Bảo mật, hiệu năng, khả năng mở rộng |
| 04 | [04_USE_CASE.md](./04_USE_CASE.md) | Use Case | Sơ đồ và mô tả chi tiết từng use case |
| 05 | [05_QUY_TRINH_NGHIEP_VU.md](./05_QUY_TRINH_NGHIEP_VU.md) | Quy Trình Nghiệp Vụ | Flowchart các quy trình chính |
| 06 | [06_API_VA_LUONG_DU_LIEU.md](./06_API_VA_LUONG_DU_LIEU.md) | API & Luồng Dữ Liệu | Danh sách endpoint, mapping màn hình ↔ API |
| 07 | [07_MO_HINH_DU_LIEU.md](./07_MO_HINH_DU_LIEU.md) | Mô Hình Dữ Liệu | ERD, mô tả entity/bảng |
| 08 | [08_GIAO_DIEN_VA_TRAI_NGHIEM_NGUOI_DUNG.md](./08_GIAO_DIEN_VA_TRAI_NGHIEM_NGUOI_DUNG.md) | Giao Diện & UX | Danh sách màn hình, chức năng, API liên quan |
| 09 | [09_KIEM_THU_VA_KICH_BAN_TEST.md](./09_KIEM_THU_VA_KICH_BAN_TEST.md) | Kiểm Thử | Test case TC-001… theo từng chức năng |
| 10 | [10_DANH_GIA_VA_DE_XUAT_CAI_TIEN.md](./10_DANH_GIA_VA_DE_XUAT_CAI_TIEN.md) | Đánh Giá & Đề Xuất | Điểm mạnh, hạn chế, roadmap |

---

## 📌 Hướng Dẫn Đọc Báo Cáo

### Đọc theo thứ tự đề xuất:

```
00 → 07 → 01 → 04 → 05 → 02 → 06 → 03 → 08 → 09 → 10
(Tổng quan) → (Dữ liệu) → (Nghiệp vụ) → (Use case) → (Quy trình) 
→ (Chức năng) → (API) → (Phi chức năng) → (UI) → (Test) → (Đề xuất)
```

### Đọc nhanh theo vai trò:

| Vai trò | Nên đọc |
|---------|---------|
| 🎓 Sinh viên / Người dùng cuối | 00, 08 |
| 👔 Product Owner / Giảng viên hướng dẫn | 00, 01, 02, 04, 10 |
| 💻 Developer | 06, 07, 03 |
| 🧪 Tester / QA | 09, 02 |
| 📊 Business Analyst | 01, 02, 04, 05, 10 |

---

## 🗂️ Tóm Tắt Từng Phần

### 📄 00 — Tổng Quan Dự Án
Giới thiệu tổng thể về hệ thống ThangLongUniversityWeb: tên dự án, mục tiêu, vấn đề cần giải quyết, đối tượng người dùng (Admin, Giảng viên, Sinh viên), phạm vi hệ thống, công nghệ sử dụng (Spring Boot + React + PostgreSQL + Kafka + Redis), và cấu trúc project.

### 📄 01 — Phân Tích Nghiệp Vụ
Phân tích chi tiết 3 tác nhân chính (Admin, Giảng viên, Sinh viên), quyền hạn và chức năng từng vai trò, bảng phân quyền tổng hợp, các quy trình nghiệp vụ chính, và các business rules tìm thấy từ source code.

### 📄 02 — Yêu Cầu Chức Năng
Danh sách 40+ yêu cầu chức năng (FR-001 đến FR-040+) bao gồm: xác thực, quản lý người dùng, quản lý khóa học, đăng ký học phần, điểm danh, chấm điểm, học phí VNPay, chat real-time, chatbot AI, thông báo.

### 📄 03 — Yêu Cầu Phi Chức Năng
Các yêu cầu về bảo mật (JWT, BCrypt, Role-based), hiệu năng (Kafka queue, Redis cache), khả năng mở rộng (microservice-ready), tính dễ sử dụng, khả năng bảo trì và tương thích.

### 📄 04 — Use Case
Sơ đồ use case cho cả 3 actor chính và mô tả chi tiết các use case quan trọng: Đăng nhập, Đăng ký học phần (qua Kafka), Nhập điểm, Thanh toán học phí VNPay, Điểm danh.

### 📄 05 — Quy Trình Nghiệp Vụ
Flowchart Mermaid cho các quy trình: Đăng nhập & phân quyền, Đăng ký học phần (có Kafka queue), Nhập và khóa điểm, Thanh toán học phí qua VNPay, Quản lý học kỳ.

### 📄 06 — API & Luồng Dữ Liệu
Tổng hợp 60+ API endpoint từ source code thực tế, phân nhóm theo Auth / Admin / Student / Teacher / Chat / Chatbot / Knowledge. Mapping màn hình frontend → API backend.

### 📄 07 — Mô Hình Dữ Liệu
ERD đầy đủ với 20+ entity/bảng: users, students, teachers, courses, class_sections, enrollments, grades, academic_results, tuition_bills, payment_transactions, chat_rooms, messages, notifications, audit_logs…

### 📄 08 — Giao Diện & Trải Nghiệm Người Dùng
Danh sách 60 màn hình/route trong frontend, phân nhóm theo portal (Landing / Admin / Student / Teacher), mục đích, chức năng và API liên quan từng màn hình.

### 📄 09 — Kiểm Thử & Kịch Bản Test
50+ test case (TC-001…) bao gồm: đăng nhập, đăng ký học phần, nhập điểm, thanh toán VNPay, chat, chatbot, quản lý hành chính; phân loại theo mức độ ưu tiên.

### 📄 10 — Đánh Giá & Đề Xuất Cải Tiến
Phân tích điểm mạnh (kiến trúc tốt, tích hợp AI, VNPay…), hạn chế hiện tại, rủi ro nghiệp vụ và kỹ thuật, đề xuất cải thiện UX/UI, chức năng và roadmap phát triển tiếp theo.

---

> 📝 **Ghi chú:** Toàn bộ nội dung tài liệu này được xây dựng dựa trên phân tích source code thực tế của dự án. Những phần chưa xác định đầy đủ sẽ được ghi chú rõ ràng trong từng file.
