/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service;

import serp.project.tms_billing_service.dto.request.CalculateShippingFeeRequest;
import serp.project.tms_billing_service.dto.response.CalculateShippingFeeResponse;

public interface IShippingFeeService {
    CalculateShippingFeeResponse calculateShippingFee(CalculateShippingFeeRequest request);
}
