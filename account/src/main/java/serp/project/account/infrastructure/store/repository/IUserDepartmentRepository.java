/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import org.springframework.stereotype.Repository;
import serp.project.account.infrastructure.store.model.UserDepartmentModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserDepartmentRepository extends IBaseRepository<UserDepartmentModel> {

    Optional<UserDepartmentModel> findByUserIdAndDepartmentId(Long userId, Long departmentId);

    List<UserDepartmentModel> findByUserId(Long userId);

    List<UserDepartmentModel> findByUserIdAndIsActive(Long userId, Boolean isActive);

    Optional<UserDepartmentModel> findByUserIdAndIsPrimaryAndIsActive(Long userId, Boolean isPrimary, Boolean isActive);

    List<UserDepartmentModel> findByDepartmentId(Long departmentId);

    List<UserDepartmentModel> findByDepartmentIdAndIsActive(Long departmentId, Boolean isActive);

    Long countByDepartmentId(Long departmentId);

    Long countByDepartmentIdAndIsActive(Long departmentId, Boolean isActive);

    Boolean existsByUserIdAndDepartmentId(Long userId, Long departmentId);
}
