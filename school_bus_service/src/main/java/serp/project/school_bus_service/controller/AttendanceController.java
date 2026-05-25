package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.dto.params.AttendanceParamsRequest;
import serp.project.school_bus_service.dto.params.TripHistoryParamsRequest;
import serp.project.school_bus_service.dto.request.AttendanceActionRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.TripHistoryResponse;
import serp.project.school_bus_service.service.IAttendanceService;
import serp.project.school_bus_service.service.ITripHistoryService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/attendance")
public class AttendanceController extends AbstractBaseController {

    private final IAttendanceService attendanceService;
    private final ITripHistoryService tripHistoryService;

    public AttendanceController(IAttendanceService attendanceService, ITripHistoryService tripHistoryService, AuthUtils authUtils) {
        super(authUtils);
        this.attendanceService = attendanceService;
        this.tripHistoryService = tripHistoryService;
    }

    @GetMapping
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendance.read')")
    public ResponseEntity<GeneralResponse<PageResponse<AttendanceResponse>>> getAttendance(
            @ModelAttribute AttendanceParamsRequest params) {
        return ok("Fetched attendance", attendanceService.getAttendance(params, getCurrentTenantId()));
    }

    @PostMapping("/check-in")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendance.check-in')")
    public ResponseEntity<GeneralResponse<AttendanceResponse>> checkIn(@Valid @RequestBody AttendanceActionRequest request) {
        return ok("Recorded check-in", attendanceService.checkIn(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/check-out")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendance.check-out')")
    public ResponseEntity<GeneralResponse<AttendanceResponse>> checkOut(@Valid @RequestBody AttendanceActionRequest request) {
        return ok("Recorded check-out", attendanceService.checkOut(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/trip-history")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.trip-history.read')")
    public ResponseEntity<GeneralResponse<PageResponse<TripHistoryResponse>>> getTripHistory(
            @ModelAttribute TripHistoryParamsRequest params) {
        return ok("Fetched trip history", tripHistoryService.getTripHistory(params, getCurrentTenantId()));
    }
}
