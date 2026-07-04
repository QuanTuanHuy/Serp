package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.enums.SubscriptionStatus;
import serp.project.school_bus_service.enums.TripOption;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class StudentSubscriptionResponse extends BaseResponse {
    private String subscriptionCode;
    private Long studentId;
    private String studentName;
    private Long schoolId;
    private String schoolName;
    private Long pickupPointId;
    private String pickupPointName;
    private Long dropoffPointId;
    private String dropoffPointName;
    private String tripOption;
    private Boolean monday;
    private Boolean tuesday;
    private Boolean wednesday;
    private Boolean thursday;
    private Boolean friday;
    private Boolean saturday;
    private Boolean sunday;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String status;
    private Long sourceRequestId;
    private String sourceRequestCode;
    private String studentCode;
    private String parentName;
    private String schoolCode;
    private String pickupPointCode;
    private String dropoffPointCode;

    public StudentSubscriptionResponse() {
    }

    public StudentSubscriptionResponse(Long id,
                                       Long tenantId,
                                       Boolean isActive,
                                       Boolean isDeleted,
                                       LocalDateTime createdAt,
                                       String createdBy,
                                       LocalDateTime updatedAt,
                                       String updatedBy,
                                       String subscriptionCode,
                                       Long studentId,
                                       String studentName,
                                       String studentCode,
                                       String parentName,
                                       Long schoolId,
                                       String schoolName,
                                       String schoolCode,
                                       Long pickupPointId,
                                       String pickupPointName,
                                       String pickupPointCode,
                                       Long dropoffPointId,
                                       String dropoffPointName,
                                       String dropoffPointCode,
                                       TripOption tripOption,
                                       Boolean monday,
                                       Boolean tuesday,
                                       Boolean wednesday,
                                       Boolean thursday,
                                       Boolean friday,
                                       Boolean saturday,
                                       Boolean sunday,
                                       LocalDate effectiveFrom,
                                       LocalDate effectiveTo,
                                       SubscriptionStatus status,
                                       Long sourceRequestId,
                                       String sourceRequestCode) {
        setId(id);
        setTenantId(tenantId);
        setIsActive(isActive);
        setIsDeleted(isDeleted);
        setCreatedAt(createdAt);
        setCreatedBy(createdBy);
        setUpdatedAt(updatedAt);
        setUpdatedBy(updatedBy);
        this.subscriptionCode = subscriptionCode;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentCode = studentCode;
        this.parentName = parentName;
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.schoolCode = schoolCode;
        this.pickupPointId = pickupPointId;
        this.pickupPointName = pickupPointName;
        this.pickupPointCode = pickupPointCode;
        this.dropoffPointId = dropoffPointId;
        this.dropoffPointName = dropoffPointName;
        this.dropoffPointCode = dropoffPointCode;
        this.tripOption = tripOption == null ? null : tripOption.name();
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.status = status == null ? null : status.name();
        this.sourceRequestId = sourceRequestId;
        this.sourceRequestCode = sourceRequestCode;
    }
}

