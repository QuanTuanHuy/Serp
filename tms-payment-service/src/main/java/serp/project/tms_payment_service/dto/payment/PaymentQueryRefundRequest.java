package serp.project.tms_payment_service.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentQueryRefundRequest {
    @NotBlank(message = "mRefundId is required")
    private String mRefundId;
}
