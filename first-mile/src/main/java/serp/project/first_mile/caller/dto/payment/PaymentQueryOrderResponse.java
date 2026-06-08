/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
