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

import serp.project.crm.core.domain.dto.request.LeadFilterRequest;
import serp.project.crm.core.domain.enums.LeadStatus;
import serp.project.crm.infrastructure.store.model.LeadModel;

public final class LeadSpecification {

    private LeadSpecification() {
    }

    public static Specification<LeadModel> build(LeadFilterRequest filter, Long tenantId) {
        LeadFilterRequest safeFilter = Optional.ofNullable(filter)
                .orElseGet(LeadFilterRequest::new);
        safeFilter.normalize();

        Specification<LeadModel> spec = BaseSpecification.equal("tenantId", tenantId);

        if (safeFilter.hasKeyword()) {
            spec = spec.and(keywordContains(safeFilter.getKeyword()));
        }

        if (safeFilter.getStatuses() != null && !safeFilter.getStatuses().isEmpty()) {
            List<String> statuses = safeFilter.getStatuses().stream()
                    .filter(Objects::nonNull)
                    .map(Enum::name)
                    .toList();
            spec = spec.and(BaseSpecification.in("leadStatus", statuses));
        }

        if (safeFilter.getSources() != null && !safeFilter.getSources().isEmpty()) {
            List<String> sources = safeFilter.getSources().stream()
                    .filter(Objects::nonNull)
                    .map(Enum::name)
                    .toList();
            spec = spec.and(BaseSpecification.in("leadSource", sources));
        }

        if (safeFilter.getIndustries() != null && !safeFilter.getIndustries().isEmpty()) {
            spec = spec.and(BaseSpecification.in("industry", safeFilter.getIndustries()));
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

        if (safeFilter.getCreatedFrom() != null || safeFilter.getCreatedTo() != null) {
            spec = spec.and(BaseSpecification.dateTimeBetween("createdAt",
                    safeFilter.getCreatedFrom(), safeFilter.getCreatedTo()));
        }

        if (safeFilter.getCity() != null && !safeFilter.getCity().isBlank()) {
            spec = spec.and(BaseSpecification.like("addressCity", safeFilter.getCity()));
        }

        if (safeFilter.getCountry() != null && !safeFilter.getCountry().isBlank()) {
            spec = spec.and(BaseSpecification.like("addressCountry", safeFilter.getCountry()));
        }

        if (Boolean.TRUE.equals(safeFilter.getHasEmail())) {
            spec = spec.and(BaseSpecification.isNotNull("email"));
        }

        if (Boolean.TRUE.equals(safeFilter.getHasPhone())) {
            spec = spec.and(BaseSpecification.isNotNull("phone"));
        }

        if (Boolean.TRUE.equals(safeFilter.getQualifiedOnly())) {
            spec = spec.and(BaseSpecification.in("leadStatus",
                    List.of(LeadStatus.QUALIFIED.name(), LeadStatus.CONVERTED.name())));
        }

        if (Boolean.TRUE.equals(safeFilter.getConvertedOnly())) {
            spec = spec.and(BaseSpecification.isNotNull("convertedCustomerId"));
        }

        return spec;
    }

    private static Specification<LeadModel> keywordContains(String keyword) {
        String lowered = keyword == null ? "" : keyword.trim().toLowerCase();
        if (lowered.isEmpty()) {
            return BaseSpecification.alwaysTrue();
        }

        List<Specification<LeadModel>> parts = new ArrayList<>();
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("email")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("company")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("phone")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("jobTitle")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("notes")), pattern(lowered)));

        Specification<LeadModel> combined = BaseSpecification.alwaysFalse();
        for (Specification<LeadModel> part : parts) {
            combined = combined.or(part);
        }
        return combined;
    }

    private static String pattern(String value) {
        return "%" + value + "%";
    }
}
