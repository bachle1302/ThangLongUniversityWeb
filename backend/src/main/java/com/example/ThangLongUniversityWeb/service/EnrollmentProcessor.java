package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.EnrollmentMessage;
import com.example.ThangLongUniversityWeb.dto.response.EnrollmentRequestResponse;
import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.Student;

/**
 * Strategy interface cho luồng xử lý đăng ký học phần.
 * - KafkaEnrollmentProcessor  : dùng khi spring.kafka.enabled=true
 * - DirectEnrollmentProcessor : dùng khi spring.kafka.enabled=false (mặc định local)
 */
public interface EnrollmentProcessor {

    /**
     * Xử lý yêu cầu đăng ký.
     * @param student       Sinh viên đang đăng ký
     * @param targetClass   Lớp học phần muốn đăng ký
     * @return              Phản hồi ngay lập tức (requestId + message trạng thái)
     */
    EnrollmentRequestResponse process(Student student, ClassSection targetClass);
}
