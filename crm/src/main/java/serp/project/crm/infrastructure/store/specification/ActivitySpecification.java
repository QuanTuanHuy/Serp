/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.specification;

import org.springframework.data.jpa.domain.Specification;
import serp.project.crm.core.domain.dto.request.ActivityFilterRequest;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.infrastructure.store.model.ActivityModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ActivitySpecification {

    private ActivitySpecification() {
    }

    public static Specification<ActivityModel> build(ActivityFilterRequest filter, Long tenantId) {
        ActivityFilterRequest safeFilter = Optional.ofNullable(filter)
                .orElseGet(ActivityFilterRequest::new);
        safeFilter.normalize();

        Specification<ActivityModel> spec = BaseSpecification.equal("tenantId", tenantId);

        if (safeFilter.hasKeyword()) {
            spec = spec.and(keywordContains(safeFilter.getKeyword()));
        }

        if (safeFilter.getTypes() != null && !safeFilter.getTypes().isEmpty()) {
            List<String> types = safeFilter.getTypes().stream()
                    .filter(Objects::nonNull)
                    .map(Enum::name)
                    .toList();
            spec = spec.and(BaseSpecification.in("activityType", types));
        }

        if (safeFilter.getStatuses() != null && !safeFilter.getStatuses().isEmpty()) {
            List<String> statuses = safeFilter.getStatuses().stream()
                    .filter(Objects::nonNull)
                    .map(Enum::name)
                    .toList();
            spec = spec.and(BaseSpecification.in("status", statuses));
        }

        if (safeFilter.getPriorities() != null && !safeFilter.getPriorities().isEmpty()) {
            List<String> priorities = safeFilter.getPriorities().stream()
                    .filter(Objects::nonNull)
                    .map(Enum::name)
                    .toList();
            spec = spec.and(BaseSpecification.in("priority", priorities));
        }

        if (Boolean.TRUE.equals(safeFilter.getUnassignedOnly())) {
            spec = spec.and(BaseSpecification.isNull("assignedTo"));
        } else if (safeFilter.getAssignedTo() != null) {
            spec = spec.and(BaseSpecification.equal("assignedTo", safeFilter.getAssignedTo()));
        }

        if (safeFilter.getLeadId() != null) {
            spec = spec.and(BaseSpecification.equal("leadId", safeFilter.getLeadId()));
        }

        if (safeFilter.getAccountId() != null) {
            spec = spec.and(BaseSpecification.equal("accountId", safeFilter.getAccountId()));
        }

        if (safeFilter.getOpportunityId() != null) {
            spec = spec.and(BaseSpecification.equal("opportunityId", safeFilter.getOpportunityId()));
        }

        if (safeFilter.getContactId() != null) {
            spec = spec.and(BaseSpecification.equal("contactId", safeFilter.getContactId()));
        }

        if (safeFilter.getActivityDateFrom() != null || safeFilter.getActivityDateTo() != null) {
            spec = spec.and(BaseSpecification.dateTimeBetween("activityDate",
                    safeFilter.getActivityDateFrom(), safeFilter.getActivityDateTo()));
        }

        if (safeFilter.getDueDateFrom() != null || safeFilter.getDueDateTo() != null) {
            spec = spec.and(BaseSpecification.dateTimeBetween("dueDate",
                    safeFilter.getDueDateFrom(), safeFilter.getDueDateTo()));
        }

        if (Boolean.TRUE.equals(safeFilter.getOverdueOnly())) {
            spec = spec.and(overdueSpec());
        }

        if (Boolean.TRUE.equals(safeFilter.getCompletedOnly())) {
            spec = spec.and(BaseSpecification.equal("status", ActivityStatus.COMPLETED.name()));
        }

        return spec;
    }

    private static Specification<ActivityModel> keywordContains(String keyword) {
        String lowered = keyword == null ? "" : keyword.trim().toLowerCase();
        if (lowered.isEmpty()) {
            return BaseSpecification.alwaysTrue();
        }

        List<Specification<ActivityModel>> parts = new ArrayList<>();
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("subject")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("description")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("notes")), pattern(lowered)));

        Specification<ActivityModel> combined = BaseSpecification.alwaysFalse();
        for (Specification<ActivityModel> part : parts) {
            combined = combined.or(part);
        }
        return combined;
    }

    private static Specification<ActivityModel> overdueSpec() {
        return (root, query, cb) -> cb.and(
                cb.lessThan(root.get("dueDate"), LocalDateTime.now()),
                cb.notEqual(root.get("status"), ActivityStatus.COMPLETED.name())
        );
    }

    private static String pattern(String value) {
        return "%" + value + "%";
    }
}
