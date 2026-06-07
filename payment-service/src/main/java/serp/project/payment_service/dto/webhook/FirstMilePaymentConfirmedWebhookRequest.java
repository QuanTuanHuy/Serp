package serp.project.payment_service.dto.webhook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirstMilePaymentConfirmedWebhookRequest {
    private String appTransId;
    private String orderCode;
    private Long tenantId;
    private Long amount;
    private LocalDateTime paidAt;
    private String gatewayCode;
    private String gatewayTransactionId;
}
