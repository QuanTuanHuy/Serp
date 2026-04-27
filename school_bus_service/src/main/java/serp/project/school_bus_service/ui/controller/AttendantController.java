package serp.project.school_bus_service.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.application.dto.params.AttendantProfileParamsRequest;
import serp.project.school_bus_service.application.dto.request.BusAttendantProfileUpsertRequest;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.AttendantProfileResponse;
import serp.project.school_bus_service.core.service.IAttendantService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/attendants")
public class AttendantController extends AbstractBaseController {

    private final IAttendantService attendantService;

    public AttendantController(IAttendantService attendantService, AuthUtils authUtils) {
        super(authUtils);
        this.attendantService = attendantService;
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageResponse<AttendantProfileResponse>>> getAttendants(
            @ModelAttribute AttendantProfileParamsRequest params) {
        return ok("Fetched attendants", attendantService.getAttendants(params, getCurrentTenantId()));
    }

    @PostMapping
    public ResponseEntity<GeneralResponse<AttendantProfileResponse>> createAttendant(
            @Valid @RequestBody BusAttendantProfileUpsertRequest request) {
        return created("Created attendant",
                attendantService.createAttendant(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<AttendantProfileResponse>> getAttendant(@PathVariable Long id) {
        return ok("Fetched attendant", attendantService.getAttendantResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponse<AttendantProfileResponse>> updateAttendant(@PathVariable Long id,
            @Valid @RequestBody BusAttendantProfileUpsertRequest request) {
        return ok("Updated attendant",
                attendantService.updateAttendant(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<Void>> deleteAttendant(@PathVariable Long id) {
        attendantService.deleteAttendant(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted attendant");
    }
}
