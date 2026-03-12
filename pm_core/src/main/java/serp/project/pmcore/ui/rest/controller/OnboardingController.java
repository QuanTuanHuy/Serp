/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import serp.project.pmcore.core.domain.constant.RestControllerConstants;
import serp.project.pmcore.core.domain.dto.response.GeneralResponse;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.core.usecase.OnboardingUseCase;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.kernel.utils.ResponseUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping(RestControllerConstants.ONBOARDING)
public class OnboardingController {
    private final OnboardingUseCase onboardingUseCase;

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @PostMapping
    public ResponseEntity<GeneralResponse<?>> onboardNewTenant() {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        onboardingUseCase.onboardNewTenant(tenantId, userId);
        return ResponseEntity.ok(responseUtils.success("Tenant onboarding completed successfully"));
    }
}
