/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import serp.project.tms_billing_service.enums.CalculationType;
import serp.project.tms_billing_service.enums.VasRuleCode;

@Data
public class UpsertVasRuleRequest {
    @NotNull(message = "INVALID_REQUEST")
    private VasRuleCode code;

    @NotBlank(message = "INVALID_REQUEST")
    private String name;

    @NotNull(message = "INVALID_REQUEST")
    private CalculationType calculationType;

    private Double ratePercent;
    private Double fixedAmount;
    private Double minAmount;
}
