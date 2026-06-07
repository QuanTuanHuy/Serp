package serp.project.payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response từ API lấy danh sách ngân hàng của ZaloPay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetBankListResponse {
    
    /**
     * Mã lỗi
     * 1 = Success
     * 2 = Failed
     */
    @JsonProperty("returncode")
    private Integer returnCode;
    
    /**
     * Thông tin lỗi
     */
    @JsonProperty("returnmessage")
    private String returnMessage;
    
    /**
     * Danh sách các ngân hàng theo pmcid
     * Key: pmcid (36, 37, 38, 39, 41)
     * Value: Danh sách các ngân hàng
     */
    @JsonProperty("banks")
    private Map<String, List<BankDTO>> banks;
}
