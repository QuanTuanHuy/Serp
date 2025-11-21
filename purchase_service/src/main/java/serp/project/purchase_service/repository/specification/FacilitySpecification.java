package serp.project.purchase_service.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import serp.project.purchase_service.entity.FacilityEntity;

public class FacilitySpecification {

    public static Specification<FacilityEntity> satisfy(String _query, String statusId, Long tenantId) {
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
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern)
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
