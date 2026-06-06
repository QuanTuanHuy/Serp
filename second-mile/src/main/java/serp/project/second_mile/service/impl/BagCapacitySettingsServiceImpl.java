/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.second_mile.domain.BagCapacitySettings;
import serp.project.second_mile.dto.request.UpdateBagCapacitySettingsRequest;
import serp.project.second_mile.dto.response.BagCapacitySettingsResponse;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.repository.BagCapacitySettingsRepository;
import serp.project.second_mile.service.BagCapacitySettingsService;

@Service
@RequiredArgsConstructor
public class BagCapacitySettingsServiceImpl implements BagCapacitySettingsService {
    private static final long SYSTEM_DEFAULT_TENANT_ID = 0L;

    private final BagCapacitySettingsRepository bagCapacitySettingsRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;

    @Override
    @Transactional(readOnly = true)
    public BagCapacitySettingsResponse getCurrentSettings() {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        return getSettingsForTenant(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public BagCapacitySettingsResponse getSettingsForTenant(Long tenantId) {
        if (tenantId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return bagCapacitySettingsRepository.findByTenantId(tenantId)
                .or(() -> bagCapacitySettingsRepository.findByTenantId(SYSTEM_DEFAULT_TENANT_ID))
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BagCapacitySettingsResponse updateCurrentSettings(UpdateBagCapacitySettingsRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        validateRequest(request);

        BagCapacitySettings settings = bagCapacitySettingsRepository.findByTenantId(tenantId)
                .orElseGet(BagCapacitySettings::new);
        settings.setMaxWeight(request.getMaxWeight());
        settings.setMaxVolume(request.getMaxVolume());
        settings.setMaxOrders(request.getMaxOrders());
        settings.setTenantId(tenantId);

        return toResponse(bagCapacitySettingsRepository.save(settings));
    }

    private void validateRequest(UpdateBagCapacitySettingsRequest request) {
        if (request == null
                || request.getMaxWeight() == null
                || request.getMaxWeight() <= 0
                || request.getMaxVolume() == null
                || request.getMaxVolume() <= 0
                || request.getMaxOrders() == null
                || request.getMaxOrders() <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private BagCapacitySettingsResponse toResponse(BagCapacitySettings settings) {
        return new BagCapacitySettingsResponse(
                settings.getId(),
                settings.getMaxWeight(),
                settings.getMaxVolume(),
                settings.getMaxOrders()
        );
    }
}
