package serp.project.school_bus_service.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ManualDispatchRequest extends BaseCommandRequest {

    @NotNull(message = "busId is required")
    private Long busId;

    @NotNull(message = "driverId is required")
    private Long driverId;

    private Long attendantId;

    private List<Long> orderedStopIds;

    private String notes;
}

