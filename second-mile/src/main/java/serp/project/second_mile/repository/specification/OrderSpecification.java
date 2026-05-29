/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import serp.project.second_mile.domain.Order;
import serp.project.second_mile.dto.request.OrderFilterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OrderSpecification {
    private OrderSpecification() {
    }

    public static Specification<Order> byFilter(Long tenantId, OrderFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (hasText(filterRequest.getKeyword())) {
                String keywordPattern = toLikePattern(filterRequest.getKeyword());
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("orderCode")), keywordPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("customerOrderCode")), keywordPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("originPostOfficeCode")), keywordPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("destinationPostOfficeCode")), keywordPattern)
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
