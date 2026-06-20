# 🔄 Quy Trình Nghiệp Vụ — ThangLong University Web

> **Mã tài liệu:** DOC-05  
> **Phiên bản:** 1.0  
> **Ngày tạo:** 28/05/2026  

---

## Mục Lục

- [1. Quy Trình Đăng Nhập & Phân Quyền](#1-quy-trình-đăng-nhập--phân-quyền)
- [2. Quy Trình Đăng Ký Học Phần (Kafka)](#2-quy-trình-đăng-ký-học-phần-kafka)
- [3. Quy Trình Nhập Điểm & Khóa Điểm](#3-quy-trình-nhập-điểm--khóa-điểm)
- [4. Quy Trình Thanh Toán Học Phí VNPay](#4-quy-trình-thanh-toán-học-phí-vnpay)
- [5. Quy Trình Quản Lý Học Kỳ (Vòng đời)](#5-quy-trình-quản-lý-học-kỳ-vòng-đời)
- [6. Quy Trình Điểm Danh](#6-quy-trình-điểm-danh)
- [7. Quy Trình Đăng Ký Thi Lại](#7-quy-trình-đăng-ký-thi-lại)

---

## 1. Quy Trình Đăng Nhập & Phân Quyền

**Mục đích:** Xác thực người dùng và điều hướng đến portal phù hợp theo role.

**Actor tham gia:** Admin, Teacher, Student

**Đầu vào:** Username, Password  
**Đầu ra:** JWT Tokens + redirect đến portal tương ứng

```mermaid
flowchart TD
    A([🌐 Truy cập /login]) --> B[Nhập username & password]
    B --> C{POST /api/auth/login}
    C --> D{Xác thực credentials}
    D -->|❌ Sai| E[Trả về 401 Unauthorized]
    E --> F[Hiển thị lỗi đăng nhập]
    F --> B
    D -->|✅ Đúng| G{Tài khoản active?}
    G -->|❌ Inactive| H[Trả về lỗi tài khoản bị khóa]
    H --> B
    G -->|✅ Active| I[Tạo accessToken + refreshToken]
    I --> J[Lưu tokens vào localStorage]
    J --> K{Đọc role từ token}
    K -->|ADMIN| L[Redirect → /admin/dashboard]
    K -->|TEACHER| M[Redirect → /teacher/dashboard]
    K -->|STUDENT| N[Redirect → /student/dashboard]
    
    style A fill:#4CAF50,color:#fff
    style L fill:#e74c3c,color:#fff
    style M fill:#3498db,color:#fff
    style N fill:#2ecc71,color:#fff
```

**Exception:**
- Quá số lần đăng nhập sai → Chưa xác định từ source code (cần bổ sung)
- Token hết hạn → Tự động refresh qua `POST /api/auth/refresh`

---

## 2. Quy Trình Đăng Ký Học Phần (Kafka)

**Mục đích:** Sinh viên đăng ký học phần với xử lý bất đồng bộ để tránh quá tải.

**Actor tham gia:** Student, Kafka System

**Đầu vào:** classSectionId  
**Đầu ra:** Enrollment record trong database

```mermaid
sequenceDiagram
    participant SV as 👨‍🎓 Sinh viên
    participant FE as 🖥️ Frontend
    participant API as ⚡ Backend API
    participant KP as 📨 Kafka Producer
    participant KC as 🔄 Kafka Consumer
    participant DB as 🗄️ Database

    SV->>FE: Xem lớp mở đăng ký
    FE->>API: GET /api/student/classes/semester/{semesterId}
    API->>DB: Query class sections còn slot
    DB-->>API: ClassSectionResponse[]
    API-->>FE: Danh sách lớp

    SV->>FE: Click "Đăng ký" lớp X
    FE->>API: POST /api/student/enroll/{classSectionId}
    API->>KP: Publish enrollment request
    KP->>KC: enrollment-requests topic
    API-->>FE: {requestId, message: "Đang xử lý"}

    loop Polling (mỗi 2 giây)
        FE->>API: GET /api/student/enrollments/status/{requestId}
        API-->>FE: {status: "PROCESSING"}
    end

    KC->>DB: Kiểm tra học kỳ mở?
    KC->>DB: Kiểm tra còn slot?
    KC->>DB: Kiểm tra trùng lịch?
    KC->>DB: Kiểm tra đã đăng ký chưa?

    alt ✅ Tất cả điều kiện OK
        KC->>DB: INSERT INTO enrollments
        KC->>DB: UPDATE currentSlots + 1
        KC-->>API: status = SUCCESS
        API-->>FE: {status: "SUCCESS"}
        FE-->>SV: ✅ Đăng ký thành công!
    else ❌ Vi phạm điều kiện
        KC-->>API: status = FAILED + reason
        API-->>FE: {status: "FAILED", message}
        FE-->>SV: ❌ Hiển thị lý do thất bại
    end
```

**Business rules trong quy trình này:**
- `registrationOpen = true` → Điều kiện bắt buộc
- `currentSlots < maxSlots` → Còn chỗ trống
- Không trùng ngày/tiết với lớp đã đăng ký
- Chưa đăng ký lớp này trước đó

---

## 3. Quy Trình Nhập Điểm & Khóa Điểm

**Mục đích:** Giảng viên nhập điểm các thành phần cho từng sinh viên và khóa điểm khi hoàn tất.

**Actor tham gia:** Teacher

**Đầu vào:** enrollmentId, {participationScore, midTermScore, finalScore, retestScore}  
**Đầu ra:** Grade record cập nhật, letter_grade và gpa4 được tính

```mermaid
flowchart TD
    A([👨‍🏫 Giảng viên đăng nhập]) --> B[Xem lớp phân công\nGET /api/teacher/my-classes/semester/...]
    B --> C[Chọn lớp học phần]
    C --> D[Xem danh sách sinh viên\nGET /api/teacher/classes/classSectionId/students]
    D --> E[Nhập điểm từng sinh viên\nPUT /api/teacher/grades/enrollmentId]

    E --> F{Kiểm tra quyền}
    F -->|❌ Không phải GV lớp| G[403 Forbidden]
    F -->|✅ Đúng GV| H{Lớp có đóng không?}
    H -->|grade_locked = true| I[400 Bad Request - Đã khóa điểm]
    H -->|grade_locked = false| J[Lưu điểm vào grades table]
    
    J --> K[Tính total_score tự động\n= 0.1×CD + 0.3×GK + 0.6×CK]
    K --> L[Xếp loại chữ A/B/C/D/F]
    L --> M[Tính gpa4]
    M --> N{Còn sinh viên nào chưa nhập?}
    N -->|Còn| E
    N -->|Xong| O[Khóa điểm cả lớp\nPOST /api/teacher/grades/class/classSectionId/lock]
    O --> P[grade_locked = true]
    P --> Q([✅ Hoàn tất nhập điểm])

    style A fill:#3498db,color:#fff
    style Q fill:#2ecc71,color:#fff
    style G fill:#e74c3c,color:#fff
    style I fill:#e74c3c,color:#fff
```

**Công thức tính điểm:**
```
total_score = (participation_score × 0.1) + (midterm_score × 0.3) + (final_score × 0.6)

Nếu có retest_score: thay thế final_score trong công thức

Xếp loại chữ:
  ≥ 8.5  → A (gpa4 = 4.0)
  ≥ 7.0  → B (gpa4 = 3.0)
  ≥ 5.5  → C (gpa4 = 2.0)
  ≥ 4.0  → D (gpa4 = 1.0)
  < 4.0  → F (gpa4 = 0.0)
```

---

## 4. Quy Trình Thanh Toán Học Phí VNPay

**Mục đích:** Sinh viên thanh toán học phí qua cổng VNPay.

**Actor tham gia:** Student, VNPay System

**Đầu vào:** semesterId  
**Đầu ra:** TuitionBill.is_completed = true

```mermaid
sequenceDiagram
    participant SV as 👨‍🎓 Sinh viên
    participant FE as 🖥️ Frontend
    participant API as ⚡ Backend
    participant VP as 💳 VNPay

    SV->>FE: Vào trang Học phí
    FE->>API: GET /api/student/tuition/{semesterId}
    API-->>FE: TuitionResponse (danh sách môn, tổng tiền)
    FE-->>SV: Hiển thị hóa đơn chi tiết

    SV->>FE: Click "Thanh toán qua VNPay"
    FE->>API: POST /api/student/tuition/{semesterId}/vnpay-url
    API->>API: Tạo txn_ref unique
    API->>API: Tạo HMAC signature
    API-->>FE: VNPay payment URL

    FE->>VP: Redirect đến VNPay checkout
    SV->>VP: Nhập thông tin thẻ/OTP
    VP->>VP: Xử lý giao dịch

    alt ✅ Thanh toán thành công (response_code = "00")
        VP->>API: GET /api/student/tuition/vnpay-return?...
        API->>API: Verify HMAC signature
        API->>API: UPDATE payment_transactions → SUCCESS
        API->>API: UPDATE tuition_bills → is_completed = true
        API-->>FE: Redirect /student/payment-result?success=true
        FE-->>SV: ✅ Thanh toán thành công!
    else ❌ Thanh toán thất bại
        VP->>API: GET /api/student/tuition/vnpay-return?response_code=XX
        API->>API: UPDATE payment_transactions → FAILED
        API-->>FE: Redirect /student/payment-result?success=false
        FE-->>SV: ❌ Thanh toán thất bại, thử lại
    end
```

---

## 5. Quy Trình Quản Lý Học Kỳ (Vòng Đời)

**Mục đích:** Mô tả toàn bộ vòng đời của một học kỳ từ khi tạo đến khi kết thúc.

**Actor tham gia:** Admin, Teacher, Student

```mermaid
flowchart LR
    A([Tạo Học Kỳ]) --> B[Tạo Lớp HP\n& Phân công GV]
    B --> C{Mở Đăng Ký\ntoggle-registration}
    C --> D[Sinh viên\nĐăng ký HP]
    D --> E{Đóng Đăng Ký}
    E --> F[GV Điểm Danh\n& Dạy Học]
    F --> G[GV Nhập Điểm\n& Khóa Điểm]
    G --> H[Admin Cài\nLịch Thi]
    H --> I{Xuất Bản\nLịch Thi\npublish-exams}
    I --> J[SV Xem\nLịch Thi]
    J --> K{Mở Đăng Ký\nThi Lại}
    K --> L[SV Đăng Ký\nThi Lại]
    L --> M{Khóa\nĐăng Ký Thi Lại}
    M --> N[GV Nhập Điểm\nThi Lại]
    N --> O([Kết Thúc\nHọc Kỳ])

    style A fill:#27ae60,color:#fff
    style O fill:#8e44ad,color:#fff
    style C fill:#e67e22,color:#fff
    style I fill:#2980b9,color:#fff
    style K fill:#e67e22,color:#fff
    style M fill:#c0392b,color:#fff
```

**Trạng thái học kỳ và flags:**

| Flag | Giá trị | Ý nghĩa |
|------|---------|---------|
| `registrationOpen` | true/false | Sinh viên có thể đăng ký học phần |
| `locked` | true/false | Học kỳ đã đóng băng (không thể thay đổi enrollments) |
| `examPublished` | true/false | Lịch thi đã công bố cho sinh viên |
| `retakeOpen` | true/false | Sinh viên có thể đăng ký thi lại |
| `retakeLocked` | true/false | Đăng ký thi lại đã đóng |

---

## 6. Quy Trình Điểm Danh

**Mục đích:** Ghi nhận sự có mặt của sinh viên trong từng buổi học.

**Actor tham gia:** Teacher

**Đầu vào:** classSectionId, sessionNumber, danh sách trạng thái điểm danh  
**Đầu ra:** AttendanceRecord cho từng sinh viên trong buổi học

```mermaid
flowchart TD
    A([👨‍🏫 Teacher]) --> B[Chọn lớp học phần]
    B --> C[Xem danh sách buổi học\nGET /attendance-sessions]
    C --> D[Chọn buổi cần điểm danh]
    D --> E{Buổi đã khóa?}
    E -->|locked = true| F[Hiển thị read-only\nKhông thể sửa]
    E -->|locked = false| G[Hiển thị form điểm danh]
    G --> H[Nhập trạng thái từng SV:\n✅ PRESENT\n⏰ LATE\n❌ ABSENT]
    H --> I[Lưu điểm danh\nPUT /attendance-sessions/sessionNumber/records]
    I --> J{Muốn khóa buổi?}
    J -->|Không| G
    J -->|Có| K[Xác nhận khóa]
    K --> L[POST /attendance-sessions/sessionNumber/lock]
    L --> M[locked = true]
    M --> N([✅ Buổi điểm danh đã hoàn tất])

    style A fill:#3498db,color:#fff
    style N fill:#2ecc71,color:#fff
    style F fill:#95a5a6,color:#fff
```

**Trạng thái điểm danh:**
- `PRESENT` — Có mặt đầy đủ
- `LATE` — Đi muộn
- `ABSENT` — Vắng mặt

**Ghi chú:**
- Số buổi vắng ảnh hưởng đến `courseStatus` (có thể bị `BANNED_FROM_EXAM` nếu vắng quá nhiều)
- Thông tin này hiển thị trong bảng điểm giảng viên qua `absenceCount`

---

## 7. Quy Trình Đăng Ký Thi Lại

**Mục đích:** Sinh viên đăng ký thi lại (RETAKE) hoặc học lại (IMPROVE) cho các môn chưa đạt.

**Actor tham gia:** Student

**Đầu vào:** Danh sách courseIds, semesterId  
**Đầu ra:** ExamRegistration records, thông báo tổng phí

```mermaid
flowchart TD
    A([👨‍🎓 Sinh viên]) --> B{Học kỳ mở\nthi lại?}
    B -->|retakeOpen = false| C[Thông báo: Chưa mở đăng ký thi lại]
    B -->|retakeOpen = true| D[Xem môn đủ điều kiện\nGET /api/student/retakes/eligible-courses]
    D --> E[Hệ thống hiển thị:\n- Môn trượt RETAKE\n- Môn muốn cải thiện IMPROVE\n- Phí thi lại từng môn]
    E --> F[SV chọn môn muốn đăng ký]
    F --> G[POST /api/student/retakes/register\n{courseIds, semesterId}]
    G --> H{Xác nhận đăng ký}
    H -->|Thành công| I[ExamRegistration được tạo]
    I --> J[Hiển thị:\n- DS môn đã đăng ký\n- Tổng phí thi lại\n- Lịch thi nếu đã có]
    J --> K{Muốn hủy?}
    K -->|Có| L[DELETE /api/student/retakes/examRegistrationId]
    L --> D
    K -->|Không| M([✅ Hoàn tất đăng ký thi lại])

    style A fill:#3498db,color:#fff
    style M fill:#2ecc71,color:#fff
    style C fill:#e74c3c,color:#fff
```

**Điều kiện đủ điều kiện thi lại:**
- Môn có điểm < 4.0 (RETAKE — thi lại)
- Môn có điểm ≥ 4.0 nhưng muốn cải thiện (IMPROVE — học lại)
- Học kỳ đang mở đăng ký thi lại (`retakeOpen = true`)

---

> 📌 **Lưu ý:** Tất cả các flowchart và sequence diagram trên được xây dựng dựa trên phân tích source code và API endpoints thực tế của dự án.
