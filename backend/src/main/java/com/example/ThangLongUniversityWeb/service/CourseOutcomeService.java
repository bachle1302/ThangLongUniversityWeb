package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.entity.Enrollment;
import com.example.ThangLongUniversityWeb.entity.Grade;
import com.example.ThangLongUniversityWeb.enums.AttendanceStatus;
import com.example.ThangLongUniversityWeb.enums.CourseStudyStatus;
import com.example.ThangLongUniversityWeb.repository.AttendanceRecordRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tính toán và cập nhật Enrollment.courseStatus sau mỗi lần lưu điểm danh hoặc điểm số.
 *
 * Luật nghiệp vụ (trọng số điểm 0.1 / 0.3 / 0.6):
 *  1. Vắng >= 4 buổi → BANNED_FROM_EXAM
 *  2. Có điểm chuyên cần + giữa kỳ nhưng chưa có điểm cuối kỳ:
 *     preFinalAvg = participation*0.25 + midterm*0.75 < 4.0 → REPEAT_COURSE
 *  3. Có đủ điểm cuối kỳ (hoặc thi lại):
 *     totalScore < 4.0 → RETAKE_EXAM
 *     totalScore >= 4.0 → PASSED
 *  4. Mặc định: IN_PROGRESS
 */
@Service
@RequiredArgsConstructor
public class CourseOutcomeService {

    private static final int ABSENT_LIMIT = 3;          // Quá giới hạn này thì cấm thi
    private static final float PRE_FINAL_THRESHOLD = 4.0f;
    private static final float PASS_THRESHOLD = 4.0f;

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Tính lại courseStatus cho một enrollment và lưu.
     */
    @Transactional
    public CourseStudyStatus recalculate(Enrollment enrollment) {
        long absences = attendanceRecordRepository.countByEnrollmentIdAndStatus(
                enrollment.getId(), AttendanceStatus.ABSENT);

        CourseStudyStatus newStatus;

        if (absences > ABSENT_LIMIT) {
            newStatus = CourseStudyStatus.BANNED_FROM_EXAM;
        } else {
            newStatus = evaluateGrade(enrollment.getGrade());
        }

        enrollment.setCourseStatus(newStatus);
        enrollmentRepository.save(enrollment);
        return newStatus;
    }

    // ─────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────

    private CourseStudyStatus evaluateGrade(Grade grade) {
        if (grade == null) {
            return CourseStudyStatus.IN_PROGRESS;
        }

        Float participation = grade.getParticipationScore();
        Float midterm = grade.getMidtermScore();
        Float finalScore = grade.getFinalScore();
        Float retest = grade.getRetestScore();

        // Chưa có đủ điểm chuyên cần + giữa kỳ
        if (participation == null || midterm == null) {
            return CourseStudyStatus.IN_PROGRESS;
        }

        // Có chuyên cần + giữa kỳ nhưng chưa có điểm cuối kỳ → kiểm tra điều kiện học lại sớm
        if (finalScore == null && retest == null) {
            float preFinalAvg = participation * 0.25f + midterm * 0.75f;
            if (preFinalAvg < PRE_FINAL_THRESHOLD) {
                return CourseStudyStatus.REPEAT_COURSE;
            }
            return CourseStudyStatus.IN_PROGRESS;
        }

        // Có đủ điểm cuối kỳ → dựa vào totalScore (đã được tính sẵn trong Grade)
        Float totalScore = grade.getTotalScore();
        if (totalScore == null) {
            return CourseStudyStatus.IN_PROGRESS;
        }
        if (totalScore < PASS_THRESHOLD) {
            return CourseStudyStatus.RETAKE_EXAM;
        }
        return CourseStudyStatus.PASSED;
    }
}
