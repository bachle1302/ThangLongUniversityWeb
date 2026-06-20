-- ==============================================================
-- MASTER DATABASE SCHEMA — ThangLong University Web
-- ==============================================================
-- File duy nhất chứa toàn bộ schema, migrations và seed data.
-- Thứ tự: Core Tables → Relationships → Indexes → Constraints → Seeds
--
-- Nguồn gốc (đã gộp từ các file):
--   - academic_results_migration.sql
--   - chat_migration.sql
--   - class_sections_grade_lock_migration.sql
--   - fix_major_fk.sql
--   - grades_migration.sql
--   - src/main/resources/db/migration/V1__security_integrity_audit.sql
--   - src/main/resources/db/migration/V2__add_period_entity.sql
--   - src/main/resources/db/migration/V2__major_and_prerequisites.sql
--   - src/main/resources/db/migration/V3__multi_day_schedule.sql
--   - src/main/resources/db/migration/V4__add_schedule_room_id.sql
-- ==============================================================


-- ==============================================================
-- PHẦN 1: CORE TABLES (Bảng cốt lõi)
-- ==============================================================

-- 1.1 Users — Tài khoản hệ thống
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    role        VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'STUDENT', 'TEACHER')),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- 1.2 Majors — Ngành học
CREATE TABLE IF NOT EXISTS majors (
    id          BIGSERIAL PRIMARY KEY,
    major_code  VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);

-- 1.3 Students — Thông tin sinh viên
CREATE TABLE IF NOT EXISTS students (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    student_code  VARCHAR(50)  NOT NULL UNIQUE,
    full_name     VARCHAR(255),
    dob           DATE,
    address       TEXT,
    major_id      BIGINT       REFERENCES majors(id),
    academic_year INTEGER
);

-- 1.4 Teachers — Thông tin giảng viên
CREATE TABLE IF NOT EXISTS teachers (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    teacher_code VARCHAR(50)  NOT NULL UNIQUE,
    full_name    VARCHAR(255),
    dob          DATE,
    phone        VARCHAR(20),
    department   VARCHAR(255),
    degree       VARCHAR(100),
    address      TEXT
);

-- 1.5 Courses — Môn học
CREATE TABLE IF NOT EXISTS courses (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(255),
    credits     INTEGER,
    description TEXT,
    course_type VARCHAR(20)  NOT NULL DEFAULT 'REQUIRED',
    major_id    BIGINT       REFERENCES majors(id)
);

-- 1.6 Course Prerequisites — Điều kiện tiên quyết môn học (N-N)
CREATE TABLE IF NOT EXISTS course_prerequisites (
    course_id              BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    prerequisite_course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
    PRIMARY KEY (course_id, prerequisite_course_id),
    CONSTRAINT chk_course_prereq_not_self CHECK (course_id <> prerequisite_course_id)
);

-- 1.7 Rooms — Phòng học
CREATE TABLE IF NOT EXISTS rooms (
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL UNIQUE,
    capacity INTEGER      NOT NULL
);

-- 1.8 Periods — Tiết học
CREATE TABLE IF NOT EXISTS periods (
    id            BIGSERIAL PRIMARY KEY,
    period_number INTEGER   NOT NULL UNIQUE,
    start_time    TIME      NOT NULL,
    end_time      TIME      NOT NULL
);

-- 1.9 Semesters — Học kỳ
CREATE TABLE IF NOT EXISTS semesters (
    id                   BIGSERIAL PRIMARY KEY,
    name                 VARCHAR(255),
    start_date           DATE,
    end_date             DATE,
    is_registration_open BOOLEAN NOT NULL DEFAULT FALSE,
    is_locked            BOOLEAN NOT NULL DEFAULT FALSE,
    exam_published       BOOLEAN NOT NULL DEFAULT FALSE,
    retake_open          BOOLEAN NOT NULL DEFAULT FALSE,
    retake_locked        BOOLEAN NOT NULL DEFAULT FALSE,
    ended                BOOLEAN NOT NULL DEFAULT FALSE,
    max_credits_per_semester INTEGER NOT NULL DEFAULT 20
);


-- ==============================================================
-- PHẦN 2: ACADEMIC STRUCTURE (Lớp học phần & Đăng ký)
-- ==============================================================

-- 2.1 Class Sections — Lớp học phần
CREATE TABLE IF NOT EXISTS class_sections (
    id              BIGSERIAL PRIMARY KEY,
    class_code      VARCHAR(100) NOT NULL,
    course_id       BIGINT       REFERENCES courses(id),
    semester_id     BIGINT       REFERENCES semesters(id),
    teacher_id      BIGINT       REFERENCES teachers(id),
    room_id         BIGINT       REFERENCES rooms(id),
    day_of_week     INTEGER      CHECK (day_of_week >= 2 AND day_of_week <= 8),
    start_period_id BIGINT       REFERENCES periods(id),
    end_period_id   BIGINT       REFERENCES periods(id),
    max_slots       INTEGER,
    current_slots   INTEGER      DEFAULT 0,
    is_closed       BOOLEAN      DEFAULT FALSE,
    grade_locked    BOOLEAN      DEFAULT FALSE,
    exam_at         TIMESTAMP,
    exam_room       VARCHAR(255)
);

COMMENT ON COLUMN class_sections.grade_locked IS 'Cờ khóa điểm sau khi giảng viên nhập xong';
COMMENT ON COLUMN class_sections.day_of_week  IS '2=Thứ 2, 3=Thứ 3, ..., 8=Chủ nhật';

-- 2.2 Class Section Schedules — Lịch học nhiều ngày mỗi lớp
CREATE TABLE IF NOT EXISTS class_section_schedules (
    id               BIGSERIAL PRIMARY KEY,
    class_section_id BIGINT    NOT NULL REFERENCES class_sections(id) ON DELETE CASCADE,
    day_of_week      INTEGER   NOT NULL CHECK (day_of_week >= 2 AND day_of_week <= 8),
    start_period_id  BIGINT    NOT NULL REFERENCES periods(id),
    end_period_id    BIGINT    NOT NULL REFERENCES periods(id),
    room_id          BIGINT    REFERENCES rooms(id),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2.3 Enrollments — Đăng ký môn học
CREATE TABLE IF NOT EXISTS enrollments (
    id               BIGSERIAL PRIMARY KEY,
    student_id       BIGINT       NOT NULL REFERENCES students(id),
    class_section_id BIGINT       NOT NULL REFERENCES class_sections(id),
    mid_term_score   FLOAT,
    final_score      FLOAT,
    total_score      FLOAT,
    status           VARCHAR(30) CHECK (status IS NULL OR status IN ('PENDING', 'REGISTERED', 'CANCELED', 'PASSED', 'FAILED')),
    CONSTRAINT uk_enrollments_student_class UNIQUE (student_id, class_section_id)
);

-- 2.4 Grades — Bảng điểm chi tiết
CREATE TABLE IF NOT EXISTS grades (
    id                  BIGSERIAL PRIMARY KEY,
    enrollment_id       BIGINT    NOT NULL UNIQUE REFERENCES enrollments(id) ON DELETE CASCADE,
    participation_score FLOAT,                         -- Chuyên cần (0-10), trọng số 10%
    midterm_score       FLOAT,                         -- Giữa kỳ (0-10), trọng số 30%
    final_score         FLOAT,                         -- Cuối kỳ (0-10), trọng số 60%
    retest_score        DOUBLE PRECISION,              -- Điểm thi lại (thay final nếu có)
    total_score         FLOAT,                         -- Tự tính: 0.1*cd + 0.3*gk + 0.6*ck
    letter_grade        VARCHAR(2),                    -- A, B, C, D, F
    gpa4                FLOAT,                         -- Điểm hệ 4
    attempt_number      INTEGER   DEFAULT 1,           -- Lượt học
    enrollment_type     VARCHAR(50) DEFAULT 'ORDINARY', -- ORDINARY / RETAKE / IMPROVE
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2.5 Academic Results — GPA học kỳ và CPA tích lũy
CREATE TABLE IF NOT EXISTS academic_results (
    id                BIGSERIAL PRIMARY KEY,
    student_id        BIGINT    NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    semester_id       BIGINT    REFERENCES semesters(id),  -- NULL = CPA tích lũy
    semester_gpa      FLOAT,
    cumulative_gpa    FLOAT,
    total_credits     INTEGER,
    cumulative_credits INTEGER,
    calculated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_academic_results_student_semester UNIQUE (student_id, semester_id)
);

COMMENT ON COLUMN academic_results.semester_id IS 'NULL nếu là bản ghi CPA tích lũy toàn khóa';


-- ==============================================================
-- PHẦN 3: TUITION & PAYMENT (Học phí & Thanh toán)
-- ==============================================================

-- 3.1 Tuition Bills — Hóa đơn học phí
CREATE TABLE IF NOT EXISTS tuition_bills (
    id           BIGSERIAL    PRIMARY KEY,
    student_id   BIGINT       NOT NULL REFERENCES students(id),
    semester_id  BIGINT       NOT NULL REFERENCES semesters(id),
    total_amount NUMERIC(15,2),
    paid_amount  NUMERIC(15,2) DEFAULT 0,
    is_completed BOOLEAN      DEFAULT FALSE,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 3.2 Payment Transactions — Lịch sử giao dịch VNPay
CREATE TABLE IF NOT EXISTS payment_transactions (
    id              BIGSERIAL    PRIMARY KEY,
    tuition_bill_id BIGINT       NOT NULL REFERENCES tuition_bills(id),
    txn_ref         VARCHAR(50)  NOT NULL UNIQUE,   -- Mã giao dịch VNPay
    amount          NUMERIC(15,2) NOT NULL,
    bank_code       VARCHAR(20),
    transaction_no  VARCHAR(50),                    -- Mã giao dịch phía ngân hàng
    response_code   VARCHAR(10),                    -- 00 = thành công
    status          VARCHAR(20)  DEFAULT 'PENDING', -- PENDING / SUCCESS / FAILED
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);


-- ==============================================================
-- PHẦN 4: AUDIT (Nhật ký hệ thống)
-- ==============================================================

-- 4.1 Audit Logs — Nhật ký thao tác
CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT,
    action      VARCHAR(100) NOT NULL,
    target_type VARCHAR(100),
    target_id   VARCHAR(100),
    ip          VARCHAR(45),
    metadata    TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ==============================================================
-- PHẦN 5: CHAT SYSTEM (Hệ thống chat real-time)
-- ==============================================================

-- 5.1 Conversations — Cuộc trò chuyện
CREATE TABLE IF NOT EXISTS conversations (
    id         BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5.2 Participants — Thành viên cuộc trò chuyện
CREATE TABLE IF NOT EXISTS participants (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (conversation_id, user_id)
);

-- 5.3 Messages — Tin nhắn
CREATE TABLE IF NOT EXISTS messages (
    id              BIGSERIAL   PRIMARY KEY,
    conversation_id BIGINT      NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id       BIGINT      NOT NULL REFERENCES users(id),
    content         TEXT,
    type            VARCHAR(20) DEFAULT 'TEXT' CHECK (type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM')),
    status          VARCHAR(20) DEFAULT 'SENT' CHECK (status IN ('SENT', 'DELIVERED', 'READ')),
    media_url       VARCHAR(500),
    created_at      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- 5.4 Chat Rooms — Phòng chat
CREATE TABLE IF NOT EXISTS chat_rooms (
    id              BIGSERIAL   PRIMARY KEY,
    name            VARCHAR(255),
    description     TEXT,
    type            VARCHAR(20) NOT NULL CHECK (type IN ('PRIVATE', 'GROUP', 'CLASS_GROUP')),
    avatar_url      VARCHAR(500),
    creator_id      BIGINT      NOT NULL REFERENCES users(id),
    last_message_id BIGINT      REFERENCES messages(id),
    member_count    INTEGER     DEFAULT 0,
    is_active       BOOLEAN     DEFAULT TRUE,
    created_at      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

-- 5.5 Chat Room Members — Thành viên phòng chat
CREATE TABLE IF NOT EXISTS chat_room_members (
    id           BIGSERIAL PRIMARY KEY,
    chat_room_id BIGINT    NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    user_id      BIGINT    NOT NULL REFERENCES users(id)      ON DELETE CASCADE,
    joined_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_read_at TIMESTAMP,
    unread_count INTEGER   DEFAULT 0,
    is_active    BOOLEAN   DEFAULT TRUE,
    UNIQUE (chat_room_id, user_id)
);


-- ==============================================================
-- PHẦN 6: INDEXES (Tối ưu truy vấn)
-- ==============================================================

-- Students
CREATE INDEX IF NOT EXISTS idx_students_major_id         ON students(major_id);
CREATE INDEX IF NOT EXISTS idx_students_user_id          ON students(user_id);

-- Teachers
CREATE INDEX IF NOT EXISTS idx_teachers_user_id          ON teachers(user_id);

-- Courses
CREATE INDEX IF NOT EXISTS idx_courses_major_id          ON courses(major_id);

-- Class Sections
CREATE INDEX IF NOT EXISTS idx_class_sections_semester_id  ON class_sections(semester_id);
CREATE INDEX IF NOT EXISTS idx_class_sections_teacher_id   ON class_sections(teacher_id);
CREATE INDEX IF NOT EXISTS idx_class_sections_course_id    ON class_sections(course_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_class_sections_semester_class_code ON class_sections(semester_id, class_code);

-- Class Section Schedules
CREATE INDEX IF NOT EXISTS idx_css_class_section_id      ON class_section_schedules(class_section_id);
CREATE INDEX IF NOT EXISTS idx_css_day_period            ON class_section_schedules(day_of_week, start_period_id, end_period_id);
CREATE INDEX IF NOT EXISTS idx_css_room_id               ON class_section_schedules(room_id);

-- Enrollments
CREATE INDEX IF NOT EXISTS idx_enrollments_student_id    ON enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_class_id      ON enrollments(class_section_id);

-- Grades
CREATE INDEX IF NOT EXISTS idx_grades_enrollment_id      ON grades(enrollment_id);
CREATE INDEX IF NOT EXISTS idx_grades_created_at         ON grades(created_at);

-- Academic Results
CREATE INDEX IF NOT EXISTS idx_academic_results_student  ON academic_results(student_id);
CREATE INDEX IF NOT EXISTS idx_academic_results_semester ON academic_results(semester_id);

-- Tuition
CREATE INDEX IF NOT EXISTS idx_tuition_student_semester  ON tuition_bills(student_id, semester_id);

-- Audit Logs
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_created   ON audit_logs(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action_created ON audit_logs(action, created_at DESC);

-- Messages & Chat
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id  ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id        ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at       ON messages(created_at);
CREATE INDEX IF NOT EXISTS idx_chat_room_type            ON chat_rooms(type);
CREATE INDEX IF NOT EXISTS idx_chat_room_created_at      ON chat_rooms(created_at);
CREATE INDEX IF NOT EXISTS idx_chat_room_members_room    ON chat_room_members(chat_room_id);
CREATE INDEX IF NOT EXISTS idx_chat_room_members_user    ON chat_room_members(user_id);

-- Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL DEFAULT 'SCHOOL',
    recipient_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    link VARCHAR(255),
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_read ON notifications(recipient_id, read_flag);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);


-- ==============================================================
-- PHẦN 7: SEED DATA (Dữ liệu mẫu — chạy một lần)
-- ==============================================================

-- 7.1 Ngành học mẫu
INSERT INTO majors (major_code, name, description) VALUES
    ('CNTT', 'Công nghệ thông tin',   'Ngành Công nghệ thông tin'),
    ('KT',   'Kinh tế',               'Ngành Kinh tế'),
    ('NN',   'Ngoại ngữ',             'Ngành Ngoại ngữ'),
    ('QTKD', 'Quản trị kinh doanh',   'Ngành Quản trị kinh doanh'),
    ('LUAT', 'Luật',                  'Ngành Luật')
ON CONFLICT (major_code) DO NOTHING;

-- 7.2 Tiết học (14 tiết / ngày)
INSERT INTO periods (period_number, start_time, end_time) VALUES
    (1,  '07:00', '07:50'),
    (2,  '07:55', '08:45'),
    (3,  '08:50', '09:40'),
    (4,  '09:45', '10:35'),
    (5,  '10:40', '11:30'),
    (6,  '11:35', '12:25'),
    (7,  '12:30', '13:20'),
    (8,  '13:25', '14:15'),
    (9,  '14:20', '15:10'),
    (10, '15:15', '16:05'),
    (11, '16:10', '17:00'),
    (12, '17:05', '17:55'),
    (13, '18:00', '18:50'),
    (14, '18:55', '19:45')
ON CONFLICT (period_number) DO NOTHING;

-- 7.3 Admin account mặc định
-- Mật khẩu: Admin@123 (BCrypt $2a$10$...)
-- TODO: Thay hash bằng giá trị thực khi deploy
INSERT INTO users (username, password_hash, email, role, is_active)
VALUES ('admin', '$2a$10$N.zmdr9zkoa05e9oJqPQ1OxTG5LI5bPe4xzFxE0oOVnErBBlJ4dNe', 'admin@tlu.edu.vn', 'ADMIN', TRUE)
ON CONFLICT (username) DO NOTHING;

-- ==============================================================
-- END OF MASTER SCHEMA
-- ==============================================================
