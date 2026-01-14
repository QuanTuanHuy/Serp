/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for unread counts
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UnreadCountResponse {
    
    private Long userId;
    private Long totalUnread;
    private Map<Long, Integer> channelCounts;
}
