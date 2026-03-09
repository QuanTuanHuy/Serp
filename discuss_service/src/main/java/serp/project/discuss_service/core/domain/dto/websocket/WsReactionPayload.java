/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket reaction event payload
 */

package serp.project.discuss_service.core.domain.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket payload for reaction events (added, removed)
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WsReactionPayload {
    
    private Long messageId;
    private Long channelId;
    private Long userId;
    private String emoji;
    private Boolean added;
    
    public static WsReactionPayload added(Long messageId, Long channelId, Long userId, String emoji) {
        return WsReactionPayload.builder()
                .messageId(messageId)
                .channelId(channelId)
                .userId(userId)
                .emoji(emoji)
                .added(true)
                .build();
    }
    
    public static WsReactionPayload removed(Long messageId, Long channelId, Long userId, String emoji) {
        return WsReactionPayload.builder()
                .messageId(messageId)
                .channelId(channelId)
                .userId(userId)
                .emoji(emoji)
                .added(false)
                .build();
    }
}
