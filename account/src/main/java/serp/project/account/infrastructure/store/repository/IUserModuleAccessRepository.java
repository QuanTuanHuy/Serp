/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.account.infrastructure.store.model.UserModuleAccessModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserModuleAccessRepository extends IBaseRepository<UserModuleAccessModel> {
        Optional<UserModuleAccessModel> findByUserIdAndModuleIdAndOrganizationId(
                        Long userId, Long moduleId, Long organizationId);

        List<UserModuleAccessModel> findByUserId(Long userId);

        List<UserModuleAccessModel> findByUserIdAndOrganizationIdAndIsActive(Long userId, Long organizationId,
                        Boolean isActive);

        List<UserModuleAccessModel> findByModuleIdAndOrganizationIdAndIsActive(
                        Long moduleId, Long organizationId, Boolean isActive);

        boolean existsByUserIdAndModuleIdAndOrganizationIdAndIsActive(
                        Long userId, Long moduleId, Long organizationId, Boolean isActive);

        @Query("SELECT COUNT(u) FROM UserModuleAccessModel u " +
                        "WHERE u.moduleId = :moduleId " +
                        "AND u.organizationId = :organizationId " +
                        "AND u.isActive = true")
        int countActiveUsersByModuleAndOrganization(
                        @Param("moduleId") Long moduleId,
                        @Param("organizationId") Long organizationId);
}
