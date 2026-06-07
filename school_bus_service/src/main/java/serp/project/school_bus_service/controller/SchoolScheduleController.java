package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.dto.request.SchoolScheduleUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.SchoolScheduleResponse;
import serp.project.school_bus_service.service.ISchoolScheduleService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

import java.util.List;

@RestController
@RequestMapping("/school-schedules")
public class SchoolScheduleController extends AbstractBaseController {

    private final ISchoolScheduleService scheduleService;

    public SchoolScheduleController(ISchoolScheduleService scheduleService, AuthUtils authUtils) {
        super(authUtils);
        this.scheduleService = scheduleService;
    }

    @GetMapping("/by-school")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.read')")
    public ResponseEntity<GeneralResponse<PageResponse<SchoolScheduleResponse>>> getBySchool(
            @RequestParam Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ok("Fetched school schedules",
                scheduleService.getSchedulesBySchool(schoolId, page, size, getCurrentTenantId()));
    }

    @GetMapping("/by-school/active")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.read')")
    public ResponseEntity<GeneralResponse<List<SchoolScheduleResponse>>> getActiveBySchool(
            @RequestParam Long schoolId) {
        return ok("Fetched active school schedules",
                scheduleService.getActiveSchedulesBySchool(schoolId, getCurrentTenantId()));
    }

    @PostMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.write')")
    public ResponseEntity<GeneralResponse<SchoolScheduleResponse>> create(
            @RequestParam Long schoolId,
            @Valid @RequestBody SchoolScheduleUpsertRequest request) {
        return created("Created school schedule",
                scheduleService.createSchedule(schoolId, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.read')")
    public ResponseEntity<GeneralResponse<SchoolScheduleResponse>> getById(@RequestParam Long id) {
        return ok("Fetched school schedule",
                scheduleService.getScheduleResponse(id, getCurrentTenantId()));
    }

    @PatchMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.write')")
    public ResponseEntity<GeneralResponse<SchoolScheduleResponse>> update(
            @RequestParam Long id,
            @Valid @RequestBody SchoolScheduleUpsertRequest request) {
        return ok("Updated school schedule",
                scheduleService.updateSchedule(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.write')")
    public ResponseEntity<GeneralResponse<Void>> delete(@RequestParam Long id) {
        scheduleService.deleteSchedule(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted school schedule");
    }
}
