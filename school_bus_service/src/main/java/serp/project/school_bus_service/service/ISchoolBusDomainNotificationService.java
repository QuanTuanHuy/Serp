package serp.project.school_bus_service.service;

import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.entity.TransportRequestEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.enums.AttendanceEventType;
import serp.project.school_bus_service.enums.SubscriptionStatus;

import java.util.List;

public interface ISchoolBusDomainNotificationService {

    void notifyTransportRequestSubmitted(TransportRequestEntity request, Long actorId);

    void notifyTransportRequestApproved(TransportRequestEntity request, Long actorId);

    void notifyTransportRequestRejected(TransportRequestEntity request, Long actorId);

    void notifyTransportRequestCancelled(TransportRequestEntity request, Long actorId);

    void notifySubscriptionCreated(StudentSubscriptionEntity subscription, Long actorId);

    void notifySubscriptionUpdated(StudentSubscriptionEntity subscription, Long actorId);

    void notifySubscriptionStatusChanged(
            StudentSubscriptionEntity subscription,
            SubscriptionStatus status,
            Long actorId);

    void notifyRouteAssigned(RouteAssignmentEntity assignment, Long actorId);

    void notifyTripCreated(TripExecutionEntity trip, Long actorId);

    void notifyTripStarted(TripExecutionEntity trip, Long actorId);

    void notifyTripCompleted(TripExecutionEntity trip, Long actorId);

    void notifyTripCancelled(TripExecutionEntity trip, Long actorId);

    void notifyAttendanceRecorded(
            TripExecutionEntity trip,
            TripStudentEntity tripStudent,
            AttendanceEventType eventType,
            Long actorId);

    void notifyStopSkipped(
            TripExecutionEntity trip,
            List<TripStudentEntity> affectedStudents,
            String reason,
            Long actorId);
}
