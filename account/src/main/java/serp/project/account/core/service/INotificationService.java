package serp.project.account.core.service;

import serp.project.account.core.domain.dto.message.CreateNotificationEvent;

public interface INotificationService {
    void sendNotification(CreateNotificationEvent event);
}
