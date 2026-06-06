package serp.project.school_bus_service.shared.base;

import org.springframework.data.jpa.domain.Specification;
import serp.project.school_bus_service.entity.BaseModel;

public final class BaseSpecification {

    private BaseSpecification() {
    }

    public static <T extends BaseModel> Specification<T> hasTenantId(Long tenantId) {
        return (root, query, builder) -> builder.equal(root.get("tenantId"), tenantId);
    }

    public static <T extends BaseModel> Specification<T> isNotDeleted() {
        return (root, query, builder) -> builder.isFalse(root.get("isDeleted"));
    }

    public static <T extends BaseModel> Specification<T> isActive(Boolean active) {
        return (root, query, builder) -> active == null ? builder.conjunction() : builder.equal(root.get("isActive"), active);
    }
}
