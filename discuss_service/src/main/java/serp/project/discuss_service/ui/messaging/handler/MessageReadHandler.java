/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Handler for MESSAGE_READ events
 */

package serp.project.discuss_service.ui.messaging.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.service.IDeliveryService;
import serp.project.discuss_service.kernel.utils.KafkaPayloadUtils;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageReadHandler implements IMessageEventHandler {

    private final IDeliveryService deliveryService;

    @Override
    public WsEventType getType() {
        return WsEventType.MESSAGE_READ;
    }

    @Override
    public void handle(WsEvent<Map<String, Object>> event) {
        Long channelId = event.getChannelId();
        Long messageId = KafkaPayloadUtils.getLong(event.getPayload(), "messageId");
        Long userId = KafkaPayloadUtils.getLong(event.getPayload(), "userId");
        Integer readCount = KafkaPayloadUtils.getInteger(event.getPayload(), "readCount");
        List<Long> readBy = KafkaPayloadUtils.getLongList(event.getPayload(), "readBy");

        if (channelId == null || messageId == null || userId == null) {
            log.warn("Missing required fields for MESSAGE_READ event");
            return;
        }

        deliveryService.notifyMessageRead(channelId, messageId, userId, readBy, readCount);
    }
}
