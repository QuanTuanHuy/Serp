/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationCreateRequest(
        Long userId,
        Long tenantId,
        String title,
        String message,
        String type,
        String category,
        String priority,
        String sourceService,
        String sourceEventId,
        String actionUrl,
        String actionType,
        String entityType,
        Long entityId,
        List<String> deliveryChannels,
        Long expiresAt,
        Map<String, Object> metadata) {
}
