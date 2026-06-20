package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.config.VNPayConfig;
import com.example.ThangLongUniversityWeb.dto.response.TuitionItemResponse;
import com.example.ThangLongUniversityWeb.dto.response.TuitionResponse;
import com.example.ThangLongUniversityWeb.entity.Enrollment;
import com.example.ThangLongUniversityWeb.entity.ExamRegistration;
import com.example.ThangLongUniversityWeb.entity.Semester;
import com.example.ThangLongUniversityWeb.entity.Student;
import com.example.ThangLongUniversityWeb.entity.TuitionBill;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.ExamRegistrationRepository;
import com.example.ThangLongUniversityWeb.repository.SemesterRepository;
import com.example.ThangLongUniversityWeb.repository.StudentRepository;
import com.example.ThangLongUniversityWeb.repository.TuitionBillRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentTuitionService {

    public record VNPayReturnResult(
            boolean success,
            String message,
            String transactionNo,
            String txnRef,
            String responseCode,
            String transactionStatus,
            String amount,
            String bankCode
    ) {}

    private final TuitionBillRepository tuitionBillRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final StudentRepository studentRepository;
    private final SemesterRepository semesterRepository;
    private final VNPayConfig vnPayConfig;

    private final BigDecimal PRICE_PER_CREDIT = new BigDecimal("850000"); // 850k/1 tín chỉ

    private Student getCurrentStudent() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return studentRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin sinh viên!"));
    }

    // 1. TẠO HOẶC XEM HÓA ĐƠN
    @Transactional
    public TuitionResponse getTuitionFee(Long semesterId) {
        Student student = getCurrentStudent();
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học kỳ!"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndClassSection_SemesterIdAndStatus(
                student.getId(), semesterId, EnrollmentStatus.REGISTERED);
        List<ExamRegistration> retakeRegistrations = examRegistrationRepository.findByStudentIdAndSemesterIdAndStatus(
                student.getId(), semesterId, EnrollmentStatus.REGISTERED);

        int totalCredits = enrollments.stream()
                .mapToInt(e -> e.getClassSection().getCourse().getCredits())
                .sum();

        long retakeTotal = retakeRegistrations.stream()
                .mapToLong(reg -> reg.getFeeCharged() != null ? reg.getFeeCharged() : 0L)
                .sum();

        BigDecimal totalAmount = PRICE_PER_CREDIT.multiply(new BigDecimal(totalCredits))
                .add(BigDecimal.valueOf(retakeTotal));

        TuitionBill bill = tuitionBillRepository.findByStudentIdAndSemesterId(student.getId(), semesterId)
                .orElse(new TuitionBill());

        if (bill.getId() == null) {
            bill.setStudent(student);
            bill.setSemester(semester);
            bill.setTotalAmount(totalAmount);
            bill.setPaidAmount(BigDecimal.ZERO);
            bill.setCompleted(false);
            bill.setCreatedAt(LocalDateTime.now());
            bill = tuitionBillRepository.save(bill);
        } else {
            // Nếu tổng học phí tính toán đã thay đổi (ví dụ do được chốt đăng ký thi lại sau khi đóng học phí chính thức)
            if (bill.getTotalAmount() == null || bill.getTotalAmount().compareTo(totalAmount) != 0) {
                bill.setTotalAmount(totalAmount);
                if (bill.getPaidAmount() == null) {
                    bill.setPaidAmount(BigDecimal.ZERO);
                }
                // Nếu số tiền đã đóng nhỏ hơn tổng tiền mới, đánh dấu hóa đơn chưa hoàn thành để sinh viên đóng tiếp phần thiếu
                if (bill.getPaidAmount().compareTo(totalAmount) < 0) {
                    bill.setCompleted(false);
                } else {
                    bill.setCompleted(true);
                }
                bill = tuitionBillRepository.save(bill);
            }
        }

        // Build chi tiet tung mon
        List<TuitionItemResponse> items = enrollments.stream().map(e -> {
            var course = e.getClassSection().getCourse();
            int credits = course.getCredits() != null ? course.getCredits() : 0;
            long subtotal = PRICE_PER_CREDIT.longValue() * credits;
            return new TuitionItemResponse(
                     "COURSE",
                     course.getCode(),
                     course.getName(),
                     credits,
                     PRICE_PER_CREDIT.longValue(),
                     subtotal
            );
        }).toList();

        List<TuitionItemResponse> retakeItems = retakeRegistrations.stream().map(reg -> {
            var course = reg.getCourse() != null ? reg.getCourse() : reg.getClassSection().getCourse();
            long fee = reg.getFeeCharged() != null ? reg.getFeeCharged() : 0L;
            return new TuitionItemResponse(
                     "RETAKE",
                     course.getCode(),
                     course.getName(),
                     course.getCredits(),
                     fee,
                     fee
            );
        }).toList();

        List<TuitionItemResponse> allItems = new ArrayList<>();
        allItems.addAll(items);
        allItems.addAll(retakeItems);

        return TuitionResponse.builder()
                .semesterName(semester.getName())
                .totalCredits(totalCredits)
                .totalAmount(bill.getTotalAmount().longValue())
                .paidAmount(bill.getPaidAmount() != null ? bill.getPaidAmount().longValue() : 0L)
                .pricePerCredit(PRICE_PER_CREDIT.longValue())
                .isPaid(bill.isCompleted())
                .items(allItems)
                .build();
    }

    // 2. TẠO URL VNPAY
    @Transactional
    public String createVNPayUrl(Long semesterId, HttpServletRequest request) {
        Student student = getCurrentStudent();
        TuitionBill bill = tuitionBillRepository.findByStudentIdAndSemesterId(student.getId(), semesterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn học phí!"));

        if (bill.isCompleted()) {
            throw new RuntimeException("Hóa đơn này đã được thanh toán!");
        }

        long paid = bill.getPaidAmount() != null ? bill.getPaidAmount().longValue() : 0L;
        long remaining = bill.getTotalAmount().longValue() - paid;
        if (remaining <= 0) {
            throw new RuntimeException("Hóa đơn này đã được thanh toán đầy đủ!");
        }

        // VNPAY yêu cầu số tiền nhân thêm 100
        long amount = remaining * 100L;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getVnpVersion());
        vnp_Params.put("vnp_Command", vnPayConfig.getVnpCommand());
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        // MẸO: Lưu ID hóa đơn nối với chuỗi ngẫu nhiên để lát nữa Return còn biết là thanh toán cho hóa đơn nào
        String txnRef = bill.getId() + "_" + vnPayConfig.getRandomNumber(8);
        vnp_Params.put("vnp_TxnRef", txnRef);

        vnp_Params.put("vnp_OrderInfo", "Thanh toan hoc phi HK " + semesterId + " SV " + student.getStudentCode());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnPayConfig.getIpAddress(request));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15); // Thời hạn thanh toán 15 phút
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo mã URL VNPAY");
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnPayConfig.getVnpPayUrl() + "?" + queryUrl;
    }

    // 3. HỨNG KẾT QUẢ TỪ VNPAY VÀ CẬP NHẬT DATABASE
    @Transactional
    public String processVNPayReturn(HttpServletRequest request) {
        return processVNPayReturnResult(request).message();
    }

    @Transactional
    public VNPayReturnResult processVNPayReturnResult(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        String transactionNo = request.getParameter("vnp_TransactionNo");
        String txnRef = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionStatus = request.getParameter("vnp_TransactionStatus");
        String amount = request.getParameter("vnp_Amount");
        String bankCode = request.getParameter("vnp_BankCode");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // Hash lại dữ liệu để kiểm tra xem có hacker sửa tiền không
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        try {
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
        } catch (Exception e) {
            return new VNPayReturnResult(false, "Lỗi giải mã dữ liệu VNPAY!", transactionNo, txnRef, responseCode, transactionStatus, amount, bankCode);
        }

        String signValue = vnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());

        if (signValue.equals(vnp_SecureHash)) {
            // Mã "00" có nghĩa là giao dịch thành công bên phía Ngân hàng
            if ("00".equals(transactionStatus)) {

                // Lấy ID hóa đơn ra từ vnp_TxnRef (Ví dụ: "5_92837482" -> Lấy số 5)
                Long billId;
                try {
                    billId = Long.parseLong(txnRef.split("_")[0]);
                } catch (Exception e) {
                    return new VNPayReturnResult(false, "Không xác định được hóa đơn thanh toán.", transactionNo, txnRef, responseCode, transactionStatus, amount, bankCode);
                }

                TuitionBill bill = tuitionBillRepository.findById(billId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

                // CẬP NHẬT TRẠNG THÁI: ĐÃ ĐÓNG TIỀN
                bill.setCompleted(true);
                bill.setPaidAmount(bill.getTotalAmount());
                tuitionBillRepository.save(bill);

                return new VNPayReturnResult(true, "Giao dịch thành công!", transactionNo, txnRef, responseCode, transactionStatus, amount, bankCode);
            } else {
                return new VNPayReturnResult(false, "Giao dịch không thành công hoặc bị hủy.", transactionNo, txnRef, responseCode, transactionStatus, amount, bankCode);
            }
        } else {
            return new VNPayReturnResult(false, "Lỗi bảo mật: Chữ ký dữ liệu không hợp lệ!", transactionNo, txnRef, responseCode, transactionStatus, amount, bankCode);
        }
    }
}
