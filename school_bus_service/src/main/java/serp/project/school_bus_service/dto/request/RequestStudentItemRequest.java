package serp.project.school_bus_service.dto.request;

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
    private Long dropoffPointId;
    private String tripOption;

    private Boolean monday;
    private Boolean tuesday;
    private Boolean wednesday;
    private Boolean thursday;
    private Boolean friday;
    private Boolean saturday;
    private Boolean sunday;

    private Long targetSubscriptionId;
    private String studentNote;
}
