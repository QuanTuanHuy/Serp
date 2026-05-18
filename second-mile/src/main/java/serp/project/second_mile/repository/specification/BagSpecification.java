/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.dto.request.BagFilterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BagSpecification {
    private BagSpecification() {
    }

    public static Specification<Bag> byFilter(Long tenantId, BagFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (hasText(filterRequest.getKeyword())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("bagCode")),
                        toLikePattern(filterRequest.getKeyword())
                ));
            }

            if (hasText(filterRequest.getBagCode())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("bagCode")),
                        toLikePattern(filterRequest.getBagCode())
                ));
            }

            if (filterRequest.getOriginHubId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("originHubId"), filterRequest.getOriginHubId()));
            }

            if (filterRequest.getDestinationType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("destinationType"), filterRequest.getDestinationType()));
            }

            if (filterRequest.getDestinationHubId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("destinationHubId"), filterRequest.getDestinationHubId()));
            }

            if (hasText(filterRequest.getDestinationPostOfficeCode())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("destinationPostOfficeCode")),
                        toLikePattern(filterRequest.getDestinationPostOfficeCode())
                ));
            }

            if (filterRequest.getVehicleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("vehicleId"), filterRequest.getVehicleId()));
            }

            if (filterRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filterRequest.getStatus()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String toLikePattern(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
