package serp.project.school_bus_service.dto.message;

import lombok.Data;

@Data
public class NotificationEventMessage<T> {

    private NotificationEventMetadata meta;
    private T data;
}
