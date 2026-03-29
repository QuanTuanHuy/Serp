/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.notification.port;

import java.util.Optional;

import serp.project.pmcore.domain.notification.entity.NotificationSchemeEntity;

public interface INotificationSchemePort {
    NotificationSchemeEntity createNotificationScheme(NotificationSchemeEntity scheme);

    Optional<NotificationSchemeEntity> getNotificationSchemeById(Long schemeId, Long tenantId);

    Optional<NotificationSchemeEntity> getNotificationSchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
