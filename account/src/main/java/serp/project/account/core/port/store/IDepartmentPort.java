/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.account.core.domain.dto.request.GetDepartmentParams;
import serp.project.account.core.domain.entity.DepartmentEntity;

import java.util.List;
import java.util.Optional;

public interface IDepartmentPort {
    DepartmentEntity save(DepartmentEntity department);

    Optional<DepartmentEntity> getById(Long id);

    Optional<DepartmentEntity> getByOrganizationIdAndCode(Long organizationId, String code);

    List<DepartmentEntity> getByOrganizationId(Long organizationId);

    List<DepartmentEntity> getActiveByOrganizationId(Long organizationId);

    List<DepartmentEntity> getByParentDepartmentId(Long parentDepartmentId);

    List<DepartmentEntity> getActiveByParentDepartmentId(Long parentDepartmentId);

    List<DepartmentEntity> getByManagerId(Long managerId);

    Pair<List<DepartmentEntity>, Long> getDepartments(GetDepartmentParams params);

    Optional<DepartmentEntity> getLatestByOrganizationId(Long organizationId);

    Long countByOrganizationId(Long organizationId);

    Long countActiveByOrganizationId(Long organizationId);

    void delete(Long id);
}
