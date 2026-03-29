/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.notification.port;

import java.util.List;

import serp.project.pmcore.domain.notification.entity.NotificationSchemeEntryEntity;

public interface INotificationSchemeEntryPort {
    List<NotificationSchemeEntryEntity> createNotificationSchemeEntries(List<NotificationSchemeEntryEntity> entries);

    List<NotificationSchemeEntryEntity> getNotificationSchemeEntriesBySchemeIdIncludingSystem(Long schemeId, Long tenantId);
}
