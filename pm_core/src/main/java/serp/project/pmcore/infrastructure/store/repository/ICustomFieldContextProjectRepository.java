/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.CustomFieldContextProjectModel;

import java.util.List;

@Repository
public interface ICustomFieldContextProjectRepository extends JpaRepository<CustomFieldContextProjectModel, Long> {

    @Query("SELECT p FROM CustomFieldContextProjectModel p WHERE p.contextId = :contextId AND p.tenantId = :tenantId ORDER BY p.id ASC")
    List<CustomFieldContextProjectModel> findAllByContextIdAndTenantId(@Param("contextId") Long contextId,
                                                                       @Param("tenantId") Long tenantId);

    @Query("SELECT p FROM CustomFieldContextProjectModel p WHERE p.contextId = :contextId AND (p.tenantId = :tenantId OR p.tenantId = 0) ORDER BY p.id ASC")
    List<CustomFieldContextProjectModel> findAllByContextIdAndTenantIdOrSystemTenant(@Param("contextId") Long contextId,
                                                                                       @Param("tenantId") Long tenantId);
}
