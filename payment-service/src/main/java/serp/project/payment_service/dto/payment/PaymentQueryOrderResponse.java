package serp.project.payment_service.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentQueryOrderResponse {
    private String appTransId;
    private Long zpTransId;
    private String status;
    private String message;
    private Long amount;
    private Long discountAmount;
    private Boolean isProcessing;
    private Integer errorCode;
    private String errorMessage;
    private String errorNote;
    private Boolean canRetry;
    private String errorCategory;
}
