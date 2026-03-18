/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.NotificationSchemeEntity;

import java.util.Optional;

public interface INotificationSchemePort {
    Optional<NotificationSchemeEntity> getNotificationSchemeById(Long schemeId, Long tenantId);

    Optional<NotificationSchemeEntity> getNotificationSchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
