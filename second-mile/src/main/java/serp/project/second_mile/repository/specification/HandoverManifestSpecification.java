/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import serp.project.second_mile.domain.HandoverManifest;
import serp.project.second_mile.dto.request.HandoverManifestFilterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HandoverManifestSpecification {
    private HandoverManifestSpecification() {
    }

    public static Specification<HandoverManifest> byFilter(
            Long tenantId,
            HandoverManifestFilterRequest filterRequest
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (filterRequest == null) {
                return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
            }

            if (filterRequest.getTargetHubId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("targetHubId"), filterRequest.getTargetHubId()));
            }

            if (filterRequest.getVehicleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("vehicleId"), filterRequest.getVehicleId()));
            }

            if (filterRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filterRequest.getStatus()));
            }

            String originPostOfficeCode = normalizeText(filterRequest.getOriginPostOfficeCode());
            if (originPostOfficeCode != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("originPostOfficeCode")),
                        originPostOfficeCode
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }
}
