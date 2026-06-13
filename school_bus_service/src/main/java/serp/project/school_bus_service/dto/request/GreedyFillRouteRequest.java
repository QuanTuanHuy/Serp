package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GreedyFillRouteRequest {

    private Boolean preserveExistingAssignments = Boolean.TRUE;

    @Min(value = 1, message = "maxStops must be greater than zero")
    private Integer maxStops;

    public boolean preserveExistingAssignments() {
        return preserveExistingAssignments == null || preserveExistingAssignments;
    }
}
