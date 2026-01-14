/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Base WebSocket event wrapper
 */

package serp.project.discuss_service.core.domain.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base wrapper for all WebSocket events.
 * Contains event type, payload, and metadata.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WsEvent<T> {
    
    private WsEventType type;
    private T payload;
    private Long channelId;
    private Long timestamp;
    
    public static <T> WsEvent<T> of(WsEventType type, T payload, Long channelId) {
        return WsEvent.<T>builder()
                .type(type)
                .payload(payload)
                .channelId(channelId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> WsEvent<T> of(WsEventType type, T payload) {
        return WsEvent.<T>builder()
                .type(type)
                .payload(payload)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
