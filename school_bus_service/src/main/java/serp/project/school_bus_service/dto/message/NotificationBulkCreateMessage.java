package serp.project.school_bus_service.dto.message;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class NotificationBulkCreateMessage {

    private List<Long> userIds = new ArrayList<>();
    private Long tenantId;
    private String title;
    private String message;
    private String type;
    private String category;
    private String priority;
    private String sourceService;
    private String actionUrl;
    private Map<String, Object> metadata = new LinkedHashMap<>();
}
