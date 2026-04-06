package serp.project.school_bus_service.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RouteAssignmentRequest extends BaseCommandRequest {

    @NotNull
    private Long busId;

    @NotNull
    private Long driverId;

    private Long attendantId;
}
