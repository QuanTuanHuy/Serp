/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.notification.service;

import serp.project.pmcore.domain.notification.dto.WorkItemStatusChangeNotificationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

public interface IWorkItemNotificationOutboxPublisher {
    void publishWorkItemCreatedNotifications(ProjectEntity project,
                                             WorkItemEntity workItem,
                                             Long tenantId,
                                             Long actorId,
                                             Long sourceEventId);

    void publishWorkItemAssignedNotifications(ProjectEntity project,
                                              WorkItemEntity workItem,
                                              Long tenantId,
                                              Long actorId,
                                              Long sourceEventId);

    void publishWorkItemStatusChangedNotifications(ProjectEntity project,
                                                   WorkItemEntity workItem,
                                                   Long tenantId,
                                                   Long actorId,
                                                   Long sourceEventId,
                                                   WorkItemStatusChangeNotificationContext context);
}
