package serp.project.tms_payment_service.dto.zalopay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response truy vấn trạng thái hoàn tiền trả về client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryRefundResponse {
    
    /**
     * Mã giao dịch hoàn tiền của Merchant
     */
    private String mRefundId;
    
    /**
     * Trạng thái: SUCCESS, PROCESSING, FAILED, PENDING
     */
    private String status;
    
    /**
     * Thông báo
     */
    private String message;
    
    /**
     * Mã lỗi (nếu có)
     */
    private Integer errorCode;
    
    /**
     * Thông báo lỗi chi tiết
     */
    private String errorMessage;
    
    /**
     * Ghi chú lỗi
     */
    private String errorNote;
    
    /**
     * Có thể retry không?
     */
    private Boolean canRetry;
    
    /**
     * Loại lỗi: USER, MERCHANT, SYSTEM, UNKNOWN
     */
    private String errorCategory;
}
