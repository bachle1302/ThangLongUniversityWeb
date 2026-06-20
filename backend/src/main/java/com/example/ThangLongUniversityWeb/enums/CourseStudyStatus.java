package com.example.ThangLongUniversityWeb.enums;

public enum CourseStudyStatus {
    /** Đang học – chưa đủ dữ liệu để kết luận */
    IN_PROGRESS,
    /** Qua môn */
    PASSED,
    /** Cấm thi: nghỉ quá 3 buổi */
    BANNED_FROM_EXAM,
    /** Học lại: điểm chuyên cần + giữa kỳ (theo trọng số 0.25/0.75) < 4 */
    REPEAT_COURSE,
    /** Thi lại: tổng kết < 4 */
    RETAKE_EXAM
}
