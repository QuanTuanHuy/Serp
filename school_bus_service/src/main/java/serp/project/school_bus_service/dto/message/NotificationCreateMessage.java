package serp.project.school_bus_service.dto.message;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class NotificationCreateMessage {

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
    private List<String> deliveryChannels = new ArrayList<>();
    private Long expiresAt;
    private Map<String, Object> metadata = new LinkedHashMap<>();
}
