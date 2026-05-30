/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.notification.port;

import java.util.Optional;

import serp.project.pmcore.domain.notification.entity.NotificationEventEntity;

public interface INotificationEventPort {
    Optional<NotificationEventEntity> getNotificationEventById(Long eventId, Long tenantId);

    Optional<NotificationEventEntity> getNotificationEventByIdIncludingSystem(Long eventId, Long tenantId);

    Optional<NotificationEventEntity> getNotificationEventByEventKeyIncludingSystem(String eventKey, Long tenantId);
}
