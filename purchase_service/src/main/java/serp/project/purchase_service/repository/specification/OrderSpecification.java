package serp.project.purchase_service.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import serp.project.purchase_service.entity.OrderEntity;

import java.time.LocalDate;

public class OrderSpecification {

    public static Specification<OrderEntity> satisfy(String _query,
                                                     String fromSupplierId,
                                                     String saleChannelId,
                                                     LocalDate orderDateFrom,
                                                     LocalDate orderDateTo,
                                                     LocalDate deliveryBefore,
                                                     LocalDate deliveryAfter,
                                                     String statusId,
                                                     Long tenantId) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));
            predicates.add(criteriaBuilder.equal(root.get("orderTypeId"), "PURCHASE"));

            if (StringUtils.hasText(fromSupplierId)) {
                predicates.add(criteriaBuilder.equal(root.get("fromSupplierId"), fromSupplierId));
            }
            if (StringUtils.hasText(saleChannelId)) {
                predicates.add(criteriaBuilder.equal(root.get("saleChannelId"), saleChannelId));
            }
            if (orderDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), orderDateFrom));
            }
            if (orderDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), orderDateTo));
            }
            if (deliveryBefore != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("deliveryBeforeDate"), deliveryBefore));
            }
            if (deliveryAfter != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("deliveryAfterDate"), deliveryAfter));
            }
            if (StringUtils.hasText(statusId)) {
                predicates.add(criteriaBuilder.equal(root.get("statusId"), statusId));
            }
            if (StringUtils.hasText(_query)) {
                String likePattern = "%" + _query.toLowerCase() + "%";
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("orderName")), likePattern)
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
