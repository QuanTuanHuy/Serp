/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel member entity with DDD rich domain model
 */

package serp.project.discuss_service.core.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.discuss_service.core.domain.enums.MemberRole;
import serp.project.discuss_service.core.domain.enums.MemberStatus;
import serp.project.discuss_service.core.domain.enums.NotificationLevel;

import java.time.Instant;
import java.util.Map;

/**
 * ChannelMember represents a user's membership in a channel.
 * Contains business logic for member operations.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ChannelMemberEntity extends BaseEntity {

    private Long channelId;
    private Long userId;
    private Long tenantId;
    
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;
    
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;
    
    // Join/Leave tracking
    private Long joinedAt;
    private Long leftAt;
    private Long removedBy;
    
    // Reading tracking
    private Long lastReadMsgId;
    
    @Builder.Default
    private Integer unreadCount = 0;
    
    // Preferences
    @Builder.Default
    private Boolean isMuted = false;
    
    @Builder.Default
    private Boolean isPinned = false;
    
    @Builder.Default
    private NotificationLevel notificationLevel = NotificationLevel.ALL;
    
    // Metadata
    private Map<String, Object> metadata;

    // ==================== FACTORY METHODS ====================

    /**
     * Create a new channel member as owner
     */
    public static ChannelMemberEntity createOwner(Long channelId, Long userId, Long tenantId) {
        long now = Instant.now().toEpochMilli();
        return ChannelMemberEntity.builder()
                .channelId(channelId)
                .userId(userId)
                .tenantId(tenantId)
                .role(MemberRole.OWNER)
                .status(MemberStatus.ACTIVE)
                .joinedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Create a new channel member with default role
     */
    public static ChannelMemberEntity createMember(Long channelId, Long userId, Long tenantId) {
        long now = Instant.now().toEpochMilli();
        return ChannelMemberEntity.builder()
                .channelId(channelId)
                .userId(userId)
                .tenantId(tenantId)
                .role(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .joinedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Create a guest member
     */
    public static ChannelMemberEntity createGuest(Long channelId, Long userId, Long tenantId) {
        long now = Instant.now().toEpochMilli();
        return ChannelMemberEntity.builder()
                .channelId(channelId)
                .userId(userId)
                .tenantId(tenantId)
                .role(MemberRole.GUEST)
                .status(MemberStatus.ACTIVE)
                .joinedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ==================== BUSINESS LOGIC ====================

    /**
     * Promote member to admin
     */
    public void promoteToAdmin() {
        validateActive();
        if (this.role == MemberRole.OWNER) {
            throw new IllegalStateException("Cannot change owner role");
        }
        if (this.role == MemberRole.ADMIN) {
            throw new IllegalStateException("Member is already an admin");
        }
        
        this.role = MemberRole.ADMIN;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Demote admin to member
     */
    public void demoteToMember() {
        validateActive();
        if (this.role == MemberRole.OWNER) {
            throw new IllegalStateException("Cannot demote owner");
        }
        if (this.role != MemberRole.ADMIN) {
            throw new IllegalStateException("Only admins can be demoted");
        }
        
        this.role = MemberRole.MEMBER;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Leave the channel voluntarily
     */
    public void leave() {
        if (this.role == MemberRole.OWNER) {
            throw new IllegalStateException("Owner cannot leave. Transfer ownership first.");
        }
        if (this.status == MemberStatus.LEFT) {
            throw new IllegalStateException("Already left the channel");
        }
        
        this.status = MemberStatus.LEFT;
        this.leftAt = Instant.now().toEpochMilli();
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Remove member from channel (by admin/owner)
     */
    public void removeBy(Long removerId) {
        if (this.role == MemberRole.OWNER) {
            throw new IllegalStateException("Cannot remove owner");
        }
        if (this.status == MemberStatus.REMOVED) {
            throw new IllegalStateException("Already removed from channel");
        }
        
        this.status = MemberStatus.REMOVED;
        this.removedBy = removerId;
        this.leftAt = Instant.now().toEpochMilli();
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Rejoin after leaving
     */
    public void rejoin() {
        if (this.status != MemberStatus.LEFT) {
            throw new IllegalStateException("Can only rejoin if previously left");
        }
        
        this.status = MemberStatus.ACTIVE;
        this.leftAt = null;
        this.joinedAt = Instant.now().toEpochMilli();
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Mute/unmute notifications
     */
    public void toggleMute() {
        validateActive();
        this.isMuted = !this.isMuted;
        if (this.isMuted) {
            this.status = MemberStatus.MUTED;
        } else {
            this.status = MemberStatus.ACTIVE;
        }
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Pin/unpin channel
     */
    public void togglePin() {
        validateActive();
        this.isPinned = !this.isPinned;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Update notification preference
     */
    public void setNotificationPreference(NotificationLevel level) {
        validateActive();
        this.notificationLevel = level;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Mark messages as read up to specific message ID
     */
    public void markAsRead(Long messageId) {
        validateCanAccess();
        this.lastReadMsgId = messageId;
        this.unreadCount = 0;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Increment unread count (called when new message arrives)
     */
    public void incrementUnread() {
        if (this.status.canAccessChannel()) {
            this.unreadCount++;
            setUpdatedAt(Instant.now().toEpochMilli());
        }
    }

    /**
     * Transfer ownership to this member
     */
    public void becomeOwner() {
        validateActive();
        this.role = MemberRole.OWNER;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Relinquish ownership (when transferring)
     */
    public void relinquishOwnership() {
        if (this.role != MemberRole.OWNER) {
            throw new IllegalStateException("Not an owner");
        }
        this.role = MemberRole.ADMIN;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    // ==================== QUERY METHODS ====================

    @JsonIgnore
    public boolean isOwner() {
        return this.role == MemberRole.OWNER;
    }

    @JsonIgnore
    public boolean isAdmin() {
        return this.role == MemberRole.ADMIN;
    }

    @JsonIgnore
    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    @JsonIgnore
    public boolean canSendMessages() {
        return this.status.canAccessChannel() && this.role.canSendMessages();
    }

    @JsonIgnore
    public boolean canManageChannel() {
        return this.status.canAccessChannel() && this.role.canManageChannel();
    }

    @JsonIgnore
    public boolean canManageMembers() {
        return this.status.canAccessChannel() && this.role.canManageMembers();
    }

    @JsonIgnore
    public boolean shouldReceiveNotification() {
        return this.status.receivesNotifications() && !Boolean.TRUE.equals(this.isMuted);
    }

    @JsonIgnore
    public boolean hasUnreadMessages() {
        return this.unreadCount != null && this.unreadCount > 0;
    }

    // ==================== VALIDATION HELPERS ====================

    private void validateActive() {
        if (!this.status.canAccessChannel()) {
            throw new IllegalStateException("Member is not active in channel");
        }
    }

    private void validateCanAccess() {
        if (!this.status.canAccessChannel()) {
            throw new IllegalStateException("Member cannot access channel");
        }
    }
}
