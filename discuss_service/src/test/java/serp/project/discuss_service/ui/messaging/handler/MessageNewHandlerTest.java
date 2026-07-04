/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for message new handler
 */

package serp.project.discuss_service.ui.messaging.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.service.IDeliveryService;
import serp.project.discuss_service.core.service.IRealtimeDeliveryService;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MessageNewHandlerTest {

    @Test
    @DisplayName("ready payload should be delivered directly without legacy reload")
    void testHandle_ReadyPayload_DeliversDirectly() {
        IDeliveryService deliveryService = mock(IDeliveryService.class);
        IRealtimeDeliveryService realtimeDeliveryService = mock(IRealtimeDeliveryService.class);
        MessageNewHandler handler = new MessageNewHandler(deliveryService, realtimeDeliveryService);
        WsEvent<Map<String, Object>> event = WsEvent.<Map<String, Object>>builder()
                .type(WsEventType.MESSAGE_NEW)
                .channelId(TestDataFactory.CHANNEL_ID)
                .payload(Map.of(
                        "messageId", TestDataFactory.MESSAGE_ID,
                        "message", Map.of("id", TestDataFactory.MESSAGE_ID)
                ))
                .build();

        handler.handle(event);

        verify(realtimeDeliveryService).deliverToChannel(TestDataFactory.CHANNEL_ID, event);
        verify(deliveryService, never()).notifyNewMessage(TestDataFactory.CHANNEL_ID, TestDataFactory.MESSAGE_ID);
    }

    @Test
    @DisplayName("legacy id-only payload should use old reload path")
    void testHandle_LegacyPayload_UsesLegacyPath() {
        IDeliveryService deliveryService = mock(IDeliveryService.class);
        IRealtimeDeliveryService realtimeDeliveryService = mock(IRealtimeDeliveryService.class);
        MessageNewHandler handler = new MessageNewHandler(deliveryService, realtimeDeliveryService);
        WsEvent<Map<String, Object>> event = WsEvent.<Map<String, Object>>builder()
                .type(WsEventType.MESSAGE_NEW)
                .channelId(TestDataFactory.CHANNEL_ID)
                .payload(Map.of("messageId", TestDataFactory.MESSAGE_ID))
                .build();

        handler.handle(event);

        verify(deliveryService).notifyNewMessage(TestDataFactory.CHANNEL_ID, TestDataFactory.MESSAGE_ID);
        verify(realtimeDeliveryService, never()).deliverToChannel(TestDataFactory.CHANNEL_ID, event);
    }
}
