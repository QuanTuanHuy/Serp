/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.response.admin;

import lombok.Builder;
import lombok.Value;
import serp.project.tms_billing_service.enums.DeliveryService;

@Value
@Builder
public class ChargeableWeightConfigAdminResponse {
    Long id;
    DeliveryService serviceCode;
    Long minDimensionCm;
    Long smallBulkyThresholdCm;
    Long baseWeightGram;
    Long stepWeightGram;
    Long maxWeightGram;
    Double volumetricDivisor;
}
