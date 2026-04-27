package serp.project.school_bus_service.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.application.dto.params.DepotParamsRequest;
import serp.project.school_bus_service.application.dto.request.DepotUpsertRequest;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.DepotResponse;
import serp.project.school_bus_service.core.service.IDepotService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/depots")
public class DepotController extends AbstractBaseController {

    private final IDepotService depotService;

    public DepotController(IDepotService depotService, AuthUtils authUtils) {
        super(authUtils);
        this.depotService = depotService;
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageResponse<DepotResponse>>> getDepots(
            @ModelAttribute DepotParamsRequest params) {
        return ok("Fetched depots", depotService.getDepots(params, getCurrentTenantId()));
    }

    @PostMapping
    public ResponseEntity<GeneralResponse<DepotResponse>> createDepot(
            @Valid @RequestBody DepotUpsertRequest request) {
        return created("Created depot",
                depotService.createDepot(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<DepotResponse>> getDepot(@PathVariable Long id) {
        return ok("Fetched depot", depotService.getDepotResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponse<DepotResponse>> updateDepot(@PathVariable Long id,
            @Valid @RequestBody DepotUpsertRequest request) {
        return ok("Updated depot",
                depotService.updateDepot(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<Void>> deleteDepot(@PathVariable Long id) {
        depotService.deleteDepot(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted depot");
    }
}
