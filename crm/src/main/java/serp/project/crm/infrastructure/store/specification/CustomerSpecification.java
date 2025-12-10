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

import serp.project.crm.core.domain.dto.request.CustomerFilterRequest;
import serp.project.crm.infrastructure.store.model.CustomerModel;

public final class CustomerSpecification {

    private CustomerSpecification() {
    }

    public static Specification<CustomerModel> build(CustomerFilterRequest filter, Long tenantId) {
        CustomerFilterRequest safeFilter = Optional.ofNullable(filter)
                .orElseGet(CustomerFilterRequest::new);
        safeFilter.normalize();

        Specification<CustomerModel> spec = BaseSpecification.equal("tenantId", tenantId);

        if (safeFilter.hasKeyword()) {
            spec = spec.and(keywordContains(safeFilter.getKeyword()));
        }

        if (safeFilter.getStatuses() != null && !safeFilter.getStatuses().isEmpty()) {
            List<String> statuses = safeFilter.getStatuses().stream()
                    .filter(Objects::nonNull)
                    .map(Enum::name)
                    .toList();
            spec = spec.and(BaseSpecification.in("activeStatus", statuses));
        }

        if (safeFilter.getIndustries() != null && !safeFilter.getIndustries().isEmpty()) {
            spec = spec.and(BaseSpecification.in("industry", safeFilter.getIndustries()));
        }

        if (safeFilter.getCompanySizes() != null && !safeFilter.getCompanySizes().isEmpty()) {
            spec = spec.and(BaseSpecification.in("companySize", safeFilter.getCompanySizes()));
        }

        if (Boolean.TRUE.equals(safeFilter.getNoParentOnly())) {
            spec = spec.and(BaseSpecification.isNull("parentCustomerId"));
        } else if (safeFilter.getParentCustomerId() != null) {
            spec = spec.and(BaseSpecification.equal("parentCustomerId", safeFilter.getParentCustomerId()));
        }

        if (safeFilter.getCreditLimitMin() != null || safeFilter.getCreditLimitMax() != null) {
            spec = spec.and(BaseSpecification.between("creditLimit",
                    safeFilter.getCreditLimitMin(), safeFilter.getCreditLimitMax()));
        }

        if (safeFilter.getTotalRevenueMin() != null || safeFilter.getTotalRevenueMax() != null) {
            spec = spec.and(BaseSpecification.between("totalRevenue",
                    safeFilter.getTotalRevenueMin(), safeFilter.getTotalRevenueMax()));
        }

        if (safeFilter.getTotalOpportunitiesMin() != null || safeFilter.getTotalOpportunitiesMax() != null) {
            spec = spec.and(BaseSpecification.between("totalOpportunities",
                    safeFilter.getTotalOpportunitiesMin(), safeFilter.getTotalOpportunitiesMax()));
        }

        if (safeFilter.getWonOpportunitiesMin() != null || safeFilter.getWonOpportunitiesMax() != null) {
            spec = spec.and(BaseSpecification.between("wonOpportunities",
                    safeFilter.getWonOpportunitiesMin(), safeFilter.getWonOpportunitiesMax()));
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

        return spec;
    }

    private static Specification<CustomerModel> keywordContains(String keyword) {
        String lowered = keyword == null ? "" : keyword.trim().toLowerCase();
        if (lowered.isEmpty()) {
            return BaseSpecification.alwaysTrue();
        }

        List<Specification<CustomerModel>> parts = new ArrayList<>();
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("email")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("phone")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("website")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("taxId")), pattern(lowered)));
        parts.add((root, query, cb) -> cb.like(cb.lower(root.get("notes")), pattern(lowered)));

        Specification<CustomerModel> combined = BaseSpecification.alwaysFalse();
        for (Specification<CustomerModel> part : parts) {
            combined = combined.or(part);
        }
        return combined;
    }

    private static String pattern(String value) {
        return "%" + value + "%";
    }
}
