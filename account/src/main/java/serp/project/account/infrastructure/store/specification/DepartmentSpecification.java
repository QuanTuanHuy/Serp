/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.specification;

import org.springframework.data.jpa.domain.Specification;
import serp.project.account.core.domain.dto.request.GetDepartmentParams;
import serp.project.account.infrastructure.store.model.DepartmentModel;

public class DepartmentSpecification extends BaseSpecification<DepartmentModel> {

    public static Specification<DepartmentModel> hasOrganizationId(Long organizationId) {
        return equal("organizationId", organizationId);
    }

    public static Specification<DepartmentModel> hasParentDepartmentId(Long parentDepartmentId) {
        return equal("parentDepartmentId", parentDepartmentId);
    }

    public static Specification<DepartmentModel> hasManagerId(Long managerId) {
        return equal("managerId", managerId);
    }

    public static Specification<DepartmentModel> isActive(Boolean isActive) {
        return equal("isActive", isActive);
    }

    public static Specification<DepartmentModel> containsName(String search) {
        return like("name", search);
    }

    public static Specification<DepartmentModel> containsCode(String search) {
        return like("code", search);
    }

    public static Specification<DepartmentModel> containsDescription(String search) {
        return like("description", search);
    }

    public static Specification<DepartmentModel> searchByKeyword(String search) {
        if (search == null || search.trim().isEmpty()) {
            return alwaysTrue();
        }
        return containsName(search)
                .or(containsCode(search))
                .or(containsDescription(search));
    }

    public static Specification<DepartmentModel> buildSpec(GetDepartmentParams params) {
        Specification<DepartmentModel> spec = alwaysTrue();

        if (params.getOrganizationId() != null) {
            spec = spec.and(hasOrganizationId(params.getOrganizationId()));
        }
        if (params.getParentDepartmentId() != null) {
            spec = spec.and(hasParentDepartmentId(params.getParentDepartmentId()));
        }
        if (params.getManagerId() != null) {
            spec = spec.and(hasManagerId(params.getManagerId()));
        }
        if (params.getIsActive() != null) {
            spec = spec.and(isActive(params.getIsActive()));
        }
        if (params.getSearch() != null && !params.getSearch().trim().isEmpty()) {
            spec = spec.and(searchByKeyword(params.getSearch()));
        }

        return spec;
    }
}
