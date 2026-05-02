/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateRequestData {
    private Long userId;
    private Long tenantId;
    private String title;
    private String message;
    private String type;
    private String category;
    private String priority;
    private String sourceService;
    private String sourceEventId;
    private String actionUrl;
    private String actionType;
    private String entityType;
    private Long entityId;
    private List<String> deliveryChannels;
    private Long expiresAt;
    private Map<String, Object> metadata;
}
