package serp.project.payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request gửi đến ZaloPay Query Refund API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayQueryRefundRequest {
    
    /**
     * app_id - Định danh cho ứng dụng của Merchant
     */
    @JsonProperty("app_id")
    private Integer appId;
    
    /**
     * m_refund_id - Mã giao dịch hoàn tiền của Merchant
     */
    @JsonProperty("m_refund_id")
    private String mRefundId;
    
    /**
     * timestamp - Thời điểm yêu cầu truy vấn (unix timestamp in millisecond)
     */
    @JsonProperty("timestamp")
    private Long timestamp;
    
    /**
     * mac - Thông tin chứng thực
     * hmacinput = app_id|m_refund_id|timestamp
     */
    @JsonProperty("mac")
    private String mac;
}
