package serp.project.school_bus_service.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.entity.SchoolBusUserRoleEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.Collection;
import java.util.List;

public interface SchoolBusUserRoleRepository extends BaseRepository<SchoolBusUserRoleEntity, Long> {

    @Modifying(flushAutomatically = true)
    @Transactional
    void deleteBySchoolBusUserId(Long schoolBusUserId);

    @Query("""
            select distinct role.schoolBusUserId
            from SchoolBusUserRoleEntity role
            where role.tenantId = :tenantId
              and role.roleName in :roleNames
              and role.isActive = true
              and role.isDeleted = false
            """)
    List<Long> findActiveUserIdsByTenantAndRoleNames(
            @Param("tenantId") Long tenantId,
            @Param("roleNames") Collection<String> roleNames);
}
