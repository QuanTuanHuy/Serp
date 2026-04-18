/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.service.impl;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigSchemeItemPort;
import serp.project.pmcore.domain.issuetype.dto.IssueTypeUpdateData;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.issuetype.query.IssueTypeListCriteria;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemePort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueTypeService implements IIssueTypeService {

    private final IIssueTypePort issueTypePort;
    private final IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    private final IIssueTypeSchemePort issueTypeSchemePort;
    private final IIssueTypeScreenSchemeItemPort issueTypeScreenSchemeItemPort;
    private final IFieldConfigSchemeItemPort fieldConfigSchemeItemPort;
    private final IWorkflowSchemeItemPort workflowSchemeItemPort;
    private final IWorkItemReadPort workItemReadPort;

    @Override
    public IssueTypeEntity createIssueType(IssueTypeEntity issueType, Long tenantId, Long userId) {
        String typeKey = normalizeRequiredText(issueType.getTypeKey(), "typeKey", 100);
        if (issueTypePort.existsByTypeKey(tenantId, typeKey)) {
            log.warn(String.format("Issue type key already exists: typeKey=%s, tenantId=%s", typeKey, tenantId));
            throw new BusinessRuleViolationException(DomainErrorCode.ISSUE_TYPE_KEY_ALREADY_EXISTS);
        }

        issueType.setTenantId(tenantId);
        issueType.setTypeKey(typeKey);
        issueType.setName(normalizeRequiredText(issueType.getName(), "name", 255));
        issueType.setDescription(normalizeOptionalText(issueType.getDescription(), 2000));
        issueType.setIconUrl(normalizeOptionalUrl(issueType.getIconUrl()));
        issueType.setHierarchyLevel(validateHierarchyLevel(issueType.getHierarchyLevel()));
        issueType.setSystem(false);
        issueType.setDeletedAt(null);
        issueType.applyCreate(userId, System.currentTimeMillis());

        return issueTypePort.createIssueType(issueType);
    }

    @Override
    public IssueTypeEntity getIssueTypeById(Long issueTypeId, Long tenantId) {
        return issueTypePort.getIssueTypeById(issueTypeId, tenantId)
                .orElseThrow(() -> {
                    log.error("Issue type not found: id={}", issueTypeId);
                    return ResourceNotFoundException.issueType(issueTypeId);
                });
    }

    @Override
    public IssueTypeEntity getVisibleIssueTypeById(Long issueTypeId, Long tenantId) {
        return issueTypePort.getIssueTypeByIdIncludingSystem(issueTypeId, tenantId)
                .orElseThrow(() -> {
                    log.warn("Issue type not found in visible scope: id={}, tenantId={}", issueTypeId, tenantId);
                    return ResourceNotFoundException.issueType(issueTypeId);
                });
    }

    @Override
    public List<IssueTypeEntity> getVisibleIssueTypesByIds(List<Long> issueTypeIds, Long tenantId) {
        if (issueTypeIds == null || issueTypeIds.isEmpty()) {
            return List.of();
        }

        List<Long> normalizedIds = issueTypeIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (normalizedIds.size() != new LinkedHashSet<>(issueTypeIds).size()) {
            throw new IllegalArgumentException("issueTypeIds must contain only positive distinct values");
        }

        List<IssueTypeEntity> issueTypes = issueTypePort.getIssueTypesByIdsIncludingSystem(normalizedIds, tenantId);
        Map<Long, IssueTypeEntity> issueTypesById = issueTypes.stream()
                .collect(Collectors.toMap(IssueTypeEntity::getId, Function.identity()));

        for (Long issueTypeId : normalizedIds) {
            if (!issueTypesById.containsKey(issueTypeId)) {
                throw ResourceNotFoundException.issueType(issueTypeId);
            }
        }

        return normalizedIds.stream()
                .map(issueTypesById::get)
                .toList();
    }

    @Override
    public PageResult<IssueTypeEntity> listVisibleIssueTypes(Long tenantId, IssueTypeListCriteria criteria) {
        return issueTypePort.listIssueTypesIncludingSystem(tenantId, criteria);
    }

    @Override
    public IssueTypeEntity updateIssueType(Long issueTypeId, IssueTypeUpdateData data, Long tenantId, Long userId) {
        IssueTypeEntity existing = getIssueTypeById(issueTypeId, tenantId);
        if (existing.isSystem()) {
            throw new BusinessRuleViolationException(DomainErrorCode.ISSUE_TYPE_IS_SYSTEM);
        }

        if (data.nameProvided()) {
            existing.setName(normalizeRequiredText(data.name(), "name", 255));
        }
        if (data.descriptionProvided()) {
            existing.setDescription(normalizeOptionalText(data.description(), 2000));
        }
        if (data.iconUrlProvided()) {
            existing.setIconUrl(normalizeOptionalUrl(data.iconUrl()));
        }
        if (data.hierarchyLevelProvided()) {
            existing.setHierarchyLevel(validateHierarchyLevel(data.hierarchyLevel()));
        }

        existing.applyUpdate(userId, System.currentTimeMillis());
        issueTypePort.updateIssueType(existing);
        return existing;
    }

    @Override
    public IssueTypeEntity deleteIssueType(Long issueTypeId, Long tenantId, Long userId) {
        IssueTypeEntity existing = getIssueTypeById(issueTypeId, tenantId);
        if (existing.isSystem()) {
            throw new BusinessRuleViolationException(DomainErrorCode.ISSUE_TYPE_IS_SYSTEM);
        }
        if (isIssueTypeInUse(issueTypeId, tenantId)) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.ISSUE_TYPE_IN_USE,
                    "Issue type is still referenced by active work items or tenant configuration: id=" + issueTypeId
            );
        }

        long now = System.currentTimeMillis();
        existing.setDeletedAt(now);
        existing.applyUpdate(userId, now);
        issueTypePort.updateIssueType(existing);
        return existing;
    }

    private boolean isIssueTypeInUse(Long issueTypeId, Long tenantId) {
        return !workItemReadPort.getWorkItemsByIssueTypeId(issueTypeId, tenantId).isEmpty()
                || issueTypeSchemeItemPort.existsByIssueTypeId(issueTypeId, tenantId)
                || issueTypeSchemePort.existsByDefaultIssueTypeId(issueTypeId, tenantId)
                || issueTypeScreenSchemeItemPort.existsByIssueTypeId(issueTypeId, tenantId)
                || fieldConfigSchemeItemPort.existsByIssueTypeId(issueTypeId, tenantId)
                || workflowSchemeItemPort.existsByIssueTypeId(issueTypeId, tenantId);
    }

    private String normalizeRequiredText(String value, String fieldName, int maxLength) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be at most " + maxLength + " characters");
        }
        return trimmed;
    }

    private String normalizeOptionalText(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException("description must be at most " + maxLength + " characters");
        }
        return trimmed;
    }

    private String normalizeOptionalUrl(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 255) {
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

    private Integer validateHierarchyLevel(Integer hierarchyLevel) {
        if (hierarchyLevel == null) {
            throw new IllegalArgumentException("hierarchyLevel is required");
        }
        if (hierarchyLevel < 0 || hierarchyLevel > 2) {
            throw new IllegalArgumentException("hierarchyLevel must be one of 0, 1, or 2");
        }
        return hierarchyLevel;
    }
}
