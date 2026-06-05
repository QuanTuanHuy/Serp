package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.dto.request.SchoolPickupPointUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.SchoolPickupPointResponse;
import serp.project.school_bus_service.service.ISchoolPickupPointService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

import serp.project.school_bus_service.dto.response.SchoolPickupPointCompatibilityResponse;

import java.util.List;

@RestController
@RequestMapping("/school-pickup-points")
public class SchoolPickupPointController extends AbstractBaseController {

    private final ISchoolPickupPointService service;

    public SchoolPickupPointController(ISchoolPickupPointService service, AuthUtils authUtils) {
        super(authUtils);
        this.service = service;
    }

    @GetMapping("/by-school")
    public ResponseEntity<GeneralResponse<PageResponse<SchoolPickupPointResponse>>> getBySchool(
            @RequestParam Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ok("Fetched school pickup points",
                service.getBySchool(schoolId, page, size, getCurrentTenantId()));
    }

    @GetMapping("/by-school/active")
    public ResponseEntity<GeneralResponse<List<SchoolPickupPointResponse>>> getActiveBySchool(
            @RequestParam Long schoolId) {
        return ok("Fetched active school pickup points",
                service.getActiveBySchool(schoolId, getCurrentTenantId()));
    }

    @GetMapping("/compatibility")
    public ResponseEntity<GeneralResponse<List<SchoolPickupPointCompatibilityResponse>>> getCompatibility(
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long schoolScheduleId) {
        return ok("Fetched school pickup points compatibility",
                service.getCompatibility(schoolId, schoolScheduleId, getCurrentTenantId()));
    }

    /** Get all active school-pickup links across all schools (for student form filtering) */
    @GetMapping("/active")
    public ResponseEntity<GeneralResponse<List<SchoolPickupPointResponse>>> getAllActiveLinks() {
        return ok("Fetched all active school pickup point links",
                service.getAllActiveLinks(getCurrentTenantId()));
    }

    @PostMapping
    public ResponseEntity<GeneralResponse<SchoolPickupPointResponse>> link(
            @RequestParam Long schoolId,
            @Valid @RequestBody SchoolPickupPointUpsertRequest request) {
        return created("Linked pickup point to school",
                service.link(schoolId, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PatchMapping
    public ResponseEntity<GeneralResponse<SchoolPickupPointResponse>> update(
            @RequestParam Long id,
            @Valid @RequestBody SchoolPickupPointUpsertRequest request) {
        return ok("Updated school pickup point config",
                service.update(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping
    public ResponseEntity<GeneralResponse<Void>> unlink(
            @RequestParam Long schoolId,
            @RequestParam Long pickupPointId) {
        service.unlink(schoolId, pickupPointId, getCurrentTenantId(), getCurrentUserId());
        return ok("Unlinked pickup point from school");
    }
}
