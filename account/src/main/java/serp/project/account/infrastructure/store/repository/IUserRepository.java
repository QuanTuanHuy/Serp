/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.infrastructure.store.model.UserModel;

@Repository
public interface IUserRepository extends IBaseRepository<UserModel> {
    Optional<UserModel> findByEmail(String email);

    List<UserModel> findByIdIn(List<Long> ids);

    List<UserModel> findByPrimaryOrganizationId(Long organizationId);

    Integer countByPrimaryOrganizationId(Long organizationId);

    Integer countByPrimaryOrganizationIdAndStatus(Long organizationId, UserStatus status);

    @Query("SELECT COUNT(u) FROM UserModel u WHERE u.primaryOrganizationId = :orgId " +
            "AND u.createdAt >= :from AND u.createdAt < :to")
    Integer countByOrganizationIdAndCreatedAtBetween(
            @Param("orgId") Long organizationId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(DISTINCT ur.userId) FROM UserRoleModel ur " +
            "JOIN UserModel u ON ur.userId = u.id " +
            "JOIN RoleModel r ON ur.roleId = r.id " +
            "WHERE u.primaryOrganizationId = :orgId " +
            "AND (r.roleType = 'ADMIN' OR r.roleType = 'OWNER')")
    Integer countAdminUsersByOrganizationId(@Param("orgId") Long organizationId);

    @Query("SELECT u.status AS status, COUNT(u) AS count " +
            "FROM UserModel u " +
            "WHERE u.primaryOrganizationId = :orgId " +
            "GROUP BY u.status")
    List<Object[]> countUsersByStatusForOrganization(@Param("orgId") Long organizationId);

    List<UserModel> findByPrimaryOrganizationIdAndIdIn(Long organizationId, List<Long> ids);
}
