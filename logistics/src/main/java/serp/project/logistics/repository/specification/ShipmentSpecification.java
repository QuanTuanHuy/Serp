package serp.project.logistics.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import serp.project.logistics.entity.ShipmentEntity;

public class ShipmentSpecification {

    public static Specification<ShipmentEntity> satisfy(String _query,
            String shipmentTypeId,
            String fromSupplierId,
            String toCustomerId,
            String orderId,
            String statusId,
            Long tenantId) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (StringUtils.hasText(shipmentTypeId)) {
                predicates.add(criteriaBuilder.equal(root.get("shipmentTypeId"), shipmentTypeId));
            }
            if (StringUtils.hasText(fromSupplierId)) {
                predicates.add(criteriaBuilder.equal(root.get("fromSupplierId"), fromSupplierId));
            }
            if (StringUtils.hasText(toCustomerId)) {
                predicates.add(criteriaBuilder.equal(root.get("toCustomerId"), toCustomerId));
            }
            if (StringUtils.hasText(orderId)) {
                predicates.add(criteriaBuilder.equal(root.get("orderId"), orderId));
            }

            if (StringUtils.hasText(statusId)) {
                predicates.add(criteriaBuilder.equal(root.get("statusId"), statusId));
            }
            if (StringUtils.hasText(_query)) {
                String likePattern = "%" + _query.toLowerCase() + "%";
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("shipmentName")), likePattern),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("note")), likePattern)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
