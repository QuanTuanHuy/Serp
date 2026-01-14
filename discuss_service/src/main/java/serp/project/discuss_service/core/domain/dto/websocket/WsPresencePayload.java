/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket presence event payload
 */

package serp.project.discuss_service.core.domain.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Boolean online;
    private Long channelId;
    
    public static WsPresencePayload online(Long userId, String userName, Long channelId) {
        return WsPresencePayload.builder()
                .userId(userId)
                .userName(userName)
                .online(true)
                .channelId(channelId)
                .build();
    }
    
    public static WsPresencePayload offline(Long userId, Long channelId) {
        return WsPresencePayload.builder()
                .userId(userId)
                .online(false)
                .channelId(channelId)
                .build();
    }
}
