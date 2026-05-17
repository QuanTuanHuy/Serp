package serp.project.payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Callback request từ ZaloPay khi thanh toán hoàn tất
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayCallbackRequest {
    
    @JsonProperty("data")
    private String data;
    
    @JsonProperty("mac")
    private String mac;
    
    @JsonProperty("type")
    private Integer type;
}
