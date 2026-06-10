package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Request body for creating a new Planning Session. */
@Getter
@Setter
@NoArgsConstructor
public class PlanningSessionCreateRequest {

    @NotNull
    private Long schoolId;


    @NotNull
    private LocalDate serviceDate;

    /** OUTBOUND or RETURN */
    @NotBlank
    private String routeDirection;

    /** MANUAL or GREEDY */
    @NotBlank
    private String planningMethod;

    private String planningNotes;
}
