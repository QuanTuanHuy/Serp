/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.Trip;
import serp.project.first_mile.domain.TripOrder;
import serp.project.first_mile.dto.request.OrderFilterRequest;
import serp.project.first_mile.enums.TripStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> byFilter(
            Long tenantId,
            OrderFilterRequest filterRequest,
            String createdByUserId,
            Set<String> managedOriginPostOfficeCodes,
            Long courierStaffId,
            Collection<TripStatus> courierTripStatuses
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (hasText(filterRequest.getKeyword())) {
                String keywordPattern = toLikePattern(filterRequest.getKeyword());
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("orderCode")), keywordPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("customerOrderCode")), keywordPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("senderName")), keywordPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("senderPhone")), keywordPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("receiverName")), keywordPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("receiverPhone")), keywordPattern)
                ));
            }

            if (hasText(filterRequest.getOrderCode())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("orderCode")),
                        toLikePattern(filterRequest.getOrderCode())
                ));
            }

            if (hasText(filterRequest.getCustomerOrderCode())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("customerOrderCode")),
                        toLikePattern(filterRequest.getCustomerOrderCode())
                ));
            }

            if (hasText(filterRequest.getSenderPhone())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("senderPhone")),
                        toLikePattern(filterRequest.getSenderPhone())
                ));
            }

            if (hasText(filterRequest.getReceiverPhone())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("receiverPhone")),
                        toLikePattern(filterRequest.getReceiverPhone())
                ));
            }

            if (hasText(filterRequest.getOriginPostOfficeCode())) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("originPostOfficeCode")),
                        filterRequest.getOriginPostOfficeCode().trim().toLowerCase(Locale.ROOT)
                ));
            }

            if (hasText(filterRequest.getDestinationPostOfficeCode())) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("destinationPostOfficeCode")),
                        filterRequest.getDestinationPostOfficeCode().trim().toLowerCase(Locale.ROOT)
                ));
            }

            if (filterRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filterRequest.getStatus()));
            }

            if (filterRequest.getIsConfirm() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isConfirm"), filterRequest.getIsConfirm()));
            }

            if (filterRequest.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filterRequest.getCreatedFrom()));
            }

            if (filterRequest.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filterRequest.getCreatedTo()));
            }

            if (filterRequest.getPickupFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("pickupTimeStart"), filterRequest.getPickupFrom()));
            }

            if (filterRequest.getPickupTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("pickupTimeStart"), filterRequest.getPickupTo()));
            }

            if (createdByUserId != null) {
                predicates.add(criteriaBuilder.equal(root.get("createdBy"), createdByUserId));
            }

            if (managedOriginPostOfficeCodes != null) {
                if (managedOriginPostOfficeCodes.isEmpty()) {
                    return criteriaBuilder.disjunction();
                }

                Set<String> normalizedCodes = new HashSet<>();
                for (String code : managedOriginPostOfficeCodes) {
                    if (hasText(code)) {
                        normalizedCodes.add(code.trim().toLowerCase(Locale.ROOT));
                    }
                }

                if (normalizedCodes.isEmpty()) {
                    return criteriaBuilder.disjunction();
                }

                predicates.add(criteriaBuilder.lower(root.get("originPostOfficeCode")).in(normalizedCodes));
            }

            if (courierStaffId != null) {
                if (courierTripStatuses == null || courierTripStatuses.isEmpty()) {
                    return criteriaBuilder.disjunction();
                }

                Subquery<Long> courierOrderSubquery = query.subquery(Long.class);
                Root<TripOrder> tripOrderRoot = courierOrderSubquery.from(TripOrder.class);
                Join<TripOrder, Trip> tripJoin = tripOrderRoot.join("trip");

                courierOrderSubquery.select(tripOrderRoot.get("orderId"));
                courierOrderSubquery.where(
                        criteriaBuilder.equal(tripOrderRoot.get("tenantId"), tenantId),
                        criteriaBuilder.equal(tripOrderRoot.get("orderId"), root.get("id")),
                        criteriaBuilder.equal(tripJoin.get("courierStaffId"), courierStaffId),
                        tripJoin.get("status").in(courierTripStatuses)
                );

                predicates.add(criteriaBuilder.exists(courierOrderSubquery));
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
