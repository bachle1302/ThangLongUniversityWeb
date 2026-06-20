# 🔒 Yêu Cầu Phi Chức Năng — ThangLong University Web

> **Mã tài liệu:** DOC-03  
> **Phiên bản:** 1.0  
> **Ngày tạo:** 28/05/2026  

---

## Mục Lục

- [1. Bảo Mật](#1-bảo-mật)
- [2. Hiệu Năng](#2-hiệu-năng)
- [3. Khả Năng Mở Rộng](#3-khả-năng-mở-rộng)
- [4. Tính Dễ Sử Dụng](#4-tính-dễ-sử-dụng)
- [5. Khả Năng Bảo Trì](#5-khả-năng-bảo-trì)
- [6. Kiểm Thử](#6-kiểm-thử)
- [7. Khả Năng Tương Thích](#7-khả-năng-tương-thích)
- [8. Tính Khả Dụng & Độ Tin Cậy](#8-tính-khả-dụng--độ-tin-cậy)

---

## 1. Bảo Mật

### 1.1 Xác thực (Authentication)

| Yêu cầu | Chi tiết | Nguồn xác định |
|---------|----------|----------------|
| **NFR-SEC-01** | JWT-based authentication (Access Token + Refresh Token) | `AuthController`, `SecurityConfig` |
| **NFR-SEC-02** | Access Token ngắn hạn, Refresh Token rotation | `AuthService`, `RedisTokenService` |
| **NFR-SEC-03** | Mật khẩu hash bằng BCrypt ($2a$10$...) | `users.password_hash`, seed data |
| **NFR-SEC-04** | Refresh token được lưu và quản lý trong Redis | `RedisTokenService` |
| **NFR-SEC-05** | Logout vô hiệu hóa token (blacklist trong Redis) | `AuthService.logout()` |

### 1.2 Phân quyền (Authorization)

| Yêu cầu | Chi tiết | Nguồn xác định |
|---------|----------|----------------|
| **NFR-SEC-06** | Role-based access control (RBAC): ADMIN, TEACHER, STUDENT | `users.role` CHECK constraint |
| **NFR-SEC-07** | Spring Security `@PreAuthorize` bảo vệ từng endpoint | `AGENTS-BACKEND.md` |
| **NFR-SEC-08** | Bearer token required cho tất cả API (trừ `/api/auth/*`) | `@SecurityRequirement(name = "bearerAuth")` |
| **NFR-SEC-09** | Giảng viên chỉ truy cập lớp học của mình | `TeacherGradeController` validation |
| **NFR-SEC-10** | Sinh viên chỉ truy cập dữ liệu của chính mình | `StudentController` — lấy từ JWT |

### 1.3 Bảo mật dữ liệu

| Yêu cầu | Chi tiết | Nguồn xác định |
|---------|----------|----------------|
| **NFR-SEC-11** | Không expose entity trực tiếp — dùng DTO pattern | `AGENTS-BACKEND.md` |
| **NFR-SEC-12** | Audit log ghi nhận tất cả thao tác quan trọng | `audit_logs` table |
| **NFR-SEC-13** | Environment variables cho secrets (không commit) | `.env.example` |
| **NFR-SEC-14** | CORS được cấu hình trong `WebConfig` | Backend config |
| **NFR-SEC-15** | Input validation bằng Jakarta Validation | DTOs với `@Valid` |

---

## 2. Hiệu Năng

| Yêu cầu | Chi tiết | Nguồn xác định |
|---------|----------|----------------|
| **NFR-PERF-01** | Đăng ký học phần xử lý bất đồng bộ qua Apache Kafka (tránh bottleneck khi nhiều SV đăng ký cùng lúc) | `EnrollmentWebSocketController`, Kafka producer/consumer |
| **NFR-PERF-02** | Redis caching cho tokens và dữ liệu hay dùng | `RedisTokenService`, `CacheConfig` |
| **NFR-PERF-03** | Database indexes trên các cột hay truy vấn (student_id, semester_id, class_section_id...) | `schema.sql` — Phần INDEXES |
| **NFR-PERF-04** | Phân trang (pagination) cho các danh sách lớn | `PageResponse<T>`, Spring Pageable |
| **NFR-PERF-05** | Lazy loading với JPA để tránh N+1 query | `AGENTS-BACKEND.md` |
| **NFR-PERF-06** | @Transactional(readOnly = true) cho các query chỉ đọc | `AGENTS-BACKEND.md` |
| **NFR-PERF-07** | TanStack Query caching phía frontend | `package.json — @tanstack/react-query` |
| **NFR-PERF-08** | Chatbot sử dụng RAG (Retrieval-Augmented Generation) để trả lời nhanh, không cần query LLM toàn bộ | `RetrieverService`, `EmbeddingService` |

---

## 3. Khả Năng Mở Rộng

| Yêu cầu | Chi tiết | Nguồn xác định |
|---------|----------|----------------|
| **NFR-SCALE-01** | Kiến trúc phân lớp rõ ràng (Controller → Service → Repository → Entity) cho phép scale từng layer độc lập | `AGENTS-BACKEND.md` |
| **NFR-SCALE-02** | Kafka message broker tách biệt producer và consumer — dễ scale consumer | `kafka/producer`, `kafka/consumer` |
| **NFR-SCALE-03** | Redis tách biệt khỏi database — có thể cluster Redis | `docker-compose.yml` |
| **NFR-SCALE-04** | Docker Compose infrastructure — dễ migrate sang Kubernetes | `docker-compose.yml` |
| **NFR-SCALE-05** | Frontend deploy lên Cloudflare Workers (edge computing) | `wrangler.jsonc` |
| **NFR-SCALE-06** | Thiết kế API RESTful chuẩn — dễ thêm client mới (mobile, third-party) | `AGENTS-BACKEND.md` |
| **NFR-SCALE-07** | Feature-based folder structure ở frontend — dễ thêm module mới | Frontend `/features/` directory |

---

## 4. Tính Dễ Sử Dụng

| Yêu cầu | Chi tiết | Nguồn xác định |
|---------|----------|----------------|
| **NFR-UX-01** | UI component library shadcn/ui + Radix UI (accessible by default) | `package.json` |
| **NFR-UX-02** | Responsive design (mobile-friendly) — Tailwind CSS breakpoints | Landing page, route files |
| **NFR-UX-03** | Real-time feedback cho đăng ký học phần (polling hoặc WebSocket) | `EnrollmentWebSocketController` |
| **NFR-UX-04** | Thông báo toast (Sonner) cho các thao tác quan trọng | `package.json — sonner` |
| **NFR-UX-05** | Form validation trực tiếp (React Hook Form + Zod) | `package.json` |
| **NFR-UX-06** | Loading states và error handling cho tất cả API calls | TanStack Query |
| **NFR-UX-07** | Chatbot hỗ trợ tìm kiếm thông tin ngay trong hệ thống | `ChatbotController` |
| **NFR-UX-08** | Dashboard tổng hợp đầy đủ thông tin quan trọng cho sinh viên | `StudentDashboardResponse` |
| **NFR-UX-09** | Scroll reveal animations, micro-animations ở landing page | `ThangLongLanding.tsx` |

---

## 5. Khả Năng Bảo Trì

| Yêu cầu | Chi tiết | Nguồn xác định |
|---------|----------|----------------|
| **NFR-MAINT-01** | Code documentation đầy đủ trong AGENTS-BACKEND.md và FRONTEND-TASK-RULES.md | Docs files |
| **NFR-MAINT-02** | TypeScript strict mode — giảm runtime errors | `tsconfig.json` |
| **NFR-MAINT-03** | ESLint + Prettier cấu hình chuẩn | `eslint.config.js`, `.prettierrc` |
| **NFR-MAINT-04** | Single schema file (`schema.sql`) — dễ quản lý DDL | `backend/sql/schema.sql` |
| **NFR-MAINT-05** | Centralized error handling (GlobalExceptionHandler) | Backend exception layer |
| **NFR-MAINT-06** | DTO pattern tách biệt API contract khỏi entity | `dto/request`, `dto/response` |
| **NFR-MAINT-07** | Enum types cho các giá trị cố định (Role, Status, Type...) | Backend `enums/` directory |
| **NFR-MAINT-08** | API types tập trung ở `frontend/src/lib/api/types.ts` | `types.ts` — 783 dòng |
| **NFR-MAINT-09** | API client tập trung ở `frontend/src/lib/api/client.ts` | `client.ts` |
| **NFR-MAINT-10** | Swagger/OpenAPI tự động từ code annotations | `OpenApiConfig.java`, `@Tag`, `@Operation` |

---

## 6. Kiểm Thử

| Yêu cầu | Chi tiết | Nguồn xác định |
|---------|----------|----------------|
| **NFR-TEST-01** | Cấu trúc `src/test/` có sẵn cho unit tests và integration tests | `backend/src/test/` |
| **NFR-TEST-02** | Swagger UI tại `/swagger-ui.html` cho manual API testing | `README.md` |
| **NFR-TEST-03** | VNPay test cards được document trong README | `README.md` — Hướng dẫn Test Thanh toán |
| **NFR-TEST-04** | Admin account mặc định để test (`admin/Admin@123`) | `schema.sql` seed data |
| **NFR-TEST-05** | Có thể chạy toàn bộ infrastructure qua Docker Compose | `docker-compose.yml` |

> ⚠️ **Lưu ý:** Hiện tại chưa xác định được test coverage cụ thể từ source code. Phần test trong `backend/src/test/` cần được kiểm tra thêm từ nhóm phát triển.

---

## 7. Khả Năng Tương Thích

| Yêu cầu | Chi tiết | Nguồn xác định |
|---------|----------|----------------|
| **NFR-COMPAT-01** | Backend Java 17 (LTS) — tương thích với các JVM providers hiện đại | `AGENTS-BACKEND.md` |
| **NFR-COMPAT-02** | Frontend React 19 + Vite 7 — tương thích browser hiện đại (Chrome, Firefox, Safari, Edge) | `package.json` |
| **NFR-COMPAT-03** | API RESTful JSON — có thể tích hợp bất kỳ client nào | Chuẩn REST |
| **NFR-COMPAT-04** | PostgreSQL 15 — tương thích với các cloud providers (AWS RDS, Supabase...) | `docker-compose.yml` |
| **NFR-COMPAT-05** | Cloudflare Workers deployment — edge-compatible frontend | `wrangler.jsonc` |
| **NFR-COMPAT-06** | WebSocket (STOMP) — tương thích với các STOMP clients | `ChatWebSocketController` |
| **NFR-COMPAT-07** | VNPay API — tương thích với sandbox và production mode | `PaymentController`, `StudentTuitionService` |

---

## 8. Tính Khả Dụng & Độ Tin Cậy

| Yêu cầu | Chi tiết | Nguồn xác định |
|---------|----------|----------------|
| **NFR-REL-01** | Kafka đảm bảo tin nhắn đăng ký học phần không bị mất khi server tải cao | Kafka durability |
| **NFR-REL-02** | Transaction management (`@Transactional`) đảm bảo tính nhất quán dữ liệu | Spring Data JPA |
| **NFR-REL-03** | GlobalExceptionHandler trả về lỗi rõ ràng, không expose stack trace | Backend exception layer |
| **NFR-REL-04** | Redis token blacklist đảm bảo logout thực sự vô hiệu hóa token | `RedisTokenService` |
| **NFR-REL-05** | Database constraints (UNIQUE, CHECK, FK) bảo vệ tính toàn vẹn dữ liệu | `schema.sql` |
| **NFR-REL-06** | Audit log không thể xóa — bảo đảm traceability | `audit_logs` table design |

---

> 📌 **Lưu ý:** Các yêu cầu phi chức năng này được rút ra từ kiến trúc và code hiện tại. Một số yêu cầu như SLA (uptime %), load testing targets chưa được xác định từ source code và cần bổ sung từ yêu cầu nghiệp vụ thực tế.
