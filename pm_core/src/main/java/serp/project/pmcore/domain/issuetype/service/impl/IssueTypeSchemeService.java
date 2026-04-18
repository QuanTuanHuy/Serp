/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import serp.project.pmcore.domain.issuetype.dto.IssueTypeSchemeUpdateData;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemePort;
import serp.project.pmcore.domain.issuetype.query.IssueTypeSchemeListCriteria;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.entity.BaseEntity;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueTypeSchemeService implements IIssueTypeSchemeService {

    private final IIssueTypeSchemePort issueTypeSchemePort;
    private final IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    private final IIssueTypePort issueTypePort;
    private final IProjectReadPort projectReadPort;
    private final IWorkItemReadPort workItemReadPort;

    @Override
    public IssueTypeSchemeEntity createIssueTypeScheme(IssueTypeSchemeEntity scheme, Long tenantId, Long userId) {
        String name = normalizeRequiredText(scheme.getName(), "name", 255);
        if (issueTypeSchemePort.existsByName(tenantId, name)) {
            throw new BusinessRuleViolationException(DomainErrorCode.ISSUE_TYPE_SCHEME_NAME_ALREADY_EXISTS);
        }

        Long defaultIssueTypeId = requireVisibleIssueTypeId(scheme.getDefaultIssueTypeId(), tenantId);
        long now = System.currentTimeMillis();

        scheme.setTenantId(tenantId);
        scheme.setName(name);
        scheme.setDescription(normalizeOptionalText(scheme.getDescription(), 2000, "description"));
        scheme.setDefaultIssueTypeId(defaultIssueTypeId);
        scheme.setDeletedAt(null);
        scheme.setItems(List.of());
        scheme.applyCreate(userId, now);
        return issueTypeSchemePort.createIssueTypeScheme(scheme);
    }

    @Override
    public IssueTypeSchemeEntity getIssueTypeSchemeById(Long schemeId, Long tenantId) {
        return issueTypeSchemePort.getIssueTypeSchemeById(schemeId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.issueTypeScheme(schemeId));
    }

    @Override
    public IssueTypeSchemeEntity getVisibleIssueTypeSchemeById(Long schemeId, Long tenantId) {
        return issueTypeSchemePort.getIssueTypeSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.issueTypeScheme(schemeId));
    }

    @Override
    public IssueTypeSchemeEntity getVisibleIssueTypeSchemeDetailById(Long schemeId, Long tenantId) {
        IssueTypeSchemeEntity scheme = getVisibleIssueTypeSchemeById(schemeId, tenantId);
        scheme.setItems(issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeIdIncludingSystem(schemeId, tenantId));
        return scheme;
    }

    @Override
    public PageResult<IssueTypeSchemeEntity> listVisibleIssueTypeSchemes(Long tenantId, IssueTypeSchemeListCriteria criteria) {
        return issueTypeSchemePort.listIssueTypeSchemesIncludingSystem(tenantId, criteria);
    }

    @Override
    public IssueTypeSchemeEntity updateIssueTypeScheme(Long schemeId,
                                                       IssueTypeSchemeUpdateData data,
                                                       Long tenantId,
                                                       Long userId) {
        IssueTypeSchemeEntity existing = getIssueTypeSchemeById(schemeId, tenantId);

        if (data.nameProvided()) {
            String newName = normalizeRequiredText(data.name(), "name", 255);
            if (!newName.equalsIgnoreCase(existing.getName())
                    && issueTypeSchemePort.existsByName(tenantId, newName)) {
                throw new BusinessRuleViolationException(DomainErrorCode.ISSUE_TYPE_SCHEME_NAME_ALREADY_EXISTS);
            }
            existing.setName(newName);
        }

        if (data.descriptionProvided()) {
            existing.setDescription(normalizeOptionalText(data.description(), 2000, "description"));
        }

        if (data.defaultIssueTypeIdProvided()) {
            Long defaultIssueTypeId = requireVisibleIssueTypeId(data.defaultIssueTypeId(), tenantId);
            List<IssueTypeSchemeItemEntity> items = issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(schemeId, tenantId);
            if (!items.isEmpty() && items.stream().noneMatch(item -> Objects.equals(item.getIssueTypeId(), defaultIssueTypeId))) {
                throw new BusinessRuleViolationException(DomainErrorCode.ISSUE_TYPE_SCHEME_DEFAULT_NOT_IN_ITEMS);
            }
            existing.setDefaultIssueTypeId(defaultIssueTypeId);
        }

        existing.applyUpdate(userId, System.currentTimeMillis());
        issueTypeSchemePort.updateIssueTypeScheme(existing);
        return existing;
    }

    @Override
    public IssueTypeSchemeEntity deleteIssueTypeScheme(Long schemeId, Long tenantId, Long userId) {
        IssueTypeSchemeEntity existing = getIssueTypeSchemeById(schemeId, tenantId);
        if (projectReadPort.existsActiveProjectByIssueTypeSchemeId(schemeId, tenantId)) {
            throw new BusinessRuleViolationException(DomainErrorCode.ISSUE_TYPE_SCHEME_BOUND_TO_PROJECT);
        }

        long now = System.currentTimeMillis();
        existing.setDeletedAt(now);
        existing.applyUpdate(userId, now);
        issueTypeSchemePort.updateIssueTypeScheme(existing);
        issueTypeSchemeItemPort.deleteIssueTypeSchemeItemsBySchemeId(schemeId, tenantId);
        existing.setItems(List.of());
        return existing;
    }

    @Override
    public IssueTypeSchemeEntity replaceIssueTypeSchemeItems(Long schemeId,
                                                             List<Long> issueTypeIds,
                                                             Long tenantId,
                                                             Long userId) {
        IssueTypeSchemeEntity existing = getIssueTypeSchemeById(schemeId, tenantId);

        List<Long> normalizedIssueTypeIds = normalizeIssueTypeIds(issueTypeIds);
        requireVisibleIssueTypeIds(normalizedIssueTypeIds, tenantId);
        if (normalizedIssueTypeIds.stream().noneMatch(id -> Objects.equals(id, existing.getDefaultIssueTypeId()))) {
            throw new BusinessRuleViolationException(DomainErrorCode.ISSUE_TYPE_SCHEME_DEFAULT_NOT_IN_ITEMS);
        }

        List<IssueTypeSchemeItemEntity> currentItems = issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(schemeId, tenantId);
        Set<Long> requestedIds = new LinkedHashSet<>(normalizedIssueTypeIds);
        Set<Long> removedIssueTypeIds = new LinkedHashSet<>();
        for (IssueTypeSchemeItemEntity currentItem : currentItems) {
            if (!requestedIds.contains(currentItem.getIssueTypeId())) {
                removedIssueTypeIds.add(currentItem.getIssueTypeId());
            }
        }
        validateRemovedIssueTypesNotInUse(schemeId, tenantId, removedIssueTypeIds);

        long now = System.currentTimeMillis();
        issueTypeSchemeItemPort.deleteIssueTypeSchemeItemsBySchemeId(schemeId, tenantId);

        List<IssueTypeSchemeItemEntity> replacementItems = new ArrayList<>(normalizedIssueTypeIds.size());
        for (int i = 0; i < normalizedIssueTypeIds.size(); i++) {
            IssueTypeSchemeItemEntity item = IssueTypeSchemeItemEntity.builder()
                    .tenantId(tenantId)
                    .schemeId(schemeId)
                    .issueTypeId(normalizedIssueTypeIds.get(i))
                    .sequence(i + 1)
                    .createdAt(now)
                    .createdBy(userId)
                    .updatedAt(now)
                    .updatedBy(userId)
                    .build();
            replacementItems.add(item);
        }

        List<IssueTypeSchemeItemEntity> savedItems = issueTypeSchemeItemPort.createIssueTypeSchemeItems(replacementItems);
        existing.setItems(savedItems);
        existing.applyUpdate(userId, now);
        issueTypeSchemePort.updateIssueTypeScheme(existing);
        return existing;
    }

    @Override
    public void validateIssueTypeInScheme(Long schemeId, Long issueTypeId, Long tenantId) {
        boolean exists = issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeIdIncludingSystem(schemeId, tenantId)
                .stream()
                .anyMatch(item -> Objects.equals(item.getIssueTypeId(), issueTypeId));
        if (!exists) {
            throw new BusinessRuleViolationException(DomainErrorCode.ISSUE_TYPE_NOT_IN_SCHEME);
        }
    }

    private Long requireVisibleIssueTypeId(Long issueTypeId, Long tenantId) {
        if (issueTypeId == null || issueTypeId <= 0) {
            throw new IllegalArgumentException("defaultIssueTypeId must be greater than 0");
        }
        return issueTypePort.getIssueTypeByIdIncludingSystem(issueTypeId, tenantId)
                .map(BaseEntity::getId)
                .orElseThrow(() -> ResourceNotFoundException.issueType(issueTypeId));
    }

    private List<Long> normalizeIssueTypeIds(List<Long> issueTypeIds) {
        if (issueTypeIds == null || issueTypeIds.isEmpty()) {
            throw new IllegalArgumentException("issueTypeIds must not be empty");
        }

        List<Long> normalized = new ArrayList<>(issueTypeIds.size());
        Set<Long> uniqueIds = new LinkedHashSet<>();
        for (Long issueTypeId : issueTypeIds) {
            if (issueTypeId == null || issueTypeId <= 0) {
                throw new IllegalArgumentException("issueTypeIds must contain only positive values");
            }
            if (!uniqueIds.add(issueTypeId)) {
                throw new IllegalArgumentException("issueTypeIds must not contain duplicates");
            }
            normalized.add(issueTypeId);
        }
        return normalized;
    }

    private void validateRemovedIssueTypesNotInUse(Long schemeId, Long tenantId, Set<Long> removedIssueTypeIds) {
        if (removedIssueTypeIds.isEmpty()) {
            return;
        }

        List<Long> projectIds = projectReadPort.getActiveProjectIdsByIssueTypeSchemeId(schemeId, tenantId);
        if (projectIds.isEmpty()) {
            return;
        }

        List<Long> inUseIssueTypeIds = workItemReadPort.getActiveIssueTypeIdsInUseByProjectIds(
                tenantId,
                projectIds,
                List.copyOf(removedIssueTypeIds)
        );
        if (!inUseIssueTypeIds.isEmpty()) {
            Long issueTypeId = inUseIssueTypeIds.getFirst();
            throw new BusinessRuleViolationException(
                    DomainErrorCode.ISSUE_TYPE_SCHEME_IN_USE,
                    "Cannot remove issue type from scheme because active work items still reference it: issueTypeId="
                            + issueTypeId
            );
        }
    }

    private void requireVisibleIssueTypeIds(List<Long> issueTypeIds, Long tenantId) {
        List<IssueTypeEntity> visibleIssueTypes = issueTypePort.getIssueTypesByIdsIncludingSystem(issueTypeIds, tenantId);
        Map<Long, IssueTypeEntity> issueTypesById = visibleIssueTypes.stream()
                .collect(Collectors.toMap(IssueTypeEntity::getId, Function.identity()));

        for (Long issueTypeId : issueTypeIds) {
            if (!issueTypesById.containsKey(issueTypeId)) {
                throw ResourceNotFoundException.issueType(issueTypeId);
            }
        }
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

    private String normalizeOptionalText(String value, int maxLength, String fieldName) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be at most " + maxLength + " characters");
        }
        return trimmed;
    }

}
