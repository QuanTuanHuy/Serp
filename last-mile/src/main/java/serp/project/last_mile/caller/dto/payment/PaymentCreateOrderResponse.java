/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.last_mile.caller.dto.payment;

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
