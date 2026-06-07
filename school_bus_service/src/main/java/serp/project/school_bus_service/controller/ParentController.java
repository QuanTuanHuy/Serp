package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.dto.params.ParentProfileParamsRequest;
import serp.project.school_bus_service.dto.request.ParentProfileUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.ParentProfileResponse;
import serp.project.school_bus_service.service.IParentService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/parents")
public class ParentController extends AbstractBaseController {

    private final IParentService parentService;

    public ParentController(IParentService parentService, AuthUtils authUtils) {
        super(authUtils);
        this.parentService = parentService;
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.read')")
    public ResponseEntity<GeneralResponse<PageResponse<ParentProfileResponse>>> getParents(
            @ModelAttribute ParentProfileParamsRequest params) {
        return ok("Fetched parents", parentService.getParents(params, getCurrentTenantId()));
    }

    @PostMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<ParentProfileResponse>> createParent(
            @Valid @RequestBody ParentProfileUpsertRequest request) {
        return created("Created parent",
                parentService.createParent(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.read')")
    public ResponseEntity<GeneralResponse<ParentProfileResponse>> getParent(@PathVariable Long id) {
        return ok("Fetched parent", parentService.getParentResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<ParentProfileResponse>> updateParent(@PathVariable Long id,
            @Valid @RequestBody ParentProfileUpsertRequest request) {
        return ok("Updated parent",
                parentService.updateParent(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<Void>> deleteParent(@PathVariable Long id) {
        parentService.deleteParent(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted parent");
    }
}
