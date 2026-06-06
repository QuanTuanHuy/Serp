/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response;

public record PaymentWebhookProcessResponse(
        String orderCode,
        String appTransId,
        boolean updated,
        String message
) {
}
