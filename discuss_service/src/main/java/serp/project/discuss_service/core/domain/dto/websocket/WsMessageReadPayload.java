/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket message read payload
 */

package serp.project.discuss_service.core.domain.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WsMessageReadPayload {

    private Long messageId;
    private Long channelId;
    private Long userId;
    private List<Long> readBy;
    private Integer readCount;
}
