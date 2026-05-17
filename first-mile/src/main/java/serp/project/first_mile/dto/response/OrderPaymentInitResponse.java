/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

public record OrderPaymentInitResponse(
        Long orderId,
        String orderCode,
        Long amount,
        String appTransId,
        String paymentUrl,
        String status,
        String message
) {
}
