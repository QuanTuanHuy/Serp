/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import serp.project.account.core.domain.dto.request.AssignUserToDepartmentRequest;
import serp.project.account.core.domain.dto.request.BulkAssignUsersToDepartmentRequest;
import serp.project.account.core.domain.entity.UserDepartmentEntity;

import java.util.List;
import java.util.Map;

public interface IUserDepartmentService {
    UserDepartmentEntity assignUserToDepartment(AssignUserToDepartmentRequest request);

    List<UserDepartmentEntity> bulkAssignUsersToDepartment(BulkAssignUsersToDepartmentRequest request);

    void removeUserFromDepartment(Long userId, Long departmentId);

    List<UserDepartmentEntity> getUserDepartmentsByUserId(Long userId);

    List<UserDepartmentEntity> getDepartmentMembers(Long departmentId);

    List<UserDepartmentEntity> getActiveDepartmentMembers(Long departmentId);

    Long countMembersByDepartmentId(Long departmentId);

    Map<Long, Long> countMembersByDepartmentIds(List<Long> departmentIds);
}
