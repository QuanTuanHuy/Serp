/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel aggregate root entity with DDD rich domain model
 */

package serp.project.discuss_service.core.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.discuss_service.core.domain.enums.ChannelType;
import serp.project.discuss_service.core.domain.enums.MemberRole;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Channel is the aggregate root for discussion channels.
 * Encapsulates business logic for channel operations following DDD principles.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ChannelEntity extends BaseEntity {

    private Long tenantId;
    private Long createdBy;
    
    private String name;
    private String description;
    
    private ChannelType type;
    
    // Entity linking (for TOPIC channels)
    private String entityType;
    private Long entityId;
    
    // Settings
    @Builder.Default
    private Boolean isPrivate = false;
    
    @Builder.Default
    private Boolean isArchived = false;
    
    // Denormalized stats
    @Builder.Default
    private Integer memberCount = 0;
    
    @Builder.Default
    private Integer messageCount = 0;
    
    private Long lastMessageAt;
    
    // Metadata (JSONB)
    private Map<String, Object> metadata;
    
    // Aggregated members (loaded when needed)
    @Builder.Default
    private List<ChannelMemberEntity> members = new ArrayList<>();

    // ==================== FACTORY METHODS ====================

    /**
     * Create a new DIRECT channel between two users
     */
    public static ChannelEntity createDirect(Long tenantId, Long userId1, Long userId2) {
        // Ensure consistent ordering (smaller ID first)
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);
        
        return ChannelEntity.builder()
                .tenantId(tenantId)
                .createdBy(smallerId)
                .name(String.format("DM-%d-%d", smallerId, largerId))
                .type(ChannelType.DIRECT)
                .entityId(largerId) // Store second user in entityId for uniqueness
                .isPrivate(true)
                .memberCount(2)
                .createdAt(Instant.now().toEpochMilli())
                .updatedAt(Instant.now().toEpochMilli())
                .build();
    }

    /**
     * Create a new GROUP channel
     */
    public static ChannelEntity createGroup(Long tenantId, Long createdBy, String name, String description, boolean isPrivate) {
        return ChannelEntity.builder()
                .tenantId(tenantId)
                .createdBy(createdBy)
                .name(name)
                .description(description)
                .type(ChannelType.GROUP)
                .isPrivate(isPrivate)
                .memberCount(1) // Creator is first member
                .createdAt(Instant.now().toEpochMilli())
                .updatedAt(Instant.now().toEpochMilli())
                .build();
    }

    /**
     * Create a new TOPIC channel linked to an entity
     */
    public static ChannelEntity createTopic(Long tenantId, Long createdBy, String name, 
                                            String entityType, Long entityId) {
        return ChannelEntity.builder()
                .tenantId(tenantId)
                .createdBy(createdBy)
                .name(name)
                .type(ChannelType.TOPIC)
                .entityType(entityType)
                .entityId(entityId)
                .isPrivate(false) // Topic channels are usually public within tenant
                .memberCount(1)
                .createdAt(Instant.now().toEpochMilli())
                .updatedAt(Instant.now().toEpochMilli())
                .build();
    }

    // ==================== BUSINESS LOGIC ====================

    /**
     * Update channel info (name, description)
     */
    public void updateInfo(String name, String description) {
        validateNotArchived();
        
        if (this.type == ChannelType.DIRECT) {
            throw new IllegalStateException("Cannot update name of DIRECT channel");
        }
        
        this.name = name;
        this.description = description;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Archive the channel (soft delete)
     */
    public void archive() {
        if (this.isArchived) {
            throw new IllegalStateException("Channel is already archived");
        }
        if (this.type == ChannelType.DIRECT) {
            throw new IllegalStateException("Cannot archive DIRECT channel");
        }
        
        this.isArchived = true;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Unarchive the channel
     */
    public void unarchive() {
        if (!this.isArchived) {
            throw new IllegalStateException("Channel is not archived");
        }
        
        this.isArchived = false;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Record a new message (updates stats)
     */
    public void recordMessage() {
        validateNotArchived();
        
        this.messageCount++;
        this.lastMessageAt = Instant.now().toEpochMilli();
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Update member count when member is added
     */
    public void incrementMemberCount() {
        if (this.type == ChannelType.DIRECT && this.memberCount >= 2) {
            throw new IllegalStateException("DIRECT channel cannot have more than 2 members");
        }
        
        this.memberCount++;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Update member count when member is removed
     */
    public void decrementMemberCount() {
        if (this.memberCount > 0) {
            this.memberCount--;
        }
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    // ==================== QUERY METHODS ====================

    /**
     * Check if user is owner of the channel
     */
    @JsonIgnore
    public boolean isOwner(Long userId) {
        return this.createdBy != null && this.createdBy.equals(userId);
    }

    /**
     * Check if this is a direct message channel
     */
    @JsonIgnore
    public boolean isDirect() {
        return this.type == ChannelType.DIRECT;
    }

    /**
     * Check if this is a group channel
     */
    @JsonIgnore
    public boolean isGroup() {
        return this.type == ChannelType.GROUP;
    }

    /**
     * Check if this is a topic channel
     */
    @JsonIgnore
    public boolean isTopic() {
        return this.type == ChannelType.TOPIC;
    }

    /**
     * Check if channel is active (not archived)
     */
    @JsonIgnore
    public boolean isActive() {
        return !Boolean.TRUE.equals(this.isArchived);
    }

    /**
     * Get the other user in a DIRECT channel
     */
    @JsonIgnore
    public Optional<Long> getOtherUserId(Long currentUserId) {
        if (this.type != ChannelType.DIRECT) {
            return Optional.empty();
        }
        
        if (this.createdBy.equals(currentUserId)) {
            return Optional.ofNullable(this.entityId);
        } else if (this.entityId != null && this.entityId.equals(currentUserId)) {
            return Optional.of(this.createdBy);
        }
        
        return Optional.empty();
    }

    /**
     * Check if channel is linked to specific entity
     */
    @JsonIgnore
    public boolean isLinkedTo(String entityType, Long entityId) {
        return this.type == ChannelType.TOPIC &&
               entityType.equals(this.entityType) &&
               entityId.equals(this.entityId);
    }

    /**
     * Find member by user ID
     */
    @JsonIgnore
    public Optional<ChannelMemberEntity> findMember(Long userId) {
        if (this.members == null) {
            return Optional.empty();
        }
        return this.members.stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * Check if user can manage channel (owner or admin)
     */
    @JsonIgnore
    public boolean canManage(Long userId) {
        if (isOwner(userId)) {
            return true;
        }
        
        return findMember(userId)
                .map(m -> m.getRole().canManageChannel())
                .orElse(false);
    }

    // ==================== VALIDATION HELPERS ====================

    private void validateNotArchived() {
        if (Boolean.TRUE.equals(this.isArchived)) {
            throw new IllegalStateException("Cannot modify archived channel");
        }
    }

    /**
     * Validate channel state for creation
     */
    public void validateForCreation() {
        if (this.tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required");
        }
        if (this.createdBy == null) {
            throw new IllegalArgumentException("Creator ID is required");
        }
        if (this.type == null) {
            throw new IllegalArgumentException("Channel type is required");
        }
        if (this.type.requiresEntity() && (this.entityType == null || this.entityId == null)) {
            throw new IllegalArgumentException("Entity type and ID are required for TOPIC channel");
        }
        if (this.name == null || this.name.isBlank()) {
            throw new IllegalArgumentException("Channel name is required");
        }
    }
}
