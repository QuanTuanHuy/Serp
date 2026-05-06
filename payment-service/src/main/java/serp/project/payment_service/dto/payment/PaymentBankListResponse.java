package serp.project.payment_service.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentBankListResponse {
    private Integer returnCode;
    private String returnMessage;
    private Map<String, List<PaymentBankDTO>> banks;
}
