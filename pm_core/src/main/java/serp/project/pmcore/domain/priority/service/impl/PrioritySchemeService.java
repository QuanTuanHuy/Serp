/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.priority.dto.PrioritySchemeUpdateData;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.port.IPriorityPort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemePort;
import serp.project.pmcore.domain.priority.query.PrioritySchemeListCriteria;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
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
public class PrioritySchemeService implements IPrioritySchemeService {

    private final IPrioritySchemePort prioritySchemePort;
    private final IPrioritySchemeItemPort prioritySchemeItemPort;
    private final IPriorityPort priorityPort;
    private final IProjectReadPort projectReadPort;
    private final IWorkItemReadPort workItemReadPort;

    @Override
    public PrioritySchemeEntity createPriorityScheme(PrioritySchemeEntity scheme, Long tenantId, Long userId) {
        String name = normalizeRequiredText(scheme.getName(), "name", 255);
        if (prioritySchemePort.existsByName(tenantId, name)) {
            log.warn("[PrioritySchemeService] Priority Scheme with name {} already exists", name);
            throw new BusinessRuleViolationException(DomainErrorCode.PRIORITY_SCHEME_NAME_ALREADY_EXISTS);
        }

        Long defaultPriorityId = requireVisiblePriorityId(scheme.getDefaultPriorityId(), tenantId);
        long now = System.currentTimeMillis();

        scheme.setTenantId(tenantId);
        scheme.setName(name);
        scheme.setDescription(normalizeOptionalText(scheme.getDescription(), 2000, "description"));
        scheme.setDefaultPriorityId(defaultPriorityId);
        scheme.setDeletedAt(null);
        scheme.setItems(List.of());
        scheme.applyCreate(userId, now);
        return prioritySchemePort.createPriorityScheme(scheme);
    }

    @Override
    public PrioritySchemeEntity getPrioritySchemeById(Long prioritySchemeId, Long tenantId) {
        return prioritySchemePort.getPrioritySchemeById(prioritySchemeId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.priorityScheme(prioritySchemeId));
    }

    @Override
    public PrioritySchemeEntity getVisiblePrioritySchemeById(Long prioritySchemeId, Long tenantId) {
        return prioritySchemePort.getPrioritySchemeByIdIncludingSystem(prioritySchemeId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.priorityScheme(prioritySchemeId));
    }

    @Override
    public PrioritySchemeEntity getVisiblePrioritySchemeDetailById(Long prioritySchemeId, Long tenantId) {
        PrioritySchemeEntity scheme = getVisiblePrioritySchemeById(prioritySchemeId, tenantId);
        scheme.setItems(prioritySchemeItemPort.getPrioritySchemeItemsBySchemeIdIncludingSystem(prioritySchemeId, tenantId));
        return scheme;
    }

    @Override
    public PageResult<PrioritySchemeEntity> listVisiblePrioritySchemes(Long tenantId, PrioritySchemeListCriteria criteria) {
        return prioritySchemePort.listPrioritySchemesIncludingSystem(tenantId, criteria);
    }

    @Override
    public PrioritySchemeEntity updatePriorityScheme(Long prioritySchemeId,
                                                     PrioritySchemeUpdateData data,
                                                     Long tenantId,
                                                     Long userId) {
        PrioritySchemeEntity existing = getPrioritySchemeById(prioritySchemeId, tenantId);

        if (data.nameProvided()) {
            String newName = normalizeRequiredText(data.name(), "name", 255);
            if (!newName.equalsIgnoreCase(existing.getName()) && prioritySchemePort.existsByName(tenantId, newName)) {
                log.warn("[PrioritySchemeService] Priority Scheme with name {} already exists", newName);
                throw new BusinessRuleViolationException(DomainErrorCode.PRIORITY_SCHEME_NAME_ALREADY_EXISTS);
            }
            existing.setName(newName);
        }

        if (data.descriptionProvided()) {
            existing.setDescription(normalizeOptionalText(data.description(), 2000, "description"));
        }

        if (data.defaultPriorityIdProvided()) {
            Long defaultPriorityId = requireVisiblePriorityId(data.defaultPriorityId(), tenantId);
            List<PrioritySchemeItemEntity> items = prioritySchemeItemPort.getPrioritySchemeItemsBySchemeId(prioritySchemeId, tenantId);
            if (!items.isEmpty() && items.stream().noneMatch(item -> Objects.equals(item.getPriorityId(), defaultPriorityId))) {
                log.warn("[PrioritySchemeService] Default priority id {} is not in the items of the priority scheme {}", defaultPriorityId, prioritySchemeId);
                throw new BusinessRuleViolationException(DomainErrorCode.PRIORITY_SCHEME_DEFAULT_NOT_IN_ITEMS);
            }
            existing.setDefaultPriorityId(defaultPriorityId);
        }

        existing.applyUpdate(userId, System.currentTimeMillis());
        prioritySchemePort.updatePriorityScheme(existing);
        return existing;
    }

    @Override
    public PrioritySchemeEntity deletePriorityScheme(Long prioritySchemeId, Long tenantId, Long userId) {
        PrioritySchemeEntity existing = getPrioritySchemeById(prioritySchemeId, tenantId);
        if (projectReadPort.existsActiveProjectByPrioritySchemeId(prioritySchemeId, tenantId)) {
            log.warn("[PrioritySchemeService] Cannot delete priority scheme {} because it is still referenced by active projects", prioritySchemeId);
            throw new BusinessRuleViolationException(DomainErrorCode.PRIORITY_SCHEME_BOUND_TO_PROJECT);
        }

        long now = System.currentTimeMillis();
        existing.setDeletedAt(now);
        existing.applyUpdate(userId, now);
        prioritySchemePort.updatePriorityScheme(existing);
        prioritySchemeItemPort.deletePrioritySchemeItemsBySchemeId(prioritySchemeId, tenantId);
        existing.setItems(List.of());
        return existing;
    }

    @Override
    public PrioritySchemeEntity replacePrioritySchemeItems(Long prioritySchemeId,
                                                           List<Long> priorityIds,
                                                           Long tenantId,
                                                           Long userId) {
        PrioritySchemeEntity existing = getPrioritySchemeById(prioritySchemeId, tenantId);

        List<Long> normalizedPriorityIds = normalizePriorityIds(priorityIds);
        requireVisiblePriorityIds(normalizedPriorityIds, tenantId);
        if (normalizedPriorityIds.stream().noneMatch(id -> Objects.equals(id, existing.getDefaultPriorityId()))) {
            log.warn("[PrioritySchemeService] Default priority id {} is not in the new items of the priority scheme {}",
                    existing.getDefaultPriorityId(), prioritySchemeId);
            throw new BusinessRuleViolationException(DomainErrorCode.PRIORITY_SCHEME_DEFAULT_NOT_IN_ITEMS);
        }

        List<PrioritySchemeItemEntity> currentItems = prioritySchemeItemPort.getPrioritySchemeItemsBySchemeId(prioritySchemeId, tenantId);
        Set<Long> requestedIds = new LinkedHashSet<>(normalizedPriorityIds);
        Set<Long> removedPriorityIds = new LinkedHashSet<>();
        for (PrioritySchemeItemEntity currentItem : currentItems) {
            if (!requestedIds.contains(currentItem.getPriorityId())) {
                removedPriorityIds.add(currentItem.getPriorityId());
            }
        }
        validateRemovedPrioritiesNotInUse(prioritySchemeId, tenantId, removedPriorityIds);

        long now = System.currentTimeMillis();
        prioritySchemeItemPort.deletePrioritySchemeItemsBySchemeId(prioritySchemeId, tenantId);

        List<PrioritySchemeItemEntity> replacementItems = new ArrayList<>(normalizedPriorityIds.size());
        for (int i = 0; i < normalizedPriorityIds.size(); i++) {
            PrioritySchemeItemEntity item = PrioritySchemeItemEntity.builder()
                    .tenantId(tenantId)
                    .schemeId(prioritySchemeId)
                    .priorityId(normalizedPriorityIds.get(i))
                    .sequence(i + 1)
                    .createdAt(now)
                    .createdBy(userId)
                    .updatedAt(now)
                    .updatedBy(userId)
                    .build();
            replacementItems.add(item);
        }

        List<PrioritySchemeItemEntity> savedItems = prioritySchemeItemPort.createPrioritySchemeItems(replacementItems);
        existing.setItems(savedItems);
        existing.applyUpdate(userId, now);
        prioritySchemePort.updatePriorityScheme(existing);
        return existing;
    }

    @Override
    public Long resolveDefaultPriorityId(Long prioritySchemeId, Long tenantId) {
        PrioritySchemeEntity priorityScheme = getVisiblePrioritySchemeById(prioritySchemeId, tenantId);
        if (priorityScheme.getDefaultPriorityId() == null) {
            throw new DomainValidationException(
                    DomainErrorCode.DEFAULT_PRIORITY_NOT_CONFIGURED,
                    "Priority scheme has no default priority: schemeId=" + prioritySchemeId
            );
        }
        return priorityScheme.getDefaultPriorityId();
    }

    @Override
    public Long validatePriorityIdInScheme(Long prioritySchemeId, Long requestedPriorityId, Long tenantId) {
        if (requestedPriorityId == null) {
            return null;
        }

        getVisiblePrioritySchemeById(prioritySchemeId, tenantId);
        boolean inScheme = prioritySchemeItemPort.getPrioritySchemeItemsBySchemeIdIncludingSystem(prioritySchemeId, tenantId)
                .stream()
                .anyMatch(item -> Objects.equals(item.getPriorityId(), requestedPriorityId));
        if (!inScheme) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PRIORITY_NOT_IN_SCHEME,
                    "Priority is not allowed in priority scheme: schemeId=" + prioritySchemeId + ", priorityId=" + requestedPriorityId
            );
        }

        return requestedPriorityId;
    }

    private Long requireVisiblePriorityId(Long priorityId, Long tenantId) {
        if (priorityId == null || priorityId <= 0) {
            throw new IllegalArgumentException("defaultPriorityId must be greater than 0");
        }
        return priorityPort.getPriorityByIdIncludingSystem(priorityId, tenantId)
                .map(PriorityEntity::getId)
                .orElseThrow(() -> ResourceNotFoundException.priority(priorityId));
    }

    private void requireVisiblePriorityIds(List<Long> priorityIds, Long tenantId) {
        List<PriorityEntity> visiblePriorities = priorityPort.getPrioritiesByIdsIncludingSystem(priorityIds, tenantId);
        Map<Long, PriorityEntity> prioritiesById = visiblePriorities.stream()
                .collect(Collectors.toMap(PriorityEntity::getId, Function.identity()));

        for (Long priorityId : priorityIds) {
            if (!prioritiesById.containsKey(priorityId)) {
                throw ResourceNotFoundException.priority(priorityId);
            }
        }
    }

    private List<Long> normalizePriorityIds(List<Long> priorityIds) {
        if (priorityIds == null || priorityIds.isEmpty()) {
            throw new IllegalArgumentException("priorityIds must not be empty");
        }

        List<Long> normalized = new ArrayList<>(priorityIds.size());
        Set<Long> uniqueIds = new LinkedHashSet<>();
        for (Long priorityId : priorityIds) {
            if (priorityId == null || priorityId <= 0) {
                throw new IllegalArgumentException("priorityIds must contain only positive values");
            }
            if (!uniqueIds.add(priorityId)) {
                throw new IllegalArgumentException("priorityIds must not contain duplicates");
            }
            normalized.add(priorityId);
        }
        return normalized;
    }

    private void validateRemovedPrioritiesNotInUse(Long prioritySchemeId, Long tenantId, Set<Long> removedPriorityIds) {
        if (removedPriorityIds.isEmpty()) {
            return;
        }

        List<Long> projectIds = projectReadPort.getActiveProjectIdsByPrioritySchemeId(prioritySchemeId, tenantId);
        if (projectIds.isEmpty()) {
            return;
        }

        List<Long> inUsePriorityIds = workItemReadPort.getActivePriorityIdsInUseByProjectIds(
                tenantId,
                projectIds,
                List.copyOf(removedPriorityIds)
        );
        if (!inUsePriorityIds.isEmpty()) {
            Long priorityId = inUsePriorityIds.getFirst();
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PRIORITY_SCHEME_IN_USE,
                    "Cannot remove priority from scheme because active work items still reference it: priorityId="
                            + priorityId
            );
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
