/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;

import serp.project.crm.core.domain.dto.request.OpportunityFilterRequest;
import serp.project.crm.infrastructure.store.model.OpportunityModel;

public final class OpportunitySpecification {

    private OpportunitySpecification() {
    }

    public static Specification<OpportunityModel> build(OpportunityFilterRequest filter, Long tenantId) {
        OpportunityFilterRequest safeFilter = Optional.ofNullable(filter)
                .orElseGet(OpportunityFilterRequest::new);
        safeFilter.normalize();

        Specification<OpportunityModel> spec = BaseSpecification.equal("tenantId", tenantId);

        if (safeFilter.hasKeyword()) {
            spec = spec.and(keywordContains(safeFilter.getKeyword()));
        }

        if (safeFilter.getStages() != null && !safeFilter.getStages().isEmpty()) {
            List<String> stages = safeFilter.getStages().stream()
                    .filter(Objects::nonNull)
                    .map(Enum::name)
                    .toList();
            spec = spec.and(BaseSpecification.in("stage", stages));
        }

        if (safeFilter.getCustomerId() != null) {
            spec = spec.and(BaseSpecification.equal("customerId", safeFilter.getCustomerId()));
        }

        if (safeFilter.getLeadId() != null) {
            spec = spec.and(BaseSpecification.equal("leadId", safeFilter.getLeadId()));
        }

        if (Boolean.TRUE.equals(safeFilter.getUnassignedOnly())) {
            spec = spec.and(BaseSpecification.isNull("assignedTo"));
        } else if (safeFilter.getAssignedTo() != null) {
            spec = spec.and(BaseSpecification.equal("assignedTo", safeFilter.getAssignedTo()));
        }

        if (safeFilter.getEstimatedValueMin() != null || safeFilter.getEstimatedValueMax() != null) {
            spec = spec.and(BaseSpecification.between("estimatedValue",
                    safeFilter.getEstimatedValueMin(), safeFilter.getEstimatedValueMax()));
        }

        if (safeFilter.getProbabilityMin() != null || safeFilter.getProbabilityMax() != null) {
            spec = spec.and(BaseSpecification.between("probability",
                    safeFilter.getProbabilityMin(), safeFilter.getProbabilityMax()));
        }

        if (safeFilter.getExpectedCloseDateFrom() != null || safeFilter.getExpectedCloseDateTo() != null) {
            spec = spec.and(BaseSpecification.between("expectedCloseDate",
                    safeFilter.getExpectedCloseDateFrom(), safeFilter.getExpectedCloseDateTo()));
        }

        if (safeFilter.getActualCloseDateFrom() != null || safeFilter.getActualCloseDateTo() != null) {
            spec = spec.and(BaseSpecification.between("actualCloseDate",
                    safeFilter.getActualCloseDateFrom(), safeFilter.getActualCloseDateTo()));
        }

        if (safeFilter.getCreatedFrom() != null || safeFilter.getCreatedTo() != null) {
            spec = spec.and(BaseSpecification.dateTimeBetween("createdAt",
                    safeFilter.getCreatedFrom(), safeFilter.getCreatedTo()));
        }

        if (Boolean.TRUE.equals(safeFilter.getHasNotes())) {
            spec = spec.and(BaseSpecification.isNotNull("notes"));
        }

        return spec;
    }

    private static Specification<OpportunityModel> keywordContains(String keyword) {
        String lowered = keyword == null ? "" : keyword.trim().toLowerCase();
        if (lowered.isEmpty()) {
            return BaseSpecification.alwaysTrue();
        }

        List<Specification<OpportunityModel>> parts = new ArrayList<>();
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("description")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("notes")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("lossReason")), pattern(lowered)));

        Specification<OpportunityModel> combined = BaseSpecification.alwaysFalse();
        for (Specification<OpportunityModel> part : parts) {
            combined = combined.or(part);
        }
        return combined;
    }

    private static String pattern(String value) {
        return "%" + value + "%";
    }
}
