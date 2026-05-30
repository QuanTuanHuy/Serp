/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.PostOfficeStaffStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostOfficeStaffRepository extends JpaRepository<PostOfficeStaff, Long> {
    boolean existsByCode(String code);

    Optional<PostOfficeStaff> findByCode(String code);

    Optional<PostOfficeStaff> findByCodeAndTenantId(String code, Long tenantId);

    Optional<PostOfficeStaff> findByIdAndTenantId(Long id, Long tenantId);

    List<PostOfficeStaff> findByTenantIdAndRoleAndStatus(
            Long tenantId,
            PostOfficeStaffRole role,
            PostOfficeStaffStatus status
    );

    @Query("""
            select s
            from PostOfficeStaff s
            where s.tenantId = :tenantId
                and s.role = :role
                and s.status = :status
                and (
                    :keyword is null
                    or lower(coalesce(s.code, '')) like concat('%', lower(:keyword), '%')
                    or lower(coalesce(s.fullName, '')) like concat('%', lower(:keyword), '%')
                )
            order by s.fullName asc
            """)
    List<PostOfficeStaff> findAssignableByTenantIdAndRoleAndStatusAndKeyword(
            @Param("tenantId") Long tenantId,
            @Param("role") PostOfficeStaffRole role,
            @Param("status") PostOfficeStaffStatus status,
            @Param("keyword") String keyword
    );

    List<PostOfficeStaff> findByTenantIdAndIdIn(Long tenantId, Collection<Long> ids);
}
