/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket typing indicator payload
 */

package serp.project.discuss_service.core.domain.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket payload for typing indicator events
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WsTypingPayload {
    
    private Long channelId;
    private Long userId;
    private String userName;
    private Boolean isTyping;
    
    public static WsTypingPayload start(Long channelId, Long userId, String userName) {
        return WsTypingPayload.builder()
                .channelId(channelId)
                .userId(userId)
                .userName(userName)
                .isTyping(true)
                .build();
    }
    
    public static WsTypingPayload stop(Long channelId, Long userId) {
        return WsTypingPayload.builder()
                .channelId(channelId)
                .userId(userId)
                .isTyping(false)
                .build();
    }
}
