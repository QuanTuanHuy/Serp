/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.UserDepartmentEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUserDepartmentPort {
    UserDepartmentEntity save(UserDepartmentEntity userDepartment);

    Optional<UserDepartmentEntity> getByUserIdAndDepartmentId(Long userId, Long departmentId);

    List<UserDepartmentEntity> getByUserId(Long userId);

    List<UserDepartmentEntity> getActiveByUserId(Long userId);

    List<UserDepartmentEntity> getByDepartmentId(Long departmentId);

    List<UserDepartmentEntity> getActiveByDepartmentId(Long departmentId);

    Optional<UserDepartmentEntity> getPrimaryByUserId(Long userId);

    Long countByDepartmentId(Long departmentId);

    Long countActiveByDepartmentId(Long departmentId);

    Long countByOrganizationId(Long organizationId);

    Long countActiveByOrganizationId(Long organizationId);

    Map<Long, Long> countByDepartmentIds(List<Long> departmentIds);

    Boolean existsByUserIdAndDepartmentId(Long userId, Long departmentId);

    void delete(Long userId, Long departmentId);
}
