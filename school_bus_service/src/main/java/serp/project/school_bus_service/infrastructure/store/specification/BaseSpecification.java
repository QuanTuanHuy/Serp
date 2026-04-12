package serp.project.school_bus_service.infrastructure.store.specification;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import serp.project.school_bus_service.infrastructure.store.model.BaseModel;

import java.util.ArrayList;
import java.util.List;

public final class BaseSpecification {

    private BaseSpecification() {
    }

    public static <T extends BaseModel> Specification<T> tenantActive(Long tenantId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("tenantId"), tenantId),
                criteriaBuilder.isFalse(root.get("isDeleted")));
    }

    public static <T extends BaseModel> Specification<T> keyword(String keyword, String... fields) {
        if (keyword == null || keyword.isBlank() || fields == null || fields.length == 0) {
            return null;
        }
        String pattern = "%" + keyword.toLowerCase().trim() + "%";
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (String field : fields) {
                Path<?> path = root;
                for (String segment : field.split("\\.")) {
                    path = path.get(segment);
                }
                Expression<String> expression = criteriaBuilder.lower(path.as(String.class));
                predicates.add(criteriaBuilder.like(expression, pattern));
            }
            return criteriaBuilder.or(predicates.toArray(Predicate[]::new));
        };
    }

    public static <T extends BaseModel> Specification<T> tenantActiveWithKeyword(Long tenantId, String keyword,
            String... fields) {
        Specification<T> keywordSpec = BaseSpecification.keyword(keyword, fields);
        Specification<T> baseSpec = BaseSpecification.tenantActive(tenantId);
        return keywordSpec == null ? baseSpec : baseSpec.and(keywordSpec);
    }
}
