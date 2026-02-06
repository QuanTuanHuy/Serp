/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Handler for TYPING_STOP events
 */

package serp.project.discuss_service.ui.messaging.handler;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.port.client.IWebSocketHubPort;
import serp.project.discuss_service.kernel.utils.KafkaPayloadUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class TypingStopHandler implements IPresenceEventHandler {

    private final IWebSocketHubPort webSocketHub;

    @Override
    public WsEventType getType() {
        return WsEventType.TYPING_STOP;
    }

    @Override
    public void handle(WsEvent<Map<String, Object>> event) {
        Long channelId = event.getChannelId();
        Long userId = KafkaPayloadUtils.getLong(event.getPayload(), "userId");
        if (channelId == null || userId == null) {
            log.warn("Missing required fields for TYPING_STOP event");
            return;
        }

        webSocketHub.notifyTyping(channelId, userId, false);
    }
}
