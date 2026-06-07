package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RouteAssignmentResponse extends BaseResponse {

    private Long routeId;
    private String routeStatus;

    private Long busId;
    private String busPlateNumber;
    private Integer busCapacity;

    private Long driverId;
    private String driverName;
    private String driverLicenseClass;
    private String driverLicenseExpiryDate;

    private Long attendantId;
    private String attendantName;

    private String status;
    private Long assignedBy;
    private String assignmentNote;

    private LocalDateTime assignedAt;
    private LocalDateTime confirmedAt;

    // Validation warnings returned to caller (not persisted)
    private String capacityWarning;
    private String licenseWarning;
}
