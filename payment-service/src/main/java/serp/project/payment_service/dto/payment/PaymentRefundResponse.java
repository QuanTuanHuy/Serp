package serp.project.payment_service.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundResponse {
    private String mRefundId;
    private String zpTransId;
    private Long refundId;
    private Long amount;
    private String status;
    private String message;
    private Integer errorCode;
    private String errorMessage;
    private String errorNote;
    private Boolean canRetry;
    private String errorCategory;
}
