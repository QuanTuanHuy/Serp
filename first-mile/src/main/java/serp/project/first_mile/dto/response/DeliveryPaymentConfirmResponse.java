/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import serp.project.first_mile.enums.PaymentStatus;

public record DeliveryPaymentConfirmResponse(
        Long manifestId,
        String orderCode,
        Long amount,
        PaymentStatus paymentStatus,
        String appTransId,
        String status,
        String message
) {
    @JsonProperty("tripId")
    public Long getTripId() {
        return manifestId;
    }
}
