package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request to manually add a student to a specific route stop.
 * planningSessionId is NOT sent by the client — backend resolves it from route.planningSession.
 */
@Getter
@Setter
public class AddStudentToStopRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "subscriptionId is required")
    private Long subscriptionId;
}
