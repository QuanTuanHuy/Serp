/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.domain.constant.TenantConstants;
import serp.project.pmcore.domain.entity.StatusEntity;
import serp.project.pmcore.domain.service.IStatusService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingUseCase {

    private final IStatusService statusService;

    @Transactional
    public void onboardNewTenant(Long tenantId, Long userId) {
        cloneDefaultStatuses(tenantId, userId);
    }

    private void cloneDefaultStatuses(Long tenantId, Long userId) {
        List<StatusEntity> existingStatuses = statusService.getStatusesByTenantId(tenantId);
        if (!existingStatuses.isEmpty()) {
            log.info("Tenant {} already has statuses, skipping cloning default statuses", tenantId);
            return;
        }

        List<StatusEntity> defaultStatuses = getDefaultStatuses();
        if (defaultStatuses.isEmpty()) {
            log.info("No default statuses found to clone for tenant {}", tenantId);
            return;
        }
        statusService.createStatuses(defaultStatuses, tenantId, userId);
        log.info("Cloned {} default statuses for tenant {}", defaultStatuses.size(), tenantId);
    }

    private List<StatusEntity> getDefaultStatuses() {
        return statusService.getStatusesByTenantId(TenantConstants.SYSTEM_TENANT_ID);
    }

}
