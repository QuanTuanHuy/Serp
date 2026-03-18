/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.pmcore.domain.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.pmcore.domain.entity.StatusEntity;
import serp.project.pmcore.domain.port.store.IStatusPort;
import serp.project.pmcore.domain.service.IStatusService;

@Service
@RequiredArgsConstructor
public class StatusService implements IStatusService {
    private final IStatusPort statusPort;

    @Override
    public List<StatusEntity> getStatusesByTenantId(Long tenantId) {
        return statusPort.getStatusesByTenantId(tenantId);
    }

    @Override
    public List<StatusEntity> createStatuses(List<StatusEntity> statuses, Long tenantId, Long userId) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        List<StatusEntity> newStatuses = statuses.stream()
                .map(s -> StatusEntity.builder()
                        .tenantId(tenantId)
                        .name(s.getName())
                        .statusKey(s.getStatusKey())
                        .description(s.getDescription())
                        .iconUrl(s.getIconUrl())
                        .categoryId(s.getCategoryId())
                        .isSystem(false)
                        .createdAt(System.currentTimeMillis())
                        .createdBy(userId)
                        .updatedAt(System.currentTimeMillis())
                        .updatedBy(userId)
                        .build())
                .collect(Collectors.toList());
        return statusPort.createStatuses(newStatuses);
    }

}
