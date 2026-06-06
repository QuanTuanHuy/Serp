package serp.project.payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response trả về cho client khi query order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryOrderResponse {
    
    /**
     * Mã giao dịch nội bộ
     */
    private String appTransId;
    
    /**
     * Mã giao dịch ZaloPay
     */
    private Long zpTransId;
    
    /**
     * Trạng thái: SUCCESS, FAILED, PENDING, PROCESSING, ERROR
     */
    private String status;
    
    /**
     * Thông báo
     */
    private String message;
    
    /**
     * Số tiền
     */
    private Long amount;
    
    /**
     * Số tiền giảm giá
     */
    private Long discountAmount;
    
    /**
     * Đang xử lý?
     */
    private Boolean isProcessing;
    
    /**
     * Mã lỗi chi tiết (sub_return_code từ ZaloPay)
     */
    private Integer errorCode;
    
    /**
     * Mô tả chi tiết lỗi (sub_return_message từ ZaloPay)
     */
    private String errorMessage;
    
    /**
     * Ghi chú về lỗi (hướng dẫn xử lý)
     */
    private String errorNote;
    
    /**
     * Có thể retry không?
     */
    private Boolean canRetry;
    
    /**
     * Lỗi thuộc về ai? USER, MERCHANT, SYSTEM
     */
    private String errorCategory;
}
