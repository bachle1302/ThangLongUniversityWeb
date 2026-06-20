# ⚙️ Yêu Cầu Chức Năng — ThangLong University Web

> **Mã tài liệu:** DOC-02  
> **Phiên bản:** 1.0  
> **Ngày tạo:** 28/05/2026  

---

## Mục Lục

- [1. Nhóm Xác Thực](#1-nhóm-xác-thực-authentication)
- [2. Nhóm Quản Lý Người Dùng](#2-nhóm-quản-lý-người-dùng)
- [3. Nhóm Cấu Trúc Học Thuật](#3-nhóm-cấu-trúc-học-thuật)
- [4. Nhóm Học Kỳ & Lớp Học Phần](#4-nhóm-học-kỳ--lớp-học-phần)
- [5. Nhóm Đăng Ký Học Phần](#5-nhóm-đăng-ký-học-phần)
- [6. Nhóm Điểm Danh](#6-nhóm-điểm-danh)
- [7. Nhóm Điểm Số](#7-nhóm-điểm-số)
- [8. Nhóm Học Phí & Thanh Toán](#8-nhóm-học-phí--thanh-toán)
- [9. Nhóm Chat](#9-nhóm-chat)
- [10. Nhóm Chatbot AI](#10-nhóm-chatbot-ai)
- [11. Nhóm Thông Báo](#11-nhóm-thông-báo)
- [12. Nhóm Xuất Dữ Liệu](#12-nhóm-xuất-dữ-liệu)
- [13. Nhóm Website Marketing](#13-nhóm-website-marketing)

---

## 1. Nhóm Xác Thực (Authentication)

| Mã | Tên chức năng | Mô tả | Actor | Đầu vào | Đầu ra | Ghi chú |
|----|--------------|-------|-------|---------|--------|---------|
| **FR-001** | Đăng nhập | Người dùng đăng nhập bằng username/password | All | `{username, password}` | `{accessToken, refreshToken, role}` | JWT, role-based redirect |
| **FR-002** | Đăng xuất | Hủy session, vô hiệu hóa refresh token | All | `{refreshToken}` | Thông báo thành công | Redis blacklist token |
| **FR-003** | Làm mới token | Dùng refresh token lấy access token mới | All | `{refreshToken}` | `{accessToken, refreshToken mới}` | Token rotation |
| **FR-004** | Xem hồ sơ bản thân | Lấy thông tin profile người dùng đang đăng nhập | All | JWT token | `UserProfile` object | `/api/users/me` |
| **FR-005** | Xem hồ sơ người khác | Tìm profile theo username/mã SV/mã GV | All | identifier (username hoặc code) | `UserProfile` object | Multi-lookup: username → studentCode → teacherCode |

---

## 2. Nhóm Quản Lý Người Dùng

| Mã | Tên chức năng | Mô tả | Actor | Đầu vào | Đầu ra | Ghi chú |
|----|--------------|-------|-------|---------|--------|---------|
| **FR-006** | Xem danh sách users | Lấy toàn bộ tài khoản hệ thống | Admin | — | `AdminUserResponse[]` | — |
| **FR-007** | Tạo tài khoản Admin | Tạo tài khoản quản trị viên mới | Admin | `{username, password, email}` | Thông báo thành công | Params qua query string |
| **FR-008** | Cập nhật thông tin user | Sửa username, email, fullName | Admin | `{username, email, fullName}` | `AdminUserResponse` | — |
| **FR-009** | Bật/tắt tài khoản | Toggle trạng thái active/inactive | Admin | userId | Thông báo | Không xóa mà chỉ vô hiệu hóa |
| **FR-010** | Xóa tài khoản Admin | Xóa tài khoản admin | Admin | adminId | Thông báo | Chỉ xóa admin, không xóa user khác qua endpoint này |
| **FR-011** | CRUD Sinh viên | Tạo/sửa/xóa hồ sơ sinh viên | Admin | `AdminStudentRequest` | `AdminStudentResponse` | Tạo kèm user account |
| **FR-012** | CRUD Giảng viên | Tạo/sửa/xóa hồ sơ giảng viên | Admin | `AdminTeacherRequest` | `AdminTeacherResponse` | Tạo kèm user account |

---

## 3. Nhóm Cấu Trúc Học Thuật

| Mã | Tên chức năng | Mô tả | Actor | Đầu vào | Đầu ra | Ghi chú |
|----|--------------|-------|-------|---------|--------|---------|
| **FR-013** | CRUD Ngành học | Quản lý danh sách ngành (Major) | Admin | `AdminMajorRequest` | `MajorResponse` | Code ngành: CNTT, KT, NN, QTKD, LUAT |
| **FR-014** | CRUD Khoa | Quản lý danh sách khoa (Department) | Admin | `DepartmentRequest` | `DepartmentResponse` | Khoa quản lý nhiều ngành |
| **FR-015** | CRUD Lớp niên chế | Quản lý homeroom class | Admin | `HomeroomRequest` | `HomeroomResponse` | Gán cố vấn (advisor) |
| **FR-016** | Thêm/xóa SV khỏi lớp | Gán sinh viên vào/ra lớp niên chế | Admin | `{studentIds[]}` | Thông báo | Batch operation |
| **FR-017** | CRUD Phòng học | Quản lý phòng học (Room) | Admin | `AdminRoomRequest` | `RoomResponse` | Có sức chứa (capacity) |
| **FR-018** | CRUD Tiết học | Quản lý tiết học theo giờ | Admin | `PeriodRequest` | `PeriodResponse` | 14 tiết/ngày, 07:00 - 19:45 |
| **FR-019** | CRUD Môn học | Quản lý môn học, tín chỉ, điều kiện tiên quyết | Admin | `AdminCourseRequest` | `CourseResponse` | Có prerequisiteCourseIds |
| **FR-020** | Xem chương trình ĐT | Xem tất cả môn học hoặc theo ngành | Student | — | `CourseResponse[]` | `/api/student/curriculum` |

---

## 4. Nhóm Học Kỳ & Lớp Học Phần

| Mã | Tên chức năng | Mô tả | Actor | Đầu vào | Đầu ra | Ghi chú |
|----|--------------|-------|-------|---------|--------|---------|
| **FR-021** | CRUD Học kỳ | Tạo/sửa/xóa học kỳ | Admin | `SemesterRequest` | `AdminSemesterResponse` | — |
| **FR-022** | Mở/đóng đăng ký | Toggle trạng thái đăng ký học phần | Admin | `{open: boolean}` | `AdminSemesterResponse` | Cập nhật `registrationOpen` |
| **FR-023** | Khóa enrollments | Khóa toàn bộ đăng ký của học kỳ | Admin | semesterId | Thông báo | Không thể đăng ký thêm |
| **FR-024** | Xuất bản lịch thi | Công bố lịch thi cho sinh viên | Admin | semesterId | `AdminSemesterResponse` | Cập nhật `examPublished` |
| **FR-025** | Mở/đóng thi lại | Toggle đăng ký thi lại | Admin | `{open: boolean}` | `AdminSemesterResponse` | Cập nhật `retakeOpen` |
| **FR-026** | Xem tóm tắt học kỳ | Dashboard tổng hợp số liệu học kỳ | Admin | semesterId | `SemesterSummaryResponse` | Số lớp, đăng ký, thi lại... |
| **FR-027** | CRUD Lớp học phần | Tạo/sửa/xóa lớp học phần | Admin | `AdminClassSectionRequest` | `ClassSectionResponse` | Gồm lịch học đa buổi |
| **FR-028** | Quản lý lịch thi | Cài đặt ngày thi, phòng thi, loại thi | Admin | `ExamScheduleRequest` | `ExamScheduleResponse` | NORMAL / RETAKE / IMPROVE |
| **FR-029** | Xem danh sách lớp GV | Giảng viên xem lớp được phân công | Teacher | semesterId | `ClassSectionResponse[]` | Theo học kỳ |

---

## 5. Nhóm Đăng Ký Học Phần

| Mã | Tên chức năng | Mô tả | Actor | Đầu vào | Đầu ra | Ghi chú |
|----|--------------|-------|-------|---------|--------|---------|
| **FR-030** | Xem lớp mở đăng ký | Danh sách lớp học phần còn slot trong học kỳ | Student | semesterId | `ClassSectionResponse[]` | — |
| **FR-031** | Đăng ký học phần | Đăng ký vào lớp học phần (async) | Student | classSectionId | `{requestId, message}` | Qua Kafka queue |
| **FR-032** | Hủy đăng ký | Hủy đăng ký một lớp học phần | Student | classSectionId | Thông báo | Chỉ khi đăng ký mở |
| **FR-033** | Kiểm tra trạng thái | Poll kết quả xử lý đăng ký | Student | requestId | `{status, message}` | PENDING/PROCESSING/SUCCESS/FAILED |
| **FR-034** | Xem đăng ký đã chọn | Danh sách lớp đã đăng ký trong học kỳ | Student | semesterId | `EnrollmentResponse[]` | — |
| **FR-035** | Admin override | Admin ghi đè đăng ký cho sinh viên | Admin | `{studentId, classSectionId}` | `AdminEnrollmentResponse` | Bỏ qua các ràng buộc thông thường |
| **FR-036** | Xem danh sách enrollments | Admin xem toàn bộ đăng ký với filter | Admin | params(semesterId, classSectionId, status...) | `Page<AdminEnrollmentResponse>` | Có phân trang |
| **FR-037** | Đăng ký thi lại | Đăng ký môn thi lại/học lại | Student | `{courseIds[], semesterId}` | `RetakeRegistrationResponse` | Kiểm tra điều kiện môn đủ điều kiện |
| **FR-038** | Hủy đăng ký thi lại | Hủy một đăng ký thi lại | Student | examRegistrationId | Thông báo | — |
| **FR-039** | Xem môn đủ điều kiện thi lại | Danh sách môn trượt/cần cải thiện | Student | semesterId? | `RetakeEligibleCourseResponse[]` | Gồm phí thi lại |

---

## 6. Nhóm Điểm Danh

| Mã | Tên chức năng | Mô tả | Actor | Đầu vào | Đầu ra | Ghi chú |
|----|--------------|-------|-------|---------|--------|---------|
| **FR-040** | Xem buổi điểm danh | Danh sách buổi điểm danh của lớp | Teacher | classSectionId | `AttendanceSessionResponse[]` | — |
| **FR-041** | Xem chi tiết buổi | Chi tiết một buổi điểm danh | Teacher | classSectionId, sessionNumber | `AttendanceSessionResponse` | Gồm danh sách records |
| **FR-042** | Lưu điểm danh | Nhập trạng thái điểm danh cho từng SV | Teacher | sessionNumber, `AttendanceRecordRequest[]` | `AttendanceSessionResponse` | PRESENT / LATE / ABSENT |
| **FR-043** | Khóa buổi điểm danh | Khóa buổi sau khi hoàn tất | Teacher | classSectionId, sessionNumber | `AttendanceSessionResponse` | Không thể sửa sau khi khóa |

---

## 7. Nhóm Điểm Số

| Mã | Tên chức năng | Mô tả | Actor | Đầu vào | Đầu ra | Ghi chú |
|----|--------------|-------|-------|---------|--------|---------|
| **FR-044** | Nhập/cập nhật điểm SV | Giảng viên nhập điểm các thành phần | Teacher | enrollmentId, `{participationScore, midTermScore, finalScore, retestScore}` | `GradeResponse` | Chỉ lớp của mình, khi chưa khóa |
| **FR-045** | Xem bảng điểm lớp | Giảng viên xem điểm cả lớp | Teacher | classSectionId | `GradeResponse[]` | — |
| **FR-046** | Khóa điểm lớp | Khóa toàn bộ điểm lớp học phần | Teacher | classSectionId | Thông báo | `grade_locked = true` |
| **FR-047** | Xem điểm cá nhân | Sinh viên xem điểm tổng hợp | Student | semesterId? | `StudentGradesSummaryResponse` | GPA + chi tiết |
| **FR-048** | Xem kết quả học tập | Xem GPA/CPA qua các học kỳ | Student | semesterId? | `LearningResultsResponse` | Gồm semesterSummaries |
| **FR-049** | Xem academic results | Admin xem GPA/CPA toàn hệ thống | Admin | — | `AcademicResultResponse[]` | — |

---

## 8. Nhóm Học Phí & Thanh Toán

| Mã | Tên chức năng | Mô tả | Actor | Đầu vào | Đầu ra | Ghi chú |
|----|--------------|-------|-------|---------|--------|---------|
| **FR-050** | Xem hóa đơn học phí | Xem chi tiết học phí theo học kỳ | Student | semesterId | `TuitionResponse` | Gồm từng môn học |
| **FR-051** | Tạo link thanh toán | Tạo URL thanh toán VNPay | Student | semesterId | URL (string) | Redirect sang VNPay |
| **FR-052** | Nhận kết quả VNPay | Xử lý callback từ VNPay | System | VNPay params | Cập nhật `paid=true` | response_code = "00" → thành công |
| **FR-053** | Xem kết quả thanh toán | Trang hiển thị kết quả giao dịch | Student | VNPay return params | Thông báo thành công/thất bại | Frontend route: `/student/payment-result` |

---

## 9. Nhóm Chat

| Mã | Tên chức năng | Mô tả | Actor | Đầu vào | Đầu ra | Ghi chú |
|----|--------------|-------|-------|---------|--------|---------|
| **FR-054** | Xem danh sách phòng chat | Xem tất cả phòng chat của mình | All | — | `Page<ChatRoom>` | PRIVATE / GROUP / CLASS_GROUP |
| **FR-055** | Tìm kiếm người dùng | Tìm user để bắt đầu chat | All | query string | `ChatUser[]` | — |
| **FR-056** | Tạo phòng chat riêng | Chat 1-1 với user khác | All | otherUserId | `ChatRoom` | Nếu đã có phòng thì trả về phòng cũ |
| **FR-057** | Tạo nhóm chat | Tạo nhóm chat nhiều người | All | `{name, memberIds[]}` | `ChatRoom` | type = GROUP |
| **FR-058** | Rời phòng chat | Rời khỏi phòng chat | All | roomId | — | — |
| **FR-059** | Xem tin nhắn | Tải lịch sử tin nhắn trong phòng | All | roomId | `Page<ChatMessage>` | — |
| **FR-060** | Gửi tin nhắn văn bản | Gửi message TEXT | All | roomId, content | `ChatMessage` | Real-time qua WebSocket |
| **FR-061** | Gửi file | Gửi tệp đính kèm | All | roomId, file | `ChatMessage` | Upload lên Cloudinary |
| **FR-062** | Đánh dấu đã đọc | Mark phòng chat là đã đọc | All | roomId | — | Reset unread_count |
| **FR-063** | Xem files/links | Xem tất cả file/link trong phòng | All | roomId | `Page<ChatMessage>` | — |

---

## 10. Nhóm Chatbot AI

| Mã | Tên chức năng | Mô tả | Actor | Đầu vào | Đầu ra | Ghi chú |
|----|--------------|-------|-------|---------|--------|---------|
| **FR-064** | Gửi câu hỏi chatbot | Hỏi chatbot AI | All | `{message, sessionId?}` | `{answer, sessionId, timestamp}` | RAG với Groq |
| **FR-065** | Xem lịch sử chat | Xem lịch sử hội thoại với chatbot | All | sessionId? | `ChatbotMessageItem[]` | — |
| **FR-066** | Xóa lịch sử | Xóa lịch sử chatbot | All | sessionId? | — | — |
| **FR-067** | Quản lý knowledge | Thêm tài liệu văn bản cho chatbot | Admin | `IngestTextPayload` | `{documentId}` | — |
| **FR-068** | Ingest URL | Thêm tài liệu từ URL web | Admin | `IngestUrlPayload` | `{documentId}` | Crawl và index nội dung |
| **FR-069** | Xóa tài liệu | Xóa document khỏi knowledge base | Admin | documentId | — | — |
| **FR-070** | Reindex | Tái tạo index embedding | Admin | documentId hoặc all | — | — |

---

## 11. Nhóm Thông Báo

| Mã | Tên chức năng | Mô tả | Actor | Đầu vào | Đầu ra | Ghi chú |
|----|--------------|-------|-------|---------|--------|---------|
| **FR-071** | Xem thông báo | Danh sách thông báo của người dùng | Student, Teacher | — | `NotificationResponse[]` | type: SCHOOL / CHAT |
| **FR-072** | Đánh dấu đã đọc | Mark một thông báo đã đọc | Student, Teacher | notificationId | — | — |
| **FR-073** | Đánh dấu tất cả đã đọc | Mark toàn bộ thông báo đã đọc | Student, Teacher | — | — | — |

---

## 12. Nhóm Xuất Dữ Liệu

| Mã | Tên chức năng | Mô tả | Actor | Đầu vào | Đầu ra | Ghi chú |
|----|--------------|-------|-------|---------|--------|---------|
| **FR-074** | Xuất danh sách đăng ký | Xuất file Excel danh sách sinh viên đã đăng ký | Admin | semesterId | File `.xlsx` | — |
| **FR-075** | Xuất lịch thi | Xuất file Excel lịch thi | Admin | semesterId | File `.xlsx` | — |
| **FR-076** | Xuất danh sách thi lại | Xuất file Excel đăng ký thi lại | Admin | semesterId | File `.xlsx` | — |

---

## 13. Nhóm Website Marketing

| Mã | Tên chức năng | Mô tả | Actor | Đầu vào | Đầu ra | Ghi chú |
|----|--------------|-------|-------|---------|--------|---------|
| **FR-077** | Xem trang chủ | Landing page giới thiệu trường | Public | — | Trang HTML | Giới thiệu, ngành học, testimonials, gallery |
| **FR-078** | Xem tuyển sinh | Thông tin tuyển sinh | Public | — | Trang HTML | — |
| **FR-079** | Xem chương trình đào tạo | Danh sách ngành đào tạo | Public | — | Trang HTML | — |
| **FR-080** | Xem bài viết | Danh sách tin tức/bài viết | Public | — | Trang HTML | API là stub, dùng mock data |
| **FR-081** | Xem chi tiết bài viết | Đọc nội dung bài viết | Public | slug | Trang HTML | — |
| **FR-082** | Xem về chúng tôi | Trang giới thiệu trường | Public | — | Trang HTML | — |
| **FR-083** | Xem học bổng | Thông tin học bổng | Public | — | Trang HTML | — |
| **FR-084** | Xem học phí công khai | Bảng học phí dành cho thí sinh | Public | — | Trang HTML | — |
| **FR-085** | Sitemap XML | Sitemap cho SEO | System | — | XML | — |

---

> 📌 **Lưu ý:** Một số chức năng (FR-080, FR-081) hiện sử dụng mock data tại frontend vì API bài viết chưa được phát triển ở backend. Đây là điểm cần bổ sung.
