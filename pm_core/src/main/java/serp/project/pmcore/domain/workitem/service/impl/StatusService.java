/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.pmcore.domain.workitem.service.impl;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.shared.util.TextNormalizationUtils;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workitem.dto.StatusUpdateData;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.port.IStatusCategoryPort;
import serp.project.pmcore.domain.workitem.service.IStatusService;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.port.IStatusPort;
import serp.project.pmcore.domain.workitem.query.StatusListCriteria;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusService implements IStatusService {
    private static final int STATUS_KEY_MAX_LENGTH = 100;
    private static final int STATUS_NAME_MAX_LENGTH = 255;
    private static final int STATUS_DESCRIPTION_MAX_LENGTH = 2000;
    private static final int STATUS_ICON_URL_MAX_LENGTH = 255;

    private final IStatusPort statusPort;
    private final IStatusCategoryPort statusCategoryPort;
    private final IWorkflowStepPort workflowStepPort;
    private final IWorkItemReadPort workItemReadPort;

    @Override
    public StatusEntity createStatus(StatusEntity status, Long tenantId, Long userId) {
        String statusKey = TextNormalizationUtils.normalizeRequiredText(
                status.getStatusKey(),
                "statusKey",
                STATUS_KEY_MAX_LENGTH
        );
        if (statusPort.existsByStatusKey(tenantId, statusKey)) {
            log.warn("Status key already exists: statusKey={}, tenantId={}", statusKey, tenantId);
            throw new BusinessRuleViolationException(DomainErrorCode.STATUS_KEY_ALREADY_EXISTS);
        }

        Long categoryId = requireVisibleStatusCategoryId(status.getCategoryId(), tenantId);

        status.setTenantId(tenantId);
        status.setStatusKey(statusKey);
        status.setName(TextNormalizationUtils.normalizeRequiredText(status.getName(), "name", STATUS_NAME_MAX_LENGTH));
        status.setDescription(TextNormalizationUtils.normalizeOptionalText(
                status.getDescription(),
                "description",
                STATUS_DESCRIPTION_MAX_LENGTH
        ));
        status.setIconUrl(normalizeOptionalIconUrl(status.getIconUrl()));
        status.setCategoryId(categoryId);
        status.setIsSystem(false);
        status.setDeletedAt(null);
        status.applyCreate(userId, System.currentTimeMillis());

        return statusPort.createStatus(status);
    }

    @Override
    public List<StatusEntity> getStatusesByTenantId(Long tenantId) {
        return statusPort.getStatusesByTenantIdIncludingSystem(tenantId);
    }

    @Override
    public PageResult<StatusEntity> listVisibleStatuses(Long tenantId, StatusListCriteria criteria) {
        return statusPort.listStatusesIncludingSystem(tenantId, criteria);
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
    public StatusEntity getVisibleStatusById(Long id, Long tenantId) {
        return statusPort.getStatusByIdIncludingSystem(id, tenantId)
                .orElseThrow(() -> {
                    log.error("Visible status not found: id={}, tenantId={}", id, tenantId);
                    return ResourceNotFoundException.status(id);
                });
    }

    @Override
    public StatusEntity updateStatus(Long id, StatusUpdateData data, Long tenantId, Long userId) {
        StatusEntity existing = getStatusById(id, tenantId);

        if (data.statusKeyProvided()) {
            String newStatusKey = TextNormalizationUtils.normalizeRequiredText(
                    data.statusKey(),
                    "statusKey",
                    STATUS_KEY_MAX_LENGTH
            );
            if (!newStatusKey.equalsIgnoreCase(existing.getStatusKey()) && statusPort.existsByStatusKey(tenantId, newStatusKey)) {
                log.warn("Status key already exists: statusKey={}, tenantId={}", newStatusKey, tenantId);
                throw new BusinessRuleViolationException(DomainErrorCode.STATUS_KEY_ALREADY_EXISTS);
            }
            existing.setStatusKey(newStatusKey);
        }

        if (data.nameProvided()) {
            existing.setName(TextNormalizationUtils.normalizeRequiredText(data.name(), "name", STATUS_NAME_MAX_LENGTH));
        }

        if (data.descriptionProvided()) {
            existing.setDescription(TextNormalizationUtils.normalizeOptionalText(
                    data.description(),
                    "description",
                    STATUS_DESCRIPTION_MAX_LENGTH
            ));
        }

        if (data.iconUrlProvided()) {
            existing.setIconUrl(normalizeOptionalIconUrl(data.iconUrl()));
        }

        if (data.statusCategoryIdProvided()) {
            existing.setCategoryId(requireVisibleStatusCategoryId(data.statusCategoryId(), tenantId));
        }

        existing.applyUpdate(userId, System.currentTimeMillis());
        statusPort.updateStatus(existing);
        return existing;
    }

    @Override
    public StatusEntity deleteStatus(Long id, Long tenantId, Long userId) {
        StatusEntity existing = getStatusById(id, tenantId);

        if (workflowStepPort.existsByStatusIdIncludingSystem(id, tenantId)) {
            throw new BusinessRuleViolationException(DomainErrorCode.STATUS_IN_USE_BY_WORKFLOW);
        }

        if (workItemReadPort.existsActiveWorkItemByStatusId(id, tenantId)) {
            throw new BusinessRuleViolationException(DomainErrorCode.STATUS_IN_USE_BY_WORK_ITEMS);
        }

        long now = System.currentTimeMillis();
        existing.setDeletedAt(now);
        existing.applyUpdate(userId, now);
        statusPort.updateStatus(existing);
        return existing;
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

    private Long requireVisibleStatusCategoryId(Long categoryId, Long tenantId) {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("statusCategoryId must be greater than 0");
        }
        return getStatusCategoryByIdIncludingSystem(categoryId, tenantId).getId();
    }

    private String normalizeOptionalIconUrl(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > STATUS_ICON_URL_MAX_LENGTH) {
            throw new IllegalArgumentException("iconUrl must be at most 255 characters");
        }

        try {
            URI uri = URI.create(trimmed);
            if (!uri.isAbsolute()) {
                throw new IllegalArgumentException("iconUrl must be a valid absolute URL");
            }
            return trimmed;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("iconUrl must be a valid absolute URL", ex);
        }
    }
}
