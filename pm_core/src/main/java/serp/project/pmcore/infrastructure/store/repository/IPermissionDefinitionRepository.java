/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.PermissionDefinitionModel;

import java.util.List;

@Repository
public interface IPermissionDefinitionRepository extends JpaRepository<PermissionDefinitionModel, Long> {

    @Query("""
            SELECT d
            FROM PermissionDefinitionModel d
            WHERE d.tenantId = :tenantId OR d.tenantId = 0
            ORDER BY d.category ASC, d.name ASC, d.permissionKey ASC
            """)
    List<PermissionDefinitionModel> findAllByTenantIdOrSystemTenant(@Param("tenantId") Long tenantId);
}
