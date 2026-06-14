package serp.project.tms_payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data trong callback từ ZaloPay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayCallbackData {
    
    @JsonProperty("app_id")
    private Integer appId;
    
    @JsonProperty("app_trans_id")
    private String appTransId;
    
    @JsonProperty("app_time")
    private Long appTime;
    
    @JsonProperty("app_user")
    private String appUser;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("embed_data")
    private String embedData;
    
    @JsonProperty("item")
    private String item;
    
    @JsonProperty("zp_trans_id")
    private Long zpTransId;
    
    @JsonProperty("server_time")
    private Long serverTime;
    
    @JsonProperty("channel")
    private Integer channel;
    
    @JsonProperty("merchant_user_id")
    private String merchantUserId;
    
    @JsonProperty("zp_user_id")
    private String zpUserId;
    
    @JsonProperty("user_fee_amount")
    private Long userFeeAmount;
    
    @JsonProperty("discount_amount")
    private Long discountAmount;
}
