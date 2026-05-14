/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.PaymentStatus;

public record OrderPaymentConfirmResponse(
        Long orderId,
        String orderCode,
        PaymentStatus paymentStatus,
        String appTransId,
        String gatewayStatus,
        String message
) {
}
