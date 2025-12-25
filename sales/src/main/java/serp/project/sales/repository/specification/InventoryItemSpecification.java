package serp.project.sales.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import serp.project.sales.entity.InventoryItemEntity;

import java.time.LocalDate;

public class InventoryItemSpecification {

    public static Specification<InventoryItemEntity> satisfy(
            String _query,
            String productId,
            String facilityId,
            LocalDate expirationDateFrom,
            LocalDate expirationDateTo,
            LocalDate manufacturingDateFrom,
            LocalDate manufacturingDateTo,
            String statusId,
            Long tenantId) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (StringUtils.hasText(statusId)) {
                predicates.add(criteriaBuilder.equal(root.get("statusId"), statusId));
            }
            if (StringUtils.hasText(productId)) {
                predicates.add(criteriaBuilder.equal(root.get("productId"), productId));
            }
            if (StringUtils.hasText(facilityId)) {
                predicates.add(criteriaBuilder.equal(root.get("facilityId"), facilityId));
            }
            if (expirationDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("expirationDate"), expirationDateFrom));
            }
            if (expirationDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("expirationDate"), expirationDateTo));
            }
            if (manufacturingDateFrom != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("manufacturingDate"), manufacturingDateFrom));
            }
            if (manufacturingDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("manufacturingDate"), manufacturingDateTo));
            }

            if (StringUtils.hasText(_query)) {
                String likePattern = "%" + _query.toLowerCase() + "%";
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("lotId")), likePattern)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
