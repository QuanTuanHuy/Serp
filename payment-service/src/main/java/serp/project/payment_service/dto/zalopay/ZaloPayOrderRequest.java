package serp.project.payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request gửi tới ZaloPay API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayOrderRequest {
    
    @JsonProperty("app_id")
    private Integer appId;
    
    @JsonProperty("app_user")
    private String appUser;
    
    @JsonProperty("app_trans_id")
    private String appTransId;
    
    @JsonProperty("app_time")
    private Long appTime;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("item")
    private String item;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("embed_data")
    private String embedData;
    
    @JsonProperty("bank_code")
    private String bankCode;
    
    @JsonProperty("mac")
    private String mac;
    
    @JsonProperty("callback_url")
    private String callbackUrl;
    
    @JsonProperty("expire_duration_seconds")
    private Long expireDurationSeconds;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("sub_app_id")
    private String subAppId;
}
