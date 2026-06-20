# 🔌 API & Luồng Dữ Liệu — ThangLong University Web

> **Mã tài liệu:** DOC-06 | **Phiên bản:** 1.0 | **Ngày tạo:** 28/05/2026

---

## Mục Lục
- [1. Tổng Quan API Backend](#1-tổng-quan-api-backend)
- [2. Auth API](#2-auth-api)
- [3. Admin API](#3-admin-api)
- [4. Student API](#4-student-api)
- [5. Teacher API](#5-teacher-api)
- [6. Chat API](#6-chat-api)
- [7. Chatbot API](#7-chatbot-api)
- [8. Knowledge API](#8-knowledge-api)
- [9. Mapping Màn Hình → API](#9-mapping-màn-hình--api)

---

## 1. Tổng Quan API Backend

| Thông tin | Chi tiết |
|-----------|---------|
| **Base URL** | `http://localhost:8080` |
| **API prefix** | `/api/` |
| **Authentication** | Bearer JWT Token (`Authorization: Bearer <accessToken>`) |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` |
| **Content-Type** | `application/json` |
| **Tổng số controllers** | 33 controllers |

### Nhóm API theo Base Path

| Base Path | Mô tả | Role |
|-----------|--------|------|
| `/api/auth/*` | Xác thực | Public |
| `/api/users/*` | Hồ sơ người dùng | All authenticated |
| `/api/admin/*` | Quản trị hệ thống | ADMIN |
| `/api/student/*` | Portal sinh viên | STUDENT |
| `/api/teacher/*` | Portal giảng viên | TEACHER |
| `/api/chat/*` | Hệ thống chat | All authenticated |
| `/api/chatbot/*` | AI chatbot | All authenticated |

---

## 2. Auth API

| Method | URL | Mô tả | Auth | Request Body | Response |
|--------|-----|--------|------|--------------|---------|
| `POST` | `/api/auth/login` | Đăng nhập | ❌ | `{username, password}` | `{accessToken, refreshToken, role}` |
| `POST` | `/api/auth/refresh` | Refresh token | ❌ | `{refreshToken}` | `{accessToken, refreshToken}` |
| `POST` | `/api/auth/logout` | Đăng xuất | ✅ | `{refreshToken}` | `"Logout successful"` |
| `GET` | `/api/users/me` | Lấy profile bản thân | ✅ | — | `UserProfileResponse` |
| `GET` | `/api/users/{identifier}` | Xem profile người khác | ✅ | — | `UserProfileResponse` |

---

## 3. Admin API

### 3.1 User Management

| Method | URL | Mô tả | Request | Response |
|--------|-----|--------|---------|---------|
| `GET` | `/api/admin/users` | Danh sách users | — | `AdminUserResponse[]` |
| `POST` | `/api/admin/users/admin?username=&password=&email=` | Tạo admin | Query params | string |
| `PUT` | `/api/admin/users/{id}` | Cập nhật user | `AdminUserUpdateRequest` | `AdminUserResponse` |
| `PUT` | `/api/admin/users/{id}/toggle-status` | Bật/tắt tài khoản | — | string |
| `DELETE` | `/api/admin/users/admin/{id}` | Xóa admin | — | string |

### 3.2 Student & Teacher Management

| Method | URL | Mô tả | Request | Response |
|--------|-----|--------|---------|---------|
| `GET` | `/api/admin/students` | Danh sách sinh viên | — | `AdminStudentResponse[]` |
| `POST` | `/api/admin/students` | Tạo sinh viên | `AdminStudentRequest` | `AdminStudentResponse` |
| `PUT` | `/api/admin/students/{id}` | Cập nhật sinh viên | Partial update | `AdminStudentResponse` |
| `DELETE` | `/api/admin/students/{id}` | Xóa sinh viên | — | string |
| `GET` | `/api/admin/teachers` | Danh sách giảng viên | — | `AdminTeacherResponse[]` |
| `POST` | `/api/admin/teachers` | Tạo giảng viên | `AdminTeacherRequest` | `AdminTeacherResponse` |
| `PUT` | `/api/admin/teachers/{id}` | Cập nhật giảng viên | Partial update | `AdminTeacherResponse` |
| `DELETE` | `/api/admin/teachers/{id}` | Xóa giảng viên | — | string |

### 3.3 Academic Structure

| Method | URL | Mô tả | Request | Response |
|--------|-----|--------|---------|---------|
| `GET` | `/api/admin/majors` | Danh sách ngành | — | `MajorResponse[]` |
| `POST` | `/api/admin/majors` | Tạo ngành | `AdminMajorRequest` | `MajorResponse` |
| `PUT` | `/api/admin/majors/{id}` | Cập nhật ngành | `AdminMajorRequest` | `MajorResponse` |
| `DELETE` | `/api/admin/majors/{id}` | Xóa ngành | — | string |
| `GET` | `/api/admin/departments` | Danh sách khoa | — | `DepartmentResponse[]` |
| `POST` | `/api/admin/departments` | Tạo khoa | `DepartmentRequest` | `DepartmentResponse` |
| `PUT` | `/api/admin/departments/{id}` | Cập nhật khoa | `DepartmentRequest` | `DepartmentResponse` |
| `DELETE` | `/api/admin/departments/{id}` | Xóa khoa | — | string |
| `GET` | `/api/admin/homerooms` | Danh sách lớp niên chế | — | `HomeroomResponse[]` |
| `POST` | `/api/admin/homerooms` | Tạo lớp niên chế | `HomeroomRequest` | `HomeroomResponse` |
| `PUT` | `/api/admin/homerooms/{id}` | Cập nhật | `HomeroomRequest` | `HomeroomResponse` |
| `DELETE` | `/api/admin/homerooms/{id}` | Xóa | — | string |
| `GET` | `/api/admin/homerooms/{id}/students` | SV trong lớp | — | `AdminStudentResponse[]` |
| `POST` | `/api/admin/homerooms/{id}/students` | Thêm SV vào lớp | `{studentIds[]}` | string |
| `DELETE` | `/api/admin/homerooms/{hId}/students/{sId}` | Xóa SV khỏi lớp | — | string |
| `GET` | `/api/admin/courses` | Danh sách môn học | — | `CourseResponse[]` |
| `POST` | `/api/admin/courses` | Tạo môn học | `AdminCourseRequest` | `CourseResponse` |
| `PUT` | `/api/admin/courses/{id}` | Cập nhật môn | `AdminCourseRequest` | `CourseResponse` |
| `DELETE` | `/api/admin/courses/{id}` | Xóa môn | — | string |
| `GET` | `/api/admin/rooms` | Danh sách phòng | — | `RoomResponse[]` |
| `POST` | `/api/admin/rooms` | Tạo phòng | `AdminRoomRequest` | `RoomResponse` |
| `PUT` | `/api/admin/rooms/{id}` | Cập nhật phòng | `AdminRoomRequest` | `RoomResponse` |
| `DELETE` | `/api/admin/rooms/{id}` | Xóa phòng | — | string |
| `GET` | `/api/admin/periods` | Danh sách tiết | — | `PeriodResponse[]` |
| `POST` | `/api/admin/periods` | Tạo tiết | `PeriodRequest` | `PeriodResponse` |
| `PUT` | `/api/admin/periods/{id}` | Cập nhật tiết | `PeriodRequest` | `PeriodResponse` |
| `DELETE` | `/api/admin/periods/{id}` | Xóa tiết | — | string |

### 3.4 Semester & Class Section

| Method | URL | Mô tả | Request | Response |
|--------|-----|--------|---------|---------|
| `GET` | `/api/admin/semesters` | Danh sách học kỳ | — | `StudentSemesterResponse[]` |
| `POST` | `/api/admin/semesters` | Tạo học kỳ | `SemesterRequest` | `StudentSemesterResponse` |
| `PUT` | `/api/admin/semesters/{id}` | Cập nhật | `SemesterRequest` | `StudentSemesterResponse` |
| `DELETE` | `/api/admin/semesters/{id}` | Xóa học kỳ | — | string |
| `GET` | `/api/admin/semesters/{id}/summary` | Tóm tắt học kỳ | — | `SemesterSummaryResponse` |
| `POST` | `/api/admin/semesters/{id}/toggle-registration` | Mở/đóng đăng ký | `{open}` | `StudentSemesterResponse` |
| `POST` | `/api/admin/semesters/{id}/lock-enrollments` | Khóa enrollments | — | `{message}` |
| `POST` | `/api/admin/semesters/{id}/publish-exams` | Xuất bản lịch thi | — | `StudentSemesterResponse` |
| `POST` | `/api/admin/semesters/{id}/unpublish-exams` | Ẩn lịch thi | — | `StudentSemesterResponse` |
| `POST` | `/api/admin/semesters/{id}/toggle-retake` | Mở/đóng thi lại | `{open}` | `StudentSemesterResponse` |
| `POST` | `/api/admin/semesters/{id}/lock-retakes` | Khóa thi lại | — | `{message}` |
| `GET` | `/api/admin/class-sections` | Tất cả lớp học phần | — | `ClassSectionResponse[]` |
| `POST` | `/api/admin/class-sections` | Tạo lớp HP | `AdminClassSectionRequest` | `ClassSectionResponse` |
| `PUT` | `/api/admin/class-sections/{id}` | Cập nhật lớp HP | `AdminClassSectionRequest` | `ClassSectionResponse` |
| `DELETE` | `/api/admin/class-sections/{id}` | Xóa lớp HP | — | string |
| `GET` | `/api/admin/class-sections/semester/{id}` | Lớp HP theo kỳ | — | `ClassSectionResponse[]` |
| `GET` | `/api/admin/class-sections/{id}/students` | SV trong lớp HP | — | `AdminClassSectionStudentResponse[]` |

### 3.5 Enrollment, Exam & Export

| Method | URL | Mô tả | Request | Response |
|--------|-----|--------|---------|---------|
| `GET` | `/api/admin/enrollments` | Danh sách đăng ký (phân trang) | `?semesterId&classSectionId&status&page&size` | `Page<AdminEnrollmentResponse>` |
| `POST` | `/api/admin/enrollments/override` | Override đăng ký | `AdminOverrideEnrollmentRequest` | `AdminEnrollmentResponse` |
| `POST` | `/api/admin/enrollments/lock-semester/{id}` | Khóa enroll học kỳ | — | string |
| `POST` | `/api/admin/enrollments/lock-retakes/{id}` | Khóa retake học kỳ | — | string |
| `GET` | `/api/admin/class-sections/semester/{id}/exam-schedules` | Lịch thi theo kỳ | — | `ExamScheduleResponse[]` |
| `PUT` | `/api/admin/class-sections/{id}/exam-schedule` | Cập nhật lịch thi 1 lớp | `ExamScheduleRequest` | `ExamScheduleResponse` |
| `PUT` | `/api/admin/class-sections/semester/{id}/exam-schedules` | Batch update lịch thi | `ExamScheduleRequest[]` | `ExamScheduleResponse[]` |
| `GET` | `/api/admin/exam-registrations` | DS đăng ký thi lại | `?semesterId&status` | `AdminExamRegistrationResponse[]` |
| `GET` | `/api/admin/exam-registrations/semester/{id}/summary` | Tóm tắt thi lại | — | `AdminExamRegistrationSummary` |
| `GET` | `/api/admin/export/enrollments/semester/{id}` | Xuất Excel đăng ký | — | File `.xlsx` |
| `GET` | `/api/admin/export/exam-schedules/semester/{id}` | Xuất Excel lịch thi | — | File `.xlsx` |
| `GET` | `/api/admin/export/retakes/semester/{id}` | Xuất Excel thi lại | — | File `.xlsx` |

### 3.6 Knowledge & Academic Results

| Method | URL | Mô tả |
|--------|-----|--------|
| `GET` | `/api/admin/knowledge/documents` | Danh sách tài liệu |
| `POST` | `/api/admin/knowledge/ingest` | Nhập văn bản |
| `POST` | `/api/admin/knowledge/ingest-url` | Nhập từ URL |
| `DELETE` | `/api/admin/knowledge/documents/{id}` | Xóa tài liệu |
| `POST` | `/api/admin/knowledge/documents/{id}/reindex` | Reindex tài liệu |
| `POST` | `/api/admin/knowledge/reindex-all` | Reindex toàn bộ |
| `GET` | `/api/admin/academic-results` | Kết quả học tập toàn hệ thống |

---

## 4. Student API

| Method | URL | Mô tả | Request | Response |
|--------|-----|--------|---------|---------|
| `GET` | `/api/student/profile` | Hồ sơ sinh viên | — | `UserProfile` |
| `GET` | `/api/student/dashboard` | Dashboard tổng quan | `?semesterId` | `StudentDashboardResponse` |
| `GET` | `/api/student/semesters` | Danh sách học kỳ | — | `StudentSemesterResponse[]` |
| `GET` | `/api/student/classes/semester/{id}` | Lớp mở đăng ký | — | `ClassSectionResponse[]` |
| `POST` | `/api/student/enroll/{classSectionId}` | Đăng ký học phần | — | `{requestId, message}` |
| `DELETE` | `/api/student/enroll/{classSectionId}` | Hủy đăng ký | — | string |
| `GET` | `/api/student/enrollments/selected` | DS đã đăng ký | `?semesterId` | `EnrollmentResponse[]` |
| `GET` | `/api/student/enrollments/status/{requestId}` | Trạng thái đăng ký | — | `{status, message}` |
| `GET` | `/api/student/my-schedule/{semesterId}` | Thời khóa biểu | — | `EnrollmentResponse[]` |
| `GET` | `/api/student/grades` | Điểm tổng hợp | `?semesterId` | `StudentGradesSummaryResponse` |
| `GET` | `/api/student/learning-results` | Kết quả học tập | `?semesterId` | `LearningResultsResponse` |
| `GET` | `/api/student/academic-results/my-results` | Academic results | — | `AcademicResultResponse[]` |
| `GET` | `/api/student/exams` | Lịch thi | `?semesterId` | `StudentExamResponse[]` |
| `GET` | `/api/student/tuition/{semesterId}` | Hóa đơn học phí | — | `TuitionResponse` |
| `POST` | `/api/student/tuition/{semesterId}/vnpay-url` | Tạo URL VNPay | — | string (URL) |
| `GET` | `/api/student/tuition/vnpay-return` | Kết quả VNPay | VNPay params | string |
| `GET` | `/api/student/curriculum` | Tất cả môn học | — | `CourseResponse[]` |
| `GET` | `/api/student/curriculum/my-major` | Môn học theo ngành | — | `CourseResponse[]` |
| `GET` | `/api/student/retakes/eligible-courses` | Môn đủ điều kiện thi lại | `?semesterId` | `RetakeEligibleCourseResponse[]` |
| `POST` | `/api/student/retakes/register` | Đăng ký thi lại | `RetakeRegistrationRequest` | `RetakeRegistrationResponse` |
| `DELETE` | `/api/student/retakes/{examRegistrationId}` | Hủy thi lại | — | string |
| `GET` | `/api/student/retakes/my-requests` | DS đăng ký thi lại | `?semesterId` | `RetakeRequestResponse[]` |
| `GET` | `/api/student/notifications` | Thông báo | — | `NotificationResponse[]` |
| `POST` | `/api/student/notifications/{id}/read` | Đánh dấu đọc | — | void |
| `POST` | `/api/student/notifications/read-all` | Đọc tất cả | — | void |

---

## 5. Teacher API

| Method | URL | Mô tả | Request | Response |
|--------|-----|--------|---------|---------|
| `GET` | `/api/teacher/semesters` | Danh sách học kỳ | — | `StudentSemesterResponse[]` |
| `GET` | `/api/teacher/my-classes/semester/{id}` | Lớp phân công | — | `ClassSectionResponse[]` |
| `GET` | `/api/teacher/classes/{id}/students` | SV trong lớp | — | `TeacherStudentGradeResponse[]` |
| `GET` | `/api/teacher/grades/class/{id}` | Bảng điểm lớp | — | `GradeResponse[]` |
| `PUT` | `/api/teacher/grades/{enrollmentId}` | Nhập/cập nhật điểm | `TeacherGradeRequest` | `GradeResponse` |
| `POST` | `/api/teacher/grades/class/{id}/lock` | Khóa điểm lớp | — | string |
| `GET` | `/api/teacher/classes/{id}/attendance-sessions` | DS buổi điểm danh | — | `AttendanceSessionResponse[]` |
| `GET` | `/api/teacher/classes/{id}/attendance-sessions/{n}` | Chi tiết buổi | — | `AttendanceSessionResponse` |
| `PUT` | `/api/teacher/classes/{id}/attendance-sessions/{n}/records` | Lưu điểm danh | `AttendanceRecordRequest[]` | `AttendanceSessionResponse` |
| `POST` | `/api/teacher/classes/{id}/attendance-sessions/{n}/lock` | Khóa buổi | — | `AttendanceSessionResponse` |
| `GET` | `/api/teacher/notifications` | Thông báo | — | `NotificationResponse[]` |
| `POST` | `/api/teacher/notifications/{id}/read` | Đánh dấu đọc | — | void |
| `POST` | `/api/teacher/notifications/read-all` | Đọc tất cả | — | void |

---

## 6. Chat API

| Method | URL | Mô tả | Request | Response |
|--------|-----|--------|---------|---------|
| `GET` | `/api/chat/rooms?size=100` | Danh sách phòng chat | — | `Page<ChatRoom>` |
| `POST` | `/api/chat/rooms/private?otherUserId={id}` | Tạo chat riêng | — | `ChatRoom` |
| `POST` | `/api/chat/rooms` | Tạo nhóm chat | `{name, type, memberIds[]}` | `ChatRoom` |
| `DELETE` | `/api/chat/rooms/{id}/members/me` | Rời phòng | — | void |
| `GET` | `/api/chat/rooms/{id}/messages?size=100` | Tin nhắn | — | `Page<ChatMessage>` |
| `POST` | `/api/chat/rooms/{id}/messages` | Gửi tin nhắn | `{content, type}` | `ChatMessage` |
| `POST` | `/api/chat/rooms/{id}/read` | Đánh dấu đã đọc | — | void |
| `POST` | `/api/chat/rooms/{id}/files` | Upload file | FormData | `ChatMessage` |
| `GET` | `/api/chat/rooms/{id}/files?size=100` | Xem files | — | `Page<ChatMessage>` |
| `GET` | `/api/chat/rooms/{id}/links?size=100` | Xem links | — | `Page<ChatMessage>` |
| `GET` | `/api/chat/users/search?q={query}` | Tìm user | — | `ChatUser[]` |
| `WS` | `/ws/chat` | WebSocket endpoint | STOMP | Real-time messages |

---

## 7. Chatbot API

| Method | URL | Mô tả | Request | Response |
|--------|-----|--------|---------|---------|
| `POST` | `/api/chatbot/send` | Gửi câu hỏi | `{message, sessionId?}` | `{answer, sessionId, timestamp}` |
| `GET` | `/api/chatbot/history` | Lịch sử chat | `?sessionId` | `ChatbotMessageItem[]` |
| `DELETE` | `/api/chatbot/history` | Xóa lịch sử | `?sessionId` | void |

---

## 8. Knowledge API (Admin)

| Method | URL | Mô tả |
|--------|-----|--------|
| `GET` | `/api/admin/knowledge/documents` | Danh sách documents |
| `POST` | `/api/admin/knowledge/ingest` | Ingest văn bản |
| `POST` | `/api/admin/knowledge/ingest-url` | Ingest từ URL |
| `DELETE` | `/api/admin/knowledge/documents/{id}` | Xóa document |
| `POST` | `/api/admin/knowledge/documents/{id}/reindex` | Reindex 1 document |
| `POST` | `/api/admin/knowledge/reindex-all` | Reindex tất cả |

---

## 9. Mapping Màn Hình → API

| Màn hình / Route | API Endpoints chính |
|-----------------|---------------------|
| `/login` | `POST /api/auth/login` |
| `/admin/dashboard` | `GET /api/admin/semesters/{id}/summary` |
| `/admin/users` | `GET /api/admin/users` |
| `/admin/students` | `GET /api/admin/students` |
| `/admin/teachers` | `GET /api/admin/teachers` |
| `/admin/majors` | `GET /api/admin/majors`, `GET /api/admin/departments` |
| `/admin/homerooms` | `GET /api/admin/homerooms` |
| `/admin/courses` | `GET /api/admin/courses` |
| `/admin/rooms` | `GET /api/admin/rooms` |
| `/admin/periods` | `GET /api/admin/periods` |
| `/admin/semesters` | `GET /api/admin/semesters` |
| `/admin/semesters/{id}` | `GET /api/admin/semesters/{id}/summary`, exam schedules |
| `/admin/enrollments` | `GET /api/admin/enrollments` |
| `/admin/exam-schedules` | `GET /api/admin/class-sections/semester/{id}/exam-schedules` |
| `/admin/exam-registrations` | `GET /api/admin/exam-registrations` |
| `/admin/knowledge` | `GET /api/admin/knowledge/documents` |
| `/student/dashboard` | `GET /api/student/dashboard` |
| `/student/course-registration` | `GET /api/student/classes/semester/{id}`, `POST /api/student/enroll/{id}` |
| `/student/schedule` | `GET /api/student/my-schedule/{semesterId}` |
| `/student/grades` | `GET /api/student/learning-results` |
| `/student/academic-results` | `GET /api/student/academic-results/my-results` |
| `/student/exams` | `GET /api/student/exams` |
| `/student/tuition` | `GET /api/student/tuition/{semesterId}` |
| `/student/retake-registration` | `GET /api/student/retakes/eligible-courses` |
| `/student/curriculum` | `GET /api/student/curriculum/my-major` |
| `/student/notifications` | `GET /api/student/notifications` |
| `/student/payment-result` | Nhận kết quả từ VNPay return |
| `/teacher/dashboard` | `GET /api/teacher/semesters` |
| `/teacher/classes` | `GET /api/teacher/my-classes/semester/{id}` |
| `/teacher/classes/{id}/students` | `GET /api/teacher/classes/{id}/students` |
| `/teacher/grades` | `GET /api/teacher/grades/class/{id}`, `PUT /api/teacher/grades/{enrollmentId}` |
| `/teacher/attendance` | `GET /api/teacher/classes/{id}/attendance-sessions` |
| `/teacher/timetable` | `GET /api/teacher/my-classes/semester/{id}` |
| `/admin/chat`, `/student/chat`, `/teacher/chat` | `GET /api/chat/rooms`, WebSocket `/ws/chat` |
