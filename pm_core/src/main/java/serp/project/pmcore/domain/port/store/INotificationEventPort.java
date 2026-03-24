/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.NotificationEventEntity;

import java.util.Optional;

public interface INotificationEventPort {
    Optional<NotificationEventEntity> getNotificationEventById(Long eventId, Long tenantId);

    Optional<NotificationEventEntity> getNotificationEventByIdIncludingSystem(Long eventId, Long tenantId);
}
