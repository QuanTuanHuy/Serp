/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.dto.request.BagFilterRequest;

import java.time.LocalDateTime;
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

            if (filterRequest.getStatuses() != null && !filterRequest.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filterRequest.getStatuses()));
            }

            if (filterRequest.getMinOrders() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.<Integer>get("currentOrders"),
                        filterRequest.getMinOrders()
                ));
            }

            if (filterRequest.getMaxOrders() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.<Integer>get("currentOrders"),
                        filterRequest.getMaxOrders()
                ));
            }

            if (filterRequest.getMinWeight() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.<Double>get("currentWeight"),
                        filterRequest.getMinWeight()
                ));
            }

            if (filterRequest.getMaxWeight() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.<Double>get("currentWeight"),
                        filterRequest.getMaxWeight()
                ));
            }

            if (filterRequest.getMinVolume() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.<Double>get("currentVolume"),
                        filterRequest.getMinVolume()
                ));
            }

            if (filterRequest.getMaxVolume() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.<Double>get("currentVolume"),
                        filterRequest.getMaxVolume()
                ));
            }

            if (filterRequest.getSealedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.<LocalDateTime>get("sealedAt"),
                        filterRequest.getSealedFrom()
                ));
            }

            if (filterRequest.getSealedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.<LocalDateTime>get("sealedAt"),
                        filterRequest.getSealedTo()
                ));
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
