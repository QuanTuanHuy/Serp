/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderConfirmedWebhookRequest {
    @NotBlank
    private String appTransId;

    @NotBlank
    private String orderCode;

    @NotNull
    private Long tenantId;

    private Long amount;

    private LocalDateTime paidAt;

    private String gatewayCode;

    private String gatewayTransactionId;
}
