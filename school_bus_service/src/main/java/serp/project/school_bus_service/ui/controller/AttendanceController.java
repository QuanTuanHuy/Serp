package serp.project.school_bus_service.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.application.dto.request.AttendanceActionRequest;
import serp.project.school_bus_service.application.dto.response.AttendanceResponse;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.application.dto.response.TripHistoryResponse;
import serp.project.school_bus_service.core.service.IAttendanceService;
import serp.project.school_bus_service.core.service.ITripHistoryService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

import java.util.List;

@RestController
@RequestMapping
public class AttendanceController extends AbstractBaseController {

    private final IAttendanceService attendanceService;
    private final ITripHistoryService tripHistoryService;

    public AttendanceController(IAttendanceService attendanceService, ITripHistoryService tripHistoryService, AuthUtils authUtils) {
        super(authUtils);
        this.attendanceService = attendanceService;
        this.tripHistoryService = tripHistoryService;
    }

    @GetMapping("/attendance")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendance.read')")
    public ResponseEntity<GeneralResponse<List<AttendanceResponse>>> getAttendance() {
        return ok("Fetched attendance", attendanceService.getAttendance(getCurrentTenantId()));
    }

    @PostMapping("/attendance/check-in")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendance.check-in')")
    public ResponseEntity<GeneralResponse<AttendanceResponse>> checkIn(@Valid @RequestBody AttendanceActionRequest request) {
        return ok("Recorded check-in", attendanceService.checkIn(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/attendance/check-out")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendance.check-out')")
    public ResponseEntity<GeneralResponse<AttendanceResponse>> checkOut(@Valid @RequestBody AttendanceActionRequest request) {
        return ok("Recorded check-out", attendanceService.checkOut(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/trip-history")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.trip-history.read')")
    public ResponseEntity<GeneralResponse<List<TripHistoryResponse>>> getTripHistory() {
        return ok("Fetched trip history", tripHistoryService.getTripHistory(getCurrentTenantId()));
    }
}
