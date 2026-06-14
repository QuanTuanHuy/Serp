package serp.project.tms_payment_service.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateOrderResponse {
    private String appTransId;
    private String orderUrl;
    private String zpTransToken;
    private String qrCode;
    private String status;
    private String message;
    private Integer errorCode;
}
