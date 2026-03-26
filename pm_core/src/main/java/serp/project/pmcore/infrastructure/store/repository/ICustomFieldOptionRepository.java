/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.CustomFieldOptionModel;

import java.util.List;

@Repository
public interface ICustomFieldOptionRepository extends JpaRepository<CustomFieldOptionModel, Long> {

    @Query("SELECT o FROM CustomFieldOptionModel o WHERE o.customFieldContextId = :contextId AND o.tenantId = :tenantId ORDER BY o.sequence ASC, o.id ASC")
    List<CustomFieldOptionModel> findAllByContextIdAndTenantId(@Param("contextId") Long contextId,
                                                               @Param("tenantId") Long tenantId);

    @Query("SELECT o FROM CustomFieldOptionModel o WHERE o.customFieldContextId = :contextId AND (o.tenantId = :tenantId OR o.tenantId = 0) ORDER BY o.sequence ASC, o.id ASC")
    List<CustomFieldOptionModel> findAllByContextIdAndTenantIdOrSystemTenant(@Param("contextId") Long contextId,
                                                                              @Param("tenantId") Long tenantId);
}
