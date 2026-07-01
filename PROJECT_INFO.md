# PROJECT_INFO.md

> Phan tich du an theo goc nhin Senior Software Engineer va Technical Recruiter.  
> Nguon chinh: source code backend, frontend, database schema, Docker, CI/CD, config va docs trong repository.  
> Luu y: file nay khong tiet lo gia tri secret trong `.env`. README/docs co mot so diem cu hon source code; phan tich ben duoi uu tien source code hien tai.

## 1. Project Summary

### Ten du an

**ThangLongUniversityWeb / University Management System (UMS)**

### Muc tieu du an

Xay dung he thong quan ly dao tao dai hoc end-to-end cho nhieu nhom nguoi dung: admin, sinh vien va giang vien. He thong tap trung vao quan ly hoc vu, dang ky hoc phan, lich hoc, diem, diem danh, thi cu, hoc phi, giao tiep noi bo va tra cuu thong tin bang chatbot.

### Bai toan thuc te giai quyet

- So hoa quy trinh quan ly dao tao cua truong dai hoc.
- Giam thao tac thu cong khi quan ly sinh vien, giang vien, lop hoc phan, phong hoc, lich hoc, diem va hoc phi.
- Ho tro dang ky hoc phan/thi lai/cai thien diem voi rang buoc nghiep vu phuc tap.
- Dong bo thong tin hoc vu theo thoi gian gan thuc cho sinh vien, giang vien va admin.
- Cung cap kenh chat, thong bao va chatbot hoi dap noi bo.

### Doi tuong su dung

- **Admin/Phong dao tao**: quan ly nguoi dung, chuong trinh hoc, hoc ky, lop hoc phan, lich thi, diem, phong hoc, tri thuc chatbot va bao cao.
- **Sinh vien**: dang ky hoc phan, xem lich hoc/lich thi, xem diem, dong hoc phi, dang ky thi lai/cai thien, nhan thong bao, chat va hoi dap chatbot.
- **Giang vien**: xem lich day, quan ly lop, diem danh, nhap diem, theo doi sinh vien va giao tiep noi bo.

## 2. Architecture

### Kien truc he thong

Kien truc tong the la **full-stack web application** tach frontend va backend:

- **Frontend**: React/TanStack Start SSR chay tren Cloudflare Worker, giao tiep voi backend qua REST API.
- **Backend**: Spring Boot REST API, bao gom authentication, authorization, business services, cache, realtime, file upload, payment, chatbot/RAG va export.
- **Database**: PostgreSQL luu du lieu hoc vu, nguoi dung, diem, hoc phi, chat, audit, tri thuc chatbot.
- **Cache/Token store**: Redis dung cho cache, refresh token JTI, enrollment status va mot so co che realtime.
- **Message Queue**: Kafka ho tro xu ly dang ky hoc phan bat dong bo khi duoc bat cau hinh.
- **Storage**: Cloudinary cho file upload trong chat; database luu metadata.
- **Realtime**: SSE cho thay doi hoc ky/lop hoc phan; WebSocket/STOMP backend cho chat va enrollment status.
- **Deployment**: Frontend deploy Cloudflare Workers qua GitHub Actions; backend co Dockerfile va Railway config.

### Frontend stack

- React 19
- TypeScript
- TanStack Start
- TanStack Router
- TanStack Query
- Vite 7
- Tailwind CSS 4
- Radix UI / shadcn-style components
- React Hook Form
- Zod
- Recharts
- Sonner
- Lucide React
- Cloudflare Worker runtime

### Backend stack

- Java 21
- Spring Boot 4.0.2
- Spring Web MVC
- Spring Security
- Spring Data JPA / Hibernate
- Spring Cache
- Spring Data Redis
- Spring Kafka
- Spring WebSocket/STOMP
- Jakarta Validation
- PostgreSQL driver
- Bucket4j
- Apache POI
- Cloudinary SDK
- Jsoup
- JJWT
- Springdoc OpenAPI / Swagger
- Gradle Kotlin DSL

### Database

- PostgreSQL 15 trong Docker Compose.
- JPA entity model hien tai co **36 entity**.
- `schema.sql` baseline co **23 bang** va nhieu index cho user, student, teacher, course, class section, enrollment, grade, tuition, audit, chat va notification.
- Hibernate `ddl-auto=update` duoc cau hinh, nen mot so bang moi co the duoc tao tu entity.
- Flyway dependency ton tai, nhung chua thay thu muc migration `db/migration`; day la diem can cai thien neu len production.

### Cache

- Redis dung cho:
  - Spring Cache.
  - Refresh token JTI/current token.
  - Enrollment request status.
  - Idempotency key khi xu ly Kafka enrollment.
- Frontend dung TanStack Query cache voi `staleTime` va `gcTime`.
- Cloudflare/static headers cache asset frontend.

### Storage

- Cloudinary cho file upload qua chat.
- PostgreSQL luu metadata va du lieu nghiep vu.
- Co endpoint download file local/legacy trong chat, nhung storage chinh duoc implement la Cloudinary.

### Message Queue

- Kafka optional qua flag `spring.kafka.enabled`.
- Topic chinh: `class-registration`.
- Dead-letter topic: `class-registration.DLT`.
- Co retry policy, DLT recoverer va consumer xu ly dang ky hoc phan.
- Khi Kafka bi tat, he thong dung `DirectEnrollmentProcessor` xu ly dong bo.

### Deployment

- Frontend: Cloudflare Workers, Wrangler, GitHub Actions.
- Backend: Dockerfile multi-stage, Railway config.
- Local infra: Docker Compose gom PostgreSQL, Redis Stack, Zookeeper, Kafka, Kafka UI va PgAdmin.

### Docker

- Backend Dockerfile dung Eclipse Temurin Java 21.
- Multi-stage build: Gradle build stage va JRE runtime stage.
- Runtime chay bang non-root user `app`.
- Docker Compose dung network `uni-net` va volume `postgres_data`.

### CI/CD

- Co workflow `.github/workflows/deploy-frontend.yml` deploy frontend len Cloudflare khi push vao `main` va thay doi `frontend/**`.
- Workflow dung Bun, build frontend voi `VITE_API_BASE_URL`, sau do deploy bang Wrangler.
- Chua thay workflow CI/CD rieng cho backend.

## 3. Features

### Authentication

- Login bang username/password.
- JWT access token.
- Refresh token co JTI, luu current JTI trong Redis.
- Logout revoke refresh token.
- Change password revoke toan bo refresh token.
- Frontend AuthProvider validate session bang `/me` va redirect theo role.

### Authorization

- RBAC voi 3 role:
  - `ADMIN`
  - `STUDENT`
  - `TEACHER`
- Spring Security route protection:
  - `/api/admin/**` chi ADMIN.
  - `/api/student/**` chi STUDENT.
  - `/api/teacher/**` chi TEACHER.
  - Chat/chatbot cho ca 3 role.
- Frontend co ProtectedOutlet va role-based navigation.

### Dashboard

- Admin dashboard.
- Student dashboard.
- Teacher dashboard.
- Cache rieng cho dashboard admin/teacher.

### CRUD/Admin Management

- User management.
- Student management.
- Teacher management.
- Department management.
- Major management.
- Homeroom management.
- Course management.
- Room management.
- Period management.
- Semester management.
- Class section management.
- Registration rounds/time slots.
- Knowledge document/chunk management cho chatbot.

### Course Registration

- Sinh vien xem lop hoc phan kha dung.
- Kiem tra dieu kien nganh, khoa, mon tien quyet, trung lich, so tin chi toi da, trang thai hoc phan da hoc.
- Dang ky/huy lop hoc phan.
- Ho tro xu ly truc tiep hoac Kafka.
- Pessimistic lock de tranh overbooking.
- Rate limiting rieng cho endpoint enrollment.

### Retake / Improve Registration

- Sinh vien dang ky thi lai/cai thien.
- Kiem tra dieu kien diem, so lan thi lai/cai thien, dot dang ky, time slot theo nganh/khoa.
- Tinh phi thi lai/cai thien.

### Exam Scheduling

- Admin tao lich thi theo hoc phan.
- Phan phong thi, ghe thi va giam thi.
- Kiem tra xung dot lich thi cua sinh vien.
- Ho tro chuyen ghe thi.
- Dong bo lop ao cho thi lai/cai thien.

### Attendance

- Teacher tao/quan ly diem danh.
- Quan ly attendance session va attendance record.
- Dieu kien du thi co tinh den so buoi vang.

### Grades / Academic Results

- Teacher nhap/cap nhat diem.
- Student xem diem, GPA, ket qua hoc tap va tien do chuong trinh.
- Admin theo doi academic results.
- Audit cho cap nhat diem.

### Tuition / Payment

- Tinh hoc phi theo so tin chi va phi thi lai/cai thien.
- Tao URL thanh toan VNPay.
- Xu ly VNPay return va cap nhat trang thai thanh toan.

### Search / Filter / Pagination

- Search user/student/teacher/course/class section.
- Pagination backend cho nhieu danh sach.
- DataTable frontend co search/filter/pagination client-side.
- Chat messages/files/links co pagination.

### Upload

- Upload file trong chat qua Cloudinary.
- Ho tro file/image metadata va link.

### Realtime

- Backend WebSocket/STOMP cho chat, typing, read receipt, user online va enrollment status.
- SSE cho semester/class-section update.
- Frontend dang su dung SSE cho invalidation du lieu hoc ky.
- Frontend chat hien tai chu yeu dung REST polling/query; chua thay STOMP client duoc wire vao UI.

### Notification

- Notification API cho student/teacher.
- Frontend polling notification moi 30 giay trong layout.
- Chat unread count.

### Chat

- Private chat.
- Group chat.
- Class/group room.
- Send message.
- Mark as read.
- Upload file.
- List files and links.
- Search users.
- Leave room.

### Chatbot / RAG

- Chatbot hoi dap thong tin dai hoc.
- Ingest text va URL.
- Chunking document.
- Full-text retrieval tren PostgreSQL.
- Groq LLM integration.
- Embedding/HuggingFace config ton tai, nhung retrieval chinh trong source hien tai la full-text/keyword.

### Reporting / Export

- Export Excel bang Apache POI:
  - Enrollment.
  - Exam schedule.
  - Retake registration.

### Admin Panel

- Admin panel kha day du cho hoc vu, nguoi dung, lich hoc, lich thi, tri thuc chatbot, enrollment va export.

## 4. Backend Engineering

### REST API

- Backend co **35 controller** va khoang **173 REST method mappings**.
- API duoc chia theo domain va role: auth, admin, student, teacher, chat, chatbot, knowledge, payment, realtime.
- Dung DTO request/response, service layer va repository layer.
- OpenAPI/Swagger duoc cau hinh voi bearer auth va group API.

### API structure

- Package chinh: `com.example.ThangLongUniversityWeb`.
- Cac layer chinh:
  - `controller`
  - `service`
  - `repository`
  - `entity`
  - `dto`
  - `security`
  - `config`
  - `exception`
  - `kafka`
  - `audit`
  - `utils`

### Validation

- Dung Jakarta Bean Validation tren DTO.
- Co validation cho login/password, user, student, teacher, course, department, major, room, period, class section, grade, knowledge ingestion, bulk scheduling.
- Nhieu controller dung `@Valid`.
- Business validation phuc tap nam trong service layer, dac biet o enrollment, class scheduling, exam scheduling va retake registration.

### Exception handling

- Co `GlobalExceptionHandler`.
- Xu ly:
  - Unauthorized.
  - Forbidden.
  - Not found.
  - Conflict.
  - Validation exception.
  - Method argument validation.
  - Constraint violation.
  - Runtime exception fallback.
- Response loi co timestamp, status, error va message.

### Security

- Spring Security stateless.
- JWT access/refresh token.
- BCrypt password hashing.
- RBAC theo endpoint.
- CORS configurable qua environment.
- CSRF disabled vi API stateless dung JWT.
- Rate limiting cho login va enrollment.
- WebSocket handshake co JWT validation.
- Refresh token JTI luu Redis, co fallback in-memory neu Redis loi.

### Rate limiting

- Bucket4j in-memory.
- `/api/auth/login`: 10 requests/phut/IP.
- `/api/student/enroll*`: 20 requests/phut/IP.
- Tra ve HTTP 429 va `Retry-After`.

### Caching

- `@EnableCaching`.
- RedisCacheManager voi TTL theo cache:
  - Default 30 phut.
  - Dashboard 60 giay.
  - Semesters/class section options 5 phut.
  - Courses/rooms/periods 60 phut.
- Cache eviction khi thay doi enrollment/class section/semester lien quan.

### Pagination

- Dung Spring Data `Pageable` va pagination DTO o nhieu API.
- Chat messages/files/links co paging.
- Admin/student/teacher search/list co paging.
- `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` giup serialize Page on dinh hon.

### File upload

- Multipart upload max 50MB.
- CloudinaryService upload image/raw file.
- Chat upload endpoint nhan `MultipartFile`.
- Ten file duoc sanitize va gan public id co UUID.

### Logging / Audit

- Logback console UTF-8, root INFO.
- AuditAspect ghi audit log cho cac action quan trong:
  - Enrollment override.
  - Enrollment lock semester.
  - Retake lock semester.
  - Grade update.
- Audit log gom user, IP, args/error metadata.

### Scheduling

- Chua thay scheduled jobs quan trong trong source code.
- Cac quy trinh dang ky, cache invalidation va realtime duoc kich hoat theo request/event thay vi cron.

### Transaction

- Service layer dung `@Transactional` cho cac use case co thay doi du lieu.
- Enrollment va class scheduling dung lock pessimistic cho tai nguyen can tranh race condition.
- Sau transaction co publish SSE/notification de tranh gui event truoc khi commit.

### ORM

- Spring Data JPA/Hibernate.
- **36 JPA entities**.
- **34 repositories**.
- Mot so query dung `@EntityGraph`, custom JPQL/native query, lock mode `PESSIMISTIC_WRITE`.
- Hibernate batch size 50.
- `open-in-view=false`, tot cho production discipline.

### Backend diem manh

- Business logic hoc vu rat day: dang ky mon, thi lai, lich thi, diem, hoc phi.
- Xu ly concurrency tot trong enrollment/class capacity.
- Ket hop Redis, Kafka optional, SSE, WebSocket va audit.
- Tich hop nhieu external services: VNPay, Cloudinary, Groq.

### Backend diem can cai thien

- Chua co migration Flyway versioned du dung nghia, du da co dependency.
- Chua thay backend CI/CD workflow.
- Default config co placeholder/default secret; production phai bat buoc dung secret manager/env an toan.
- Refresh token duoc tra ve frontend va frontend luu localStorage; can can nhac httpOnly cookie neu muon tang security.
- OAuth khong duoc implement.

## 5. Frontend Engineering

### SSR

- Frontend dung TanStack Start voi server entry `src/server.ts`.
- Build/deploy cho Cloudflare Worker.
- Co branded error fallback cho loi SSR/h3 nghiem trong.

### SSG

- Chua thay SSG route generation ro rang.
- He thong chu yeu la authenticated app nen SSR/client fetching la hop ly hon SSG.

### ISR

- Khong thay ISR.
- Cache va revalidation duoc xu ly bang TanStack Query, SSE invalidation va Cloudflare headers.

### State management

- TanStack Query cho server state.
- React Context cho auth state.
- Local component state cho UI state.
- `localStorage` luu token/user info frontend.

### Data fetching

- API client rieng tai `frontend/src/lib/api`.
- Co refresh-token retry flow khi gap 401/403.
- Refresh request duoc deduplicate bang shared promise.
- Query defaults:
  - `staleTime`: 2 phut.
  - `gcTime`: 15 phut.
  - `refetchOnWindowFocus`: false.
  - `retry`: 1.

### Form handling

- React Hook Form + Zod cho cac form phuc tap.
- Validation client-side va mutation handling qua TanStack Query.

### UI architecture

- Route-based app voi TanStack Router.
- Layout dung theo role:
  - Admin.
  - Student.
  - Teacher.
- Shared UI components theo shadcn/Radix style.
- Feature modules cho chat, chatbot, semester detail, data table, dialogs, forms.
- ProtectedOutlet xu ly guard va redirect theo role.

### Responsive design

- AppLayout co sidebar desktop va sheet/mobile navigation.
- Chat UI co layout responsive.
- Tailwind breakpoint classes duoc dung rong rai.
- Component/card/table duoc thiet ke cho dashboard va operational tool.

### Performance optimization

- Lazy loading:
  - ChatbotWidget.
  - ChatbotPanel.
  - Semester detail tabs.
- Vite manual chunks:
  - React vendor.
  - Recharts vendor.
  - Framework vendor.
- TanStack Query caching.
- SSE-driven invalidation giam viec refetch thua.
- Static asset cache headers cho `/assets/*` va `/images/*`.

### Frontend diem manh

- Routing va role-based UX kha ro rang.
- API client co refresh token flow va file download support.
- UI bao phu day du cac portal admin/student/teacher.
- Co Cloudflare deployment ready.

### Frontend diem can cai thien

- Token dang luu localStorage, co rui ro XSS neu app bi inject script.
- Backend da co WebSocket/STOMP chat, nhung frontend chat hien chu yeu REST; co the nang cap realtime chat.
- Co ca `bun.lock` va `package-lock.json`; nen chuan hoa package manager.

## 6. DevOps & Deployment

### Docker

- Backend Dockerfile multi-stage Java 21.
- Docker Compose cung cap local infra:
  - PostgreSQL.
  - Redis Stack.
  - Zookeeper.
  - Kafka.
  - Kafka UI.
  - PgAdmin.

### Docker Compose

- Network: `uni-net`.
- PostgreSQL volume: `postgres_data`.
- Port local:
  - Postgres 5432.
  - Redis 6379.
  - Redis Insight/Stack UI 8001.
  - Kafka 9092.
  - Kafka UI 8090.
  - PgAdmin 5050.

### Reverse Proxy

- Chua thay Nginx/reverse proxy config trong repository.
- Frontend dung Cloudflare Worker lam edge runtime.

### Nginx

- Khong co Nginx config.

### CI/CD

- Frontend CI/CD qua GitHub Actions + Cloudflare Wrangler.
- Trigger khi push vao `main` va thay doi frontend.
- Chua thay backend CI/CD workflow.

### Environment management

- Backend import `.env` qua `spring.config.import=optional:file:.env[.properties]`.
- Frontend dung `VITE_API_BASE_URL`.
- `.env.example` backend liet ke server, database, Redis, Kafka, JWT, cookie, VNPay, CORS, Groq, Cloudinary va HuggingFace config.
- File `.env` thuc te ton tai trong repo local; khong nen commit secret len remote.

### Production deployment

- Frontend ready cho Cloudflare Workers.
- Backend co Railway config va Dockerfile.
- Can them:
  - Backend CI pipeline.
  - Migration strategy.
  - Secret management.
  - Monitoring/log aggregation.
  - Production-grade reverse proxy/API gateway neu tach domain.

## 7. Performance Optimizations

### Cache

- Redis cache backend voi TTL rieng theo domain.
- Redis token/status/idempotency.
- TanStack Query frontend cache.
- Cloudflare/static asset cache headers.

### Lazy loading

- Lazy load ChatbotWidget va ChatbotPanel.
- Lazy load tabs o trang semester detail.
- Route-based code splitting tu TanStack Router/Vite.

### Code splitting

- Vite manual chunks cho React, Recharts va framework.
- Build frontend minify production.

### Image/static optimization

- Public `_headers` cache immutable 1 nam cho `/assets/*`.
- `/images/*` cache 30 ngay voi stale-while-revalidate.
- Server compression bat cho JSON/HTML/CSS/JS.

### Query optimization

- Database schema co nhieu index cho cac bang chinh.
- JPA `@EntityGraph` giam N+1 o mot so query.
- Custom query cho search/filter.
- Pagination cho list lon.
- Hibernate batch size 50.
- `open-in-view=false`.

### Pagination

- Backend pagination cho danh sach lon.
- Frontend DataTable pagination.
- Chat message/file/link pagination.

### Memoization

- Frontend dung `useMemo`/derived sorting/filtering o cac module UI nhu chat/data table.

### Concurrency optimization

- Pessimistic lock khi cap nhat class capacity va semester/class section.
- Idempotency key khi Kafka consumer xu ly enrollment.
- Direct/Kafka processor pattern giup he thong chay duoc ca local va production-like mode.

## 8. Security

### JWT

- Access token HS256.
- Refresh token co JTI.
- Refresh token JTI/current user token luu Redis.
- Logout/change password revoke token.

### OAuth

- Khong thay OAuth login.
- Neu phong van, nen noi ro he thong dung username/password + JWT, chua tich hop OAuth/SSO.

### RBAC

- 3 role: ADMIN, STUDENT, TEACHER.
- Backend enforce bang Spring Security.
- Frontend enforce bang route guard va menu theo role.

### Password hashing

- BCrypt PasswordEncoder.

### API protection

- Stateless session.
- Role-based endpoint security.
- JWT filter.
- Rate limit filter.
- CORS configurable.
- Swagger/OpenAPI public.
- WebSocket endpoint public o filter chain nhung handshake co JWT validation.

### CORS

- Allowed origins/patterns cau hinh qua env.
- Allow credentials true.
- Allowed methods: GET, POST, PUT, DELETE, OPTIONS.

### CSRF

- CSRF disabled do API stateless JWT.
- Neu chuyen sang httpOnly cookie refresh/access token, can thiet ke lai CSRF protection.

### Rate limiting

- Bucket4j cho login va enrollment.

### Input validation

- Bean Validation tren DTO.
- Business validation trong service.
- Frontend form validation bang Zod.

### Security headers

- Frontend `_headers` cau hinh:
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `Referrer-Policy: strict-origin-when-cross-origin`

### Rui ro can neu trung thuc

- Token frontend luu localStorage.
- Production secret phai lay tu env/secret manager, khong dung default.
- Chua co OAuth/SSO.
- Chua thay backend CI security scan.
- Chua thay centralized monitoring/audit dashboard.

## 9. Technical Challenges

### Thiet ke he thong

Du an khong chi la CRUD. He thong phai dieu phoi nhieu domain hoc vu lien quan nhau: sinh vien, giang vien, hoc ky, mon hoc, lop hoc phan, phong hoc, lich hoc, diem, thi, hoc phi va chat.

### Dong bo du lieu

- Enrollment thay doi anh huong den class capacity, dashboard, schedule, tuition, notification va realtime UI.
- Class section thay doi can invalidate cache va gui SSE sau commit.
- Retake/improve registration lien quan grade history, exam session va tuition.

### Hieu nang

- Danh sach sinh vien/lop/diem/lich co the lon, nen can pagination, indexes, caching va query optimization.
- Realtime update can tranh refetch lien tuc.
- Chat va chatbot can quan ly history, pagination va retrieval.

### Authentication

- JWT access/refresh flow, Redis-backed refresh token, revoke all tokens khi change password.
- Role protection ca backend va frontend.

### Caching

- Cache phai duoc evict dung luc khi enrollment/class/semester thay doi.
- Redis fallback giup he thong van chay khi local Redis gap loi, nhung production can Redis on dinh.

### Deployment

- Frontend deploy Cloudflare, backend deploy Docker/Railway, infra local bang Docker Compose.
- Thach thuc la dong bo env, CORS, URL backend/frontend, secret va database migration giua moi truong.

### Concurrency

- Dang ky hoc phan la phan kho nhat: can lock lop, kiem tra trung lich, mon tien quyet, capacity, duplicate enrollment, max credits, registration round/time slot va trang thai khoa/mo.

### Scheduling

- Tao class schedule va exam schedule can kiem tra phong, giao vien, suc chua, trung lich, period va dieu kien tham gia thi.

### Payment

- VNPay integration can ky HMAC, xu ly return URL, parse transaction ref va dam bao khong cap nhat sai bill.

### AI/RAG

- Knowledge ingestion, chunking, full-text retrieval, prompt construction va goi Groq LLM la diem cong lon cho fullstack/AI-enabled application.

## 10. Quantifiable Impact

> So lieu duoc uoc luong bang static scan source code tai thoi diem phan tich.

| Hang muc | So luong |
|---|---:|
| Backend controllers | 35 |
| REST API method mappings | ~173 |
| WebSocket/STOMP message mappings | 5 |
| Service Java files | 52 |
| JPA entities | 36 |
| Repositories | 34 |
| DTO request files | ~38 |
| DTO response files | ~64 |
| Roles | 3 |
| Frontend route files | ~50 |
| Frontend API client modules | 10 |
| Shared UI component files | ~49 |
| Docker Compose infra services | 6 |
| GitHub Actions workflows | 1 |
| Baseline SQL tables in `schema.sql` | 23 |
| Audit-protected operations found | 4 |

### Uoc luong tac dong ky thuat

- Ho tro day du 3 portal nguoi dung: admin, student, teacher.
- Bao phu chu trinh hoc vu cot loi: tu quan ly du lieu dau vao, dang ky hoc, hoc tap, thi, diem, hoc phi den giao tiep.
- Co kha nang xu ly cac tinh huong enrollment can do tranh chap tai nguyen.
- Co nen tang production-oriented: Docker, Redis, Kafka optional, Cloudflare deploy, Swagger, validation, audit, rate limiting.

## 11. CV Ready Content

### 3 bullet manh nhat cho CV

- Developed a full-stack University Management System using Java 21, Spring Boot 4, PostgreSQL, Redis and React/TanStack Start, covering 3 role-based portals and ~173 REST APIs.
- Engineered high-concurrency course registration with pessimistic locking, business-rule validation, Redis/Kafka-backed processing, rate limiting and realtime status updates.
- Built end-to-end academic workflows including scheduling, grading, attendance, exam seating, retake registration, VNPay tuition payment, Cloudinary file upload, chat and RAG chatbot.

### 5 bullet mo rong

- Implemented JWT authentication, Redis-backed refresh token management, RBAC authorization, BCrypt password hashing, CORS configuration, audit logging and Bucket4j rate limiting.
- Designed complex academic domain modules for students, teachers, courses, semesters, class sections, rooms, periods, enrollments, grades, tuition bills and exam sessions.
- Built admin tools for bulk class scheduling, enrollment override/locking, exam room/seat allocation, Excel export and chatbot knowledge ingestion from text/URL sources.
- Developed a React 19 + TypeScript frontend with TanStack Router/Query, SSR on Cloudflare Workers, role-based layouts, responsive dashboards and optimized data fetching.
- Containerized backend and local infrastructure with Docker Compose, integrated PostgreSQL, Redis, Kafka, PgAdmin and Kafka UI, and automated frontend deployment with GitHub Actions + Wrangler.

### Mo ta du an 40-60 tu

ThangLongUniversityWeb la he thong quan ly dao tao dai hoc full-stack cho admin, sinh vien va giang vien. Du an xu ly dang ky hoc phan, lich hoc, diem, diem danh, lich thi, hoc phi VNPay, chat va chatbot RAG, su dung Spring Boot, PostgreSQL, Redis, Kafka va React/TanStack Start.

### Mo ta du an 80-120 tu

ThangLongUniversityWeb la nen tang University Management System full-stack mo phong quy trinh dao tao dai hoc thuc te. Backend duoc xay dung bang Java 21, Spring Boot 4, PostgreSQL, Redis, Kafka optional va Spring Security JWT; frontend dung React 19, TypeScript, TanStack Start/Router/Query va deploy len Cloudflare Workers. He thong bao gom 3 portal admin, student, teacher voi cac chuc nang quan ly nguoi dung, khoa/nganh/mon hoc, hoc ky, lop hoc phan, dang ky hoc, thi lai/cai thien, diem, diem danh, lich thi, hoc phi VNPay, chat, thong bao, export Excel va chatbot RAG.

## 12. ATS Keywords

### Backend / Java

Java 21, Spring Boot 4, Spring Web MVC, Spring Security, Spring Data JPA, Hibernate, REST API, DTO, Jakarta Validation, Bean Validation, Spring Cache, Spring Data Redis, Spring Kafka, Spring WebSocket, STOMP, SSE, OpenAPI, Swagger, Gradle, Lombok, JJWT, BCrypt, Bucket4j, Apache POI, Jsoup.

### Database / Infrastructure

PostgreSQL, Redis, Kafka, Zookeeper, Docker, Docker Compose, Railway, Cloudflare Workers, GitHub Actions, Wrangler, PgAdmin, Kafka UI, HikariCP, SQL indexes, transaction management, pessimistic locking, database schema design.

### Frontend

React 19, TypeScript, TanStack Start, TanStack Router, TanStack Query, Vite, Tailwind CSS, Radix UI, shadcn UI, React Hook Form, Zod, Recharts, Lucide React, Sonner, SSR, code splitting, lazy loading, responsive design.

### Security

JWT, Refresh Token, RBAC, BCrypt, CORS, CSRF, Rate Limiting, API Security, Security Filter Chain, Token Revocation, Audit Logging, Input Validation, Secure Headers.

### Product / Domain

University Management System, Student Information System, Course Registration, Enrollment System, Academic Management, Grade Management, Attendance Management, Exam Scheduling, Tuition Payment, VNPay, Notification, Chat, Chatbot, RAG, Knowledge Base, Cloudinary, Excel Export.

## 13. Recruiter Selling Points

### Neu ung tuyen Java Backend Developer

Nha tuyen dung se danh gia cao nhat cac diem sau:

- **Do phuc tap backend that**: du an co 35 controller, 52 service files, 36 entity va ~173 REST APIs, vuot xa CRUD co ban.
- **Spring Boot/Spring Security tot**: JWT, refresh token, RBAC, BCrypt, CORS, filter chain, rate limiting va exception handling.
- **Transactional thinking**: enrollment/class capacity dung pessimistic lock, idempotency, Redis status va optional Kafka processor.
- **Database/ORM kha manh**: PostgreSQL, JPA/Hibernate, repository layer, custom queries, EntityGraph, indexes, pagination va batch config.
- **Integration experience**: Redis, Kafka, Cloudinary, VNPay, Groq, Apache POI, OpenAPI/Swagger.
- **Business domain depth**: dang ky hoc phan, lich thi, diem, hoc phi va thi lai/cai thien co nhieu rule thuc te, rat tot de noi trong phong van.

### Neu ung tuyen Fullstack Java Developer

Nha tuyen dung se danh gia cao nhat cac diem sau:

- **End-to-end ownership**: tu database/backend/security den frontend route, UI, deploy va CI/CD frontend.
- **Modern frontend stack**: React 19, TypeScript, TanStack Start, TanStack Query, Tailwind, Radix/shadcn, Cloudflare SSR.
- **Role-based product UX**: 3 portal rieng cho admin, student, teacher voi navigation, dashboard va workflow khac nhau.
- **API integration skill**: frontend co API client rieng, token refresh flow, file upload/download, query invalidation, SSE handling.
- **Production awareness**: Docker Compose local infra, backend Dockerfile, Cloudflare deployment, cache headers, environment configuration.
- **Good interview story**: co the trinh bay duoc cac trade-off giua direct enrollment va Kafka enrollment, giua REST chat va WebSocket, giua localStorage token va httpOnly cookie.

## Ghi chu phong van nen noi trung thuc

- He thong co Kafka optional, nhung mac dinh local config dang tat Kafka processing de dung direct processor.
- Backend WebSocket/STOMP da implement, frontend chat hien tai chu yeu dung REST; SSE frontend duoc dung cho semester realtime.
- Flyway dependency co trong backend, nhung migration versioned chua duoc to chuc hoan chinh.
- Chua thay Nginx va backend CI/CD workflow trong repository.
- Khong nen claim OAuth vi source code khong implement OAuth.
- README/docs co mot so thong tin cu; nen dua vao source code hien tai khi phong van.
