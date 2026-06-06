package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.dto.request.SchoolPickupPointWindowUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.SchoolPickupPointWindowResponse;
import serp.project.school_bus_service.service.ISchoolPickupPointWindowService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

import java.util.List;

@RestController
@RequestMapping("/school-pickup-point-windows")
public class SchoolPickupPointWindowController extends AbstractBaseController {

    private final ISchoolPickupPointWindowService service;

    public SchoolPickupPointWindowController(ISchoolPickupPointWindowService service, AuthUtils authUtils) {
        super(authUtils);
        this.service = service;
    }

    /** List windows for a specific linked pickup point */
    @GetMapping
    public ResponseEntity<GeneralResponse<List<SchoolPickupPointWindowResponse>>> getBySchoolPickupPoint(
            @RequestParam Long schoolPickupPointId) {
        return ok("Fetched windows", service.getBySchoolPickupPoint(schoolPickupPointId, getCurrentTenantId()));
    }

    /** List windows for a specific schedule (across all linked pickup points) */
    @GetMapping("/by-schedule")
    public ResponseEntity<GeneralResponse<List<SchoolPickupPointWindowResponse>>> getBySchedule(
            @RequestParam Long schoolScheduleId) {
        return ok("Fetched windows by schedule", service.getBySchedule(schoolScheduleId, getCurrentTenantId()));
    }

    /** Create a new window */
    @PostMapping
    public ResponseEntity<GeneralResponse<SchoolPickupPointWindowResponse>> create(
            @Valid @RequestBody SchoolPickupPointWindowUpsertRequest request) {
        return created("Created window",
                service.create(request, getCurrentTenantId(), getCurrentUserId()));
    }

    /** Update an existing window */
    @PutMapping
    public ResponseEntity<GeneralResponse<SchoolPickupPointWindowResponse>> update(
            @RequestParam Long id,
            @Valid @RequestBody SchoolPickupPointWindowUpsertRequest request) {
        return ok("Updated window",
                service.update(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    /** Soft-delete a window */
    @DeleteMapping
    public ResponseEntity<GeneralResponse<Void>> delete(@RequestParam Long id) {
        service.delete(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted window");
    }
}
