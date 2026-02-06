/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Handler for USER_OFFLINE events
 */

package serp.project.discuss_service.ui.messaging.handler;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.ui.messaging.WsEventPayloadUtils;

@Component
@Slf4j
public class UserOfflineHandler implements IPresenceEventHandler {

    @Override
    public WsEventType getType() {
        return WsEventType.USER_OFFLINE;
    }

    @Override
    public void handle(WsEvent<Map<String, Object>> event) {
        Long userId = WsEventPayloadUtils.getLong(event.getPayload(), "userId");
        log.debug("Received presence event: {} for user {}", event.getType(), userId);
    }
}
