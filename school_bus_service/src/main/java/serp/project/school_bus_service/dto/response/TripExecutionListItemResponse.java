package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.TripStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TripExecutionListItemResponse extends BaseResponse {

    private String tripCode;
    private Long routeId;
    private String routeCode;
    private String routeName;
    private LocalDate serviceDate;
    private String routeDirection;
    private String status;
    private LocalDateTime plannedStartAt;
    private LocalDateTime plannedEndAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private Long busId;
    private String busPlateNumber;
    private Long driverId;
    private String driverName;
    private Long attendantId;
    private String attendantName;
    private String startLocationType;
    private String startLocationName;
    private String endLocationType;
    private String endLocationName;
    private int totalStops;
    private int completedStops;
    private Long nextRouteStopId;
    private String nextStopName;
    private String nextStopStatus;

    public TripExecutionListItemResponse() {
    }

    public TripExecutionListItemResponse(
            Long id,
            Long tenantId,
            Boolean isActive,
            Boolean isDeleted,
            LocalDateTime createdAt,
            String createdBy,
            LocalDateTime updatedAt,
            String updatedBy,
            String tripCode,
            Long routeId,
            String routeCode,
            String routeName,
            LocalDate serviceDate,
            RouteDirection routeDirection,
            TripStatus status,
            LocalDateTime plannedStartAt,
            LocalDateTime plannedEndAt,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            LocalDateTime cancelledAt,
            String cancellationReason) {
        setId(id);
        setTenantId(tenantId);
        setIsActive(isActive);
        setIsDeleted(isDeleted);
        setCreatedAt(createdAt);
        setCreatedBy(createdBy);
        setUpdatedAt(updatedAt);
        setUpdatedBy(updatedBy);
        this.tripCode = tripCode;
        this.routeId = routeId;
        this.routeCode = routeCode;
        this.routeName = routeName;
        this.serviceDate = serviceDate;
        this.routeDirection = routeDirection == null ? null : routeDirection.name();
        this.status = status == null ? null : status.name();
        this.plannedStartAt = plannedStartAt;
        this.plannedEndAt = plannedEndAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.cancelledAt = cancelledAt;
        this.cancellationReason = cancellationReason;
    }
}
