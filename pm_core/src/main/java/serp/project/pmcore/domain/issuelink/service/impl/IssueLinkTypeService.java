/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuelink.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkTypeEntity;
import serp.project.pmcore.domain.issuelink.port.IIssueLinkTypePort;
import serp.project.pmcore.domain.issuelink.query.IssueLinkTypeListCriteria;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkTypeService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.shared.util.TextNormalizationUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueLinkTypeService implements IIssueLinkTypeService {

    private static final int NAME_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 100;

    private final IIssueLinkTypePort issueLinkTypePort;

    @Override
    public IssueLinkTypeEntity create(IssueLinkTypeEntity draft, Long tenantId, Long userId) {
        String normalizedName = normalizeRequired(draft.getName(), "name", NAME_MAX_LENGTH);
        ensureNameNotTaken(tenantId, normalizedName, null);

        draft.setTenantId(tenantId);
        draft.setName(normalizedName);
        draft.setOutwardDescription(normalizeRequired(draft.getOutwardDescription(), "outwardDescription", DESCRIPTION_MAX_LENGTH));
        draft.setInwardDescription(normalizeRequired(draft.getInwardDescription(), "inwardDescription", DESCRIPTION_MAX_LENGTH));
        draft.setIsSystem(false);
        draft.setDeletedAt(null);
        draft.applyCreate(userId, System.currentTimeMillis());
        return issueLinkTypePort.save(draft);
    }

    @Override
    public IssueLinkTypeEntity update(Long id, IssueLinkTypeEntity changes, Long tenantId, Long userId) {
        IssueLinkTypeEntity existing = getById(id, tenantId);
        if (Boolean.TRUE.equals(existing.getIsSystem())) {
            throw new BusinessRuleViolationException(DomainErrorCode.CONFLICT,
                    "System issue link types cannot be modified");
        }

        String normalizedName = normalizeRequired(changes.getName(), "name", NAME_MAX_LENGTH);
        ensureNameNotTaken(tenantId, normalizedName, existing.getId());

        existing.setName(normalizedName);
        existing.setOutwardDescription(normalizeRequired(changes.getOutwardDescription(), "outwardDescription", DESCRIPTION_MAX_LENGTH));
        existing.setInwardDescription(normalizeRequired(changes.getInwardDescription(), "inwardDescription", DESCRIPTION_MAX_LENGTH));
        existing.applyUpdate(userId, System.currentTimeMillis());
        return issueLinkTypePort.save(existing);
    }

    @Override
    public IssueLinkTypeEntity getById(Long id, Long tenantId) {
        return issueLinkTypePort.getById(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_LINK_TYPE_NOT_FOUND,
                        "Issue link type not found: id=" + id
                ));
    }

    @Override
    public IssueLinkTypeEntity getVisibleById(Long id, Long tenantId) {
        return issueLinkTypePort.getByIdIncludingSystem(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_LINK_TYPE_NOT_FOUND,
                        "Issue link type not found: id=" + id
                ));
    }

    @Override
    public PageResult<IssueLinkTypeEntity> listVisible(Long tenantId, IssueLinkTypeListCriteria criteria) {
        List<IssueLinkTypeEntity> items = issueLinkTypePort.listByTenant(tenantId).stream()
                .filter(item -> criteria.getIsSystem() == null || criteria.getIsSystem().equals(item.getIsSystem()))
                .filter(item -> matchesSearch(item, criteria.getSearch()))
                .sorted(resolveComparator(criteria))
                .toList();
        int page = criteria.getPage();
        int pageSize = criteria.getPageSize();
        int fromIndex = Math.min(page * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageResult<>(items.subList(fromIndex, toIndex), items.size());
    }

    @Override
    public IssueLinkTypeEntity delete(Long id, Long tenantId, Long userId) {
        IssueLinkTypeEntity existing = getById(id, tenantId);
        if (Boolean.TRUE.equals(existing.getIsSystem())) {
            throw new BusinessRuleViolationException(DomainErrorCode.CONFLICT,
                    "System issue link types cannot be deleted");
        }
        long now = System.currentTimeMillis();
        existing.setDeletedAt(now);
        existing.applyUpdate(userId, now);
        return issueLinkTypePort.save(existing);
    }

    private void ensureNameNotTaken(Long tenantId, String normalizedName, Long currentId) {
        issueLinkTypePort.getByName(tenantId, normalizedName.toLowerCase(Locale.ROOT))
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    log.warn("Issue link type name already exists: tenantId={}, name={}", tenantId, normalizedName);
                    throw new BusinessRuleViolationException(DomainErrorCode.CONFLICT,
                            "Issue link type name already exists");
                });
    }

    private String normalizeRequired(String value, String fieldName, int maxLength) {
        return TextNormalizationUtils.normalizeRequiredText(value, fieldName, maxLength);
    }

    private boolean matchesSearch(IssueLinkTypeEntity item, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String needle = search.trim().toLowerCase(Locale.ROOT);
        return item.getName().toLowerCase(Locale.ROOT).contains(needle)
                || item.getOutwardDescription().toLowerCase(Locale.ROOT).contains(needle)
                || item.getInwardDescription().toLowerCase(Locale.ROOT).contains(needle);
    }

    private Comparator<IssueLinkTypeEntity> resolveComparator(IssueLinkTypeListCriteria criteria) {
        Comparator<IssueLinkTypeEntity> comparator = switch (criteria.getSortBy().toLowerCase(Locale.ROOT)) {
            case "created_at" -> Comparator.comparing(IssueLinkTypeEntity::getCreatedAt,
                    Comparator.nullsLast(Long::compareTo));
            case "updated_at" -> Comparator.comparing(IssueLinkTypeEntity::getUpdatedAt,
                    Comparator.nullsLast(Long::compareTo));
            default -> Comparator.comparing(IssueLinkTypeEntity::getName, String.CASE_INSENSITIVE_ORDER);
        };
        if ("DESC".equalsIgnoreCase(criteria.getSortDirection())) {
            comparator = comparator.reversed();
        }
        return comparator.thenComparing(IssueLinkTypeEntity::getId, Comparator.nullsLast(Long::compareTo));
    }
}
