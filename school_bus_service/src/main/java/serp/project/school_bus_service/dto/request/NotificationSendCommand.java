package serp.project.school_bus_service.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import serp.project.school_bus_service.enums.NotificationDeliveryChannel;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationSendCommand extends BaseNotificationCommand {

    private Long userId;
    private String sourceEventId;
    private String actionType;
    private String entityType;
    private Long entityId;
    private List<NotificationDeliveryChannel> deliveryChannels =
            new ArrayList<>(List.of(NotificationDeliveryChannel.IN_APP));
    private Long expiresAt;
}
