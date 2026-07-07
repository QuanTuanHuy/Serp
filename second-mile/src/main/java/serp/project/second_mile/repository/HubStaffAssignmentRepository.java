/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.HubStaffAssignment;
import serp.project.second_mile.enums.HubStaffRole;
import serp.project.second_mile.enums.HubStaffStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HubStaffAssignmentRepository extends JpaRepository<HubStaffAssignment, Long> {
    Optional<HubStaffAssignment> findByIdAndTenantId(Long id, Long tenantId);

    @Query("""
            select a
            from HubStaffAssignment a
            join fetch a.hub h
            join fetch a.staff s
            where a.staff.id = :staffId
                and a.tenantId = :tenantId
                and a.assignedFrom <= :today
                and (a.assignedTo is null or a.assignedTo >= :today)
            order by a.id desc
            """)
    List<HubStaffAssignment> findActiveAssignmentsByStaffIdAndTenantId(
            @Param("staffId") Long staffId,
            @Param("tenantId") Long tenantId,
            @Param("today") LocalDate today
    );

    default Optional<HubStaffAssignment> findFirstActiveAssignmentByStaffIdAndHubIdAndTenantId(
            Long staffId,
            Long hubId,
            Long tenantId,
            LocalDate today
    ) {
        return findActiveAssignmentsByStaffIdAndTenantId(staffId, tenantId, today)
                .stream()
                .filter(assignment -> assignment.getHub() != null && hubId.equals(assignment.getHub().getId()))
                .findFirst();
    }

    @Query("""
            select a
            from HubStaffAssignment a
            join fetch a.hub h
            join fetch a.staff s
            where a.hub.id = :hubId
                and a.tenantId = :tenantId
                and a.assignedFrom <= :today
                and (a.assignedTo is null or a.assignedTo >= :today)
                and s.status = :staffStatus
            order by s.fullName asc
            """)
    List<HubStaffAssignment> findActiveAssignmentsByHubIdAndTenantIdAndStaffStatus(
            @Param("hubId") Long hubId,
            @Param("tenantId") Long tenantId,
            @Param("today") LocalDate today,
            @Param("staffStatus") HubStaffStatus staffStatus
    );

    @Query("""
            select a
            from HubStaffAssignment a
            join fetch a.hub h
            join fetch a.staff s
            where a.hub.id = :hubId
                and a.tenantId = :tenantId
                and a.assignedFrom <= :today
                and (a.assignedTo is null or a.assignedTo >= :today)
                and s.role = :staffRole
                and s.status = :staffStatus
            order by s.fullName asc
            """)
    List<HubStaffAssignment> findActiveAssignmentsByHubIdAndTenantIdAndStaffRoleAndStatus(
            @Param("hubId") Long hubId,
            @Param("tenantId") Long tenantId,
            @Param("today") LocalDate today,
            @Param("staffRole") HubStaffRole staffRole,
            @Param("staffStatus") HubStaffStatus staffStatus
    );

    @Query("""
            select a
            from HubStaffAssignment a
            join fetch a.hub h
            join fetch a.staff s
            where a.hub.id = :hubId
                and a.tenantId = :tenantId
                and a.assignedFrom <= :today
                and (a.assignedTo is null or a.assignedTo >= :today)
                and s.role = :staffRole
            order by s.fullName asc
            """)
    List<HubStaffAssignment> findActiveAssignmentsByHubIdAndTenantIdAndStaffRole(
            @Param("hubId") Long hubId,
            @Param("tenantId") Long tenantId,
            @Param("today") LocalDate today,
            @Param("staffRole") HubStaffRole staffRole
    );
}
