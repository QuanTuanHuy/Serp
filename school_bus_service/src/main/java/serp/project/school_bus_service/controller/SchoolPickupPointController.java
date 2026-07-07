package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.dto.request.SchoolPickupPointUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.SchoolPickupPointResponse;
import serp.project.school_bus_service.service.ISchoolPickupPointService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

import java.util.List;

@RestController
@RequestMapping("/school-pickup-points")
public class SchoolPickupPointController extends AbstractBaseController {

    private final ISchoolPickupPointService service;
    private final MessageCommon messageCommon;

    public SchoolPickupPointController(ISchoolPickupPointService service, AuthUtils authUtils, MessageCommon messageCommon) {
        super(authUtils);
        this.service = service;
        this.messageCommon = messageCommon;
    }

    @GetMapping("/by-school")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.read')")
    public ResponseEntity<GeneralResponse<PageResponse<SchoolPickupPointResponse>>> getBySchool(
            @RequestParam Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ok(messageCommon.getMessage("schoolPickupPoint.fetch.list"),
                service.getBySchool(schoolId, page, size, getCurrentTenantId()));
    }

    @GetMapping("/by-school/active")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.read')")
    public ResponseEntity<GeneralResponse<List<SchoolPickupPointResponse>>> getActiveBySchool(
            @RequestParam Long schoolId) {
        return ok(messageCommon.getMessage("schoolPickupPoint.fetch.activeList"),
                service.getActiveBySchool(schoolId, getCurrentTenantId()));
    }

    /** Get all active school-pickup links across all schools (for student form filtering) */
    @GetMapping("/active")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.read')")
    public ResponseEntity<GeneralResponse<List<SchoolPickupPointResponse>>> getAllActiveLinks() {
        return ok(messageCommon.getMessage("schoolPickupPoint.fetch.allActiveLinks"),
                service.getAllActiveLinks(getCurrentTenantId()));
    }

    @PostMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.write')")
    public ResponseEntity<GeneralResponse<SchoolPickupPointResponse>> link(
            @RequestParam Long schoolId,
            @Valid @RequestBody SchoolPickupPointUpsertRequest request) {
        return created(messageCommon.getMessage("schoolPickupPoint.link.success"),
                service.link(schoolId, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PatchMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.write')")
    public ResponseEntity<GeneralResponse<SchoolPickupPointResponse>> update(
            @RequestParam Long id,
            @Valid @RequestBody SchoolPickupPointUpsertRequest request) {
        return ok(messageCommon.getMessage("schoolPickupPoint.update.success"),
                service.update(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.school.write')")
    public ResponseEntity<GeneralResponse<Void>> unlink(
            @RequestParam Long schoolId,
            @RequestParam Long pickupPointId) {
        service.unlink(schoolId, pickupPointId, getCurrentTenantId(), getCurrentUserId());
        return ok(messageCommon.getMessage("schoolPickupPoint.unlink.success"));
    }
}
