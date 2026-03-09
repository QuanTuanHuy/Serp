/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket hub adapter with server-side fan-out delivery
 */

package serp.project.discuss_service.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.port.client.IWebSocketHubPort;

import java.util.Set;

/**
 * WebSocket hub adapter with server-side fan-out delivery (Telegram-style).
 * Clients only need to subscribe to a single destination: /user/queue/events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHubAdapter implements IWebSocketHubPort {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String USER_EVENTS_QUEUE = "/queue/events";

    @Override
    public void sendToUser(Long userId, Object payload) {
        if (userId == null || payload == null) {
            log.warn("Cannot send message to user with null userId or payload");
            return;
        }
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                USER_EVENTS_QUEUE,
                payload
        );
        log.debug("Sent event to user {} queue", userId);
    }

    @Override
    public void sendErrorToUser(Long userId, Object payload) {
        if (userId == null || payload == null) {
            log.warn("Cannot send error message to user with null userId or payload");
            return;
        }
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                USER_EVENTS_QUEUE,
                payload
        );
        log.debug("Sent error to user {} via events queue", userId);
    }

    @Override
    public void sendToUsers(Set<Long> userIds, Object payload) {
        if (userIds == null || userIds.isEmpty() || payload == null) {
            return;
        }
        for (Long userId : userIds) {
            sendToUser(userId, payload);
        }
        log.debug("Sent event to {} users", userIds.size());
    }

}
