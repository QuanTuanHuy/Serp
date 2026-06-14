package serp.project.tms_payment_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity lưu thông tin giao dịch hoàn tiền ZaloPay
 */
@Entity
@Table(name = "zalopay_refund_transactions", indexes = {
        @Index(name = "idx_m_refund_id", columnList = "m_refund_id", unique = true),
        @Index(name = "idx_zp_trans_id", columnList = "zp_trans_id"),
        @Index(name = "idx_refund_status", columnList = "status"),
        @Index(name = "idx_app_trans_id", columnList = "app_trans_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayRefundTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Mã giao dịch hoàn tiền của Merchant (format: yymmdd_appid_xxxx)
     */
    @Column(name = "m_refund_id", nullable = false, unique = true, length = 50)
    private String mRefundId;
    
    /**
     * Mã giao dịch ZaloPay gốc cần hoàn tiền
     */
    @Column(name = "zp_trans_id", nullable = false)
    private String zpTransId;
    
    /**
     * Mã giao dịch nội bộ gốc (app_trans_id)
     */
    @Column(name = "app_trans_id", length = 40)
    private String appTransId;
    
    /**
     * Mã giao dịch hoàn tiền từ ZaloPay
     */
    @Column(name = "refund_id")
    private Long refundId;
    
    /**
     * Số tiền hoàn
     */
    @Column(name = "amount", nullable = false)
    private Long amount;
    
    /**
     * Phí hoàn tiền
     */
    @Column(name = "refund_fee_amount")
    private Long refundFeeAmount;
    
    /**
     * Lý do hoàn tiền
     */
    @Column(name = "description", nullable = false, length = 256)
    private String description;
    
    /**
     * Trạng thái: PROCESSING, SUCCESS, FAILED
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RefundStatus status;
    
    /**
     * Return code từ ZaloPay (1=SUCCESS, 2=FAIL, 3=PROCESSING)
     */
    @Column(name = "return_code")
    private Integer returnCode;
    
    /**
     * Return message từ ZaloPay
     */
    @Column(name = "return_message", length = 255)
    private String returnMessage;
    
    /**
     * Sub return code từ ZaloPay (mã lỗi chi tiết)
     */
    @Column(name = "sub_return_code")
    private Integer subReturnCode;
    
    /**
     * Sub return message từ ZaloPay (thông báo lỗi chi tiết)
     */
    @Column(name = "sub_return_message", length = 500)
    private String subReturnMessage;
    
    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Thời gian hoàn tiền thành công
     */
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    /**
     * Enum trạng thái hoàn tiền
     */
    public enum RefundStatus {
        PROCESSING,  // Đang xử lý
        SUCCESS,     // Hoàn tiền thành công
        FAILED       // Hoàn tiền thất bại
    }
}
