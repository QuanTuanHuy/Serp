package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class RoutePlanListItemResponse extends BaseResponse {

    private Long schoolId;
    private String schoolName;
    private String routeDirection;
    private String startLocationName;
    private String endLocationName;
    private String routeCode;
    private String routeName;
    private LocalDate serviceDate;
    private String status;
    private Double plannedDistanceKm;
    private Integer plannedDurationMin;
    private Integer plannedStudentCount;
    private Long busId;
    private String busPlateNumber;
    private String busName;
    private Integer busCapacity;
    private String busStatus;
    private Integer stopsCount;
    private Long driverId;
    private String driverName;
    private Long attendantId;
    private String attendantName;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer requiredCapacity;
    private Long planningSessionId;

    public RoutePlanListItemResponse(
            Long id,
            Long tenantId,
            Boolean isActive,
            Boolean isDeleted,
            LocalDateTime createdAt,
            String createdBy,
            LocalDateTime updatedAt,
            String updatedBy,
            Long schoolId,
            String schoolName,
            RouteDirection routeDirection,
            String routeCode,
            String routeName,
            LocalDate serviceDate,
            RouteStatus status,
            Double plannedDistanceKm,
            Integer plannedDurationMin,
            Integer plannedStudentCount,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            Integer requiredCapacity,
            Long planningSessionId) {
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
        this.routeDirection = routeDirection == null ? null : routeDirection.name();
        this.routeCode = routeCode;
        this.routeName = routeName;
        this.serviceDate = serviceDate;
        this.status = status == null ? null : status.name();
        this.plannedDistanceKm = plannedDistanceKm;
        this.plannedDurationMin = plannedDurationMin;
        this.plannedStudentCount = plannedStudentCount;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.requiredCapacity = requiredCapacity;
        this.planningSessionId = planningSessionId;
    }
}
