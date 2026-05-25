package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Used by the preview API to check demand without creating a session. */
@Getter
@Setter
@NoArgsConstructor
public class PlanningSessionPreviewRequest {

    @NotNull
    private Long schoolId;

    @NotNull
    private Long schoolScheduleId;

    @NotNull
    private LocalDate serviceDate;

    /** OUTBOUND or RETURN */
    @NotBlank
    private String routeDirection;
}
