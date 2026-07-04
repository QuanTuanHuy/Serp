package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.dto.params.DepotParamsRequest;
import serp.project.school_bus_service.dto.request.DepotUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.DepotResponse;
import serp.project.school_bus_service.service.IDepotService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/depots")
public class DepotController extends AbstractBaseController {

    private final IDepotService depotService;

    public DepotController(IDepotService depotService, AuthUtils authUtils) {
        super(authUtils);
        this.depotService = depotService;
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.read')")
    public ResponseEntity<GeneralResponse<PageResponse<DepotResponse>>> getDepots(
            @ModelAttribute DepotParamsRequest params) {
        return ok("Fetched depots", depotService.getDepots(params, getCurrentTenantId()));
    }

    @PostMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<DepotResponse>> createDepot(
            @Valid @RequestBody DepotUpsertRequest request) {
        return created("Created depot",
                depotService.createDepot(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.read')")
    public ResponseEntity<GeneralResponse<DepotResponse>> getDepot(@PathVariable Long id) {
        return ok("Fetched depot", depotService.getDepotResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<DepotResponse>> updateDepot(@PathVariable Long id,
            @Valid @RequestBody DepotUpsertRequest request) {
        return ok("Updated depot",
                depotService.updateDepot(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.write')")
    public ResponseEntity<GeneralResponse<Void>> deleteDepot(@PathVariable Long id) {
        depotService.deleteDepot(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted depot");
    }
}
