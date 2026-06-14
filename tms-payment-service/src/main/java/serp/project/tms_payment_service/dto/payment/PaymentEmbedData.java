package serp.project.tms_payment_service.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEmbedData {
    private String redirectUrl;
    private String merchantInfo;
    private String promotionInfo;
    private String columnInfo;
    private List<String> preferredPaymentMethod;
    private String zlpPaymentId;
}
