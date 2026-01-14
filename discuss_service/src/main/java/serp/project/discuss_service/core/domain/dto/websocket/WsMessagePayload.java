/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket message event payload
 */

package serp.project.discuss_service.core.domain.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.discuss_service.core.domain.dto.response.MessageResponse;
import serp.project.discuss_service.core.domain.entity.MessageEntity;

/**
 * WebSocket payload for message events (new, updated, deleted)
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WsMessagePayload {
    
    private Long messageId;
    private Long channelId;
    private Long senderId;
    private MessageResponse message;
    
    public static WsMessagePayload fromEntity(MessageEntity entity) {
        return WsMessagePayload.builder()
                .messageId(entity.getId())
                .channelId(entity.getChannelId())
                .senderId(entity.getSenderId())
                .message(MessageResponse.fromEntity(entity))
                .build();
    }
    
    public static WsMessagePayload forDeletion(Long messageId, Long channelId) {
        return WsMessagePayload.builder()
                .messageId(messageId)
                .channelId(channelId)
                .build();
    }
}
