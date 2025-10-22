/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.specification;

import org.springframework.data.jpa.domain.Specification;

import serp.project.account.infrastructure.store.model.OrganizationModel;

public class OrganizationSpecification extends BaseSpecification<OrganizationModel> {
    public static Specification<OrganizationModel> hasStatus(String status) {
        return equal("status", status);
    }

    public static Specification<OrganizationModel> hasType(String type) {
        return equal("organizationType", type);
    }

    public static Specification<OrganizationModel> containsName(String name) {
        return like("name", name);
    }

    public static Specification<OrganizationModel> hasEmail(String email) {
        return equal("email", email);
    }

    public static Specification<OrganizationModel> getOrganizations(String search, String status, String type) {
        Specification<OrganizationModel> spec = alwaysTrue();

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and(containsName(search));
        }
        if (status != null && !status.trim().isEmpty()) {
            spec = spec.and(hasStatus(status));
        }
        if (type != null && !type.trim().isEmpty()) {
            spec = spec.and(hasType(type));
        }

        return spec;
    }
}
