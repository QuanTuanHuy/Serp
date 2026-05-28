/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service;

import serp.project.tms_billing_service.dto.request.CalculateShippingFeeRequest;
import serp.project.tms_billing_service.dto.response.CalculateShippingFeeResponse;
import serp.project.tms_billing_service.enums.DeliveryService;

public interface IDeliveryPricingStrategy {
    DeliveryService getSupportedService();

    CalculateShippingFeeResponse calculate(CalculateShippingFeeRequest request);
}
