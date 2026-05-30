/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.response.admin;

import lombok.Builder;
import lombok.Value;
import serp.project.tms_billing_service.enums.CalculationType;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;

import java.time.LocalDate;

@Value
@Builder
public class SurchargeRuleAdminResponse {
    Long id;
    SurchargeRuleEnum code;
    String name;
    CalculationType calculationType;
    Double ratePercent;
    Double fixedAmount;
    Double minAmount;
    Double baseWeight;
    Double basePrice;
    Double stepWeight;
    Double stepPrice;
    LocalDate effectiveDate;
    LocalDate expirationDate;
}
