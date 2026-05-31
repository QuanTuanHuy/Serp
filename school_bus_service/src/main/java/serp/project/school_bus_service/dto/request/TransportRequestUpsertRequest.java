package serp.project.school_bus_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TransportRequestUpsertRequest extends BaseCommandRequest {

    @NotNull
    private Long parentProfileId;

    @NotNull
    private Long schoolId;

    @NotBlank
    private String requestType;

    @NotNull
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private String notes;

    private String changeReason;

    @Valid
    @NotEmpty
    private List<RequestStudentItemRequest> students;
}
