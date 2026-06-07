package serp.project.payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmbedData {
    
    /**
     * URL redirect sau khi thanh toán
     */
    @JsonProperty("redirecturl")
    private String redirectUrl;
    
    /**
     * Thông tin riêng của merchant
     */
    @JsonProperty("merchantinfo")
    private String merchantInfo;
    
    /**
     * Thông tin khuyến mãi
     */
    @JsonProperty("promotioninfo")
    private String promotionInfo;
    
    /**
     * Thông tin cột hiển thị
     */
    @JsonProperty("columninfo")
    private String columnInfo;
    
    /**
     * Phương thức thanh toán ưu tiên
     * Ví dụ: ["domestic_card", "vietqr"]
     */
    @JsonProperty("preferred_payment_method")
    private List<String> preferredPaymentMethod;
    
    /**
     * ZaloPay Payment ID
     */
    @JsonProperty("zlppaymentid")
    private String zlpPaymentId;
}
