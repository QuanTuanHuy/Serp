/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for typing start handler
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
import static org.mockito.Mockito.verify;

class TypingStartHandlerTest {

    @Test
    @DisplayName("typing start should deliver ready payload to channel except sender")
    void testHandle_DeliversTypingEventExceptSender() {
        IDeliveryService deliveryService = mock(IDeliveryService.class);
        IRealtimeDeliveryService realtimeDeliveryService = mock(IRealtimeDeliveryService.class);
        TypingStartHandler handler = new TypingStartHandler(deliveryService, realtimeDeliveryService);
        WsEvent<Map<String, Object>> event = WsEvent.<Map<String, Object>>builder()
                .type(WsEventType.TYPING_START)
                .channelId(TestDataFactory.CHANNEL_ID)
                .payload(Map.of("userId", TestDataFactory.USER_ID_1))
                .build();

        handler.handle(event);

        verify(realtimeDeliveryService).deliverToChannelExcept(
                TestDataFactory.CHANNEL_ID,
                TestDataFactory.USER_ID_1,
                event
        );
    }
}
