package serp.project.school_bus_service.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BulkNotificationSendCommand extends BaseNotificationCommand {

    private List<Long> userIds = new ArrayList<>();
}
