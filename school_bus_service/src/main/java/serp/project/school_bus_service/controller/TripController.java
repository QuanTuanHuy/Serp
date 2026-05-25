package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.dto.params.TripExecutionParamsRequest;
import serp.project.school_bus_service.dto.request.TripAttendanceActionRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.service.IAttendanceService;
import serp.project.school_bus_service.service.ITripExecutionService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

import java.util.List;

@RestController
@RequestMapping("/trips")
public class TripController extends AbstractBaseController {

    private final ITripExecutionService tripExecutionService;
    private final IAttendanceService attendanceService;

    public TripController(
            ITripExecutionService tripExecutionService,
            IAttendanceService attendanceService,
            AuthUtils authUtils) {
        super(authUtils);
        this.tripExecutionService = tripExecutionService;
        this.attendanceService = attendanceService;
    }

    @PostMapping("/from-route/{routePlanId}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.trip.write')")
    public ResponseEntity<GeneralResponse<TripExecutionResponse>> createTripFromRoute(@PathVariable Long routePlanId) {
        return created("Created trip from route",
                tripExecutionService.createTripFromRoute(routePlanId, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.trip.read')")
    public ResponseEntity<GeneralResponse<PageResponse<TripExecutionResponse>>> getTrips(
            @ModelAttribute TripExecutionParamsRequest params) {
        return ok("Fetched trips", tripExecutionService.getTrips(params, getCurrentTenantId()));
    }

    @GetMapping("/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.trip.read')")
    public ResponseEntity<GeneralResponse<TripExecutionResponse>> getTrip(@PathVariable Long id) {
        return ok("Fetched trip", tripExecutionService.getTrip(id, getCurrentTenantId()));
    }

    @PostMapping("/{id}/start")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.trip.operate')")
    public ResponseEntity<GeneralResponse<TripExecutionResponse>> startTrip(@PathVariable Long id) {
        return ok("Started trip", tripExecutionService.startTrip(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/arrive-stop/{routeStopId}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.trip.operate')")
    public ResponseEntity<GeneralResponse<TripExecutionResponse>> arriveStop(
            @PathVariable Long id,
            @PathVariable Long routeStopId) {
        return ok("Arrived route stop",
                tripExecutionService.arriveStop(id, routeStopId, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/depart-stop/{routeStopId}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.trip.operate')")
    public ResponseEntity<GeneralResponse<TripExecutionResponse>> departStop(
            @PathVariable Long id,
            @PathVariable Long routeStopId) {
        return ok("Departed route stop",
                tripExecutionService.departStop(id, routeStopId, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/complete")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.trip.operate')")
    public ResponseEntity<GeneralResponse<TripExecutionResponse>> completeTrip(@PathVariable Long id) {
        return ok("Completed trip", tripExecutionService.completeTrip(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}/attendance")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendance.read')")
    public ResponseEntity<GeneralResponse<List<AttendanceResponse>>> getTripAttendance(@PathVariable Long id) {
        return ok("Fetched trip attendance", attendanceService.getTripAttendance(id, getCurrentTenantId()));
    }

    @PostMapping("/{id}/attendance/board")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendance.check-in')")
    public ResponseEntity<GeneralResponse<AttendanceResponse>> boardStudent(
            @PathVariable Long id,
            @Valid @RequestBody TripAttendanceActionRequest request) {
        return ok("Recorded student boarding",
                attendanceService.boardTripStudent(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/attendance/dropoff")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendance.check-out')")
    public ResponseEntity<GeneralResponse<AttendanceResponse>> dropoffStudent(
            @PathVariable Long id,
            @Valid @RequestBody TripAttendanceActionRequest request) {
        return ok("Recorded student dropoff",
                attendanceService.dropoffTripStudent(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/attendance/absent")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendance.check-in')")
    public ResponseEntity<GeneralResponse<AttendanceResponse>> markStudentAbsent(
            @PathVariable Long id,
            @Valid @RequestBody TripAttendanceActionRequest request) {
        return ok("Recorded student absence",
                attendanceService.markTripStudentAbsent(id, request, getCurrentTenantId(), getCurrentUserId()));
    }
}
