package serp.project.school_bus_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteTripRequest extends BaseCommandRequest {

    /** Optional completion note. */
    private String note;
}
