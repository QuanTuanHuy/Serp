/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for ChannelMemberEntity
 */

package serp.project.discuss_service.core.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import serp.project.discuss_service.core.domain.enums.MemberRole;
import serp.project.discuss_service.core.domain.enums.MemberStatus;
import serp.project.discuss_service.core.domain.enums.NotificationLevel;
import serp.project.discuss_service.testutil.TestDataFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChannelMemberEntity domain logic.
 * Tests factory methods, role management, status changes, and preferences.
 */
class ChannelMemberEntityTest {

    // ==================== FACTORY METHOD TESTS ====================

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("createOwner - should create member with OWNER role")
        void testCreateOwner_ValidInput_CreatesOwnerMember() {
            // When
            ChannelMemberEntity member = ChannelMemberEntity.createOwner(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID
            );

            // Then
            assertNotNull(member);
            assertEquals(MemberRole.OWNER, member.getRole());
            assertEquals(MemberStatus.ACTIVE, member.getStatus());
            assertNotNull(member.getJoinedAt());
            assertNotNull(member.getCreatedAt());
            assertEquals(TestDataFactory.USER_ID_1, member.getUserId());
        }

        @Test
        @DisplayName("createMember - should create member with MEMBER role")
        void testCreateMember_ValidInput_CreatesMemberRole() {
            // When
            ChannelMemberEntity member = ChannelMemberEntity.createMember(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_2,
                    TestDataFactory.TENANT_ID
            );

            // Then
            assertNotNull(member);
            assertEquals(MemberRole.MEMBER, member.getRole());
            assertEquals(MemberStatus.ACTIVE, member.getStatus());
        }

        @Test
        @DisplayName("createGuest - should create member with GUEST role")
        void testCreateGuest_ValidInput_CreatesGuestMember() {
            // When
            ChannelMemberEntity member = ChannelMemberEntity.createGuest(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_3,
                    TestDataFactory.TENANT_ID
            );

            // Then
            assertNotNull(member);
            assertEquals(MemberRole.GUEST, member.getRole());
            assertEquals(MemberStatus.ACTIVE, member.getStatus());
        }
    }

    // ==================== ROLE MANAGEMENT TESTS ====================

    @Nested
    @DisplayName("Role Management - promoteToAdmin")
    class PromoteToAdminTests {

        @Test
        @DisplayName("promoteToAdmin - should promote regular member to admin")
        void testPromoteToAdmin_RegularMember_BecomesAdmin() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            assertEquals(MemberRole.MEMBER, member.getRole());

            // When
            member.promoteToAdmin();

            // Then
            assertEquals(MemberRole.ADMIN, member.getRole());
        }

        @Test
        @DisplayName("promoteToAdmin - should throw for owner")
        void testPromoteToAdmin_Owner_ThrowsException() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createOwnerMember();

            // When/Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    member::promoteToAdmin
            );
            assertTrue(exception.getMessage().toLowerCase().contains("owner"));
        }

        @Test
        @DisplayName("promoteToAdmin - should throw if already admin")
        void testPromoteToAdmin_AlreadyAdmin_ThrowsException() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createAdminMember();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    member::promoteToAdmin
            );
        }

        @Test
        @DisplayName("promoteToAdmin - should throw for inactive member")
        void testPromoteToAdmin_InactiveMember_ThrowsException() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createLeftMember();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    member::promoteToAdmin
            );
        }
    }

    @Nested
    @DisplayName("Role Management - demoteToMember")
    class DemoteToMemberTests {

        @Test
        @DisplayName("demoteToMember - should demote admin to member")
        void testDemoteToMember_Admin_BecomesMember() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createAdminMember();
            assertEquals(MemberRole.ADMIN, member.getRole());

            // When
            member.demoteToMember();

            // Then
            assertEquals(MemberRole.MEMBER, member.getRole());
        }

        @Test
        @DisplayName("demoteToMember - should throw for owner")
        void testDemoteToMember_Owner_ThrowsException() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createOwnerMember();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    member::demoteToMember
            );
        }

        @Test
        @DisplayName("demoteToMember - should throw for non-admin")
        void testDemoteToMember_RegularMember_ThrowsException() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();

            // When/Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    member::demoteToMember
            );
            assertTrue(exception.getMessage().toLowerCase().contains("admin"));
        }
    }

    @Nested
    @DisplayName("Role Management - ownership transfer")
    class OwnershipTests {

        @Test
        @DisplayName("becomeOwner - should make member become owner")
        void testBecomeOwner_ActiveMember_BecomesOwner() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createAdminMember();

            // When
            member.becomeOwner();

            // Then
            assertEquals(MemberRole.OWNER, member.getRole());
        }

        @Test
        @DisplayName("relinquishOwnership - owner becomes admin")
        void testRelinquishOwnership_Owner_BecomesAdmin() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createOwnerMember();

            // When
            member.relinquishOwnership();

            // Then
            assertEquals(MemberRole.ADMIN, member.getRole());
        }

        @Test
        @DisplayName("relinquishOwnership - should throw for non-owner")
        void testRelinquishOwnership_NonOwner_ThrowsException() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createAdminMember();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    member::relinquishOwnership
            );
        }
    }

    // ==================== STATUS MANAGEMENT TESTS ====================

    @Nested
    @DisplayName("Status Management - leave")
    class LeaveTests {

        @Test
        @DisplayName("leave - regular member can leave")
        void testLeave_RegularMember_StatusChangesToLeft() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();

            // When
            member.leave();

            // Then
            assertEquals(MemberStatus.LEFT, member.getStatus());
            assertNotNull(member.getLeftAt());
        }

        @Test
        @DisplayName("leave - owner cannot leave")
        void testLeave_Owner_ThrowsException() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createOwnerMember();

            // When/Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    member::leave
            );
            assertTrue(exception.getMessage().toLowerCase().contains("owner"));
        }

        @Test
        @DisplayName("leave - already left member throws exception")
        void testLeave_AlreadyLeft_ThrowsException() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createLeftMember();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    member::leave
            );
        }
    }

    @Nested
    @DisplayName("Status Management - removeBy")
    class RemoveByTests {

        @Test
        @DisplayName("removeBy - should remove member and record remover")
        void testRemoveBy_RegularMember_StatusChangesToRemoved() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            Long removerId = TestDataFactory.USER_ID_1;

            // When
            member.removeBy(removerId);

            // Then
            assertEquals(MemberStatus.REMOVED, member.getStatus());
            assertEquals(removerId, member.getRemovedBy());
            assertNotNull(member.getLeftAt());
        }

        @Test
        @DisplayName("removeBy - cannot remove owner")
        void testRemoveBy_Owner_ThrowsException() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createOwnerMember();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    () -> member.removeBy(999L)
            );
        }

        @Test
        @DisplayName("removeBy - cannot remove already removed member")
        void testRemoveBy_AlreadyRemoved_ThrowsException() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            member.removeBy(TestDataFactory.USER_ID_1);

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    () -> member.removeBy(TestDataFactory.USER_ID_2)
            );
        }
    }

    @Nested
    @DisplayName("Status Management - rejoin")
    class RejoinTests {

        @Test
        @DisplayName("rejoin - left member can rejoin")
        void testRejoin_LeftMember_StatusChangesToActive() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createLeftMember();

            // When
            member.rejoin();

            // Then
            assertEquals(MemberStatus.ACTIVE, member.getStatus());
            assertNull(member.getLeftAt());
            assertNotNull(member.getJoinedAt());
        }

        @Test
        @DisplayName("rejoin - active member cannot rejoin")
        void testRejoin_ActiveMember_ThrowsException() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    member::rejoin
            );
        }
    }

    // ==================== PREFERENCE TESTS ====================

    @Nested
    @DisplayName("Preferences - mute/pin")
    class PreferenceTests {

        @Test
        @DisplayName("toggleMute - should toggle mute status")
        void testToggleMute_ActiveMember_TogglesSuccessfully() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            assertFalse(member.getIsMuted());

            // When
            member.toggleMute();

            // Then
            assertTrue(member.getIsMuted());
            assertEquals(MemberStatus.MUTED, member.getStatus());

            // Toggle back
            member.toggleMute();
            assertFalse(member.getIsMuted());
            assertEquals(MemberStatus.ACTIVE, member.getStatus());
        }

        @Test
        @DisplayName("toggleMute - should throw for inactive member")
        void testToggleMute_InactiveMember_ThrowsException() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createLeftMember();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    member::toggleMute
            );
        }

        @Test
        @DisplayName("togglePin - should toggle pin status")
        void testTogglePin_ActiveMember_TogglesSuccessfully() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            assertFalse(member.getIsPinned());

            // When
            member.togglePin();

            // Then
            assertTrue(member.getIsPinned());

            // Toggle back
            member.togglePin();
            assertFalse(member.getIsPinned());
        }

        @Test
        @DisplayName("setNotificationPreference - should update level")
        void testSetNotificationPreference_ValidLevel_UpdatesSuccessfully() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();

            // When
            member.setNotificationPreference(NotificationLevel.MENTIONS);

            // Then
            assertEquals(NotificationLevel.MENTIONS, member.getNotificationLevel());
        }
    }

    // ==================== READ TRACKING TESTS ====================

    @Nested
    @DisplayName("Read Tracking")
    class ReadTrackingTests {

        @Test
        @DisplayName("markAsRead - should update lastReadMsgId and reset unread")
        void testMarkAsRead_ValidMessage_UpdatesReadStatus() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            member.setUnreadCount(5);
            Long messageId = 12345L;

            // When
            member.markAsRead(messageId);

            // Then
            assertEquals(messageId, member.getLastReadMsgId());
            assertEquals(0, member.getUnreadCount());
        }

        @Test
        @DisplayName("incrementUnread - should increment unread count")
        void testIncrementUnread_ActiveMember_IncrementsCount() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            assertEquals(0, member.getUnreadCount());

            // When
            member.incrementUnread();

            // Then
            assertEquals(1, member.getUnreadCount());
        }

        @Test
        @DisplayName("incrementUnread - should not increment for inactive member")
        void testIncrementUnread_InactiveMember_DoesNotIncrement() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createLeftMember();
            int originalCount = member.getUnreadCount();

            // When
            member.incrementUnread();

            // Then - count should remain same since member can't access channel
            assertEquals(originalCount, member.getUnreadCount());
        }
    }

    // ==================== QUERY METHOD TESTS ====================

    @Nested
    @DisplayName("Query Methods")
    class QueryMethodTests {

        @Test
        @DisplayName("isOwner - should return true for owner role")
        void testIsOwner_OwnerRole_ReturnsTrue() {
            ChannelMemberEntity member = TestDataFactory.createOwnerMember();
            assertTrue(member.isOwner());
        }

        @Test
        @DisplayName("isOwner - should return false for non-owner")
        void testIsOwner_AdminRole_ReturnsFalse() {
            ChannelMemberEntity member = TestDataFactory.createAdminMember();
            assertFalse(member.isOwner());
        }

        @Test
        @DisplayName("isAdmin - should return true for admin role")
        void testIsAdmin_AdminRole_ReturnsTrue() {
            ChannelMemberEntity member = TestDataFactory.createAdminMember();
            assertTrue(member.isAdmin());
        }

        @Test
        @DisplayName("isActive - should return true for ACTIVE status")
        void testIsActive_ActiveStatus_ReturnsTrue() {
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            assertTrue(member.isActive());
        }

        @Test
        @DisplayName("isActive - should return false for LEFT status")
        void testIsActive_LeftStatus_ReturnsFalse() {
            ChannelMemberEntity member = TestDataFactory.createLeftMember();
            assertFalse(member.isActive());
        }

        @Test
        @DisplayName("canSendMessages - owner can send")
        void testCanSendMessages_Owner_ReturnsTrue() {
            ChannelMemberEntity member = TestDataFactory.createOwnerMember();
            assertTrue(member.canSendMessages());
        }

        @Test
        @DisplayName("canSendMessages - guest cannot send")
        void testCanSendMessages_Guest_ReturnsFalse() {
            ChannelMemberEntity member = TestDataFactory.createGuestMember();
            assertFalse(member.canSendMessages());
        }

        @Test
        @DisplayName("canManageChannel - owner can manage")
        void testCanManageChannel_Owner_ReturnsTrue() {
            ChannelMemberEntity member = TestDataFactory.createOwnerMember();
            assertTrue(member.canManageChannel());
        }

        @Test
        @DisplayName("canManageChannel - admin can manage")
        void testCanManageChannel_Admin_ReturnsTrue() {
            ChannelMemberEntity member = TestDataFactory.createAdminMember();
            assertTrue(member.canManageChannel());
        }

        @Test
        @DisplayName("canManageChannel - regular member cannot manage")
        void testCanManageChannel_RegularMember_ReturnsFalse() {
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            assertFalse(member.canManageChannel());
        }

        @Test
        @DisplayName("canManageMembers - owner can manage members")
        void testCanManageMembers_Owner_ReturnsTrue() {
            ChannelMemberEntity member = TestDataFactory.createOwnerMember();
            assertTrue(member.canManageMembers());
        }

        @Test
        @DisplayName("shouldReceiveNotification - active unmuted member receives")
        void testShouldReceiveNotification_ActiveUnmuted_ReturnsTrue() {
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            assertTrue(member.shouldReceiveNotification());
        }

        @Test
        @DisplayName("shouldReceiveNotification - muted member does not receive")
        void testShouldReceiveNotification_Muted_ReturnsFalse() {
            ChannelMemberEntity member = TestDataFactory.createMutedMember();
            assertFalse(member.shouldReceiveNotification());
        }

        @Test
        @DisplayName("hasUnreadMessages - true when unread > 0")
        void testHasUnreadMessages_PositiveCount_ReturnsTrue() {
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            member.setUnreadCount(5);
            assertTrue(member.hasUnreadMessages());
        }

        @Test
        @DisplayName("hasUnreadMessages - false when unread = 0")
        void testHasUnreadMessages_ZeroCount_ReturnsFalse() {
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            assertFalse(member.hasUnreadMessages());
        }
    }
}
