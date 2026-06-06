/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import serp.project.second_mile.domain.Route;
import serp.project.second_mile.dto.request.RouteFilterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RouteSpecification {
    private RouteSpecification() {
    }

    public static Specification<Route> byFilter(Long tenantId, RouteFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (hasText(filterRequest.getKeyword())) {
                String keywordPattern = toLikePattern(filterRequest.getKeyword());
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("routeCode")), keywordPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("routeName")), keywordPattern)
                ));
            }

            if (hasText(filterRequest.getRouteCode())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("routeCode")),
                        toLikePattern(filterRequest.getRouteCode())
                ));
            }

            if (filterRequest.getOriginType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("originType"), filterRequest.getOriginType()));
            }

            if (filterRequest.getOriginHubId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("originHubId"), filterRequest.getOriginHubId()));
            }

            if (hasText(filterRequest.getOriginPostOfficeCode())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("originPostOfficeCode")),
                        toLikePattern(filterRequest.getOriginPostOfficeCode())
                ));
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
