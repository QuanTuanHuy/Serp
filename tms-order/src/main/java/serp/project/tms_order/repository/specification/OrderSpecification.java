/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.dto.request.OrderFilterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> byFilter(
            Long tenantId,
            OrderFilterRequest filterRequest,
            String createdByUserId
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

            if (filterRequest.getOriginPostOfficeCodes() != null
                    && !filterRequest.getOriginPostOfficeCodes().isEmpty()) {
                predicates.add(criteriaBuilder.lower(root.get("originPostOfficeCode"))
                        .in(filterRequest.getOriginPostOfficeCodes()));
            } else if (hasText(filterRequest.getOriginPostOfficeCode())) {
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

            if (filterRequest.getStatuses() != null && !filterRequest.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filterRequest.getStatuses()));
            } else if (filterRequest.getStatus() != null) {
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
