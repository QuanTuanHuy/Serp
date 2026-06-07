/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.response;

import lombok.Builder;
import lombok.Value;
import serp.project.tms_billing_service.enums.DeliveryService;
import serp.project.tms_billing_service.enums.RouteType;

import java.util.List;

@Value
@Builder
public class CalculateShippingFeeResponse {
    DeliveryService serviceCode;
    RouteType routeType;
    Long chargeableWeightGram;
    Long baseFee;
    Long surchargeFee;
    Long vasFee;
    Long totalFee;
    List<FeeLineItemResponse> feeItems;
}
