/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.NotificationSchemeEntryEntity;

import java.util.List;

public interface INotificationSchemeEntryPort {
    List<NotificationSchemeEntryEntity> createNotificationSchemeEntries(List<NotificationSchemeEntryEntity> entries);

    List<NotificationSchemeEntryEntity> getNotificationSchemeEntriesBySchemeIdIncludingSystem(Long schemeId, Long tenantId);
}
