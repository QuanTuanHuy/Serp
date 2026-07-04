/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for realtime payload builder
 */

package serp.project.discuss_service.core.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;
import serp.project.discuss_service.core.domain.dto.response.MessageResponse;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.domain.dto.websocket.WsMessagePayload;
import serp.project.discuss_service.core.domain.dto.websocket.WsMessageReadPayload;
import serp.project.discuss_service.core.domain.dto.websocket.WsReactionPayload;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.service.IAttachmentUrlService;
import serp.project.discuss_service.core.service.IUserInfoService;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealtimePayloadBuilderTest {

    @Mock
    private IAttachmentUrlService attachmentUrlService;

    @Mock
    private IUserInfoService userInfoService;

    @InjectMocks
    private RealtimePayloadBuilder payloadBuilder;

    @Test
    @DisplayName("buildMessageNew should return render-complete message event")
    void testBuildMessageNew_RenderCompletePayload() {
        MessageEntity message = TestDataFactory.createTextMessage("Hello realtime");
        MessageResponse withUrls = MessageResponse.fromEntity(message);
        MessageResponse enriched = MessageResponse.fromEntity(message);
        enriched.setSender(ChannelMemberResponse.UserInfo.builder()
                .id(TestDataFactory.USER_ID_1)
                .name("Sender")
                .build());
        when(attachmentUrlService.enrichMessageWithUrls(message)).thenReturn(withUrls);
        when(userInfoService.enrichMessageWithUserInfo(withUrls)).thenReturn(enriched);

        WsEvent<WsMessagePayload> event = payloadBuilder.buildMessageNew(message);

        assertEquals(WsEventType.MESSAGE_NEW, event.getType());
        assertEquals(TestDataFactory.CHANNEL_ID, event.getChannelId());
        assertEquals(TestDataFactory.MESSAGE_ID, event.getPayload().getMessageId());
        assertEquals("Sender", event.getPayload().getMessage().getSender().getName());
    }

    @Test
    @DisplayName("buildMessageNew should degrade when enrichment throws")
    void testBuildMessageNew_EnrichmentFails_DegradesToBasicMessage() {
        MessageEntity message = TestDataFactory.createTextMessage("Basic realtime");
        when(attachmentUrlService.enrichMessageWithUrls(message)).thenThrow(new RuntimeException("s3-down"));

        WsEvent<WsMessagePayload> event = payloadBuilder.buildMessageNew(message);

        assertEquals(WsEventType.MESSAGE_NEW, event.getType());
        assertEquals(TestDataFactory.MESSAGE_ID, event.getPayload().getMessageId());
        assertEquals("Basic realtime", event.getPayload().getMessage().getContent());
        assertNull(event.getPayload().getMessage().getSender());
    }

    @Test
    @DisplayName("buildMessageRead should build ready read event")
    void testBuildMessageRead_BuildsReadPayload() {
        WsEvent<WsMessageReadPayload> event = payloadBuilder.buildMessageRead(
                TestDataFactory.CHANNEL_ID,
                TestDataFactory.MESSAGE_ID,
                TestDataFactory.USER_ID_2,
                List.of(TestDataFactory.USER_ID_1, TestDataFactory.USER_ID_2),
                2
        );

        assertEquals(WsEventType.MESSAGE_READ, event.getType());
        assertEquals(TestDataFactory.CHANNEL_ID, event.getChannelId());
        assertEquals(2, event.getPayload().getReadCount());
    }

    @Test
    @DisplayName("buildReaction should build ready reaction event")
    void testBuildReaction_BuildsReactionPayload() {
        WsEvent<WsReactionPayload> event = payloadBuilder.buildReaction(
                TestDataFactory.CHANNEL_ID,
                TestDataFactory.MESSAGE_ID,
                TestDataFactory.USER_ID_2,
                ":+1:",
                true
        );

        assertEquals(WsEventType.REACTION_ADDED, event.getType());
        assertEquals(TestDataFactory.CHANNEL_ID, event.getChannelId());
        assertNotNull(event.getPayload());
        assertEquals(Boolean.TRUE, event.getPayload().getAdded());
    }
}
