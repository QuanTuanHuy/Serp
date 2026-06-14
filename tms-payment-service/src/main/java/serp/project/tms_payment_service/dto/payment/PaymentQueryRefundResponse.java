package serp.project.tms_payment_service.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentQueryRefundResponse {
    private String mRefundId;
    private String status;
    private String message;
    private Integer errorCode;
    private String errorMessage;
    private String errorNote;
    private Boolean canRetry;
    private String errorCategory;
}
