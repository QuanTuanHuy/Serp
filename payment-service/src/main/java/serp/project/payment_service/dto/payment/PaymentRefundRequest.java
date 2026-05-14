package serp.project.payment_service.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundRequest {
    @NotBlank(message = "zpTransId is required")
    private String zpTransId;

    @NotNull(message = "amount is required")
    @Min(value = 1000, message = "amount must be at least 1000")
    private Long amount;

    private Long refundFeeAmount;

    @NotBlank(message = "description is required")
    private String description;
}
