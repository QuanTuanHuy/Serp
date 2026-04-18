/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.shared.util.TextNormalizationUtils;
import serp.project.pmcore.domain.workitem.dto.StatusCategoryUpdateData;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.port.IStatusCategoryPort;
import serp.project.pmcore.domain.workitem.port.IStatusPort;
import serp.project.pmcore.domain.workitem.query.StatusCategoryListCriteria;
import serp.project.pmcore.domain.workitem.service.IStatusCategoryService;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusCategoryService implements IStatusCategoryService {

    private static final int NAME_MAX_LENGTH = 50;
    private static final int KEY_MAX_LENGTH = 50;
    private static final int COLOR_MAX_LENGTH = 50;

    private final IStatusCategoryPort statusCategoryPort;
    private final IStatusPort statusPort;

    @Override
    public StatusCategoryEntity createStatusCategory(StatusCategoryEntity statusCategory, Long tenantId, Long userId) {
        String key = TextNormalizationUtils.normalizeRequiredText(statusCategory.getKey(), "key", KEY_MAX_LENGTH);
        if (statusCategoryPort.existsByKey(tenantId, key)) {
            log.warn("Status category key already exists: key={}, tenantId={}", key, tenantId);
            throw new BusinessRuleViolationException(DomainErrorCode.STATUS_CATEGORY_KEY_ALREADY_EXISTS);
        }

        statusCategory.setTenantId(tenantId);
        statusCategory.setName(TextNormalizationUtils.normalizeRequiredText(
                statusCategory.getName(),
                "name",
                NAME_MAX_LENGTH
        ));
        statusCategory.setKey(key);
        statusCategory.setColor(TextNormalizationUtils.normalizeOptionalText(
                statusCategory.getColor(),
                "color",
                COLOR_MAX_LENGTH
        ));
        statusCategory.setIsSystem(false);
        statusCategory.setDeletedAt(null);
        statusCategory.applyCreate(userId, System.currentTimeMillis());

        return statusCategoryPort.createStatusCategory(statusCategory);
    }

    @Override
    public StatusCategoryEntity getStatusCategoryById(Long statusCategoryId, Long tenantId) {
        return statusCategoryPort.getStatusCategoryById(statusCategoryId, tenantId)
                .orElseThrow(() -> {
                    log.warn("Status category not found: id={}, tenantId={}", statusCategoryId, tenantId);
                    return ResourceNotFoundException.statusCategory(statusCategoryId);
                });
    }

    @Override
    public StatusCategoryEntity getVisibleStatusCategoryById(Long statusCategoryId, Long tenantId) {
        return statusCategoryPort.getStatusCategoryByIdIncludingSystem(statusCategoryId, tenantId)
                .orElseThrow(() -> {
                    log.warn("Visible status category not found: id={}, tenantId={}", statusCategoryId, tenantId);
                    return ResourceNotFoundException.statusCategory(statusCategoryId);
                });
    }

    @Override
    public PageResult<StatusCategoryEntity> listVisibleStatusCategories(Long tenantId, StatusCategoryListCriteria criteria) {
        return statusCategoryPort.listStatusCategoriesIncludingSystem(tenantId, criteria);
    }

    @Override
    public StatusCategoryEntity updateStatusCategory(Long statusCategoryId,
                                                     StatusCategoryUpdateData data,
                                                     Long tenantId,
                                                     Long userId) {
        StatusCategoryEntity existing = getStatusCategoryById(statusCategoryId, tenantId);

        if (data.nameProvided()) {
            existing.setName(TextNormalizationUtils.normalizeRequiredText(data.name(), "name", NAME_MAX_LENGTH));
        }

        if (data.keyProvided()) {
            String newKey = TextNormalizationUtils.normalizeRequiredText(data.key(), "key", KEY_MAX_LENGTH);
            if (!newKey.equalsIgnoreCase(existing.getKey()) && statusCategoryPort.existsByKey(tenantId, newKey)) {
                log.warn("Status category key already exists: key={}, tenantId={}", newKey, tenantId);
                throw new BusinessRuleViolationException(DomainErrorCode.STATUS_CATEGORY_KEY_ALREADY_EXISTS);
            }
            existing.setKey(newKey);
        }

        if (data.colorProvided()) {
            existing.setColor(TextNormalizationUtils.normalizeOptionalText(data.color(), "color", COLOR_MAX_LENGTH));
        }

        existing.applyUpdate(userId, System.currentTimeMillis());
        statusCategoryPort.updateStatusCategory(existing);
        return existing;
    }

    @Override
    public StatusCategoryEntity deleteStatusCategory(Long statusCategoryId, Long tenantId, Long userId) {
        StatusCategoryEntity existing = getStatusCategoryById(statusCategoryId, tenantId);
        if (statusPort.existsByCategoryId(statusCategoryId, tenantId)) {
            throw new BusinessRuleViolationException(DomainErrorCode.STATUS_CATEGORY_IN_USE);
        }

        long now = System.currentTimeMillis();
        existing.setDeletedAt(now);
        existing.applyUpdate(userId, now);
        statusCategoryPort.updateStatusCategory(existing);
        return existing;
    }

}
