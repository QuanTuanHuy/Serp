/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import serp.project.tms_billing_service.enums.DeliveryService;

@Data
public class CalculateShippingFeeRequest {
    @NotNull(message = "INVALID_REQUEST")
    private DeliveryService serviceCode;

    @NotBlank(message = "INVALID_REQUEST")
    private String senderWardCode;

    @NotBlank(message = "INVALID_REQUEST")
    private String receiverWardCode;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Long actualWeightGram;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Integer lengthCm;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Integer widthCm;

    @NotNull(message = "INVALID_REQUEST")
    @Positive(message = "INVALID_REQUEST")
    private Integer heightCm;

    @PositiveOrZero(message = "INVALID_REQUEST")
    private Long codAmount;

}
