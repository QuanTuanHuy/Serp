/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.PostOfficeStaffAssignment;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.PostOfficeStaffStatus;
import serp.project.first_mile.repository.projection.CodeNameProjection;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PostOfficeStaffAssignmentRepository extends JpaRepository<PostOfficeStaffAssignment, Long> {
    boolean existsByStaffIdAndAssignedToIsNull(Long staffId);

    @EntityGraph(attributePaths = {"staff", "postOffice"})
    Optional<PostOfficeStaffAssignment> findByIdAndTenantId(Long id, Long tenantId);

    @Query("""
        select a
        from PostOfficeStaffAssignment a
        join fetch a.staff s
        join fetch a.postOffice p
        where a.staff.id = :staffId
        and a.postOffice.id = :postOfficeId
        and a.tenantId = :tenantId
        and a.assignedFrom <= :today
        and (a.assignedTo is null or a.assignedTo >= :today)
        order by a.id desc
        """)
    List<PostOfficeStaffAssignment> findActiveAssignmentsByStaffIdAndPostOfficeIdAndTenantId(
        @Param("staffId") Long staffId,
        @Param("postOfficeId") Long postOfficeId,
        @Param("tenantId") Long tenantId,
        @Param("today") LocalDate today
    );

    default Optional<PostOfficeStaffAssignment> findFirstActiveAssignmentByStaffIdAndPostOfficeIdAndTenantId(
        Long staffId,
        Long postOfficeId,
        Long tenantId,
        LocalDate today
    ) {
        return findActiveAssignmentsByStaffIdAndPostOfficeIdAndTenantId(staffId, postOfficeId, tenantId, today)
                .stream()
                .findFirst();
    }

    @Query("""
        select distinct a.postOffice.id
        from PostOfficeStaffAssignment a
        where a.staff.id = :staffId
        and a.tenantId = :tenantId
        and a.assignedFrom <= :today
        and (a.assignedTo is null or a.assignedTo >= :today)
        """)
    Set<Long> findActivePostOfficeIdsByStaffIdAndTenantId(
        @Param("staffId") Long staffId,
        @Param("tenantId") Long tenantId,
        @Param("today") LocalDate today
    );

    @Query("""
        select (count(a) > 0)
        from PostOfficeStaffAssignment a
        where a.staff.id = :staffId
        and a.postOffice.id in :postOfficeIds
        and a.tenantId = :tenantId
        and a.assignedFrom <= :today
        and (a.assignedTo is null or a.assignedTo >= :today)
        """)
    boolean existsActiveAssignmentByStaffIdAndPostOfficeIdsAndTenantId(
        @Param("staffId") Long staffId,
        @Param("postOfficeIds") Collection<Long> postOfficeIds,
        @Param("tenantId") Long tenantId,
        @Param("today") LocalDate today
    );

    @Query("""
        select (count(a) > 0)
        from PostOfficeStaffAssignment a
        where a.staff.id = :staffId
        and a.postOffice.id = :postOfficeId
        and a.tenantId = :tenantId
        and a.assignedFrom <= :today
        and (a.assignedTo is null or a.assignedTo >= :today)
        """)
    boolean existsActiveAssignmentByStaffIdAndPostOfficeIdAndTenantId(
        @Param("staffId") Long staffId,
        @Param("postOfficeId") Long postOfficeId,
        @Param("tenantId") Long tenantId,
        @Param("today") LocalDate today
    );

    @Query("""
        select a
        from PostOfficeStaffAssignment a
        join fetch a.staff s
        where a.postOffice.id = :postOfficeId
            and a.tenantId = :tenantId
            and a.assignedFrom <= :today
            and (a.assignedTo is null or a.assignedTo >= :today)
            and s.role = :role
            and s.status = :status
        order by a.isPrimary desc, a.id asc
        """)
    List<PostOfficeStaffAssignment> findActiveAssignmentsByPostOfficeIdAndTenantIdAndStaffRoleAndStaffStatus(
        @Param("postOfficeId") Long postOfficeId,
        @Param("tenantId") Long tenantId,
        @Param("today") LocalDate today,
        @Param("role") PostOfficeStaffRole role,
        @Param("status") PostOfficeStaffStatus status
    );

    @Query("""
        select a
        from PostOfficeStaffAssignment a
        join fetch a.staff s
        join fetch a.postOffice p
        where a.postOffice.id = :postOfficeId
            and a.tenantId = :tenantId
            and a.assignedFrom <= :today
            and (a.assignedTo is null or a.assignedTo >= :today)
            and s.role = :role
        order by a.id asc
        """)
    List<PostOfficeStaffAssignment> findActiveAssignmentsByPostOfficeIdAndTenantIdAndStaffRole(
        @Param("postOfficeId") Long postOfficeId,
        @Param("tenantId") Long tenantId,
        @Param("today") LocalDate today,
        @Param("role") PostOfficeStaffRole role
    );

    @Query("""
        select distinct s.code as code, s.fullName as name
        from PostOfficeStaffAssignment a
        join a.staff s
        where a.tenantId = :tenantId
            and a.assignedFrom <= :today
            and (a.assignedTo is null or a.assignedTo >= :today)
            and s.role = :role
            and s.status = :status
        order by s.fullName asc
        """)
    List<CodeNameProjection> findActiveCourierTemplateCodeNameListByTenantId(
        @Param("tenantId") Long tenantId,
        @Param("today") LocalDate today,
        @Param("role") PostOfficeStaffRole role,
        @Param("status") PostOfficeStaffStatus status
    );

    @Query("""
        select distinct s.code as code, s.fullName as name
        from PostOfficeStaffAssignment a
        join a.staff s
        where a.tenantId = :tenantId
            and a.postOffice.id in :postOfficeIds
            and a.assignedFrom <= :today
            and (a.assignedTo is null or a.assignedTo >= :today)
            and s.role = :role
            and s.status = :status
        order by s.fullName asc
        """)
    List<CodeNameProjection> findActiveCourierTemplateCodeNameListByTenantIdAndPostOfficeIds(
        @Param("tenantId") Long tenantId,
        @Param("postOfficeIds") Collection<Long> postOfficeIds,
        @Param("today") LocalDate today,
        @Param("role") PostOfficeStaffRole role,
        @Param("status") PostOfficeStaffStatus status
    );
}
