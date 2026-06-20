# 🖥️ Giao Diện & Trải Nghiệm Người Dùng — ThangLong University Web

> **Mã tài liệu:** DOC-08 | **Phiên bản:** 1.0 | **Ngày tạo:** 28/05/2026

---

## Mục Lục
- [1. Landing & Marketing Portal](#1-landing--marketing-portal)
- [2. Admin Portal](#2-admin-portal)
- [3. Student Portal](#3-student-portal)
- [4. Teacher Portal](#4-teacher-portal)
- [5. Gợi Ý Cải Thiện UI/UX](#5-gợi-ý-cải-thiện-uiux)

---

## 1. Landing & Marketing Portal

Tổng số màn hình: **9 routes**

| Route | Màn hình | Actor | Chức năng chính | API liên quan |
|-------|---------|-------|----------------|---------------|
| `/` | Trang chủ | Public | Hero slideshow, giới thiệu trường, ngành học, testimonials, gallery, CTA tuyển sinh | Không (static) |
| `/about` | Giới thiệu | Public | Lịch sử, sứ mệnh, tầm nhìn, ban giám hiệu, thành tựu | Không (static) |
| `/programs` | Chương trình đào tạo | Public | Danh sách ngành, số tín chỉ, mô tả | Không (static) |
| `/admissions` | Tuyển sinh | Public | Thông tin xét tuyển, chỉ tiêu, thời gian, điểm chuẩn | Không (static) |
| `/scholarships` | Học bổng | Public | Các loại học bổng, điều kiện, hồ sơ | Không (static) |
| `/tuition` | Học phí công khai | Public | Bảng học phí theo ngành | Không (static) |
| `/articles` | Danh sách tin tức | Public | Danh sách bài viết, phân trang | `articleApi.listArticles()` (stub/mock) |
| `/articles/:slug` | Chi tiết bài viết | Public | Nội dung bài viết đầy đủ | `articleApi.getArticle(slug)` (stub) |
| `/contact` | Liên hệ | Public | Thông tin liên lạc, bản đồ | Không (static) |
| `/login` | Đăng nhập | All | Form đăng nhập | `POST /api/auth/login` |

**Đặc điểm thiết kế Landing:**
- **Màu sắc chủ đạo:** Đỏ `#C8102E` (màu TLU), đen, trắng ngà `#EBE9E4`, xanh navy `#00204A`
- **Animations:** Scroll reveal (IntersectionObserver), hero image slideshow (5s interval), hover grayscale → color
- **Responsive:** Mobile hamburger menu với full-screen overlay
- **Typography:** Sans-serif, tracking-tight, large headlines

---

## 2. Admin Portal

Tổng số màn hình: **17 routes**

| Route | Màn hình | Chức năng chính | API chính |
|-------|---------|----------------|-----------|
| `/admin` | Admin shell | Layout, sidebar navigation | — |
| `/admin/dashboard` | Dashboard | Thống kê hệ thống, charts, semester summary | `GET /api/admin/semesters/{id}/summary` |
| `/admin/users` | Quản lý Users | CRUD tài khoản, toggle active, filter theo role | `GET /api/admin/users` |
| `/admin/students` | Quản lý Sinh viên | CRUD sinh viên, xem hồ sơ đầy đủ, filter | `GET /api/admin/students` |
| `/admin/teachers` | Quản lý Giảng viên | CRUD giảng viên, gán khoa | `GET /api/admin/teachers` |
| `/admin/courses` | Quản lý Môn học | CRUD môn học, điều kiện tiên quyết | `GET /api/admin/courses` |
| `/admin/semesters` | Quản lý Học kỳ | CRUD, toggle registration/exam/retake | `GET /api/admin/semesters` |
| `/admin/semesters/:id` | Chi tiết Học kỳ | Tổng quan kỳ, lớp HP, lịch thi, dashboard | `GET /api/admin/semesters/{id}/summary` |
| `/admin/departments` | Quản lý Khoa | CRUD departments | `GET /api/admin/departments` |
| `/admin/majors` | Quản lý Ngành | CRUD majors | `GET /api/admin/majors` |
| `/admin/homerooms` | Quản lý Lớp niên chế | CRUD homerooms, gán/xóa sinh viên | `GET /api/admin/homerooms` |
| `/admin/rooms` | Quản lý Phòng học | CRUD rooms | `GET /api/admin/rooms` |
| `/admin/periods` | Quản lý Tiết học | CRUD periods | `GET /api/admin/periods` |
| `/admin/enrollments` | Quản lý Đăng ký | Xem, filter, override enrollments | `GET /api/admin/enrollments` |
| `/admin/exam-schedules` | Lịch thi | Cài đặt ngày thi, phòng thi | Exam schedule APIs |
| `/admin/exam-registrations` | Đăng ký thi lại | Xem thi lại, tổng hợp | Exam registration APIs |
| `/admin/academic-results` | Kết quả học tập | GPA/CPA toàn hệ thống | `GET /api/admin/academic-results` |
| `/admin/knowledge` | Knowledge Base | Quản lý tài liệu chatbot | Knowledge APIs |
| `/admin/chat` | Chat | Giao diện chat admin | Chat APIs |

**Đặc điểm thiết kế Admin:**
- **Layout:** Sidebar cố định trái, content area rộng
- **Components:** DataTable với sort, filter, pagination; Dialog form CRUD
- **Charts:** Recharts (line chart GPA, bar chart enrollments)
- **Export:** Nút download Excel trực tiếp

---

## 3. Student Portal

Tổng số màn hình: **14 routes**

| Route | Màn hình | Chức năng chính | API chính | Dữ liệu hiển thị |
|-------|---------|----------------|-----------|-----------------|
| `/student` | Student shell | Layout, sidebar | — | — |
| `/student/dashboard` | Dashboard | GPA, CPA, TKB hôm nay, lịch thi sắp tới, học phí | `GET /api/student/dashboard` | profile, semesterGpa, cumulativeGpa, todaySchedule, upcomingExams, tuitionStatus |
| `/student/profile` | Hồ sơ | Thông tin cá nhân đầy đủ | `GET /api/student/profile` | Họ tên, MSV, ngành, lớp, email, CMND, địa chỉ... |
| `/student/course-registration` | Đăng ký HP | Xem lớp mở, đăng ký, hủy, trạng thái | Enrollment APIs | Danh sách lớp: môn, GV, lịch, slot còn lại |
| `/student/schedule` | Thời khóa biểu | Lịch học theo tuần/kỳ | `GET /api/student/my-schedule/{id}` | Tên môn, phòng, tiết, ngày trong tuần |
| `/student/grades` | Điểm số | Bảng điểm + GPA/CPA | `GET /api/student/learning-results` | Điểm thành phần, tổng kết, GPA kỳ, CPA tích lũy |
| `/student/academic-results` | Kết quả học tập | Lịch sử GPA các kỳ | `GET /api/student/academic-results/my-results` | GPA từng kỳ, CPA tích lũy, biểu đồ |
| `/student/exams` | Lịch thi | Lịch thi theo học kỳ | `GET /api/student/exams` | Môn thi, ngày, phòng |
| `/student/retake-registration` | Đăng ký thi lại | Môn đủ điều kiện, đăng ký | Retake APIs | Môn, điểm cũ, phí thi lại |
| `/student/tuition` | Học phí | Hóa đơn, thanh toán VNPay | Tuition APIs | Từng môn, số tín chỉ, đơn giá, tổng |
| `/student/curriculum` | Chương trình ĐT | Tất cả môn học theo ngành | Curriculum APIs | Môn học, tín chỉ, bắt buộc/tự chọn |
| `/student/notifications` | Thông báo | Danh sách thông báo | Notification APIs | Tiêu đề, nội dung, ngày, đã đọc/chưa |
| `/student/payment-result` | Kết quả thanh toán | Thành công/thất bại VNPay | VNPay return | Trạng thái, mã giao dịch |
| `/student/chat` | Chat | Chat với GV, SV khác | Chat APIs | Phòng chat, tin nhắn |

**Đặc điểm thiết kế Student:**
- **Dashboard:** Cards tóm tắt (GPA, học phí, lịch thi), layout 2-3 cột
- **Course Registration:** Table với filter, real-time slot count, badge trạng thái
- **Schedule:** Grid theo ngày tuần, màu theo môn học
- **Grades:** Table với badge điểm chữ màu (A=xanh, F=đỏ)

---

## 4. Teacher Portal

Tổng số màn hình: **8 routes**

| Route | Màn hình | Chức năng chính | API chính |
|-------|---------|----------------|-----------|
| `/teacher` | Teacher shell | Layout, sidebar | — |
| `/teacher/dashboard` | Dashboard | Tổng quan: số lớp, số SV, lịch dạy hôm nay | Semester APIs |
| `/teacher/profile` | Hồ sơ | Thông tin cá nhân giảng viên | `GET /api/users/me` |
| `/teacher/classes` | Lớp học phần | Danh sách lớp được phân công | `GET /api/teacher/my-classes/semester/{id}` |
| `/teacher/classes/:id/students` | Sinh viên lớp | Danh sách SV, điểm số | `GET /api/teacher/classes/{id}/students` |
| `/teacher/grades` | Nhập điểm | Form nhập điểm từng SV, khóa điểm | Grade APIs |
| `/teacher/attendance` | Điểm danh | Danh sách buổi, nhập trạng thái, khóa buổi | Attendance APIs |
| `/teacher/timetable` | Thời khóa biểu | Lịch dạy trong tuần | Class section APIs |
| `/teacher/notifications` | Thông báo | Danh sách thông báo | Notification APIs |
| `/teacher/chat` | Chat | Chat nội bộ | Chat APIs |

**Đặc điểm thiết kế Teacher:**
- **Attendance:** Table với dropdown PRESENT/LATE/ABSENT cho từng SV, nút Lock buổi
- **Grades:** Inline editable table, auto-calculate total khi nhập điểm thành phần
- **Timetable:** Weekly grid view

---

## 5. Gợi Ý Cải Thiện UI/UX

Dưới đây là các điểm phát hiện từ phân tích source code cần cải thiện:

| # | Vấn đề | Mức độ | Gợi ý cải thiện |
|---|--------|--------|----------------|
| 1 | **Articles API chỉ là stub** — Trang bài viết dùng mock data, không có backend thực | 🔴 Cao | Xây dựng Article/CMS module cho backend |
| 2 | **Chatbot không có UI loading indicator** rõ ràng khi AI đang xử lý | 🟡 Trung bình | Thêm typing animation, skeleton loader |
| 3 | **Đăng ký HP phải polling** — UX kém khi phải chờ | 🟡 Trung bình | Dùng WebSocket thay vì polling để real-time hơn |
| 4 | **Không có mobile app** — Chỉ có web | 🟡 Trung bình | PWA hoặc React Native app |
| 5 | **Admin dashboard thiếu biểu đồ xu hướng** | 🟢 Thấp | Thêm line chart GPA trends theo thời gian |
| 6 | **Không có dark mode** cho student/teacher portal | 🟢 Thấp | Thêm theme toggle |
| 7 | **Trang contact** chỉ hiển thị thông tin tĩnh, không có form liên hệ | 🟢 Thấp | Thêm contact form với email integration |
| 8 | **`/admin/class-sections`** và **`/admin/exam-registrations`** chỉ có nội dung placeholder (253 bytes) | 🔴 Cao | Cần hoàn thiện UI cho 2 màn hình này |
| 9 | **`/student/grades`** (253 bytes) — Màn hình quá nhỏ, có thể chưa hoàn thiện | 🔴 Cao | Cần kiểm tra lại UI |
| 10 | **Không có onboarding** cho sinh viên/GV mới | 🟢 Thấp | Thêm welcome tour, tooltips hướng dẫn |
