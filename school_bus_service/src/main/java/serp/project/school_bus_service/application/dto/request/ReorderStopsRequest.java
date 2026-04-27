package serp.project.school_bus_service.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReorderStopsRequest extends BaseCommandRequest {

    @NotEmpty(message = "orderedStopIds is required")
    private List<Long> orderedStopIds;
}

