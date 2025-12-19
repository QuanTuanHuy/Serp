package serp.project.sales.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import serp.project.sales.entity.CustomerEntity;

public class CustomerSpecification {

    public static Specification<CustomerEntity> satisfy(String _query, String statusId, Long tenantId) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (StringUtils.hasText(statusId)) {
                predicates.add(criteriaBuilder.equal(root.get("statusId"), statusId));
            }

            if (StringUtils.hasText(_query)) {
                String likePattern = "%" + _query.toLowerCase() + "%";
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), likePattern)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
