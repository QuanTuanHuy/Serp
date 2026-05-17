package serp.project.payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response từ ZaloPay Refund API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayRefundResponse {
    
    /**
     * return_code:
     * 1 = SUCCESS
     * 2 = FAIL
     * 3 = PROCESSING
     */
    @JsonProperty("return_code")
    private Integer returnCode;
    
    /**
     * return_message - Thông báo kết quả
     */
    @JsonProperty("return_message")
    private String returnMessage;
    
    /**
     * sub_return_code - Mã lỗi chi tiết
     */
    @JsonProperty("sub_return_code")
    private Integer subReturnCode;
    
    /**
     * sub_return_message - Thông báo lỗi chi tiết
     */
    @JsonProperty("sub_return_message")
    private String subReturnMessage;
    
    /**
     * refund_id - Mã giao dịch hoàn tiền từ ZaloPay
     */
    @JsonProperty("refund_id")
    private Long refundId;
}
