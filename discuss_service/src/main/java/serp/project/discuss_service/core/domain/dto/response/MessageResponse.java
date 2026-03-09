/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse.UserInfo;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.domain.enums.MessageType;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {
    
    private Long id;
    private Long channelId;
    private Long senderId;
    private Long tenantId;
    private String content;
    private MessageType messageType;
    private List<Long> mentions;
    private Long parentId;
    private Integer threadCount;
    private Boolean isEdited;
    private Long editedAt;
    private Boolean isDeleted;
    private Long deletedAt;
    private Long deletedBy;
    private List<ReactionResponse> reactions;
    private Integer readCount;
    private Map<String, Object> metadata;
    private Long createdAt;
    private Long updatedAt;
    
    // Additional computed fields
    private Boolean isSentByMe;
    private Boolean isReadByMe;
    private UserInfo sender;
    private List<AttachmentResponse> attachments;

    public static MessageResponse fromEntity(MessageEntity entity) {
        if (entity == null) {
            return null;
        }
        List<ReactionResponse> reactionResponses = null;
        if (entity.getReactions() != null && !entity.getReactions().isEmpty()) {
            reactionResponses = entity.getReactions().stream()
                    .map(r -> ReactionResponse.builder()
                            .emoji(r.getEmoji())
                            .userIds(r.getUserIds())
                            .count(r.getUserIds().size())
                            .build())
                    .toList();
        }
        List<AttachmentResponse> attachmentResponses = null;
        if (entity.getAttachments() != null && !entity.getAttachments().isEmpty()) {
            attachmentResponses = entity.getAttachments().stream()
                    .map(AttachmentResponse::fromEntity)
                    .toList();
        }
        
        return MessageResponse.builder()
                .id(entity.getId())
                .channelId(entity.getChannelId())
                .senderId(entity.getSenderId())
                .tenantId(entity.getTenantId())
                .content(entity.getContent())
                .messageType(entity.getMessageType())
                .mentions(entity.getMentions())
                .parentId(entity.getParentId())
                .threadCount(entity.getThreadCount())
                .isEdited(entity.getIsEdited())
                .editedAt(entity.getEditedAt())
                .isDeleted(entity.getIsDeleted())
                .deletedAt(entity.getDeletedAt())
                .deletedBy(entity.getDeletedBy())
                .reactions(reactionResponses)
                .readCount(entity.getReadCount())
                .metadata(entity.getMetadata())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .attachments(attachmentResponses)
                .build();
    }

    /**
     * Basic sender info for display
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class SenderInfo {
        private Long id;
        private String name;
        private String email;
        private String avatarUrl;
    }
}
