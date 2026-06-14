package serp.project.school_bus_service.dto.message;

import lombok.Data;

@Data
public class NotificationEventMetadata {

    private String id;
    private String type;
    private String source;
    private String v;
    private Long ts;
    private String traceId;
}
