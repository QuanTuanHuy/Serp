/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.response.admin;

import lombok.Builder;
import lombok.Value;
import serp.project.tms_billing_service.enums.CalculationType;
import serp.project.tms_billing_service.enums.VasRuleCode;

@Value
@Builder
public class VasRuleAdminResponse {
    Long id;
    VasRuleCode code;
    String name;
    CalculationType calculationType;
    Double ratePercent;
    Double fixedAmount;
    Double minAmount;
}
