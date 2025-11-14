/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import org.springframework.data.util.Pair;
import serp.project.account.core.domain.dto.request.CreateDepartmentRequest;
import serp.project.account.core.domain.dto.request.GetDepartmentParams;
import serp.project.account.core.domain.dto.request.UpdateDepartmentRequest;
import serp.project.account.core.domain.entity.DepartmentEntity;

import java.util.List;

public interface IDepartmentService {
    DepartmentEntity createDepartment(Long organizationId, CreateDepartmentRequest request);

    DepartmentEntity updateDepartment(Long departmentId, UpdateDepartmentRequest request);

    DepartmentEntity getDepartmentById(Long departmentId);

    List<DepartmentEntity> getDepartmentsByIds(List<Long> departmentIds);

    List<DepartmentEntity> getDepartmentsByOrganizationId(Long organizationId);

    List<DepartmentEntity> getActiveDepartmentsByOrganizationId(Long organizationId);

    List<DepartmentEntity> getChildDepartments(Long parentDepartmentId);

    Pair<List<DepartmentEntity>, Long> getDepartments(GetDepartmentParams params);

    void deleteDepartment(Long departmentId);

    void validateManager(Long managerId, Long organizationId);

    void validateParentDepartment(Long departmentId, Long parentDepartmentId, Long organizationId);
}
