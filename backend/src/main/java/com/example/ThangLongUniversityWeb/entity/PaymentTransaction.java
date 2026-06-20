package com.example.ThangLongUniversityWeb.entity;

import com.example.ThangLongUniversityWeb.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lưu thông tin giao dịch thanh toán VNPAY.
 * Mỗi lần sinh viên tạo URL VNPAY và hoàn tất thanh toán sẽ ghi lại 1 bản ghi ở đây.
 */
@Entity
@Table(name = "payment_transactions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã giao dịch nội bộ (UUID hoặc txnRef VNPAY) */
    @Column(name = "txn_id", nullable = false, unique = true, length = 100)
    private String txnId;

    /** ID hóa đơn học phí liên quan */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private TuitionBill bill;

    /** Số tiền giao dịch (VND) */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /** Mã giao dịch phía VNPAY trả về (vnp_TransactionNo) */
    @Column(name = "vnpay_transaction_no", length = 50)
    private String vnpayTransactionNo;

    /** Mã ngân hàng (vnp_BankCode) */
    @Column(name = "bank_code", length = 20)
    private String bankCode;

    /** Trạng thái giao dịch */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    /** Thông điệp kết quả từ VNPAY */
    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
