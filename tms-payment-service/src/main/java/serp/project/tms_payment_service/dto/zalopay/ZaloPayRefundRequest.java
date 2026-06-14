package serp.project.tms_payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request gửi đến ZaloPay Refund API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZaloPayRefundRequest {
    
    /**
     * app_id - Định danh cho ứng dụng của Merchant
     */
    @JsonProperty("app_id")
    private Integer appId;
    
    /**
     * m_refund_id - Mã giao dịch hoàn tiền của Merchant
     * Format: yymmdd_appid_mã định danh hoàn tiền
     */
    @JsonProperty("m_refund_id")
    private String mRefundId;
    
    /**
     * zp_trans_id - Mã giao dịch Zalopay muốn hoàn tiền
     */
    @JsonProperty("zp_trans_id")
    private String zpTransId;
    
    /**
     * amount - Số tiền cần hoàn tiền
     */
    @JsonProperty("amount")
    private Long amount;
    
    /**
     * refund_fee_amount - Phí của giao dịch hoàn tiền (optional)
     */
    @JsonProperty("refund_fee_amount")
    private Long refundFeeAmount;
    
    /**
     * timestamp - Thời điểm yêu cầu hoàn tiền (unix timestamp in millisecond)
     */
    @JsonProperty("timestamp")
    private Long timestamp;
    
    /**
     * description - Lý do hoàn tiền
     */
    @JsonProperty("description")
    private String description;
    
    /**
     * mac - Thông tin chứng thực
     * Nếu không có phí hoàn: app_id|zp_trans_id|amount|description|timestamp
     * Có phí hoàn: app_id|zp_trans_id|amount|refund_fee_amount|description|timestamp
     */
    @JsonProperty("mac")
    private String mac;
}
