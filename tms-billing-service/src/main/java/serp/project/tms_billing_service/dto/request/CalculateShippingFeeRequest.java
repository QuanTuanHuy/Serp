/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import serp.project.tms_billing_service.enums.ProductCategory;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;

import java.util.Set;

@Data
public class CalculateShippingFeeRequest {
    @NotBlank(message = "INVALID_REQUEST")
    private String serviceCode;

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

    private ProductCategory productCategory;

    private Set<SurchargeRuleEnum> surchargeRuleCodes;
}
