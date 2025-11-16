/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import org.springframework.stereotype.Repository;
import serp.project.account.infrastructure.store.model.DepartmentModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDepartmentRepository extends IBaseRepository<DepartmentModel> {
    Optional<DepartmentModel> findByOrganizationIdAndCode(Long organizationId, String code);

    List<DepartmentModel> findByOrganizationId(Long organizationId);

    List<DepartmentModel> findByOrganizationIdAndIsActive(Long organizationId, Boolean isActive);

    List<DepartmentModel> findByParentDepartmentId(Long parentDepartmentId);

    List<DepartmentModel> findByParentDepartmentIdAndIsActive(Long parentDepartmentId, Boolean isActive);

    List<DepartmentModel> findByIdIn(List<Long> departmentIds);

    List<DepartmentModel> findByManagerId(Long managerId);

    Optional<DepartmentModel> findTopByOrganizationIdOrderByIdDesc(Long organizationId);

    Long countByOrganizationId(Long organizationId);

    Long countByOrganizationIdAndIsActive(Long organizationId, Boolean isActive);

    Long countDistinctByOrganizationIdAndManagerIdIsNotNull(Long organizationId);
}
