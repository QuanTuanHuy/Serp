/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Handler for USER_ONLINE events
 */

package serp.project.discuss_service.ui.messaging.handler;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.kernel.utils.KafkaPayloadUtils;

@Component
@Slf4j
public class UserOnlineHandler implements IPresenceEventHandler {

    @Override
    public WsEventType getType() {
        return WsEventType.USER_ONLINE;
    }

    @Override
    public void handle(WsEvent<Map<String, Object>> event) {
        Long userId = KafkaPayloadUtils.getLong(event.getPayload(), "userId");
        log.debug("Received presence event: {} for user {}", event.getType(), userId);
    }
}
