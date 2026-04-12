/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.service.impl;

import java.net.URI;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.priority.dto.PriorityUpdateData;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.port.IPriorityPort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemePort;
import serp.project.pmcore.domain.priority.query.PriorityListCriteria;
import serp.project.pmcore.domain.priority.service.IPriorityService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriorityService implements IPriorityService {

    private static final int PRIORITY_NAME_MAX_LENGTH = 50;
    private static final int PRIORITY_KEY_MAX_LENGTH = 100;

    private final IPriorityPort priorityPort;
    private final IPrioritySchemePort prioritySchemePort;
    private final IPrioritySchemeItemPort prioritySchemeItemPort;
    private final IWorkItemReadPort workItemReadPort;

    @Override
    public PriorityEntity createPriority(PriorityEntity priority, Long tenantId, Long userId) {
        String name = normalizeRequiredText(priority.getName(), "name", PRIORITY_NAME_MAX_LENGTH);
        if (priorityPort.existsByName(tenantId, name)) {
            log.warn("Priority name already exists: name={}, tenantId={}", name, tenantId);
            throw new BusinessRuleViolationException(DomainErrorCode.PRIORITY_NAME_ALREADY_EXISTS);
        }

        priority.setTenantId(tenantId);
        priority.setPriorityKey(generatePriorityKey(name, tenantId));
        priority.setName(name);
        priority.setDescription(normalizeOptionalText(priority.getDescription(), 2000, "description"));
        priority.setIconUrl(normalizeOptionalUrl(priority.getIconUrl()));
        priority.setColor(normalizeOptionalColor(priority.getColor()));
        priority.setSequence(validateSequence(priority.getSequence()));
        priority.setSystem(false);
        priority.setDeletedAt(null);
        priority.applyCreate(userId, System.currentTimeMillis());

        return priorityPort.createPriority(priority);
    }

    @Override
    public PriorityEntity getPriorityById(Long priorityId, Long tenantId) {
        return priorityPort.getPriorityById(priorityId, tenantId)
                .orElseThrow(() -> {
                    log.error("Priority not found: id={}, tenantId={}", priorityId, tenantId);
                    return ResourceNotFoundException.priority(priorityId);
                });
    }

    @Override
    public PriorityEntity getVisiblePriorityById(Long priorityId, Long tenantId) {
        return priorityPort.getPriorityByIdIncludingSystem(priorityId, tenantId)
                .orElseThrow(() -> {
                    log.error("Visible priority not found: id={}, tenantId={}", priorityId, tenantId);
                    return ResourceNotFoundException.priority(priorityId);
                });
    }

    @Override
    public PageResult<PriorityEntity> listVisiblePriorities(Long tenantId, PriorityListCriteria criteria) {
        return priorityPort.listPrioritiesIncludingSystem(tenantId, criteria);
    }

    @Override
    public PriorityEntity updatePriority(Long priorityId, PriorityUpdateData data, Long tenantId, Long userId) {
        PriorityEntity existing = getPriorityById(priorityId, tenantId);
        if (existing.isSystem()) {
            throw new BusinessRuleViolationException(DomainErrorCode.PRIORITY_IS_SYSTEM);
        }

        if (data.nameProvided()) {
            String newName = normalizeRequiredText(data.name(), "name", PRIORITY_NAME_MAX_LENGTH);
            if (!newName.equalsIgnoreCase(existing.getName()) && priorityPort.existsByName(tenantId, newName)) {
                log.error("Priority name already exists: name={}, tenantId={}", newName, tenantId);
                throw new BusinessRuleViolationException(DomainErrorCode.PRIORITY_NAME_ALREADY_EXISTS);
            }
            existing.setName(newName);
        }
        if (data.descriptionProvided()) {
            existing.setDescription(normalizeOptionalText(data.description(), 2000, "description"));
        }
        if (data.iconUrlProvided()) {
            existing.setIconUrl(normalizeOptionalUrl(data.iconUrl()));
        }
        if (data.colorProvided()) {
            existing.setColor(normalizeOptionalColor(data.color()));
        }
        if (data.sequenceProvided()) {
            existing.setSequence(validateSequence(data.sequence()));
        }

        existing.applyUpdate(userId, System.currentTimeMillis());
        priorityPort.updatePriority(existing);
        return existing;
    }

    @Override
    public PriorityEntity deletePriority(Long priorityId, Long tenantId, Long userId) {
        PriorityEntity existing = getPriorityById(priorityId, tenantId);
        if (existing.isSystem()) {
            log.error("Cannot delete system priority: id={}, tenantId={}", priorityId, tenantId);
            throw new BusinessRuleViolationException(DomainErrorCode.PRIORITY_IS_SYSTEM);
        }
        if (isPriorityInUse(priorityId, tenantId)) {
            log.error("Cannot delete priority in use: id={}, tenantId={}", priorityId, tenantId);
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PRIORITY_IN_USE,
                    "Priority is still referenced by active work items or tenant configuration: id=" + priorityId);
        }

        long now = System.currentTimeMillis();
        existing.setDeletedAt(now);
        existing.applyUpdate(userId, now);
        priorityPort.updatePriority(existing);
        return existing;
    }

    private boolean isPriorityInUse(Long priorityId, Long tenantId) {
        return !workItemReadPort.getWorkItemsByPriorityId(priorityId, tenantId).isEmpty()
                || prioritySchemePort.existsByDefaultPriorityId(priorityId, tenantId)
                || prioritySchemeItemPort.existsByPriorityId(priorityId, tenantId);
    }

    private String generatePriorityKey(String name, Long tenantId) {
        String normalized = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        String baseKey = normalized.isEmpty() ? "priority" : trimToMaxLength(normalized, PRIORITY_KEY_MAX_LENGTH);
        String candidate = baseKey;
        int suffix = 2;

        while (priorityPort.getPriorityByPriorityKey(tenantId, candidate).isPresent()) {
            String suffixValue = "_" + suffix;
            candidate = trimToMaxLength(baseKey, PRIORITY_KEY_MAX_LENGTH - suffixValue.length()) + suffixValue;
            suffix++;
        }

        return candidate;
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

    private String normalizeOptionalColor(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 20) {
            throw new IllegalArgumentException("color must be at most 20 characters");
        }
        if (!trimmed.matches("^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$")) {
            throw new IllegalArgumentException("color must be a valid hex color");
        }
        return trimmed;
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

    private String trimToMaxLength(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(maxLength, 1));
    }
}
