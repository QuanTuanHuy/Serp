/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.enums.ChannelType;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChannelResponse {
    
    private Long id;
    private Long tenantId;
    private Long createdBy;
    private String name;
    private String description;
    private ChannelType type;
    private String entityType;
    private Long entityId;
    private Boolean isPrivate;
    private Boolean isArchived;
    private Integer memberCount;
    private Integer messageCount;
    private Long lastMessageAt;
    private Map<String, Object> metadata;
    private Long createdAt;
    private Long updatedAt;
    
    // Additional computed fields
    private Integer unreadCount;
    private Boolean isMember;
    private Boolean canManage;
    private List<ChannelMemberResponse> members;
    private MessageResponse lastMessage;

    public static ChannelResponse fromEntity(ChannelEntity entity) {
        if (entity == null) {
            return null;
        }
        return ChannelResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .createdBy(entity.getCreatedBy())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .isPrivate(entity.getIsPrivate())
                .isArchived(entity.getIsArchived())
                .memberCount(entity.getMemberCount())
                .messageCount(entity.getMessageCount())
                .lastMessageAt(entity.getLastMessageAt())
                .metadata(entity.getMetadata())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
