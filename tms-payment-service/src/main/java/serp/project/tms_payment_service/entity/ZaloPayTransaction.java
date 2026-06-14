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
 * Entity lưu thông tin giao dịch ZaloPay
 */
@Entity
@Table(name = "zalopay_transactions", indexes = {
        @Index(name = "idx_app_trans_id", columnList = "app_trans_id", unique = true),
        @Index(name = "idx_zp_trans_id", columnList = "zp_trans_id"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Mã giao dịch nội bộ (format: yyMMdd_timestamp)
     */
    @Column(name = "app_trans_id", nullable = false, unique = true, length = 40)
    private String appTransId;
    
    /**
     * Mã giao dịch ZaloPay (nhận được từ callback)
     */
    @Column(name = "zp_trans_id")
    private Long zpTransId;
    
    /**
     * ID người dùng
     */
    @Column(name = "app_user", nullable = false, length = 50)
    private String appUser;
    
    /**
     * Số tiền thanh toán
     */
    @Column(name = "amount", nullable = false)
    private Long amount;
    
    /**
     * Mô tả đơn hàng
     */
    @Column(name = "description", nullable = false, length = 256)
    private String description;
    
    /**
     * Danh sách items (JSON)
     */
    @Column(name = "items", columnDefinition = "TEXT")
    private String items;
    
    /**
     * Embed data (JSON)
     */
    @Column(name = "embed_data", columnDefinition = "TEXT")
    private String embedData;
    
    /**
     * Mã ngân hàng
     */
    @Column(name = "bank_code", length = 20)
    private String bankCode;
    
    /**
     * Token đơn hàng ZaloPay
     */
    @Column(name = "zp_trans_token", length = 255)
    private String zpTransToken;
    
    /**
     * URL đơn hàng
     */
    @Column(name = "order_url", length = 500)
    private String orderUrl;
    
    /**
     * QR Code
     */
    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;
    
    /**
     * Trạng thái: PENDING, SUCCESS, FAILED, EXPIRED
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    
    /**
     * Return code từ ZaloPay
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
     * Channel thanh toán (từ callback)
     */
    @Column(name = "channel")
    private Integer channel;
    
    /**
     * Số tiền phí người dùng chịu
     */
    @Column(name = "user_fee_amount")
    private Long userFeeAmount;
    
    /**
     * Số tiền được giảm giá
     */
    @Column(name = "discount_amount")
    private Long discountAmount;
    
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
     * Thời gian thanh toán (từ callback)
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    /**
     * Email người dùng (để gửi thông báo)
     */
    @Column(name = "email", length = 100)
    private String email;
    
    /**
     * Tiêu đề đơn hàng
     */
    @Column(name = "title", length = 256)
    private String title;
    
    /**
     * Enum trạng thái giao dịch
     */
    public enum TransactionStatus {
        PENDING,    // Đang chờ thanh toán
        SUCCESS,    // Thanh toán thành công
        FAILED,     // Thanh toán thất bại
        EXPIRED     // Đã hết hạn
    }
}
