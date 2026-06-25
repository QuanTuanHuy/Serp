package serp.project.school_bus_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.dto.params.AttendanceParamsRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.service.IAttendanceService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/attendance")
public class AttendanceController extends AbstractBaseController {

    private final IAttendanceService attendanceService;

    public AttendanceController(IAttendanceService attendanceService, AuthUtils authUtils) {
        super(authUtils);
        this.attendanceService = attendanceService;
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.attendance.read')")
    public ResponseEntity<GeneralResponse<PageResponse<AttendanceResponse>>> getAttendance(
            @ModelAttribute AttendanceParamsRequest params) {
        return ok("Fetched attendance", attendanceService.getAttendance(params, getCurrentTenantId()));
    }

}
