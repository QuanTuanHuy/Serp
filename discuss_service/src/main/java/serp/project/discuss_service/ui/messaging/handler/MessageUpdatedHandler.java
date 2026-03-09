/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Handler for MESSAGE_UPDATED events
 */

package serp.project.discuss_service.ui.messaging.handler;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.service.IDeliveryService;
import serp.project.discuss_service.kernel.utils.KafkaPayloadUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageUpdatedHandler implements IMessageEventHandler {

    private final IDeliveryService deliveryService;

    @Override
    public WsEventType getType() {
        return WsEventType.MESSAGE_UPDATED;
    }

    @Override
    public void handle(WsEvent<Map<String, Object>> event) {
        Long channelId = event.getChannelId();
        Long messageId = KafkaPayloadUtils.getLong(event.getPayload(), "messageId");
        if (channelId == null || messageId == null) {
            log.warn("Missing required fields for MESSAGE_UPDATED event");
            return;
        }
        deliveryService.notifyMessageUpdated(channelId, messageId);
    }
}
