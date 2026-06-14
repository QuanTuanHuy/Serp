package serp.project.tms_payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request gửi tới ZaloPay Query API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayQueryRequest {
    
    @JsonProperty("app_id")
    private Integer appId;
    
    @JsonProperty("app_trans_id")
    private String appTransId;
    
    @JsonProperty("mac")
    private String mac;
}
