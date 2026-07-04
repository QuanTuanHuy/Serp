/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for user online handler
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

class UserOnlineHandlerTest {

    @Test
    @DisplayName("user online should deliver presence change through realtime delivery service")
    void testHandle_DeliversPresenceChange() {
        IDeliveryService deliveryService = mock(IDeliveryService.class);
        IRealtimeDeliveryService realtimeDeliveryService = mock(IRealtimeDeliveryService.class);
        UserOnlineHandler handler = new UserOnlineHandler(deliveryService, realtimeDeliveryService);
        WsEvent<Map<String, Object>> event = WsEvent.<Map<String, Object>>builder()
                .type(WsEventType.USER_ONLINE)
                .payload(Map.of("userId", TestDataFactory.USER_ID_1))
                .build();

        handler.handle(event);

        verify(realtimeDeliveryService).deliverPresenceChange(TestDataFactory.USER_ID_1, event);
    }
}
