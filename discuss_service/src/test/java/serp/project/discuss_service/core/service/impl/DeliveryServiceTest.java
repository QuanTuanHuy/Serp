/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for DeliveryService
 */

package serp.project.discuss_service.core.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.domain.dto.websocket.WsMessageReadPayload;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.entity.UserPresenceEntity;
import serp.project.discuss_service.core.port.client.IWebSocketHubPort;
import serp.project.discuss_service.core.service.IAttachmentUrlService;
import serp.project.discuss_service.core.service.IChannelMemberService;
import serp.project.discuss_service.core.service.IMessageService;
import serp.project.discuss_service.core.service.IPresenceService;
import serp.project.discuss_service.core.service.IUserInfoService;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private IWebSocketHubPort webSocketHub;

    @Mock
    private IMessageService messageService;

    @Mock
    private IChannelMemberService memberService;

    @Mock
    private IPresenceService presenceService;

    @Mock
    private IAttachmentUrlService attachmentUrlService;

    @Mock
    private IUserInfoService userInfoService;

    @InjectMocks
    private DeliveryService deliveryService;

    @Test
    @DisplayName("notifyPresenceChange should fan out to online users sharing a channel")
    void testNotifyPresenceChange_FansOutToOnlineSharedChannelMembers() {
        // Given
        Long changedUserId = TestDataFactory.USER_ID_1;
        Long onlineRecipientId = TestDataFactory.USER_ID_2;
        Long offlineRecipientId = TestDataFactory.USER_ID_3;

        ChannelMemberEntity changedUserMembership =
                TestDataFactory.createMember(changedUserId, serp.project.discuss_service.core.domain.enums.MemberRole.MEMBER);
        changedUserMembership.setChannelId(TestDataFactory.CHANNEL_ID);

        UserPresenceEntity presence = UserPresenceEntity.online(
                changedUserId,
                TestDataFactory.TENANT_ID
        );

        when(presenceService.getUserPresence(changedUserId)).thenReturn(Optional.of(presence));
        when(userInfoService.getUserById(changedUserId)).thenReturn(Optional.of(
                ChannelMemberResponse.UserInfo.builder()
                        .id(changedUserId)
                        .name("Changed User")
                        .avatarUrl("https://example.com/avatar.png")
                        .build()
        ));
        when(memberService.getUserChannels(changedUserId))
                .thenReturn(List.of(changedUserMembership));
        when(memberService.getMemberIds(TestDataFactory.CHANNEL_ID))
                .thenReturn(Set.of(changedUserId, onlineRecipientId, offlineRecipientId));
        when(presenceService.getOnlineUsers(Set.of(onlineRecipientId, offlineRecipientId)))
                .thenReturn(Set.of(onlineRecipientId));

        // When
        deliveryService.notifyPresenceChange(changedUserId);

        // Then
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(webSocketHub).sendToUsers(eq(Set.of(onlineRecipientId)), eventCaptor.capture());

        Object eventObject = eventCaptor.getValue();
        assertNotNull(eventObject);
        WsEvent<?> event = (WsEvent<?>) eventObject;
        assertEquals(WsEventType.USER_PRESENCE_CHANGED, event.getType());

        verify(webSocketHub, never()).sendToUser(any(), any());
    }

    @Test
    @DisplayName("notifyMessageRead should fan out read receipt event")
    void testNotifyMessageRead_FansOutReadEvent() {
        // Given
        Set<Long> memberIds = Set.of(
                TestDataFactory.USER_ID_1,
                TestDataFactory.USER_ID_2,
                TestDataFactory.USER_ID_3
        );
        Set<Long> onlineIds = Set.of(TestDataFactory.USER_ID_1, TestDataFactory.USER_ID_2);

        when(memberService.getMemberIds(TestDataFactory.CHANNEL_ID)).thenReturn(memberIds);
        when(presenceService.getOnlineUsers(memberIds)).thenReturn(onlineIds);

        // When
        deliveryService.notifyMessageRead(
                TestDataFactory.CHANNEL_ID,
                TestDataFactory.MESSAGE_ID,
                TestDataFactory.USER_ID_2,
                List.of(TestDataFactory.USER_ID_1, TestDataFactory.USER_ID_2),
                2
        );

        // Then
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(webSocketHub).sendToUsers(eq(onlineIds), eventCaptor.capture());

        WsEvent<?> event = (WsEvent<?>) eventCaptor.getValue();
        assertEquals(WsEventType.MESSAGE_READ, event.getType());
        assertEquals(TestDataFactory.CHANNEL_ID, event.getChannelId());

        WsMessageReadPayload payload = (WsMessageReadPayload) event.getPayload();
        assertEquals(TestDataFactory.CHANNEL_ID, payload.getChannelId());
        assertEquals(TestDataFactory.MESSAGE_ID, payload.getMessageId());
        assertEquals(TestDataFactory.USER_ID_2, payload.getUserId());
        assertEquals(List.of(TestDataFactory.USER_ID_1, TestDataFactory.USER_ID_2), payload.getReadBy());
        assertEquals(2, payload.getReadCount());
    }
}
