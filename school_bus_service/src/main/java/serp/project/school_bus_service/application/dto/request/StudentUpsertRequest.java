package serp.project.school_bus_service.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentUpsertRequest extends BaseCommandRequest {

    @NotNull
    private Long schoolId;

    @NotNull
    private Long parentProfileId;

    private Long pickupPointId;

    @NotBlank
    private String fullName;

    private String studentCode;

    private String grade;

    private String homeAddress;
}
