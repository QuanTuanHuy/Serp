/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.caller.dto.payment;

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
    private String status;
    private String message;
    private Integer errorCode;
    private String errorMessage;
}
