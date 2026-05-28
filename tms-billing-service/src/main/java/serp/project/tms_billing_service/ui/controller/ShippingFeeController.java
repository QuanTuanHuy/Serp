/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.tms_billing_service.core.service.IShippingFeeService;
import serp.project.tms_billing_service.dto.ApiResponse;
import serp.project.tms_billing_service.dto.request.CalculateShippingFeeRequest;
import serp.project.tms_billing_service.dto.response.CalculateShippingFeeResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shipping-fees")
public class ShippingFeeController {
    private final IShippingFeeService shippingFeeService;

    @PostMapping("/calculate")
    public ApiResponse<CalculateShippingFeeResponse> calculate(@RequestBody @Valid CalculateShippingFeeRequest request) {
        return ApiResponse.<CalculateShippingFeeResponse>builder()
                .result(shippingFeeService.calculateShippingFee(request))
                .message("OK")
                .build();
    }
}
