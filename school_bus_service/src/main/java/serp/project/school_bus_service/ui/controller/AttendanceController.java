package serp.project.school_bus_service.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.application.dto.request.AttendanceActionRequest;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.core.service.IAttendanceService;
import serp.project.school_bus_service.core.service.ITripHistoryService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/school-bus/api/v1")
public class AttendanceController extends AbstractBaseController {

    private final IAttendanceService attendanceService;
    private final ITripHistoryService tripHistoryService;

    public AttendanceController(IAttendanceService attendanceService, ITripHistoryService tripHistoryService, AuthUtils authUtils) {
        super(authUtils);
        this.attendanceService = attendanceService;
        this.tripHistoryService = tripHistoryService;
    }

    @GetMapping("/attendance")
    public ResponseEntity<?> getAttendance() {
        return ok("Fetched attendance", attendanceService.getAttendance(getCurrentTenantId()));
    }

    @PostMapping("/attendance/check-in")
    public ResponseEntity<?> checkIn(@Valid @RequestBody AttendanceActionRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER", "SCHOOL_BUS_DRIVER", "SCHOOL_BUS_ATTENDANT");
        return ok("Recorded check-in", attendanceService.checkIn(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/attendance/check-out")
    public ResponseEntity<?> checkOut(@Valid @RequestBody AttendanceActionRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER", "SCHOOL_BUS_DRIVER", "SCHOOL_BUS_ATTENDANT");
        return ok("Recorded check-out", attendanceService.checkOut(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/trip-history")
    public ResponseEntity<?> getTripHistory() {
        return ok("Fetched trip history", tripHistoryService.getTripHistory(getCurrentTenantId()));
    }
}
