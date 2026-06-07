/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import serp.project.tms_billing_service.enums.CalculationType;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;

import java.time.LocalDate;

@Data
public class UpsertSurchargeRuleRequest {
    @NotNull(message = "INVALID_REQUEST")
    private SurchargeRuleEnum code;

    @NotBlank(message = "INVALID_REQUEST")
    private String name;

    @NotNull(message = "INVALID_REQUEST")
    private CalculationType calculationType;

    private Double ratePercent;
    private Double fixedAmount;
    private Double minAmount;
    private Double baseWeight;
    private Double basePrice;
    private Double stepWeight;
    private Double stepPrice;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
}
