/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for ChannelUseCase
 */

package serp.project.discuss_service.core.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.util.Pair;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.discuss_service.core.domain.dto.request.GetChannelsParams;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.enums.MemberRole;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.service.IChannelMemberService;
import serp.project.discuss_service.core.service.IChannelService;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;
import serp.project.discuss_service.core.service.IPresenceService;
import serp.project.discuss_service.core.service.IUserInfoService;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChannelUseCase.
 * Tests orchestration of channel operations across multiple services.
 */
@ExtendWith(MockitoExtension.class)
class ChannelUseCaseTest {

    @Mock
    private IChannelService channelService;

    @Mock
    private IChannelMemberService memberService;

    @Mock
    private IPresenceService presenceService;

    @Mock
    private IDiscussEventPublisher eventPublisher;

    @Mock
    private IUserInfoService userInfoService;

    @InjectMocks
    private ChannelUseCase channelUseCase;

    // ==================== CREATE GROUP CHANNEL TESTS ====================

    @Nested
    @DisplayName("createGroupChannel")
    class CreateGroupChannelTests {

        @Test
        @DisplayName("should create channel, add owner, and publish event")
        void testCreateGroupChannel_ValidInput_CreatesWithOwner() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            ChannelMemberEntity owner = TestDataFactory.createOwnerMember();

            when(channelService.createGroupChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    "Test Group",
                    "Description",
                    false
            )).thenReturn(channel);

            when(memberService.addOwner(channel.getId(), TestDataFactory.USER_ID_1, TestDataFactory.TENANT_ID))
                    .thenReturn(owner);

            // When
            ChannelEntity result = channelUseCase.createGroupChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    "Test Group",
                    "Description",
                    false,
                    null
            );

            // Then
            assertNotNull(result);
            verify(channelService).createGroupChannel(any(), any(), any(), any(), anyBoolean());
            verify(memberService).addOwner(channel.getId(), TestDataFactory.USER_ID_1, TestDataFactory.TENANT_ID);
            verify(eventPublisher).publishChannelCreated(channel);
        }

        @Test
        @DisplayName("should add initial members excluding creator")
        void testCreateGroupChannel_WithMembers_AddsAllExceptCreator() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            List<Long> initialMembers = List.of(TestDataFactory.USER_ID_1, TestDataFactory.USER_ID_2, TestDataFactory.USER_ID_3);

            when(channelService.createGroupChannel(any(), any(), any(), any(), anyBoolean())).thenReturn(channel);
            when(memberService.addOwner(any(), any(), any())).thenReturn(TestDataFactory.createOwnerMember());
            when(memberService.addMembers(any(), any(), any())).thenReturn(List.of());

            // When
            channelUseCase.createGroupChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    "Team",
                    "Desc",
                    false,
                    initialMembers
            );

            // Then - should add members excluding creator (USER_ID_1)
            verify(memberService).addMembers(
                    eq(channel.getId()),
                    argThat(list -> list.size() == 2 && !list.contains(TestDataFactory.USER_ID_1)),
                    eq(TestDataFactory.TENANT_ID)
            );
        }
    }

    // ==================== GET OR CREATE DIRECT CHANNEL TESTS ====================

    @Nested
    @DisplayName("getOrCreateDirectChannel")
    class GetOrCreateDirectChannelTests {

        @Test
        @DisplayName("should return existing direct channel when present")
        void testGetOrCreateDirectChannel_ExistingChannel_ReturnsExisting() {
            // Given
            ChannelEntity channel = TestDataFactory.createDirectChannel();

            when(channelService.getDirectChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.USER_ID_2
            )).thenReturn(Optional.of(channel));

            // When
            ChannelEntity result = channelUseCase.getOrCreateDirectChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.USER_ID_2
            );

            // Then
            assertNotNull(result);
            assertEquals(channel.getId(), result.getId());
            verify(channelService).getDirectChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.USER_ID_2
            );
            verify(channelService, never()).createDirectChannel(any(), any(), any());
            verify(memberService, never()).addOwner(any(), any(), any());
            verify(memberService, never()).addMember(any(), any(), any(), any());
        }

        @Test
        @DisplayName("should create direct channel and add both users when absent")
        void testGetOrCreateDirectChannel_NewChannel_CreatesAndAddsMembers() {
            // Given
            ChannelEntity channel = TestDataFactory.createDirectChannel();

            when(channelService.getDirectChannel(any(), any(), any())).thenReturn(Optional.empty());
            when(channelService.createDirectChannel(any(), any(), any())).thenReturn(channel);

            // When
            channelUseCase.getOrCreateDirectChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.USER_ID_2
            );

            // Then
            verify(channelService).getDirectChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.USER_ID_2
            );
            verify(channelService).createDirectChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.USER_ID_2
            );
            verify(memberService).addOwner(channel.getId(), TestDataFactory.USER_ID_1, TestDataFactory.TENANT_ID);
            verify(memberService).addMember(channel.getId(), TestDataFactory.USER_ID_2, TestDataFactory.TENANT_ID, MemberRole.MEMBER);
        }
    }

    // ==================== CREATE TOPIC CHANNEL TESTS ====================

    @Nested
    @DisplayName("createTopicChannel")
    class CreateTopicChannelTests {

        @Test
        @DisplayName("should create topic channel linked to entity")
        void testCreateTopicChannel_ValidInput_CreatesLinkedChannel() {
            // Given
            ChannelEntity channel = TestDataFactory.createTopicChannel("PROJECT", 500L);

            when(channelService.createTopicChannel(any(), any(), any(), any(), any())).thenReturn(channel);
            when(memberService.isMember(channel.getId(), TestDataFactory.USER_ID_1)).thenReturn(false);
            when(memberService.addOwner(any(), any(), any())).thenReturn(TestDataFactory.createOwnerMember());

            // When
            ChannelEntity result = channelUseCase.createTopicChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    "Project Discussion",
                    "PROJECT",
                    500L,
                    null
            );

            // Then
            assertNotNull(result);
            verify(channelService).createTopicChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    "Project Discussion",
                    "PROJECT",
                    500L
            );
            verify(eventPublisher).publishChannelCreated(channel);
        }
    }

    // ==================== GET CHANNEL WITH MEMBERS TESTS ====================

    @Nested
    @DisplayName("getChannelWithMembers")
    class GetChannelWithMembersTests {

        @Test
        @DisplayName("should return channel with members populated")
        void testGetChannelWithMembers_ValidChannel_ReturnsWithMembers() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            List<ChannelMemberEntity> members = List.of(
                    TestDataFactory.createOwnerMember(),
                    TestDataFactory.createRegularMember()
            );

            when(memberService.isMember(channel.getId(), TestDataFactory.USER_ID_1)).thenReturn(true);
            when(channelService.getChannelByIdOrThrow(channel.getId())).thenReturn(channel);
            when(memberService.getActiveMembers(channel.getId())).thenReturn(members);

            // When
            ChannelEntity result = channelUseCase.getChannelWithMembers(channel.getId(), TestDataFactory.USER_ID_1);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getMembers().size());
        }
    }

    // ==================== GET USER CHANNELS TESTS ====================

    @Nested
    @DisplayName("getUserChannels")
    class GetUserChannelsTests {

        @Test
        @DisplayName("should delegate filters to channelService")
        void testGetUserChannels_WithParams_DelegatesToChannelService() {
            // Given
            GetChannelsParams params = GetChannelsParams.builder()
                    .page(0)
                    .pageSize(20)
                    .searchQuery("team")
                    .build();

            List<ChannelEntity> channels = List.of(
                    TestDataFactory.createGroupChannel(),
                    TestDataFactory.createDirectChannel()
            );
            Pair<Long, List<ChannelEntity>> expected = Pair.of(2L, channels);

            when(channelService.getUserChannels(TestDataFactory.USER_ID_3, TestDataFactory.TENANT_ID, params))
                    .thenReturn(expected);

            // When
            Pair<Long, List<ChannelEntity>> result = channelUseCase.getUserChannels(
                    TestDataFactory.USER_ID_3,
                    TestDataFactory.TENANT_ID,
                    params);

            // Then
            assertNotNull(result);
            assertEquals(2L, result.getFirst());
            assertEquals(2, result.getSecond().size());
            verify(channelService).getUserChannels(TestDataFactory.USER_ID_3, TestDataFactory.TENANT_ID, params);
            verifyNoInteractions(memberService);
        }
    }

    // ==================== UPDATE CHANNEL TESTS ====================

    @Nested
    @DisplayName("updateChannel")
    class UpdateChannelTests {

        @Test
        @DisplayName("should update channel when user has permission")
        void testUpdateChannel_UserCanManage_UpdatesSuccessfully() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();

            when(memberService.canManageChannel(channel.getId(), TestDataFactory.USER_ID_1)).thenReturn(true);
            when(channelService.updateChannel(channel.getId(), "New Name", "New Desc")).thenReturn(channel);

            // When
            ChannelEntity result = channelUseCase.updateChannel(
                    channel.getId(),
                    TestDataFactory.USER_ID_1,
                    "New Name",
                    "New Desc"
            );

            // Then
            assertNotNull(result);
            verify(eventPublisher).publishChannelUpdated(channel);
        }

        @Test
        @DisplayName("should throw when user cannot manage channel")
        void testUpdateChannel_UserCannotManage_ThrowsException() {
            // Given
            when(memberService.canManageChannel(1L, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> channelUseCase.updateChannel(1L, TestDataFactory.USER_ID_3, "Name", "Desc"));

            assertEquals(ErrorCode.CHANNEL_UPDATE_FORBIDDEN.getMessage(), exception.getMessage());
            verify(channelService, never()).updateChannel(any(), any(), any());
        }
    }

    // ==================== ARCHIVE CHANNEL TESTS ====================

    @Nested
    @DisplayName("archiveChannel")
    class ArchiveChannelTests {

        @Test
        @DisplayName("should archive channel when user has permission")
        void testArchiveChannel_UserCanManage_ArchivesSuccessfully() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();

            when(memberService.canManageChannel(channel.getId(), TestDataFactory.USER_ID_1)).thenReturn(true);
            when(channelService.archiveChannel(channel.getId())).thenReturn(channel);

            // When
            ChannelEntity result = channelUseCase.archiveChannel(channel.getId(), TestDataFactory.USER_ID_1);

            // Then
            assertNotNull(result);
            verify(eventPublisher).publishChannelArchived(channel);
        }

        @Test
        @DisplayName("should throw when user cannot manage channel")
        void testArchiveChannel_UserCannotManage_ThrowsException() {
            // Given
            when(memberService.canManageChannel(1L, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> channelUseCase.archiveChannel(1L, TestDataFactory.USER_ID_3));

            assertEquals(ErrorCode.CHANNEL_ARCHIVE_FORBIDDEN.getMessage(), exception.getMessage());
        }
    }

    // ==================== ADD MEMBER TESTS ====================

    @Nested
    @DisplayName("addMember")
    class AddMemberTests {

        @Test
        @DisplayName("should add member when adder has permission")
        void testAddMember_AdderCanManage_AddsMember() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            ChannelMemberEntity newMember = TestDataFactory.createRegularMember();

            when(memberService.canManageChannel(channel.getId(), TestDataFactory.USER_ID_1)).thenReturn(true);
            when(memberService.addMember(channel.getId(), TestDataFactory.USER_ID_3, TestDataFactory.TENANT_ID, MemberRole.MEMBER))
                    .thenReturn(newMember);

            // When
            ChannelMemberEntity result = channelUseCase.addMember(
                    channel.getId(),
                    TestDataFactory.USER_ID_3,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID
            );

            // Then
            assertNotNull(result);
            verify(channelService).incrementMemberCount(channel.getId());
            verify(eventPublisher).publishMemberJoined(newMember);
        }

        @Test
        @DisplayName("should throw when adder cannot manage channel")
        void testAddMember_AdderCannotManage_ThrowsException() {
            // Given
            when(memberService.canManageChannel(1L, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> channelUseCase.addMember(1L, 999L, TestDataFactory.USER_ID_3, TestDataFactory.TENANT_ID));

            assertEquals(ErrorCode.CANNOT_ADD_MEMBERS.getMessage(), exception.getMessage());
        }
    }

    // ==================== REMOVE MEMBER TESTS ====================

    @Nested
    @DisplayName("removeMember")
    class RemoveMemberTests {

        @Test
        @DisplayName("should remove member when remover has permission")
        void testRemoveMember_RemoverCanManage_RemovesMember() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            ChannelMemberEntity removedMember = TestDataFactory.createLeftMember();

            when(memberService.canManageChannel(channel.getId(), TestDataFactory.USER_ID_1)).thenReturn(true);
            when(memberService.removeMember(channel.getId(), TestDataFactory.USER_ID_3, TestDataFactory.USER_ID_1))
                    .thenReturn(removedMember);

            // When
            ChannelMemberEntity result = channelUseCase.removeMember(
                    channel.getId(),
                    TestDataFactory.USER_ID_3,
                    TestDataFactory.USER_ID_1
            );

            // Then
            assertNotNull(result);
            verify(channelService).decrementMemberCount(channel.getId());
            verify(eventPublisher).publishMemberRemoved(removedMember);
        }

        @Test
        @DisplayName("should throw when remover cannot manage channel")
        void testRemoveMember_RemoverCannotManage_ThrowsException() {
            // Given
            when(memberService.canManageChannel(1L, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> channelUseCase.removeMember(1L, 999L, TestDataFactory.USER_ID_3));

            assertEquals(ErrorCode.CANNOT_REMOVE_MEMBERS.getMessage(), exception.getMessage());
        }
    }

    // ==================== LEAVE CHANNEL TESTS ====================

    @Nested
    @DisplayName("leaveChannel")
    class LeaveChannelTests {

        @Test
        @DisplayName("should leave channel and publish event")
        void testLeaveChannel_ActiveMember_LeavesSuccessfully() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            ChannelMemberEntity leftMember = TestDataFactory.createLeftMember();

            when(memberService.leaveChannel(channel.getId(), TestDataFactory.USER_ID_3)).thenReturn(leftMember);

            // When
            ChannelMemberEntity result = channelUseCase.leaveChannel(channel.getId(), TestDataFactory.USER_ID_3);

            // Then
            assertNotNull(result);
            verify(channelService).decrementMemberCount(channel.getId());
            verify(eventPublisher).publishMemberLeft(leftMember);
        }
    }

    // ==================== DELETE CHANNEL TESTS ====================

    @Nested
    @DisplayName("deleteChannel")
    class DeleteChannelTests {

        @Test
        @DisplayName("should delete channel when user has permission")
        void testDeleteChannel_UserCanManage_DeletesSuccessfully() {
            // Given
            when(memberService.canManageChannel(1L, TestDataFactory.USER_ID_1)).thenReturn(true);

            // When
            channelUseCase.deleteChannel(1L, TestDataFactory.USER_ID_1);

            // Then
            verify(channelService).deleteChannel(1L);
        }

        @Test
        @DisplayName("should throw when user cannot manage channel")
        void testDeleteChannel_UserCannotManage_ThrowsException() {
            // Given
            when(memberService.canManageChannel(1L, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> channelUseCase.deleteChannel(1L, TestDataFactory.USER_ID_3));

            assertEquals(ErrorCode.CHANNEL_DELETE_FORBIDDEN.getMessage(), exception.getMessage());
            verify(channelService, never()).deleteChannel(any());
        }
    }

    // ==================== GET ONLINE MEMBERS TESTS ====================

    @Nested
    @DisplayName("getOnlineMembers")
    class GetOnlineMembersTests {

        @Test
        @DisplayName("should return member IDs")
        void testGetOnlineMembers_ReturnsAllMembers() {
            // Given
            Set<Long> memberIds = Set.of(100L, 200L, 300L);
            when(memberService.getMemberIds(1L)).thenReturn(memberIds);
            when(presenceService.getOnlineUsers(memberIds)).thenReturn(memberIds);

            // When
            Set<Long> result = channelUseCase.getOnlineMembers(1L);

            // Then
            assertEquals(3, result.size());
        }
    }
}
