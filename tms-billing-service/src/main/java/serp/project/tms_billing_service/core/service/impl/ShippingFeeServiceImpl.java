/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.tms_billing_service.core.service.IShippingFeeService;
import serp.project.tms_billing_service.dto.request.CalculateShippingFeeRequest;
import serp.project.tms_billing_service.dto.response.CalculateShippingFeeResponse;

@Service
@RequiredArgsConstructor
public class ShippingFeeServiceImpl implements IShippingFeeService {
    private final ConfiguredDeliveryPricingCalculator pricingCalculator;

    /**
     * Tính phí bằng công thức chung; calculator sẽ chọn đúng cấu hình theo serviceCode trong request.
     */
    @Override
    public CalculateShippingFeeResponse calculateShippingFee(CalculateShippingFeeRequest request) {
        return pricingCalculator.calculate(request);
    }
}
