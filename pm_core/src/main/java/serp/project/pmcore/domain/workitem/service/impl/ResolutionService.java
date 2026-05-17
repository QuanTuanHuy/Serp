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
import serp.project.pmcore.domain.workitem.dto.ResolutionUpdateData;
import serp.project.pmcore.domain.workitem.entity.ResolutionEntity;
import serp.project.pmcore.domain.workitem.port.IResolutionPort;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.query.ResolutionListCriteria;
import serp.project.pmcore.domain.workitem.service.IResolutionService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResolutionService implements IResolutionService {

    private static final int NAME_MAX_LENGTH = 255;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;

    private final IResolutionPort resolutionPort;
    private final IWorkItemReadPort workItemReadPort;

    @Override
    public ResolutionEntity createResolution(ResolutionEntity resolution, Long tenantId, Long userId) {
        String normalizedName = TextNormalizationUtils.normalizeRequiredText(
                resolution.getName(),
                "name",
                NAME_MAX_LENGTH
        );
        ensureVisibleNameNotTaken(tenantId, normalizedName, null);

        resolution.setTenantId(tenantId);
        resolution.setName(normalizedName);
        resolution.setDescription(TextNormalizationUtils.normalizeOptionalText(
                resolution.getDescription(),
                "description",
                DESCRIPTION_MAX_LENGTH
        ));
        resolution.setSequence(validateSequence(resolution.getSequence()));
        resolution.setIsSystem(false);
        resolution.setDeletedAt(null);
        resolution.applyCreate(userId, System.currentTimeMillis());
        return resolutionPort.createResolution(resolution);
    }

    @Override
    public ResolutionEntity getResolutionById(Long resolutionId, Long tenantId) {
        return resolutionPort.getResolutionById(resolutionId, tenantId)
                .orElseThrow(() -> {
                    log.warn("Resolution not found: id={}, tenantId={}", resolutionId, tenantId);
                    return ResourceNotFoundException.resolution(resolutionId);
                });
    }

    @Override
    public ResolutionEntity getVisibleResolutionById(Long resolutionId, Long tenantId) {
        return resolutionPort.getResolutionByIdIncludingSystem(resolutionId, tenantId)
                .orElseThrow(() -> {
                    log.warn("Visible resolution not found: id={}, tenantId={}", resolutionId, tenantId);
                    return ResourceNotFoundException.resolution(resolutionId);
                });
    }

    @Override
    public PageResult<ResolutionEntity> listVisibleResolutions(Long tenantId, ResolutionListCriteria criteria) {
        return resolutionPort.listResolutionsIncludingSystem(tenantId, criteria);
    }

    @Override
    public ResolutionEntity updateResolution(Long resolutionId, ResolutionUpdateData data, Long tenantId, Long userId) {
        ResolutionEntity existing = getResolutionById(resolutionId, tenantId);
        if (Boolean.TRUE.equals(existing.getIsSystem())) {
            throw new BusinessRuleViolationException(DomainErrorCode.RESOLUTION_IS_SYSTEM);
        }

        if (data.nameProvided()) {
            String normalizedName = TextNormalizationUtils.normalizeRequiredText(data.name(), "name", NAME_MAX_LENGTH);
            ensureVisibleNameNotTaken(tenantId, normalizedName, existing.getId());
            existing.setName(normalizedName);
        }

        if (data.descriptionProvided()) {
            existing.setDescription(TextNormalizationUtils.normalizeOptionalText(
                    data.description(),
                    "description",
                    DESCRIPTION_MAX_LENGTH
            ));
        }

        if (data.sequenceProvided()) {
            existing.setSequence(validateSequence(data.sequence()));
        }

        existing.applyUpdate(userId, System.currentTimeMillis());
        resolutionPort.updateResolution(existing);
        return existing;
    }

    @Override
    public ResolutionEntity deleteResolution(Long resolutionId, Long tenantId, Long userId) {
        ResolutionEntity existing = getResolutionById(resolutionId, tenantId);
        if (Boolean.TRUE.equals(existing.getIsSystem())) {
            throw new BusinessRuleViolationException(DomainErrorCode.RESOLUTION_IS_SYSTEM);
        }
        if (!workItemReadPort.getWorkItemsByResolutionId(resolutionId, tenantId).isEmpty()) {
            throw new BusinessRuleViolationException(DomainErrorCode.RESOLUTION_IN_USE);
        }

        long now = System.currentTimeMillis();
        existing.setDeletedAt(now);
        existing.applyUpdate(userId, now);
        resolutionPort.updateResolution(existing);
        return existing;
    }

    private void ensureVisibleNameNotTaken(Long tenantId, String name, Long currentId) {
        resolutionPort.getResolutionByNameIncludingSystem(tenantId, name)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    log.warn("Resolution name already exists: tenantId={}, name={}", tenantId, name);
                    throw new BusinessRuleViolationException(DomainErrorCode.CONFLICT, "Resolution name already exists");
                });
    }

    private Integer validateSequence(Integer sequence) {
        if (sequence == null) {
            throw new IllegalArgumentException("sequence is required");
        }
        if (sequence < 0) {
            throw new IllegalArgumentException("sequence must be greater than or equal to 0");
        }
        return sequence;
    }
}
