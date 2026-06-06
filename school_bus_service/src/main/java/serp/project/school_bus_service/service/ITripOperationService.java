package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.request.CancelTripRequest;
import serp.project.school_bus_service.dto.request.CompleteTripRequest;
import serp.project.school_bus_service.dto.request.SkipStopRequest;
import serp.project.school_bus_service.dto.request.TripAttendanceActionRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.TripExecutionResponse;

public interface ITripOperationService {

    TripExecutionResponse startTrip(Long tripId, Long tenantId, Long actorId);

    TripExecutionResponse completeTrip(Long tripId, CompleteTripRequest request, Long tenantId, Long actorId);

    TripExecutionResponse cancelTrip(Long tripId, CancelTripRequest request, Long tenantId, Long actorId);

    TripExecutionResponse arriveStop(Long tripId, Long routeStopId, Long tenantId, Long actorId);

    TripExecutionResponse startBoarding(Long tripId, Long routeStopId, Long tenantId, Long actorId);

    TripExecutionResponse departStop(Long tripId, Long routeStopId, Long tenantId, Long actorId);

    TripExecutionResponse skipStop(Long tripId, Long routeStopId, SkipStopRequest request, Long tenantId, Long actorId);

    AttendanceResponse boardStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse dropoffStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse markStudentAbsent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse markStudentNoShow(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse markStudentNotServed(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);
}
