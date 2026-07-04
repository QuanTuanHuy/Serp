package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.enums.RequestSource;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.enums.RequestType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransportRequestResponse extends BaseResponse {

    private Long parentProfileId;
    private String parentProfileName;
    private Long schoolId;
    private String schoolName;
    private Double schoolLatitude;
    private Double schoolLongitude;
    private String requestCode;
    private LocalDateTime requestedAt;
    private String requestSource;
    private String requestType;
    private String status;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String notes;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private String changeReason;
    private Integer studentCount;

    public TransportRequestResponse() {
    }

    public TransportRequestResponse(Long id,
                                    Long tenantId,
                                    Boolean isActive,
                                    Boolean isDeleted,
                                    LocalDateTime createdAt,
                                    String createdBy,
                                    LocalDateTime updatedAt,
                                    String updatedBy,
                                    Long parentProfileId,
                                    String parentProfileName,
                                    String requestCode,
                                    LocalDateTime requestedAt,
                                    RequestSource requestSource,
                                    RequestType requestType,
                                    RequestStatus status,
                                    LocalDate effectiveFrom,
                                    LocalDate effectiveTo,
                                    String notes,
                                    Long approvedBy,
                                    LocalDateTime approvedAt,
                                    String rejectionReason,
                                    String changeReason) {
        setId(id);
        setTenantId(tenantId);
        setIsActive(isActive);
        setIsDeleted(isDeleted);
        setCreatedAt(createdAt);
        setCreatedBy(createdBy);
        setUpdatedAt(updatedAt);
        setUpdatedBy(updatedBy);
        this.parentProfileId = parentProfileId;
        this.parentProfileName = parentProfileName;
        this.requestCode = requestCode;
        this.requestedAt = requestedAt;
        this.requestSource = requestSource == null ? null : requestSource.name();
        this.requestType = requestType == null ? null : requestType.name();
        this.status = status == null ? null : status.name();
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.notes = notes;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.rejectionReason = rejectionReason;
        this.changeReason = changeReason;
    }
}
