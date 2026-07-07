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

    @NotNull(message = "Vui lòng chọn học sinh")
    private Long studentId;

    @NotNull(message = "Vui lòng chọn đăng ký")
    private Long subscriptionId;
}
