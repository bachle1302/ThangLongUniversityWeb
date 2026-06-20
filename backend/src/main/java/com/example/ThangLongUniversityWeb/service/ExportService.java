package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.Enrollment;
import com.example.ThangLongUniversityWeb.entity.ExamRegistration;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.ExamRegistrationRepository;
import com.example.ThangLongUniversityWeb.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final EnrollmentRepository enrollmentRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final ClassSectionRepository classSectionRepository;
    private final SemesterRepository semesterRepository;

    /**
     * Export danh sách đăng ký học phần của một học kỳ ra Excel.
     */
    public byte[] exportEnrollmentsToExcel(Long semesterId) {
        List<Enrollment> enrollments = enrollmentRepository.findByClassSectionSemesterId(semesterId);
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Danh sach dang ky");
            CellStyle headerStyle = createHeaderStyle(wb);

            // Header row
            Row header = sheet.createRow(0);
            String[] headers = {"STT", "Mã SV", "Họ tên", "Mã lớp", "Môn học", "Tín chỉ", "Trạng thái"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            // Data rows
            int rowNum = 1;
            for (Enrollment e : enrollments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(e.getStudent().getStudentCode());
                row.createCell(2).setCellValue(e.getStudent().getFullName());
                row.createCell(3).setCellValue(e.getClassSection().getClassCode());
                row.createCell(4).setCellValue(e.getClassSection().getCourse().getName());
                row.createCell(5).setCellValue(e.getClassSection().getCourse().getCredits());
                row.createCell(6).setCellValue(e.getStatus() != null ? e.getStatus().name() : "");
            }

            wb.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Lỗi khi xuất Excel: " + ex.getMessage(), ex);
        }
    }

    /**
     * Export lịch thi của một học kỳ ra Excel.
     */
    public byte[] exportExamSchedulesToExcel(Long semesterId) {
        List<ClassSection> sections = classSectionRepository.findBySemesterId(semesterId);
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Lich thi");
            CellStyle headerStyle = createHeaderStyle(wb);

            Row header = sheet.createRow(0);
            String[] headers = {"STT", "Mã lớp", "Môn học", "Tín chỉ", "Giảng viên", "Loại thi", "Ngày thi", "Phòng thi", "Số SV"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5500);
            }

            int rowNum = 1;
            for (ClassSection cs : sections) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(cs.getClassCode());
                row.createCell(2).setCellValue(cs.getCourse().getName());
                row.createCell(3).setCellValue(cs.getCourse().getCredits());
                row.createCell(4).setCellValue(cs.getTeacher() != null ? cs.getTeacher().getFullName() : "Chưa phân công");
                row.createCell(5).setCellValue(cs.getExamType() != null ? cs.getExamType().name() : "NORMAL");
                row.createCell(6).setCellValue(cs.getExamAt() != null ? cs.getExamAt().toString() : "Chưa có");
                row.createCell(7).setCellValue(cs.getExamRoom() != null ? cs.getExamRoom() : "Chưa có");
                int studentCount = (int) enrollmentRepository.countByClassSectionIdAndStatusIn(
                        cs.getId(), List.of(EnrollmentStatus.PENDING, EnrollmentStatus.REGISTERED));
                row.createCell(8).setCellValue(studentCount);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Lỗi khi xuất Excel: " + ex.getMessage(), ex);
        }
    }

    /**
     * Export danh sách đăng ký thi lại của một học kỳ ra Excel.
     */
    public byte[] exportRetakesToExcel(Long semesterId) {
        var retakes = examRegistrationRepository.findBySemesterId(semesterId);
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Dang ky thi lai");
            CellStyle headerStyle = createHeaderStyle(wb);

            Row header = sheet.createRow(0);
            String[] headers = {"STT", "Mã SV", "Họ tên", "Mã lớp", "Môn học", "Loại", "Lần thi", "Phí", "Trạng thái"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            int rowNum = 1;
            for (ExamRegistration r : retakes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(r.getStudent().getStudentCode());
                row.createCell(2).setCellValue(r.getStudent().getFullName());
                var classSection = r.getClassSection();
                var course = r.getCourse() != null ? r.getCourse() : classSection.getCourse();
                row.createCell(3).setCellValue(classSection != null ? classSection.getClassCode() : "Chua xep lop thi");
                row.createCell(4).setCellValue(course.getName());
                row.createCell(5).setCellValue(r.getRegistrationType() != null ? r.getRegistrationType().name() : "");
                row.createCell(6).setCellValue(r.getAttemptNumber() != null ? r.getAttemptNumber() : 0);
                row.createCell(7).setCellValue(r.getFeeCharged() != null ? r.getFeeCharged() : 0L);
                row.createCell(8).setCellValue(r.getStatus() != null ? r.getStatus().name() : "");
            }

            wb.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Lỗi khi xuất Excel: " + ex.getMessage(), ex);
        }
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }
}
