/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.HubStaff;
import serp.project.second_mile.enums.HubStaffRole;
import serp.project.second_mile.enums.HubStaffStatus;
import serp.project.second_mile.repository.projection.CodeNameProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HubStaffRepository extends JpaRepository<HubStaff, Long>, JpaSpecificationExecutor<HubStaff> {
    Optional<HubStaff> findByCode(String code);

    Optional<HubStaff> findByIdAndTenantId(Long id, Long tenantId);

    @Query("""
            select s.code as code, s.fullName as name
            from HubStaff s
            where s.tenantId = :tenantId
                and s.role = :role
                and s.status = :status
            order by s.fullName asc
            """)
    List<CodeNameProjection> findTemplateCodeNameListByTenantIdAndRoleAndStatus(
            @Param("tenantId") Long tenantId,
            @Param("role") HubStaffRole role,
            @Param("status") HubStaffStatus status
    );

    List<HubStaff> findByTenantIdAndRoleAndStatus(
            Long tenantId,
            HubStaffRole role,
            HubStaffStatus status
    );

    Optional<HubStaff> findByTenantIdAndUserIdAndRoleAndStatus(
            Long tenantId,
            Long userId,
            HubStaffRole role,
            HubStaffStatus status
    );

    @Query("""
            select s
            from HubStaff s
            where s.tenantId = :tenantId
                and s.role = :role
                and s.status = :status
                and (
                    :keywordLike is null
                    or lower(coalesce(s.code, '')) like :keywordLike
                    or lower(coalesce(s.fullName, '')) like :keywordLike
                )
                and not exists (
                    select 1
                    from HubStaffAssignment a
                    where a.staff = s
                        and a.tenantId = :tenantId
                        and a.assignedFrom <= :today
                        and (a.assignedTo is null or a.assignedTo >= :today)
                )
            order by s.fullName asc
            """)
    List<HubStaff> findAssignableByTenantIdAndRoleAndStatusAndKeyword(
            @Param("tenantId") Long tenantId,
            @Param("role") HubStaffRole role,
            @Param("status") HubStaffStatus status,
            @Param("keywordLike") String keywordLike,
            @Param("today") LocalDate today
    );

    boolean existsByTenantIdAndUserIdAndRoleInAndStatus(
            Long tenantId,
            Long userId,
            List<HubStaffRole> roles,
            HubStaffStatus status
    );

    boolean existsByTenantIdAndIdAndRoleAndStatus(
            Long tenantId,
            Long id,
            HubStaffRole role,
            HubStaffStatus status
    );

    boolean existsByTenantIdAndIdAndUserIdAndRoleAndStatus(
            Long tenantId,
            Long id,
            Long userId,
            HubStaffRole role,
            HubStaffStatus status
    );
}
