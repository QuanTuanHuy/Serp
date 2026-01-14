/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Activity feed entity
 */

package serp.project.discuss_service.core.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.discuss_service.core.domain.enums.ActionType;

import java.time.Instant;
import java.util.Map;

/**
 * ActivityFeed entity represents an activity notification for a user.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ActivityFeedEntity extends BaseEntity {

    private Long tenantId;
    private Long userId; // Recipient of the activity
    
    private ActionType actionType;
    private Long actorId; // Who performed the action
    
    // Entity references
    private String entityType;
    private Long entityId;
    
    // Channel/Message references
    private Long channelId;
    private Long messageId;
    
    // Display
    private String title;
    private String description;
    
    // Status
    @Builder.Default
    private Boolean isRead = false;
    private Long readAt;
    
    // When the activity occurred
    private Long occurredAt;
    
    // Metadata
    private Map<String, Object> metadata;

    // ==================== FACTORY METHODS ====================

    /**
     * Create activity for a message mention
     */
    public static ActivityFeedEntity createMention(Long tenantId, Long userId, Long actorId,
                                                   Long channelId, Long messageId, 
                                                   String actorName, String channelName) {
        long now = Instant.now().toEpochMilli();
        return ActivityFeedEntity.builder()
                .tenantId(tenantId)
                .userId(userId)
                .actionType(ActionType.MENTION_RECEIVED)
                .actorId(actorId)
                .channelId(channelId)
                .messageId(messageId)
                .title("New Mention")
                .description(String.format("%s mentioned you in %s", actorName, channelName))
                .occurredAt(now)
                .createdAt(now)
                .build();
    }

    /**
     * Create activity for a reply
     */
    public static ActivityFeedEntity createReply(Long tenantId, Long userId, Long actorId,
                                                 Long channelId, Long messageId,
                                                 String actorName) {
        long now = Instant.now().toEpochMilli();
        return ActivityFeedEntity.builder()
                .tenantId(tenantId)
                .userId(userId)
                .actionType(ActionType.REPLY_RECEIVED)
                .actorId(actorId)
                .channelId(channelId)
                .messageId(messageId)
                .title("New Reply")
                .description(String.format("%s replied to your message", actorName))
                .occurredAt(now)
                .createdAt(now)
                .build();
    }

    /**
     * Create activity for reaction
     */
    public static ActivityFeedEntity createReaction(Long tenantId, Long userId, Long actorId,
                                                    Long channelId, Long messageId,
                                                    String actorName, String emoji) {
        long now = Instant.now().toEpochMilli();
        return ActivityFeedEntity.builder()
                .tenantId(tenantId)
                .userId(userId)
                .actionType(ActionType.REACTION_ADDED)
                .actorId(actorId)
                .channelId(channelId)
                .messageId(messageId)
                .title("New Reaction")
                .description(String.format("%s reacted %s to your message", actorName, emoji))
                .occurredAt(now)
                .createdAt(now)
                .build();
    }

    /**
     * Create activity for member added
     */
    public static ActivityFeedEntity createMemberAdded(Long tenantId, Long userId, Long actorId,
                                                       Long channelId, String actorName, 
                                                       String channelName) {
        long now = Instant.now().toEpochMilli();
        return ActivityFeedEntity.builder()
                .tenantId(tenantId)
                .userId(userId)
                .actionType(ActionType.MEMBER_ADDED)
                .actorId(actorId)
                .channelId(channelId)
                .title("Added to Channel")
                .description(String.format("%s added you to %s", actorName, channelName))
                .occurredAt(now)
                .createdAt(now)
                .build();
    }

    /**
     * Create activity from external event (task assigned, customer created, etc.)
     */
    public static ActivityFeedEntity createFromEvent(Long tenantId, Long userId, Long actorId,
                                                     ActionType actionType, String entityType,
                                                     Long entityId, String title, String description) {
        long now = Instant.now().toEpochMilli();
        return ActivityFeedEntity.builder()
                .tenantId(tenantId)
                .userId(userId)
                .actionType(actionType)
                .actorId(actorId)
                .entityType(entityType)
                .entityId(entityId)
                .title(title)
                .description(description)
                .occurredAt(now)
                .createdAt(now)
                .build();
    }

    // ==================== BUSINESS LOGIC ====================

    /**
     * Mark activity as read
     */
    public void markAsRead() {
        if (!Boolean.TRUE.equals(this.isRead)) {
            this.isRead = true;
            this.readAt = Instant.now().toEpochMilli();
        }
    }

    /**
     * Mark activity as unread
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    // ==================== QUERY METHODS ====================

    @JsonIgnore
    public boolean isUnread() {
        return !Boolean.TRUE.equals(this.isRead);
    }

    @JsonIgnore
    public boolean isChannelActivity() {
        return this.channelId != null;
    }

    @JsonIgnore
    public boolean isEntityActivity() {
        return this.entityType != null && this.entityId != null;
    }

    @JsonIgnore
    public boolean requiresNotification() {
        return this.actionType.requiresNotification();
    }
}
