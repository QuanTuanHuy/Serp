/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.ui.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.tms_billing_service.core.service.IAdminPricingService;
import serp.project.tms_billing_service.dto.ApiResponse;
import serp.project.tms_billing_service.dto.request.admin.UpsertSurchargeRuleRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertTariffRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertVasRuleRequest;
import serp.project.tms_billing_service.dto.response.admin.SurchargeRuleAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.TariffAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.VasRuleAdminResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/pricing")
public class AdminPricingController {
    private final IAdminPricingService adminPricingService;

    @PutMapping("/tariffs")
    public ApiResponse<TariffAdminResponse> upsertTariff(@RequestBody @Valid UpsertTariffRequest request) {
        return ApiResponse.<TariffAdminResponse>builder()
                .message("OK")
                .result(adminPricingService.upsertTariff(request))
                .build();
    }

    @PutMapping("/surcharge-rules")
    public ApiResponse<SurchargeRuleAdminResponse> upsertSurchargeRule(
            @RequestBody @Valid UpsertSurchargeRuleRequest request
    ) {
        return ApiResponse.<SurchargeRuleAdminResponse>builder()
                .message("OK")
                .result(adminPricingService.upsertSurchargeRule(request))
                .build();
    }

    @PutMapping("/vas-rules")
    public ApiResponse<VasRuleAdminResponse> upsertVasRule(@RequestBody @Valid UpsertVasRuleRequest request) {
        return ApiResponse.<VasRuleAdminResponse>builder()
                .message("OK")
                .result(adminPricingService.upsertVasRule(request))
                .build();
    }
}
