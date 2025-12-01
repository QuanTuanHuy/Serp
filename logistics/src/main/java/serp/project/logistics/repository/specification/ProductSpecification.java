package serp.project.logistics.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import serp.project.logistics.entity.ProductEntity;

public class ProductSpecification {

    public static Specification<ProductEntity> satisfy(String _query, String categoryId, String statusId, Long tenantId) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (StringUtils.hasText(categoryId)) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), categoryId));
            }

            if (StringUtils.hasText(statusId)) {
                predicates.add(criteriaBuilder.equal(root.get("statusId"), statusId));
            }

            if (StringUtils.hasText(_query)) {
                String likePattern = "%" + _query.toLowerCase() + "%";
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("skuCode")), likePattern),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("extraProps")), likePattern)
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
