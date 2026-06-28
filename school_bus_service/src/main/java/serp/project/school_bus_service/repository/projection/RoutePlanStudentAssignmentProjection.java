package serp.project.school_bus_service.repository.projection;

public interface RoutePlanStudentAssignmentProjection {

    Long getRouteId();

    Long getStudentId();

    Long getSubscriptionId();

    Long getPickupStopId();

    Long getDropoffStopId();
}
