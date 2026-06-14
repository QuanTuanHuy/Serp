/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import serp.project.second_mile.domain.BagDistributionManifest;
import serp.project.second_mile.dto.request.BagDistributionManifestFilterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BagDistributionManifestSpecification {
    private BagDistributionManifestSpecification() {
    }

    public static Specification<BagDistributionManifest> byFilter(
            Long tenantId,
            BagDistributionManifestFilterRequest filterRequest
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (filterRequest == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            if (filterRequest.getOriginHubId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("originHubId"), filterRequest.getOriginHubId()));
            }
            if (filterRequest.getDestinationType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("destinationType"), filterRequest.getDestinationType()));
            }
            if (filterRequest.getDestinationHubId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("destinationHubId"),
                        filterRequest.getDestinationHubId()
                ));
            }
            String destinationPostOfficeCode = normalizeText(filterRequest.getDestinationPostOfficeCode());
            if (destinationPostOfficeCode != null) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("destinationPostOfficeCode")),
                        destinationPostOfficeCode
                ));
            }
            if (filterRequest.getRouteId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("routeId"), filterRequest.getRouteId()));
            }
            if (filterRequest.getVehicleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("vehicleId"), filterRequest.getVehicleId()));
            }
            if (filterRequest.getAssignedDriverId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignedDriverId"), filterRequest.getAssignedDriverId()));
            }
            if (filterRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filterRequest.getStatus()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase(Locale.ROOT);
    }
}
