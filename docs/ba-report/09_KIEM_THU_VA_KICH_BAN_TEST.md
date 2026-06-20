# 🧪 Kiểm Thử & Kịch Bản Test — ThangLong University Web

> **Mã tài liệu:** DOC-09 | **Phiên bản:** 1.0 | **Ngày tạo:** 28/05/2026

---

## Mục Lục
- [1. Nhóm Authentication](#1-nhóm-authentication)
- [2. Nhóm Đăng Ký Học Phần](#2-nhóm-đăng-ký-học-phần)
- [3. Nhóm Điểm Danh & Điểm Số](#3-nhóm-điểm-danh--điểm-số)
- [4. Nhóm Học Phí & Thanh Toán](#4-nhóm-học-phí--thanh-toán)
- [5. Nhóm Quản Trị Admin](#5-nhóm-quản-trị-admin)
- [6. Nhóm Chat & Chatbot](#6-nhóm-chat--chatbot)

---

## 1. Nhóm Authentication

| TC-ID | Chức năng | Điều kiện test | Các bước | Kết quả mong đợi | Ưu tiên |
|-------|-----------|---------------|---------|-----------------|---------|
| **TC-001** | Đăng nhập thành công (Admin) | Tài khoản admin tồn tại, is_active=true | 1. Vào /login<br>2. Nhập username="admin", password="Admin@123"<br>3. Click đăng nhập | Redirect đến /admin/dashboard, nhận accessToken + refreshToken | 🔴 Cao |
| **TC-002** | Đăng nhập thành công (Student) | Tài khoản SV tồn tại, active | 1. Nhập credentials sinh viên<br>2. Đăng nhập | Redirect /student/dashboard | 🔴 Cao |
| **TC-003** | Đăng nhập thành công (Teacher) | Tài khoản GV tồn tại, active | 1. Nhập credentials GV<br>2. Đăng nhập | Redirect /teacher/dashboard | 🔴 Cao |
| **TC-004** | Đăng nhập sai mật khẩu | Tài khoản tồn tại, password sai | 1. Nhập đúng username<br>2. Nhập sai password<br>3. Đăng nhập | Hiển thị lỗi 401, không redirect | 🔴 Cao |
| **TC-005** | Đăng nhập tài khoản không tồn tại | Username không có trong DB | 1. Nhập username không tồn tại | 401, thông báo lỗi | 🔴 Cao |
| **TC-006** | Đăng nhập tài khoản bị vô hiệu hóa | is_active=false | 1. Đăng nhập với tài khoản inactive | 401, thông báo tài khoản bị khóa | 🔴 Cao |
| **TC-007** | Đăng xuất | Đã đăng nhập | 1. Click logout<br>2. Xác nhận | Xóa tokens, redirect /login | 🟡 Trung bình |
| **TC-008** | Refresh token | Access token hết hạn | 1. Access token hết hạn<br>2. Hệ thống tự refresh | Nhận access token mới, session tiếp tục | 🟡 Trung bình |
| **TC-009** | Truy cập URL bảo vệ khi chưa đăng nhập | Chưa có token | 1. Truy cập /student/dashboard | Redirect về /login | 🔴 Cao |
| **TC-010** | Truy cập sai role | Student truy cập /admin | 1. Student đăng nhập<br>2. Truy cập /admin/users | 403 Forbidden hoặc redirect | 🔴 Cao |

---

## 2. Nhóm Đăng Ký Học Phần

| TC-ID | Chức năng | Điều kiện test | Các bước | Kết quả mong đợi | Ưu tiên |
|-------|-----------|---------------|---------|-----------------|---------|
| **TC-011** | Đăng ký thành công | Kỳ mở đăng ký, lớp còn slot, không trùng lịch | 1. Vào course-registration<br>2. Chọn học kỳ<br>3. Chọn lớp HP<br>4. Click Đăng ký<br>5. Poll status | status=SUCCESS, enrollment được tạo | 🔴 Cao |
| **TC-012** | Đăng ký khi học kỳ đóng | registrationOpen=false | 1. Chọn lớp trong kỳ đóng<br>2. Click đăng ký | status=FAILED, "Học kỳ không mở đăng ký" | 🔴 Cao |
| **TC-013** | Đăng ký khi lớp đầy | currentSlots >= maxSlots | 1. Chọn lớp đã đầy<br>2. Click đăng ký | status=FAILED, "Lớp đã đầy" | 🔴 Cao |
| **TC-014** | Đăng ký trùng lịch | SV đã có lớp trùng ngày/tiết | 1. Đăng ký lớp trùng TKB | status=FAILED, "Xung đột lịch học" | 🔴 Cao |
| **TC-015** | Đăng ký trùng môn | SV đã đăng ký lớp này | 1. Đăng ký lại lớp đã đăng ký | status=FAILED, "Đã đăng ký" | 🔴 Cao |
| **TC-016** | Hủy đăng ký thành công | SV đã đăng ký, kỳ còn mở | 1. Xem danh sách đã đăng ký<br>2. Click Hủy<br>3. Xác nhận | Enrollment bị hủy, slot giảm 1 | 🔴 Cao |
| **TC-017** | Xem trạng thái xử lý | Vừa đăng ký | 1. Sau khi đăng ký<br>2. Poll GET /status/{requestId} | Nhận status PENDING → PROCESSING → SUCCESS/FAILED | 🟡 Trung bình |
| **TC-018** | Admin override enrollment | Admin đã đăng nhập | 1. Admin vào Enrollments<br>2. Override cho SV vào lớp đã đầy | Enrollment được tạo dù lớp đầy | 🟡 Trung bình |

---

## 3. Nhóm Điểm Danh & Điểm Số

| TC-ID | Chức năng | Điều kiện test | Các bước | Kết quả mong đợi | Ưu tiên |
|-------|-----------|---------------|---------|-----------------|---------|
| **TC-019** | Nhập điểm danh thành công | GV đăng nhập, buổi chưa khóa | 1. Vào Attendance<br>2. Chọn lớp, chọn buổi<br>3. Nhập PRESENT/LATE/ABSENT<br>4. Lưu | Records được lưu | 🔴 Cao |
| **TC-020** | Sửa điểm danh | Buổi chưa khóa | 1. Vào buổi đã nhập<br>2. Sửa trạng thái<br>3. Lưu lại | Records được cập nhật | 🟡 Trung bình |
| **TC-021** | Khóa buổi điểm danh | Buổi có records | 1. Click khóa buổi | locked=true, không thể sửa nữa | 🟡 Trung bình |
| **TC-022** | Thử sửa buổi đã khóa | locked=true | 1. Cố sửa buổi đã khóa | Không cho sửa (readonly/error) | 🟡 Trung bình |
| **TC-023** | Nhập điểm thành công | GV là người dạy lớp, grade_locked=false | 1. Vào Grades<br>2. Chọn lớp<br>3. Nhập participationScore, midTermScore, finalScore<br>4. Lưu | Điểm được lưu, total_score tự tính | 🔴 Cao |
| **TC-024** | Kiểm tra công thức điểm | Nhập 8.0, 7.5, 9.0 | total = 0.1×8 + 0.3×7.5 + 0.6×9 = 8.45 | total_score = 8.45, letter_grade = A | 🔴 Cao |
| **TC-025** | GV không phải người dạy lớp | Teacher B thử nhập điểm lớp của Teacher A | 1. GV B vào lớp GV A<br>2. Thử nhập điểm | 403 Forbidden | 🔴 Cao |
| **TC-026** | Nhập điểm sau khi khóa | grade_locked=true | 1. Nhập điểm lớp đã khóa | 400 Bad Request "Đã khóa điểm" | 🔴 Cao |
| **TC-027** | Khóa điểm cả lớp | GV đã nhập đủ điểm | 1. Click Khóa điểm<br>2. Xác nhận | grade_locked=true, không thể sửa | 🟡 Trung bình |
| **TC-028** | SV xem điểm của mình | SV đã có grades | 1. Vào /student/grades | Hiển thị bảng điểm với GPA kỳ, CPA | 🔴 Cao |

---

## 4. Nhóm Học Phí & Thanh Toán

| TC-ID | Chức năng | Điều kiện test | Các bước | Kết quả mong đợi | Ưu tiên |
|-------|-----------|---------------|---------|-----------------|---------|
| **TC-029** | Xem hóa đơn học phí | SV có đăng ký môn học | 1. Vào /student/tuition<br>2. Chọn học kỳ | Hiển thị chi tiết: từng môn, tín chỉ, đơn giá, tổng | 🔴 Cao |
| **TC-030** | Tạo link thanh toán VNPay | SV chưa thanh toán | 1. Click "Thanh toán"<br>2. Backend tạo URL | Redirect đến trang VNPay với đúng số tiền | 🔴 Cao |
| **TC-031** | Thanh toán thành công (test) | Dùng thẻ test NCB | 1. Chọn NCB<br>2. Số thẻ: 9704198526191432198<br>3. OTP: 123456 | Redirect về /payment-result?success=true, is_completed=true | 🔴 Cao |
| **TC-032** | Thanh toán thất bại | Hủy giao dịch VNPay | 1. Vào VNPay<br>2. Hủy giao dịch | Redirect về /payment-result?success=false | 🔴 Cao |
| **TC-033** | Kiểm tra trạng thái sau thanh toán | Sau TC-031 | 1. Vào /student/tuition lại | Hiển thị "Đã thanh toán" | 🟡 Trung bình |

---

## 5. Nhóm Quản Trị Admin

| TC-ID | Chức năng | Điều kiện test | Các bước | Kết quả mong đợi | Ưu tiên |
|-------|-----------|---------------|---------|-----------------|---------|
| **TC-034** | Tạo sinh viên mới | Admin đăng nhập | 1. Vào Students<br>2. Click Thêm<br>3. Nhập thông tin (username, password, email, studentCode, fullName, majorId...)<br>4. Lưu | Sinh viên được tạo, user account kèm theo | 🔴 Cao |
| **TC-035** | Tạo giảng viên mới | Admin đăng nhập | Tương tự TC-034 nhưng cho teacher | GV được tạo | 🔴 Cao |
| **TC-036** | Tạo học kỳ mới | Admin đăng nhập | 1. Vào Semesters<br>2. Tạo kỳ với name, startDate, endDate | Kỳ được tạo, registrationOpen=false | 🔴 Cao |
| **TC-037** | Mở đăng ký học kỳ | Kỳ tồn tại | 1. Chọn kỳ<br>2. Toggle đăng ký → ON | registrationOpen=true, SV có thể đăng ký | 🔴 Cao |
| **TC-038** | Tạo lớp học phần | Kỳ học, môn học, GV, phòng có sẵn | 1. Vào class sections<br>2. Tạo lớp với đầy đủ thông tin | Lớp HP được tạo với lịch học | 🔴 Cao |
| **TC-039** | Cài lịch thi | Lớp HP có SV đăng ký | 1. Vào Exam Schedules<br>2. Nhập ngày thi, phòng | exam_at và exam_room được lưu | 🟡 Trung bình |
| **TC-040** | Xuất bản lịch thi | Đã cài lịch thi | 1. Click Publish Exams | examPublished=true, SV thấy lịch thi | 🟡 Trung bình |
| **TC-041** | Xuất Excel đăng ký | Kỳ có enrollments | 1. Vào Export<br>2. Chọn kỳ<br>3. Xuất file đăng ký | Tải về file .xlsx đúng dữ liệu | 🟡 Trung bình |
| **TC-042** | Toggle trạng thái user | Admin đăng nhập | 1. Chọn user<br>2. Toggle active/inactive | is_active thay đổi, nếu inactive không đăng nhập được | 🟡 Trung bình |

---

## 6. Nhóm Chat & Chatbot

| TC-ID | Chức năng | Điều kiện test | Các bước | Kết quả mong đợi | Ưu tiên |
|-------|-----------|---------------|---------|-----------------|---------|
| **TC-043** | Tạo chat riêng | 2 user đã đăng nhập | 1. Tìm user<br>2. Click nhắn tin | Tạo phòng PRIVATE, hiển thị chat UI | 🟡 Trung bình |
| **TC-044** | Gửi tin nhắn text | Đã vào phòng chat | 1. Nhập nội dung<br>2. Gửi | Tin nhắn hiển thị real-time qua WebSocket | 🟡 Trung bình |
| **TC-045** | Gửi file | Đang trong phòng chat | 1. Chọn file<br>2. Upload | File được upload Cloudinary, hiển thị trong chat | 🟢 Thấp |
| **TC-046** | Tạo nhóm chat | Nhiều user | 1. Tạo nhóm<br>2. Thêm thành viên | Phòng GROUP được tạo, đa người chat | 🟢 Thấp |
| **TC-047** | Chatbot trả lời câu hỏi | Knowledge base có dữ liệu | 1. Gõ câu hỏi về trường<br>2. Gửi | Chatbot trả lời dựa trên RAG knowledge | 🟡 Trung bình |
| **TC-048** | Chatbot không biết | Câu hỏi ngoài knowledge base | 1. Hỏi topic không có trong KB | Chatbot thừa nhận không biết, không bịa | 🟡 Trung bình |
| **TC-049** | Thông báo đánh dấu đọc | SV có thông báo chưa đọc | 1. Click vào thông báo<br>2. POST read | read_flag=true, badge giảm | 🟡 Trung bình |
| **TC-050** | Đọc tất cả thông báo | Nhiều thông báo chưa đọc | 1. Click "Đọc tất cả" | Tất cả read_flag=true | 🟢 Thấp |

---

## Tổng Kết Test Coverage

| Nhóm chức năng | Số TC | Critical (🔴) | Medium (🟡) | Low (🟢) |
|---------------|-------|--------------|------------|---------|
| Authentication | 10 | 7 | 2 | 1 |
| Đăng ký học phần | 8 | 6 | 2 | 0 |
| Điểm danh & Điểm số | 10 | 6 | 4 | 0 |
| Học phí & Thanh toán | 5 | 4 | 1 | 0 |
| Quản trị Admin | 9 | 4 | 5 | 0 |
| Chat & Chatbot | 8 | 0 | 4 | 4 |
| **Tổng** | **50** | **27** | **18** | **5** |

> ⚠️ **Lưu ý:** Chưa xác định được test code tự động nào từ source code (`backend/src/test/` cần kiểm tra thêm). Tất cả test case trên là kịch bản manual test dựa trên phân tích nghiệp vụ và API.
