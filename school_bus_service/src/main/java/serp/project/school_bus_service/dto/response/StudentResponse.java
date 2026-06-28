package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public StudentResponse() {
    }

    public StudentResponse(Long id,
                           Long tenantId,
                           Boolean isActive,
                           Boolean isDeleted,
                           LocalDateTime createdAt,
                           String createdBy,
                           LocalDateTime updatedAt,
                           String updatedBy,
                           Long schoolId,
                           String schoolName,
                           Long parentProfileId,
                           String parentProfileName,
                           Long pickupPointId,
                           String pickupPointName,
                           Long defaultDropoffPointId,
                           String defaultDropoffPointName,
                           String fullName,
                           String studentCode,
                           String grade,
                           String className,
                           String homeAddress,
                           LocalDate dateOfBirth,
                           String gender,
                           String specialNote) {
        setId(id);
        setTenantId(tenantId);
        setIsActive(isActive);
        setIsDeleted(isDeleted);
        setCreatedAt(createdAt);
        setCreatedBy(createdBy);
        setUpdatedAt(updatedAt);
        setUpdatedBy(updatedBy);
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.parentProfileId = parentProfileId;
        this.parentProfileName = parentProfileName;
        this.pickupPointId = pickupPointId;
        this.pickupPointName = pickupPointName;
        this.defaultDropoffPointId = defaultDropoffPointId;
        this.defaultDropoffPointName = defaultDropoffPointName;
        this.fullName = fullName;
        this.studentCode = studentCode;
        this.grade = grade;
        this.className = className;
        this.homeAddress = homeAddress;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.specialNote = specialNote;
    }
}
