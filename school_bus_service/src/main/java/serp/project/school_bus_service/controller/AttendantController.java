package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.dto.params.AttendantProfileParamsRequest;
import serp.project.school_bus_service.dto.request.BusAttendantProfileUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.AttendantProfileResponse;
import serp.project.school_bus_service.service.IAttendantService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/attendants")
public class AttendantController extends AbstractBaseController {

    private final IAttendantService attendantService;

    public AttendantController(IAttendantService attendantService, AuthUtils authUtils) {
        super(authUtils);
        this.attendantService = attendantService;
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.read')")
    public ResponseEntity<GeneralResponse<PageResponse<AttendantProfileResponse>>> getAttendants(
            @ModelAttribute AttendantProfileParamsRequest params) {
        return ok("Fetched attendants", attendantService.getAttendants(params, getCurrentTenantId()));
    }

    @PostMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<AttendantProfileResponse>> createAttendant(
            @Valid @RequestBody BusAttendantProfileUpsertRequest request) {
        return created("Created attendant",
                attendantService.createAttendant(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.read')")
    public ResponseEntity<GeneralResponse<AttendantProfileResponse>> getAttendant(@PathVariable Long id) {
        return ok("Fetched attendant", attendantService.getAttendantResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<AttendantProfileResponse>> updateAttendant(@PathVariable Long id,
            @Valid @RequestBody BusAttendantProfileUpsertRequest request) {
        return ok("Updated attendant",
                attendantService.updateAttendant(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<Void>> deleteAttendant(@PathVariable Long id) {
        attendantService.deleteAttendant(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted attendant");
    }
}
