package serp.project.school_bus_service.dto.request;

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

    private Long parentProfileId;

    /** Default pickup point (legacy field name) */
    private Long pickupPointId;

    private Long defaultDropoffPointId;

    @NotBlank
    private String fullName;

    private String studentCode;

    private String grade;

    private String className;

    private String homeAddress;

    private java.time.LocalDate dateOfBirth;

    private String gender;

    private String specialNote;
}
