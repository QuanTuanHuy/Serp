/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Handler for TYPING_START events
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
public class TypingStartHandler implements IPresenceEventHandler {

    private final IWebSocketHubPort webSocketHub;

    @Override
    public WsEventType getType() {
        return WsEventType.TYPING_START;
    }

    @Override
    public void handle(WsEvent<Map<String, Object>> event) {
        Long channelId = event.getChannelId();
        Long userId = WsEventPayloadUtils.getLong(event.getPayload(), "userId");
        if (channelId == null || userId == null) {
            log.warn("Missing required fields for TYPING_START event");
            return;
        }

        webSocketHub.notifyTyping(channelId, userId, true);
    }
}
