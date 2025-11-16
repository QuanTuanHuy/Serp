/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import org.springframework.data.jpa.repository.Query;
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

    @Query("SELECT COUNT(ud) FROM UserDepartmentModel ud "
            + "JOIN DepartmentModel d ON ud.departmentId = d.id "
            + "WHERE d.organizationId = :organizationId")
    Long countByOrganizationId(Long organizationId);

    @Query("SELECT COUNT(ud) FROM UserDepartmentModel ud "
            + "JOIN DepartmentModel d ON ud.departmentId = d.id "
            + "WHERE d.organizationId = :organizationId AND ud.isActive = true")
    Long countByOrganizationIdAndIsActive(Long organizationId, Boolean isActive);

    Boolean existsByUserIdAndDepartmentId(Long userId, Long departmentId);
}
