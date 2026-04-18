/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.pmcore.domain.workitem.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.port.IStatusCategoryPort;
import serp.project.pmcore.domain.workitem.service.IStatusService;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.port.IStatusPort;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusService implements IStatusService {
    private final IStatusPort statusPort;
    private final IStatusCategoryPort statusCategoryPort;

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

    @Override
    public StatusEntity getStatusById(Long id, Long tenantId) {
        return statusPort.getStatusById(id, tenantId)
                .orElseThrow(() -> {
                    log.error("Status not found: id={}, tenantId={}", id, tenantId);
                    return new ResourceNotFoundException(
                            DomainErrorCode.STATUS_NOT_FOUND,
                            String.format("Status not found: id=%s, tenantId=%s", id, tenantId)
                    );
                });
    }

    @Override
    public StatusCategoryEntity getStatusCategoryById(Long id, Long tenantId) {
        return statusCategoryPort.getStatusCategoryById(id, tenantId)
                .orElseThrow(() -> {
                    log.error("Status category not found: id={}, tenantId={}", id, tenantId);
                    return new ResourceNotFoundException(
                            DomainErrorCode.STATUS_CATEGORY_NOT_FOUND,
                            String.format("Status category not found: id=%s, tenantId=%s", id, tenantId)
                    );
                });
    }

    @Override
    public StatusCategoryEntity getStatusCategoryByIdIncludingSystem(Long id, Long tenantId) {
        return statusCategoryPort.getStatusCategoryByIdIncludingSystem(id, tenantId)
                .orElseThrow(() -> {
                    log.error("Visible status category not found: id={}, tenantId={}", id, tenantId);
                    return new ResourceNotFoundException(
                            DomainErrorCode.STATUS_CATEGORY_NOT_FOUND,
                            String.format("Status category not found: id=%s, tenantId=%s", id, tenantId)
                    );
                });
    }
}
