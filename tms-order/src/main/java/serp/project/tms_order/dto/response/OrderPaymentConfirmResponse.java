/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response;

import serp.project.tms_order.enums.PaymentStatus;

public record OrderPaymentConfirmResponse(
        Long orderId,
        String orderCode,
        PaymentStatus paymentStatus,
        String appTransId,
        String gatewayStatus,
        String message
) {
}
