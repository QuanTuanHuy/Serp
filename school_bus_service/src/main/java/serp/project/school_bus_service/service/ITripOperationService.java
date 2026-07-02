package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.request.BatchAttendanceRequest;
import serp.project.school_bus_service.dto.request.CancelTripRequest;
import serp.project.school_bus_service.dto.request.CompleteTripRequest;
import serp.project.school_bus_service.dto.request.SkipStopRequest;
import serp.project.school_bus_service.dto.request.TripAttendanceActionRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.BatchAttendanceResponse;
import serp.project.school_bus_service.dto.response.TripOperationActionResponse;

public interface ITripOperationService {

    TripOperationActionResponse startTrip(Long tripId, Long tenantId, Long actorId);

    TripOperationActionResponse completeTrip(Long tripId, CompleteTripRequest request, Long tenantId, Long actorId);

    TripOperationActionResponse cancelTrip(Long tripId, CancelTripRequest request, Long tenantId, Long actorId);

    TripOperationActionResponse arriveStop(Long tripId, Long routeStopId, Long tenantId, Long actorId);

    TripOperationActionResponse startBoarding(Long tripId, Long routeStopId, Long tenantId, Long actorId);

    TripOperationActionResponse departStop(Long tripId, Long routeStopId, Long tenantId, Long actorId);

    TripOperationActionResponse skipStop(Long tripId, Long routeStopId, SkipStopRequest request, Long tenantId, Long actorId);

    AttendanceResponse boardStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse dropoffStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse markStudentAbsent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse markStudentNoShow(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse markStudentNotServed(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    BatchAttendanceResponse batchUpdateAttendance(Long tripId, Long routeStopId,
            BatchAttendanceRequest request, Long tenantId, Long actorId);
}
