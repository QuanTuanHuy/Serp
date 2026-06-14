package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.request.BulkNotificationSendCommand;
import serp.project.school_bus_service.dto.request.NotificationSendCommand;

public interface ISchoolBusNotificationService {

    /**
     * Queues one user notification for publication after the current transaction commits.
     *
     * @return generated notification event ID
     */
    String sendNotification(NotificationSendCommand command);

    /**
     * Queues one bulk notification event for publication after the current transaction commits.
     *
     * @return generated notification event ID
     */
    String sendBulkNotification(BulkNotificationSendCommand command);
}
