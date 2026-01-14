/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message entity with DDD rich domain model
 */

package serp.project.discuss_service.core.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.discuss_service.core.domain.enums.MessageType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Message entity represents a message in a channel.
 * Contains business logic for message operations following DDD principles.
 * 
 * Message types:
 * - STANDARD: User messages with text, attachments, or both
 * - SYSTEM: System-generated notifications (requires content)
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class MessageEntity extends BaseEntity {

    private Long channelId;
    private Long senderId;
    private Long tenantId;
    
    private String content;
    
    @Builder.Default
    private MessageType messageType = MessageType.STANDARD;
    
    // Mentions (@user)
    @Builder.Default
    private List<Long> mentions = new ArrayList<>();
    
    // Threading
    private Long parentId;
    
    @Builder.Default
    private Integer threadCount = 0;
    
    // Editing & Deletion
    @Builder.Default
    private Boolean isEdited = false;
    private Long editedAt;
    
    @Builder.Default
    private Boolean isDeleted = false;
    private Long deletedAt;
    private Long deletedBy;
    
    // Reactions - stored as list of Reaction value objects
    @Builder.Default
    private List<ReactionVO> reactions = new ArrayList<>();
    
    // Read receipts
    @Builder.Default
    private List<Long> readBy = new ArrayList<>();
    
    // Metadata
    private Map<String, Object> metadata;
    
    // Aggregated attachments (loaded when needed)
    @Builder.Default
    private List<AttachmentEntity> attachments = new ArrayList<>();

    // ==================== FACTORY METHODS ====================

    /**
     * Create a new standard message (text with optional attachments)
     */
    public static MessageEntity create(Long channelId, Long senderId, Long tenantId, 
                                       String content, List<Long> mentions) {
        long now = Instant.now().toEpochMilli();
        return MessageEntity.builder()
                .channelId(channelId)
                .senderId(senderId)
                .tenantId(tenantId)
                .content(content)
                .messageType(MessageType.STANDARD)
                .mentions(mentions != null ? mentions : new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Create a new standard message (backward compatibility alias)
     */
    public static MessageEntity createText(Long channelId, Long senderId, Long tenantId, 
                                           String content, List<Long> mentions) {
        return create(channelId, senderId, tenantId, content, mentions);
    }

    /**
     * Create a system message (requires content)
     */
    public static MessageEntity createSystem(Long channelId, Long tenantId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content is required for system messages");
        }
        long now = Instant.now().toEpochMilli();
        return MessageEntity.builder()
                .channelId(channelId)
                .senderId(0L) // System user
                .tenantId(tenantId)
                .content(content)
                .messageType(MessageType.SYSTEM)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Create a reply message
     */
    public static MessageEntity createReply(Long channelId, Long senderId, Long tenantId,
                                            String content, Long parentId, List<Long> mentions) {
        long now = Instant.now().toEpochMilli();
        return MessageEntity.builder()
                .channelId(channelId)
                .senderId(senderId)
                .tenantId(tenantId)
                .content(content)
                .messageType(MessageType.STANDARD)
                .parentId(parentId)
                .mentions(mentions != null ? mentions : new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Create a message with attachments (content is optional)
     */
    public static MessageEntity createWithAttachments(Long channelId, Long senderId, Long tenantId,
                                                      String content, List<Long> mentions) {
        long now = Instant.now().toEpochMilli();
        return MessageEntity.builder()
                .channelId(channelId)
                .senderId(senderId)
                .tenantId(tenantId)
                .content(content != null ? content : "")
                .messageType(MessageType.STANDARD)
                .mentions(mentions != null ? mentions : new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ==================== BUSINESS LOGIC ====================

    /**
     * Edit message content
     */
    public void edit(String newContent, Long editorId) {
        validateNotDeleted();
        validateOwnership(editorId);
        
        if (!this.messageType.isUserGenerated()) {
            throw new IllegalStateException("Cannot edit system messages");
        }
        
        this.content = newContent;
        this.isEdited = true;
        this.editedAt = Instant.now().toEpochMilli();
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Soft delete message
     */
    public void delete(Long deleterId, boolean isAdmin) {
        validateNotDeleted();
        
        if (!this.senderId.equals(deleterId) && !isAdmin) {
            throw new IllegalStateException("Only sender or admin can delete message");
        }
        
        this.isDeleted = true;
        this.deletedAt = Instant.now().toEpochMilli();
        this.deletedBy = deleterId;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Add reaction to message
     */
    public void addReaction(String emoji, Long userId) {
        validateNotDeleted();
        
        if (this.reactions == null) {
            this.reactions = new ArrayList<>();
        }
        
        Optional<ReactionVO> existing = this.reactions.stream()
                .filter(r -> r.getEmoji().equals(emoji))
                .findFirst();
        
        if (existing.isPresent()) {
            existing.get().addUser(userId);
        } else {
            this.reactions.add(new ReactionVO(emoji, userId));
        }
        
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Remove reaction from message
     */
    public void removeReaction(String emoji, Long userId) {
        validateNotDeleted();
        
        if (this.reactions == null) {
            return;
        }
        
        this.reactions.stream()
                .filter(r -> r.getEmoji().equals(emoji))
                .findFirst()
                .ifPresent(r -> {
                    r.removeUser(userId);
                    if (r.isEmpty()) {
                        this.reactions.remove(r);
                    }
                });
        
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Mark message as read by user
     */
    public void markReadBy(Long userId) {
        if (this.readBy == null) {
            this.readBy = new ArrayList<>();
        }
        
        if (!this.readBy.contains(userId)) {
            this.readBy.add(userId);
        }
    }

    /**
     * Increment thread count (when reply is added)
     */
    public void incrementThreadCount() {
        if (this.threadCount == null) {
            this.threadCount = 0;
        }
        this.threadCount++;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Decrement thread count (when reply is deleted)
     */
    public void decrementThreadCount() {
        if (this.threadCount != null && this.threadCount > 0) {
            this.threadCount--;
            setUpdatedAt(Instant.now().toEpochMilli());
        }
    }

    // ==================== QUERY METHODS ====================

    @JsonIgnore
    public boolean isReply() {
        return this.parentId != null;
    }

    @JsonIgnore
    public boolean hasThread() {
        return this.threadCount != null && this.threadCount > 0;
    }

    @JsonIgnore
    public boolean hasMentions() {
        return this.mentions != null && !this.mentions.isEmpty();
    }

    @JsonIgnore
    public boolean mentions(Long userId) {
        return this.mentions != null && this.mentions.contains(userId);
    }

    @JsonIgnore
    public boolean hasReactions() {
        return this.reactions != null && !this.reactions.isEmpty();
    }

    @JsonIgnore
    public boolean hasAttachments() {
        return this.attachments != null && !this.attachments.isEmpty();
    }

    @JsonIgnore
    public boolean hasContent() {
        return this.content != null && !this.content.trim().isEmpty();
    }

    @JsonIgnore
    public boolean isReadBy(Long userId) {
        return this.readBy != null && this.readBy.contains(userId);
    }

    @JsonIgnore
    public boolean isSentBy(Long userId) {
        return this.senderId != null && this.senderId.equals(userId);
    }

    @JsonIgnore
    public boolean isSystemMessage() {
        return this.messageType == MessageType.SYSTEM;
    }

    @JsonIgnore
    public int getReactionCount() {
        if (this.reactions == null) {
            return 0;
        }
        return this.reactions.stream()
                .mapToInt(r -> r.getUserIds().size())
                .sum();
    }

    @JsonIgnore
    public int getReadCount() {
        return this.readBy != null ? this.readBy.size() : 0;
    }

    // ==================== VALIDATION HELPERS ====================

    private void validateNotDeleted() {
        if (Boolean.TRUE.equals(this.isDeleted)) {
            throw new IllegalStateException("Cannot modify deleted message");
        }
    }

    private void validateOwnership(Long userId) {
        if (!this.senderId.equals(userId)) {
            throw new IllegalStateException("Only sender can modify message");
        }
    }

    /**
     * Validate message for creation.
     * 
     * Rules:
     * - channelId, senderId, tenantId are required
     * - SYSTEM messages require content
     * - STANDARD messages require content OR attachments (checked at service layer when attachments are known)
     */
    public void validateForCreation() {
        if (this.channelId == null) {
            throw new IllegalArgumentException("Channel ID is required");
        }
        if (this.senderId == null) {
            throw new IllegalArgumentException("Sender ID is required");
        }
        if (this.tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required");
        }
        // SYSTEM messages require content
        if (this.messageType == MessageType.SYSTEM && !hasContent()) {
            throw new IllegalArgumentException("Content is required for system messages");
        }
        // For STANDARD messages: content OR attachments required
        // Attachment check is done at service layer since attachments may be added after creation
    }

    /**
     * Validate that message has content or attachments.
     * Called after attachments are set.
     */
    public void validateHasContentOrAttachments() {
        if (this.messageType == MessageType.STANDARD && !hasContent() && !hasAttachments()) {
            throw new IllegalArgumentException("Message must have content or attachments");
        }
    }

    // ==================== NESTED VALUE OBJECT ====================

    /**
     * Reaction value object
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactionVO {
        private String emoji;
        private List<Long> userIds = new ArrayList<>();

        public ReactionVO(String emoji, Long userId) {
            this.emoji = emoji;
            this.userIds = new ArrayList<>();
            this.userIds.add(userId);
        }

        public void addUser(Long userId) {
            if (!this.userIds.contains(userId)) {
                this.userIds.add(userId);
            }
        }

        public void removeUser(Long userId) {
            this.userIds.remove(userId);
        }

        public boolean isEmpty() {
            return this.userIds.isEmpty();
        }

        public boolean hasUser(Long userId) {
            return this.userIds.contains(userId);
        }
    }
}
