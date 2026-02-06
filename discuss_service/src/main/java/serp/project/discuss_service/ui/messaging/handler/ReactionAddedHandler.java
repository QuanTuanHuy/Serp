/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Handler for REACTION_ADDED events
 */

package serp.project.discuss_service.ui.messaging.handler;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.port.client.IWebSocketHubPort;
import serp.project.discuss_service.ui.messaging.WsEventPayloadUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReactionAddedHandler implements IReactionEventHandler {

    private final IWebSocketHubPort webSocketHub;

    @Override
    public WsEventType getType() {
        return WsEventType.REACTION_ADDED;
    }

    @Override
    public void handle(WsEvent<Map<String, Object>> event) {
        Long channelId = event.getChannelId();
        Map<String, Object> payload = event.getPayload();
        Long messageId = WsEventPayloadUtils.getLong(payload, "messageId");
        Long userId = WsEventPayloadUtils.getLong(payload, "userId");
        String emoji = WsEventPayloadUtils.getString(payload, "emoji");

        if (channelId == null || messageId == null || userId == null || emoji == null) {
            log.warn("Missing required fields for REACTION_ADDED event");
            return;
        }

        webSocketHub.notifyReaction(channelId, messageId, userId, emoji, true);
    }
}
