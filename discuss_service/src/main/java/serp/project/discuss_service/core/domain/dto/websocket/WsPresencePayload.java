/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket presence event payload
 */

package serp.project.discuss_service.core.domain.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.discuss_service.core.domain.entity.UserPresenceEntity;
import serp.project.discuss_service.core.domain.enums.UserStatus;

/**
 * WebSocket payload for presence events (online/offline)
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WsPresencePayload {
    
    private Long userId;
    private String userName;
    private String avatarUrl;
    private Boolean online;
    private UserStatus status;
    private String statusMessage;
    private Long lastSeenAt;

    public static WsPresencePayload fromEntity(UserPresenceEntity presence, String userName, String avatarUrl) {
        return WsPresencePayload.builder()
                .userId(presence.getUserId())
                .userName(userName)
                .avatarUrl(avatarUrl)
                .online(presence.isOnline())
                .status(presence.getStatus())
                .statusMessage(presence.getStatusMessage())
                .lastSeenAt(presence.getLastSeenAt())
                .build();
    }
}
