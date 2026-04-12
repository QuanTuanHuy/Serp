/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.NotificationEventModel;

import java.util.Optional;

@Repository
public interface INotificationEventRepository extends JpaRepository<NotificationEventModel, Long> {
    Optional<NotificationEventModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT e FROM NotificationEventModel e WHERE e.id = :id AND (e.tenantId = :tenantId OR e.tenantId = 0)")
    Optional<NotificationEventModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
