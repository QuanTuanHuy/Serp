/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.NotificationSchemeEntryModel;

import java.util.List;

@Repository
public interface INotificationSchemeEntryRepository extends JpaRepository<NotificationSchemeEntryModel, Long> {
    @Query("SELECT e FROM NotificationSchemeEntryModel e WHERE e.schemeId = :schemeId AND (e.tenantId = :tenantId OR e.tenantId = 0) ORDER BY e.id ASC")
    List<NotificationSchemeEntryModel> findAllBySchemeIdAndTenantIdOrSystemTenant(@Param("schemeId") Long schemeId,
                                                                                   @Param("tenantId") Long tenantId);
}
