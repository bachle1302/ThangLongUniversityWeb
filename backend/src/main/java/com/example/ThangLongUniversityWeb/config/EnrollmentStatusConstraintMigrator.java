package com.example.ThangLongUniversityWeb.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
public class EnrollmentStatusConstraintMigrator implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        migrateEnrollmentStatusConstraint();
        migrateExamRegistrationStatusConstraint();
        migrateCourseStatusColumn();
        migrateCompletedEnrollmentCourseStatuses();
        migrateClassSectionClassCodeConstraint();
        migrateRegistrationRoundConstraint();
        migrateSemesterEndedColumn();
        migrateClassSectionStatusColumn();
        migrateExamRegistrationCourseSemesterColumns();
        migrateExamRoomAssignmentProctorColumn();
        migrateExamSessionCandidateSelection();
        migrateClassSectionSourceExamSession();
    }

    private void migrateClassSectionStatusColumn() {
        jdbcTemplate.execute("ALTER TABLE class_sections ADD COLUMN IF NOT EXISTS status VARCHAR(20)");
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
                        WHERE table_name = 'class_sections' AND column_name = 'is_closed'
                    ) THEN
                        UPDATE class_sections cs
                        SET status = CASE
                            WHEN cs.is_closed = true THEN 'CLOSED'
                            WHEN rr.registration_open = true AND rr.locked = false THEN 'OPEN'
                            WHEN rr.locked = true THEN 'CLOSED'
                            ELSE 'DRAFT'
                        END
                        FROM registration_rounds rr
                        WHERE cs.registration_round_id = rr.id
                          AND cs.status IS NULL;

                        UPDATE class_sections
                        SET status = CASE WHEN is_closed = true THEN 'CLOSED' ELSE 'DRAFT' END
                        WHERE status IS NULL;
                    ELSE
                        UPDATE class_sections SET status = 'DRAFT' WHERE status IS NULL;
                    END IF;
                END $$;
                """);
        jdbcTemplate.execute("ALTER TABLE class_sections ALTER COLUMN status SET DEFAULT 'DRAFT'");
        jdbcTemplate.execute("ALTER TABLE class_sections ALTER COLUMN status SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE class_sections DROP CONSTRAINT IF EXISTS class_sections_status_check");
        jdbcTemplate.execute("""
                ALTER TABLE class_sections
                ADD CONSTRAINT class_sections_status_check
                CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED', 'CANCELLED'))
                """);
    }

    private void migrateSemesterEndedColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE semesters ADD COLUMN IF NOT EXISTS ended boolean DEFAULT false NOT NULL");
        } catch (Exception ignored) {
            // Best-effort migration for existing local databases.
        }
    }

    private void migrateCourseStatusColumn() {
        // Thêm cột course_status nếu chưa tồn tại (ddl-auto=update có thể đã thêm, câu lệnh này safe)
        jdbcTemplate.execute("""
                ALTER TABLE enrollments
                ADD COLUMN IF NOT EXISTS course_status VARCHAR(30) DEFAULT 'IN_PROGRESS'
                """);
    }

    private void migrateClassSectionClassCodeConstraint() {
        jdbcTemplate.execute("""
                DO $$
                DECLARE
                    constraint_name text;
                BEGIN
                    FOR constraint_name IN
                        SELECT c.conname
                        FROM pg_constraint c
                        JOIN pg_class t ON t.oid = c.conrelid
                        JOIN pg_namespace n ON n.oid = t.relnamespace
                        WHERE t.relname = 'class_sections'
                          AND c.contype = 'u'
                          AND pg_get_constraintdef(c.oid) = 'UNIQUE (class_code)'
                    LOOP
                        EXECUTE format('ALTER TABLE class_sections DROP CONSTRAINT IF EXISTS %I', constraint_name);
                    END LOOP;
                END $$;
                """);
        jdbcTemplate.execute("DROP INDEX IF EXISTS uk_class_sections_semester_class_code");
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_class_sections_semester_class_code
                ON class_sections (semester_id, class_code)
                """);
    }

    private void migrateRegistrationRoundConstraint() {
        jdbcTemplate.execute("ALTER TABLE registration_rounds DROP CONSTRAINT IF EXISTS uk_registration_round_semester_number");
        jdbcTemplate.execute("ALTER TABLE registration_rounds DROP CONSTRAINT IF EXISTS uk_registration_round_semester_number_type");
        jdbcTemplate.execute("""
                ALTER TABLE registration_rounds
                ADD CONSTRAINT uk_registration_round_semester_number_type
                UNIQUE (semester_id, round_number, round_type)
                """);
    }

    private void migrateCompletedEnrollmentCourseStatuses() {
        jdbcTemplate.execute("""
                UPDATE enrollments e
                SET course_status = CASE
                    WHEN g.total_score < 4.0 THEN 'RETAKE_EXAM'
                    ELSE 'PASSED'
                END
                FROM grades g
                WHERE g.enrollment_id = e.id
                  AND g.total_score IS NOT NULL
                  AND (e.course_status IS NULL OR e.course_status = 'IN_PROGRESS')
                """);
    }

    private void migrateEnrollmentStatusConstraint() {
        jdbcTemplate.execute("ALTER TABLE enrollments DROP CONSTRAINT IF EXISTS enrollments_status_check");
        jdbcTemplate.execute("""
                ALTER TABLE enrollments
                ADD CONSTRAINT enrollments_status_check
                CHECK (status IS NULL OR status IN ('PENDING', 'REGISTERED', 'CANCELED', 'PASSED', 'FAILED'))
                """);
    }

    private void migrateExamRegistrationStatusConstraint() {
        jdbcTemplate.execute("ALTER TABLE exam_registrations DROP CONSTRAINT IF EXISTS exam_registrations_status_check");
        jdbcTemplate.execute("""
                ALTER TABLE exam_registrations
                ADD CONSTRAINT exam_registrations_status_check
                CHECK (status IS NULL OR status IN ('PENDING', 'REGISTERED', 'CANCELED', 'PASSED', 'FAILED'))
                """);
    }

    private void migrateExamRegistrationCourseSemesterColumns() {
        jdbcTemplate.execute("ALTER TABLE exam_registrations ADD COLUMN IF NOT EXISTS semester_id BIGINT");
        jdbcTemplate.execute("ALTER TABLE exam_registrations ADD COLUMN IF NOT EXISTS course_id BIGINT");
        jdbcTemplate.execute("""
                UPDATE exam_registrations er
                SET semester_id = cs.semester_id,
                    course_id = cs.course_id
                FROM class_sections cs
                WHERE er.class_section_id = cs.id
                  AND (er.semester_id IS NULL OR er.course_id IS NULL)
                """);
        jdbcTemplate.execute("ALTER TABLE exam_registrations ALTER COLUMN class_section_id DROP NOT NULL");
        jdbcTemplate.execute("""
                DO $$
                DECLARE
                    constraint_name text;
                BEGIN
                    FOR constraint_name IN
                        SELECT c.conname
                        FROM pg_constraint c
                        JOIN pg_class t ON t.oid = c.conrelid
                        WHERE t.relname = 'exam_registrations'
                          AND c.contype = 'u'
                          AND pg_get_constraintdef(c.oid) = 'UNIQUE (student_id, class_section_id)'
                    LOOP
                        EXECUTE format('ALTER TABLE exam_registrations DROP CONSTRAINT IF EXISTS %I', constraint_name);
                    END LOOP;
                END $$;
                """);
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_exam_reg_student_semester_course
                ON exam_registrations (student_id, semester_id, course_id)
                WHERE semester_id IS NOT NULL AND course_id IS NOT NULL
                """);
    }

    private void migrateExamRoomAssignmentProctorColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE exam_room_assignments ADD COLUMN IF NOT EXISTS proctor_id BIGINT REFERENCES teachers(id)");
        } catch (Exception ignored) {
            // Best-effort migration
        }
    }

    private void migrateExamSessionCandidateSelection() {
        try {
            // Add column candidate_selection if not exists, default to 'ALL'
            jdbcTemplate.execute("ALTER TABLE exam_sessions ADD COLUMN IF NOT EXISTS candidate_selection VARCHAR(20) DEFAULT 'ALL' NOT NULL");
            
            // Drop old unique constraint if it exists (which is on semester_id, course_id, exam_type)
            jdbcTemplate.execute("ALTER TABLE exam_sessions DROP CONSTRAINT IF EXISTS uk_exam_session_semester_course_type");
            
            // Re-add unique constraint including candidate_selection
            jdbcTemplate.execute("ALTER TABLE exam_sessions ADD CONSTRAINT uk_exam_session_semester_course_type UNIQUE (semester_id, course_id, exam_type, candidate_selection)");
        } catch (Exception e) {
            System.err.println("Error migrating exam session candidate selection: " + e.getMessage());
        }
    }

    private void migrateClassSectionSourceExamSession() {
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE class_sections
                    ADD COLUMN IF NOT EXISTS source_exam_session_id BIGINT REFERENCES exam_sessions(id)
                    """);
        } catch (Exception ignored) {
            // Best-effort migration
        }
    }
}
