/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for realtime delivery service
 */

package serp.project.discuss_service.core.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.port.client.IWebSocketHubPort;
import serp.project.discuss_service.core.service.IChannelMemberService;
import serp.project.discuss_service.core.service.IPresenceService;
import serp.project.discuss_service.kernel.property.RealtimeProperties;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RealtimeDeliveryServiceTest {

    @Test
    @DisplayName("deliverToChannel should filter offline users before fan-out")
    void testDeliverToChannel_FiltersOfflineUsers() {
        IWebSocketHubPort webSocketHub = mock(IWebSocketHubPort.class);
        IChannelMemberService memberService = mock(IChannelMemberService.class);
        IPresenceService presenceService = mock(IPresenceService.class);
        RealtimeDeliveryService service = service(webSocketHub, memberService, presenceService, 64);
        WsEvent<String> event = WsEvent.of(WsEventType.MESSAGE_NEW, "payload", TestDataFactory.CHANNEL_ID);
        Set<Long> members = Set.of(100L, 200L, 300L);
        when(memberService.getMemberIds(TestDataFactory.CHANNEL_ID)).thenReturn(members);
        when(presenceService.getOnlineUsers(members)).thenReturn(Set.of(100L, 300L));

        service.deliverToChannel(TestDataFactory.CHANNEL_ID, event);

        verify(webSocketHub).sendToUser(100L, event);
        verify(webSocketHub).sendToUser(300L, event);
    }

    @Test
    @DisplayName("deliverToChannelExcept should exclude sender before presence filtering")
    void testDeliverToChannelExcept_ExcludesSender() {
        IWebSocketHubPort webSocketHub = mock(IWebSocketHubPort.class);
        IChannelMemberService memberService = mock(IChannelMemberService.class);
        IPresenceService presenceService = mock(IPresenceService.class);
        RealtimeDeliveryService service = service(webSocketHub, memberService, presenceService, 64);
        WsEvent<String> event = WsEvent.of(WsEventType.TYPING_START, "payload", TestDataFactory.CHANNEL_ID);
        Set<Long> expectedPresenceInput = Set.of(200L, 300L);
        when(memberService.getMemberIds(TestDataFactory.CHANNEL_ID)).thenReturn(Set.of(100L, 200L, 300L));
        when(presenceService.getOnlineUsers(expectedPresenceInput)).thenReturn(Set.of(200L));

        service.deliverToChannelExcept(TestDataFactory.CHANNEL_ID, 100L, event);

        verify(presenceService).getOnlineUsers(expectedPresenceInput);
        verify(webSocketHub).sendToUser(200L, event);
    }

    @Test
    @DisplayName("deliverToUsers should continue when one user send fails")
    void testDeliverToUsers_OneSendFails_Continues() {
        AtomicInteger delivered = new AtomicInteger();
        IWebSocketHubPort webSocketHub = new IWebSocketHubPort() {
            @Override
            public void sendToUser(Long userId, Object payload) {
                if (userId.equals(200L)) {
                    throw new RuntimeException("send failed");
                }
                delivered.incrementAndGet();
            }

            @Override
            public void sendErrorToUser(Long userId, Object payload) {
            }

            @Override
            public void sendToUsers(Set<Long> userIds, Object payload) {
            }
        };
        IChannelMemberService memberService = mock(IChannelMemberService.class);
        IPresenceService presenceService = mock(IPresenceService.class);
        RealtimeDeliveryService service = service(webSocketHub, memberService, presenceService, 64);

        service.deliverToUsers(Set.of(100L, 200L, 300L), "payload");

        assertEquals(2, delivered.get());
    }

    @Test
    @DisplayName("deliverToUsers should respect max concurrency")
    void testDeliverToUsers_RespectsMaxConcurrency() {
        AtomicInteger inFlight = new AtomicInteger();
        AtomicInteger maxObserved = new AtomicInteger();
        IWebSocketHubPort webSocketHub = new IWebSocketHubPort() {
            @Override
            public void sendToUser(Long userId, Object payload) {
                int active = inFlight.incrementAndGet();
                maxObserved.updateAndGet(previous -> Math.max(previous, active));
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    inFlight.decrementAndGet();
                }
            }

            @Override
            public void sendErrorToUser(Long userId, Object payload) {
            }

            @Override
            public void sendToUsers(Set<Long> userIds, Object payload) {
            }
        };
        IChannelMemberService memberService = mock(IChannelMemberService.class);
        IPresenceService presenceService = mock(IPresenceService.class);
        RealtimeDeliveryService service = service(webSocketHub, memberService, presenceService, 2);

        service.deliverToUsers(Set.of(1L, 2L, 3L, 4L, 5L), "payload");

        assertEquals(2, maxObserved.get());
    }

    private RealtimeDeliveryService service(
            IWebSocketHubPort webSocketHub,
            IChannelMemberService memberService,
            IPresenceService presenceService,
            int maxConcurrency) {
        RealtimeProperties properties = new RealtimeProperties();
        properties.getDelivery().setMaxConcurrency(maxConcurrency);
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        return new RealtimeDeliveryService(webSocketHub, memberService, presenceService, properties, executorService);
    }
}
