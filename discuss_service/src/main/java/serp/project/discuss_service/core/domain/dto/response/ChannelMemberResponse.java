/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.enums.MemberRole;
import serp.project.discuss_service.core.domain.enums.MemberStatus;
import serp.project.discuss_service.core.domain.enums.NotificationLevel;

import java.util.Map;

/**
 * Response DTO for channel member data
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChannelMemberResponse {
    
    private Long id;
    private Long channelId;
    private Long userId;
    private Long tenantId;
    private MemberRole role;
    private MemberStatus status;
    private Long joinedAt;
    private Long leftAt;
    private Long removedBy;
    private Long lastReadMsgId;
    private Integer unreadCount;
    private Boolean isMuted;
    private Boolean isPinned;
    private NotificationLevel notificationLevel;
    private Map<String, Object> metadata;
    private Long createdAt;
    private Long updatedAt;
    
    // Additional computed fields
    private Boolean isOnline;
    private UserInfo user;

    public static ChannelMemberResponse fromEntity(ChannelMemberEntity entity) {
        if (entity == null) {
            return null;
        }
        return ChannelMemberResponse.builder()
                .id(entity.getId())
                .channelId(entity.getChannelId())
                .userId(entity.getUserId())
                .tenantId(entity.getTenantId())
                .role(entity.getRole())
                .status(entity.getStatus())
                .joinedAt(entity.getJoinedAt())
                .leftAt(entity.getLeftAt())
                .removedBy(entity.getRemovedBy())
                .lastReadMsgId(entity.getLastReadMsgId())
                .unreadCount(entity.getUnreadCount())
                .isMuted(entity.getIsMuted())
                .isPinned(entity.getIsPinned())
                .notificationLevel(entity.getNotificationLevel())
                .metadata(entity.getMetadata())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Basic user info for display
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String avatarUrl;
    }
}
