package serp.project.school_bus_service.dto.request;

import lombok.Data;
import serp.project.school_bus_service.enums.NotificationCategory;
import serp.project.school_bus_service.enums.NotificationPriority;
import serp.project.school_bus_service.enums.NotificationType;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public abstract class BaseNotificationCommand {

    private Long tenantId;
    private String title;
    private String message;
    private NotificationType type = NotificationType.INFO;
    private NotificationCategory category = NotificationCategory.SCHOOL_BUS;
    private NotificationPriority priority = NotificationPriority.MEDIUM;
    private String actionUrl;
    private Map<String, Object> metadata = new LinkedHashMap<>();
}
