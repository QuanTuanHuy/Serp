package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RoutePlanUpsertRequest extends BaseCommandRequest {

    @NotNull
    private Long schoolId;

    private String routeCode;

    @NotBlank
    private String routeDirection;

    @NotBlank
    private String startLocationType;

    private Long startSchoolId;

    private Long startDepotId;

    @NotBlank
    private String endLocationType;

    private Long endSchoolId;

    private Long endDepotId;

    private Long busId;

    @NotBlank
    private String routeName;

    @NotNull
    private LocalDate serviceDate;


    private String planningNotes;
}
