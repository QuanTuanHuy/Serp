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

import java.util.List;
import java.util.Optional;

@Repository
public interface HubStaffRepository extends JpaRepository<HubStaff, Long>, JpaSpecificationExecutor<HubStaff> {
    Optional<HubStaff> findByCode(String code);

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
}
