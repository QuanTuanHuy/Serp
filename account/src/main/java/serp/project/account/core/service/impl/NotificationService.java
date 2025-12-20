package serp.project.account.core.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.constant.KafkaConstants;
import serp.project.account.core.domain.dto.message.BaseKafkaMessage;
import serp.project.account.core.domain.dto.message.CreateNotificationEvent;
import serp.project.account.core.port.client.IKafkaProducer;
import serp.project.account.core.service.INotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements INotificationService {
    private final IKafkaProducer kafkaProducer;

    @Override
    public void sendNotification(CreateNotificationEvent event) {
        var kafkaMessage = BaseKafkaMessage.<CreateNotificationEvent>of(
                Constants.SERVICE_NAME,
                KafkaConstants.Notification.EVENT_CREATE_REQUESTED,
                event);
        kafkaProducer.sendMessageAsync(
                kafkaMessage.getMeta().getEventId(),
                kafkaMessage,
                KafkaConstants.Notification.USER_NOTIFICATION_TOPIC);
        log.info("Send notification for user: {}", event.getUserId());
    }
}
