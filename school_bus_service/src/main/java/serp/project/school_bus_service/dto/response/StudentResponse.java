package serp.project.school_bus_service.dto.response;

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
    /** Default pickup point (legacy: pickupPointId) */
    private Long pickupPointId;
    private String pickupPointName;
    private Long defaultDropoffPointId;
    private String defaultDropoffPointName;
    private String fullName;
    private String studentCode;
    private String grade;
    private String className;
    private String homeAddress;
    private LocalDate dateOfBirth;
    private String gender;
    private String specialNote;
}
