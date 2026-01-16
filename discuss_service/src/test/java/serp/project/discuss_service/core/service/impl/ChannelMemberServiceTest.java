/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for ChannelMemberService
 */

package serp.project.discuss_service.core.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.enums.MemberRole;
import serp.project.discuss_service.core.domain.enums.MemberStatus;
import serp.project.discuss_service.core.domain.enums.NotificationLevel;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.port.store.IChannelMemberPort;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChannelMemberService.
 * Tests all member business operations with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class ChannelMemberServiceTest {

    @Mock
    private IChannelMemberPort memberPort;

    @Mock
    private IDiscussCacheService cacheService;

    @InjectMocks
    private ChannelMemberService memberService;

    // ==================== ADD MEMBER TESTS ====================

    @Nested
    @DisplayName("addMember")
    class AddMemberTests {

        @Test
        @DisplayName("should add new member with specified role")
        void testAddMember_NewMember_AddsWithRole() {
            // Given
            when(memberPort.findByChannelIdAndUserId(any(), any())).thenReturn(Optional.empty());
            
            ChannelMemberEntity saved = TestDataFactory.createRegularMember();
            when(memberPort.save(any(ChannelMemberEntity.class))).thenReturn(saved);

            // When
            ChannelMemberEntity result = memberService.addMember(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_3,
                    TestDataFactory.TENANT_ID,
                    MemberRole.MEMBER
            );

            // Then
            assertNotNull(result);
            verify(memberPort).save(any(ChannelMemberEntity.class));
            verify(cacheService).addMemberToChannelCache(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3);
            verify(cacheService).addChannelToUserCache(TestDataFactory.USER_ID_3, TestDataFactory.CHANNEL_ID);
        }

        @Test
        @DisplayName("should return existing member if already active")
        void testAddMember_ExistingActiveMember_ReturnsExisting() {
            // Given
            ChannelMemberEntity existing = TestDataFactory.createRegularMember();
            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3))
                    .thenReturn(Optional.of(existing));

            // When
            ChannelMemberEntity result = memberService.addMember(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_3,
                    TestDataFactory.TENANT_ID,
                    MemberRole.MEMBER
            );

            // Then
            assertEquals(existing, result);
            verify(memberPort, never()).save(any());
        }

        @Test
        @DisplayName("should rejoin if member previously left")
        void testAddMember_PreviouslyLeftMember_Rejoins() {
            // Given
            ChannelMemberEntity leftMember = TestDataFactory.createLeftMember();
            when(memberPort.findByChannelIdAndUserId(any(), any())).thenReturn(Optional.of(leftMember));

            ChannelMemberEntity rejoined = TestDataFactory.createRegularMember();
            when(memberPort.save(any(ChannelMemberEntity.class))).thenReturn(rejoined);

            // When
            ChannelMemberEntity result = memberService.addMember(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_3,
                    TestDataFactory.TENANT_ID,
                    MemberRole.MEMBER
            );

            // Then
            verify(memberPort).save(leftMember);
            verify(cacheService).addMemberToChannelCache(any(), any());
        }

        @Test
        @DisplayName("should create owner with OWNER role")
        void testAddOwner_CreatesOwner() {
            // Given
            when(memberPort.findByChannelIdAndUserId(any(), any())).thenReturn(Optional.empty());

            ChannelMemberEntity saved = TestDataFactory.createOwnerMember();
            when(memberPort.save(any(ChannelMemberEntity.class))).thenReturn(saved);

            // When
            ChannelMemberEntity result = memberService.addOwner(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID
            );

            // Then
            assertNotNull(result);
            verify(memberPort).save(any(ChannelMemberEntity.class));
        }

        @Test
        @DisplayName("addMembers should add multiple members")
        void testAddMembers_MultipleUsers_AddsAll() {
            // Given
            when(memberPort.findByChannelIdAndUserId(any(), any())).thenReturn(Optional.empty());
            when(memberPort.save(any(ChannelMemberEntity.class)))
                    .thenReturn(TestDataFactory.createRegularMember());

            List<Long> userIds = List.of(100L, 200L, 300L);

            // When
            List<ChannelMemberEntity> result = memberService.addMembers(
                    TestDataFactory.CHANNEL_ID,
                    userIds,
                    TestDataFactory.TENANT_ID
            );

            // Then
            assertEquals(3, result.size());
            verify(memberPort, times(3)).save(any(ChannelMemberEntity.class));
        }
    }

    // ==================== GET MEMBER TESTS ====================

    @Nested
    @DisplayName("getMember / getMemberOrThrow")
    class GetMemberTests {

        @Test
        @DisplayName("getMember should delegate to port")
        void testGetMember_ExistingMember_ReturnsMember() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3))
                    .thenReturn(Optional.of(member));

            // When
            Optional<ChannelMemberEntity> result = memberService.getMember(
                    TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3);

            // Then
            assertTrue(result.isPresent());
            assertEquals(member, result.get());
        }

        @Test
        @DisplayName("getMemberOrThrow should throw when not found")
        void testGetMemberOrThrow_NotFound_ThrowsException() {
            // Given
            when(memberPort.findByChannelIdAndUserId(999L, 999L)).thenReturn(Optional.empty());

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> memberService.getMemberOrThrow(999L, 999L));

            assertEquals(ErrorCode.MEMBER_NOT_FOUND.getMessage(), exception.getMessage());
        }
    }

    // ==================== GET MEMBERS TESTS ====================

    @Nested
    @DisplayName("Query Methods")
    class QueryMethodTests {

        @Test
        @DisplayName("getActiveMembers should return active members")
        void testGetActiveMembers_ReturnsActiveOnly() {
            // Given
            List<ChannelMemberEntity> activeMembers = List.of(
                    TestDataFactory.createOwnerMember(),
                    TestDataFactory.createRegularMember()
            );
            when(memberPort.findByChannelIdAndStatus(TestDataFactory.CHANNEL_ID, MemberStatus.ACTIVE))
                    .thenReturn(activeMembers);

            // When
            List<ChannelMemberEntity> result = memberService.getActiveMembers(TestDataFactory.CHANNEL_ID);

            // Then
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("getMemberIds should use cache first")
        void testGetMemberIds_CacheHit_ReturnsCached() {
            // Given
            Set<Long> cachedIds = Set.of(100L, 200L, 300L);
            when(cacheService.getCachedChannelMembers(TestDataFactory.CHANNEL_ID)).thenReturn(cachedIds);

            // When
            Set<Long> result = memberService.getMemberIds(TestDataFactory.CHANNEL_ID);

            // Then
            assertEquals(3, result.size());
            verify(memberPort, never()).findByChannelIdAndStatus(any(), any());
        }

        @Test
        @DisplayName("getMemberIds should query database on cache miss")
        void testGetMemberIds_CacheMiss_QueriesDbAndCaches() {
            // Given
            when(cacheService.getCachedChannelMembers(TestDataFactory.CHANNEL_ID))
                    .thenReturn(new HashSet<>()); // Empty cache

            List<ChannelMemberEntity> members = List.of(
                    TestDataFactory.createOwnerMember(),
                    TestDataFactory.createRegularMember()
            );
            when(memberPort.findByChannelIdAndStatus(TestDataFactory.CHANNEL_ID, MemberStatus.ACTIVE))
                    .thenReturn(members);

            // When
            Set<Long> result = memberService.getMemberIds(TestDataFactory.CHANNEL_ID);

            // Then
            assertEquals(2, result.size());
            verify(cacheService).cacheChannelMembers(eq(TestDataFactory.CHANNEL_ID), any());
        }

        @Test
        @DisplayName("getUserChannels should return active channels for user")
        void testGetUserChannels_ReturnsActiveChannels() {
            // Given
            List<ChannelMemberEntity> memberships = List.of(
                    TestDataFactory.createRegularMember()
            );
            when(memberPort.findByUserIdAndStatus(TestDataFactory.USER_ID_3, MemberStatus.ACTIVE))
                    .thenReturn(memberships);

            // When
            List<ChannelMemberEntity> result = memberService.getUserChannels(TestDataFactory.USER_ID_3);

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("getPinnedChannels should delegate to port")
        void testGetPinnedChannels_DelegatesToPort() {
            // Given
            when(memberPort.findPinnedChannels(TestDataFactory.USER_ID_1)).thenReturn(List.of());

            // When
            memberService.getPinnedChannels(TestDataFactory.USER_ID_1);

            // Then
            verify(memberPort).findPinnedChannels(TestDataFactory.USER_ID_1);
        }
    }

    // ==================== PERMISSION CHECKS ====================

    @Nested
    @DisplayName("Permission Checks")
    class PermissionTests {

        @Test
        @DisplayName("isMember should check cache first")
        void testIsMember_CacheHit_ReturnsCachedResult() {
            // Given
            when(cacheService.isMemberCached(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1))
                    .thenReturn(true);

            // When
            boolean result = memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1);

            // Then
            assertTrue(result);
            verify(memberPort, never()).isMember(any(), any());
        }

        @Test
        @DisplayName("isMember should check database on cache miss")
        void testIsMember_CacheMiss_ChecksDatabase() {
            // Given
            when(cacheService.isMemberCached(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1))
                    .thenReturn(false);
            when(memberPort.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1))
                    .thenReturn(true);

            // When
            boolean result = memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1);

            // Then
            assertTrue(result);
            verify(memberPort).isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1);
        }

        @Test
        @DisplayName("canSendMessages should return true for active member")
        void testCanSendMessages_ActiveMember_ReturnsTrue() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3))
                    .thenReturn(Optional.of(member));

            // When
            boolean result = memberService.canSendMessages(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("canManageChannel should return true for admin/owner")
        void testCanManageChannel_AdminMember_ReturnsTrue() {
            // Given
            ChannelMemberEntity admin = TestDataFactory.createAdminMember();
            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_2))
                    .thenReturn(Optional.of(admin));

            // When
            boolean result = memberService.canManageChannel(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_2);

            // Then
            assertTrue(result);
        }
    }

    // ==================== ROLE MANAGEMENT TESTS ====================

    @Nested
    @DisplayName("Role Management")
    class RoleManagementTests {

        @Test
        @DisplayName("promoteToAdmin should promote regular member")
        void testPromoteToAdmin_RegularMember_PromotesToAdmin() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3))
                    .thenReturn(Optional.of(member));

            ChannelMemberEntity promoted = TestDataFactory.createAdminMember();
            when(memberPort.save(any(ChannelMemberEntity.class))).thenReturn(promoted);

            // When
            ChannelMemberEntity result = memberService.promoteToAdmin(
                    TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3);

            // Then
            assertNotNull(result);
            verify(memberPort).save(any(ChannelMemberEntity.class));
        }

        @Test
        @DisplayName("demoteToMember should demote admin")
        void testDemoteToMember_AdminMember_DemotesToMember() {
            // Given
            ChannelMemberEntity admin = TestDataFactory.createAdminMember();
            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_2))
                    .thenReturn(Optional.of(admin));

            ChannelMemberEntity demoted = TestDataFactory.createRegularMember();
            when(memberPort.save(any(ChannelMemberEntity.class))).thenReturn(demoted);

            // When
            ChannelMemberEntity result = memberService.demoteToMember(
                    TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_2);

            // Then
            assertNotNull(result);
            verify(memberPort).save(any(ChannelMemberEntity.class));
        }

        @Test
        @DisplayName("transferOwnership should transfer between members")
        void testTransferOwnership_ValidMembers_TransfersOwnership() {
            // Given
            ChannelMemberEntity currentOwner = TestDataFactory.createOwnerMember();
            ChannelMemberEntity newOwner = TestDataFactory.createRegularMember();

            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1))
                    .thenReturn(Optional.of(currentOwner));
            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3))
                    .thenReturn(Optional.of(newOwner));

            // When
            memberService.transferOwnership(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.USER_ID_3
            );

            // Then
            verify(memberPort, times(2)).save(any(ChannelMemberEntity.class));
        }
    }

    // ==================== LEAVE/REMOVE TESTS ====================

    @Nested
    @DisplayName("Leave / Remove Member")
    class LeaveRemoveTests {

        @Test
        @DisplayName("leaveChannel should mark member as left and update caches")
        void testLeaveChannel_ActiveMember_LeavesAndUpdatesCache() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3))
                    .thenReturn(Optional.of(member));

            ChannelMemberEntity left = TestDataFactory.createLeftMember();
            when(memberPort.save(any(ChannelMemberEntity.class))).thenReturn(left);

            // When
            memberService.leaveChannel(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3);

            // Then
            verify(memberPort).save(any(ChannelMemberEntity.class));
            verify(cacheService).removeMemberFromChannelCache(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3);
            verify(cacheService).removeChannelFromUserCache(TestDataFactory.USER_ID_3, TestDataFactory.CHANNEL_ID);
        }

        @Test
        @DisplayName("removeMember should mark as removed by remover")
        void testRemoveMember_ByAdmin_RemovesAndUpdatesCache() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3))
                    .thenReturn(Optional.of(member));

            ChannelMemberEntity removed = TestDataFactory.createRegularMember();
            removed.setStatus(MemberStatus.REMOVED);
            removed.setRemovedBy(TestDataFactory.USER_ID_1);
            when(memberPort.save(any(ChannelMemberEntity.class))).thenReturn(removed);

            // When
            memberService.removeMember(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_3,
                    TestDataFactory.USER_ID_1
            );

            // Then
            verify(memberPort).save(any(ChannelMemberEntity.class));
            verify(cacheService).removeMemberFromChannelCache(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3);
        }
    }

    // ==================== PREFERENCE TESTS ====================

    @Nested
    @DisplayName("Preferences")
    class PreferenceTests {

        @Test
        @DisplayName("toggleMute should toggle mute status")
        void testToggleMute_TogglesMuteStatus() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3))
                    .thenReturn(Optional.of(member));
            when(memberPort.save(any(ChannelMemberEntity.class))).thenReturn(member);

            // When
            memberService.toggleMute(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3);

            // Then
            verify(memberPort).save(any(ChannelMemberEntity.class));
        }

        @Test
        @DisplayName("togglePin should toggle pin status")
        void testTogglePin_TogglesPinStatus() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3))
                    .thenReturn(Optional.of(member));
            when(memberPort.save(any(ChannelMemberEntity.class))).thenReturn(member);

            // When
            memberService.togglePin(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3);

            // Then
            verify(memberPort).save(any(ChannelMemberEntity.class));
        }

        @Test
        @DisplayName("updateNotificationLevel should update level")
        void testUpdateNotificationLevel_SetsNewLevel() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3))
                    .thenReturn(Optional.of(member));
            when(memberPort.save(any(ChannelMemberEntity.class))).thenReturn(member);

            // When
            memberService.updateNotificationLevel(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_3,
                    NotificationLevel.MENTIONS
            );

            // Then
            verify(memberPort).save(any(ChannelMemberEntity.class));
        }
    }

    // ==================== READ/UNREAD TESTS ====================

    @Nested
    @DisplayName("Read/Unread Management")
    class ReadUnreadTests {

        @Test
        @DisplayName("markAsRead should update member and reset cache")
        void testMarkAsRead_UpdatesAndResetsCache() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            when(memberPort.findByChannelIdAndUserId(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3))
                    .thenReturn(Optional.of(member));
            when(memberPort.save(any(ChannelMemberEntity.class))).thenReturn(member);

            // When
            memberService.markAsRead(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3, 100L);

            // Then
            verify(memberPort).save(any(ChannelMemberEntity.class));
            verify(cacheService).resetUnreadCount(TestDataFactory.USER_ID_3, TestDataFactory.CHANNEL_ID);
        }

        @Test
        @DisplayName("incrementUnreadForChannel should update all members except sender")
        void testIncrementUnreadForChannel_UpdatesNonSenders() {
            // Given
            Set<Long> memberIds = Set.of(100L, 200L, 300L);
            when(cacheService.getCachedChannelMembers(TestDataFactory.CHANNEL_ID)).thenReturn(memberIds);

            // When
            memberService.incrementUnreadForChannel(TestDataFactory.CHANNEL_ID, 100L); // 100L is sender

            // Then
            verify(memberPort).incrementUnreadForChannel(TestDataFactory.CHANNEL_ID, 100L);
            // Changed: Now uses batch increment instead of individual calls
            verify(cacheService).incrementUnreadCountBatch(eq(Set.of(200L, 300L)), eq(TestDataFactory.CHANNEL_ID));
            verify(cacheService, never()).incrementUnreadCount(eq(100L), any()); // Sender excluded
        }
    }

    // ==================== COUNT TESTS ====================

    @Nested
    @DisplayName("Count Methods")
    class CountTests {

        @Test
        @DisplayName("countActiveMembers should delegate to port")
        void testCountActiveMembers_DelegatesToPort() {
            // Given
            when(memberPort.countActiveMembers(TestDataFactory.CHANNEL_ID)).thenReturn(5L);

            // When
            long count = memberService.countActiveMembers(TestDataFactory.CHANNEL_ID);

            // Then
            assertEquals(5L, count);
            verify(memberPort).countActiveMembers(TestDataFactory.CHANNEL_ID);
        }
    }
}
