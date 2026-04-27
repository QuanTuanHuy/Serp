package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StudentResponse extends BaseResponse {

    private Long schoolId;
    private String schoolName;
    private Long parentProfileId;
    private String parentProfileName;
    private Long pickupPointId;
    private String pickupPointName;
    private String fullName;
    private String studentCode;
    private String grade;
    private String homeAddress;
    private LocalDate dateOfBirth;
    private String gender;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String specialNote;
}
