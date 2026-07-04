package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RouteAssignmentRequest extends BaseCommandRequest {

    private Long busId;

    @NotNull
    private Long driverId;

    private Long attendantId;

    /** Optional note visible in assignment record. */
    private String assignmentNote;

    /** Reason logged to assignment history. */
    private String reason;
}

