/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.ProjectRoleModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProjectRoleRepository extends JpaRepository<ProjectRoleModel, Long> {
    Optional<ProjectRoleModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT r FROM ProjectRoleModel r WHERE r.id = :id AND (r.tenantId = :tenantId OR r.tenantId = 0)")
    Optional<ProjectRoleModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id,
                                                                 @Param("tenantId") Long tenantId);

    @Query("SELECT r FROM ProjectRoleModel r WHERE r.name = :name AND (r.tenantId = :tenantId OR r.tenantId = 0) ORDER BY CASE WHEN r.tenantId = :tenantId THEN 0 ELSE 1 END, r.id ASC")
    List<ProjectRoleModel> findByNameAndTenantIdOrSystemTenant(@Param("name") String name,
                                                               @Param("tenantId") Long tenantId);

    @Query("SELECT r FROM ProjectRoleModel r WHERE r.tenantId = :tenantId OR r.tenantId = 0 ORDER BY CASE WHEN r.tenantId = :tenantId THEN 0 ELSE 1 END, r.name ASC, r.id ASC")
    List<ProjectRoleModel> findAllByTenantIdOrSystemTenant(@Param("tenantId") Long tenantId);
}
