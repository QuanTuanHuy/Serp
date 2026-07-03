/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for message event listener
 */

package serp.project.discuss_service.core.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.domain.dto.websocket.WsMessagePayload;
import serp.project.discuss_service.core.domain.event.MessageReadInternalEvent;
import serp.project.discuss_service.core.domain.event.MessageSentInternalEvent;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;
import serp.project.discuss_service.core.service.impl.RealtimePayloadBuilder;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageEventListenerTest {

    @Test
    @DisplayName("handleMessageSent should publish ready realtime payload after commit")
    void testHandleMessageSent_PublishesReadyPayload() {
        IDiscussEventPublisher eventPublisher = mock(IDiscussEventPublisher.class);
        IDiscussCacheService cacheService = mock(IDiscussCacheService.class);
        RealtimePayloadBuilder payloadBuilder = mock(RealtimePayloadBuilder.class);
        MessageEventListener listener = new MessageEventListener(
                eventPublisher,
                cacheService,
                payloadBuilder,
                Executors.newSingleThreadExecutor()
        );
        var message = TestDataFactory.createTextMessage("ready payload");
        WsEvent<WsMessagePayload> readyEvent = WsEvent.of(
                WsEventType.MESSAGE_NEW,
                WsMessagePayload.fromEntity(message),
                message.getChannelId()
        );
        when(payloadBuilder.buildMessageNew(message)).thenReturn(readyEvent);
        when(cacheService.prependMessageToFirstPage(message.getChannelId(), message, 50)).thenReturn(true);

        listener.handleMessageSent(new MessageSentInternalEvent(this, message));

        ArgumentCaptor<WsEvent<?>> eventCaptor = ArgumentCaptor.forClass(WsEvent.class);
        verify(eventPublisher, timeout(1000)).publishRealtimeEvent(
                eq(String.valueOf(message.getChannelId())),
                eventCaptor.capture(),
                eq(IDiscussEventPublisher.TOPIC_MESSAGE_EVENTS)
        );
        assertEquals(WsEventType.MESSAGE_NEW, eventCaptor.getValue().getType());
        verify(cacheService, timeout(1000)).cacheMessage(message);
    }

    @Test
    @DisplayName("handleMessageRead should publish ready read payload")
    void testHandleMessageRead_PublishesReadyPayload() {
        IDiscussEventPublisher eventPublisher = mock(IDiscussEventPublisher.class);
        IDiscussCacheService cacheService = mock(IDiscussCacheService.class);
        RealtimePayloadBuilder payloadBuilder = mock(RealtimePayloadBuilder.class);
        MessageEventListener listener = new MessageEventListener(
                eventPublisher,
                cacheService,
                payloadBuilder,
                Executors.newSingleThreadExecutor()
        );
        WsEvent<?> readyEvent = WsEvent.of(WsEventType.MESSAGE_READ, "read", TestDataFactory.CHANNEL_ID);
        when(payloadBuilder.buildMessageRead(
                TestDataFactory.CHANNEL_ID,
                TestDataFactory.MESSAGE_ID,
                TestDataFactory.USER_ID_2,
                List.of(TestDataFactory.USER_ID_1, TestDataFactory.USER_ID_2),
                2
        )).thenReturn((WsEvent) readyEvent);

        listener.handleMessageRead(new MessageReadInternalEvent(
                this,
                TestDataFactory.CHANNEL_ID,
                TestDataFactory.MESSAGE_ID,
                TestDataFactory.USER_ID_2,
                List.of(TestDataFactory.USER_ID_1, TestDataFactory.USER_ID_2),
                2
        ));

        verify(eventPublisher, timeout(1000)).publishRealtimeEvent(
                eq(String.valueOf(TestDataFactory.CHANNEL_ID)),
                eq(readyEvent),
                eq(IDiscussEventPublisher.TOPIC_MESSAGE_EVENTS)
        );
    }
}
