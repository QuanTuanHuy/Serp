package serp.project.school_bus_service.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StudentSubscriptionUpsertRequest extends BaseCommandRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "schoolId is required")
    private Long schoolId;

    private Long pickupPointId;
    private Long dropoffPointId;

    @NotBlank(message = "tripOption is required")
    private String tripOption;

    private Boolean monday = Boolean.TRUE;
    private Boolean tuesday = Boolean.TRUE;
    private Boolean wednesday = Boolean.TRUE;
    private Boolean thursday = Boolean.TRUE;
    private Boolean friday = Boolean.TRUE;
    private Boolean saturday = Boolean.FALSE;
    private Boolean sunday = Boolean.FALSE;

    @NotNull(message = "effectiveFrom is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
    private String status;
    private Long sourceRequestId;
}

