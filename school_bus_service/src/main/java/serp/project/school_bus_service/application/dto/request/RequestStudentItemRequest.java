package serp.project.school_bus_service.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequestStudentItemRequest extends BaseCommandRequest {

    @NotNull
    private Long studentId;

    private Long pickupPointId;
}
