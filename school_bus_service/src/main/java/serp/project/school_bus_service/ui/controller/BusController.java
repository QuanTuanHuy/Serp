package serp.project.school_bus_service.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.application.dto.params.BusParamsRequest;
import serp.project.school_bus_service.application.dto.request.BusUpsertRequest;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.BusResponse;
import serp.project.school_bus_service.core.service.IBusService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/buses")
public class BusController extends AbstractBaseController {

    private final IBusService busService;

    public BusController(IBusService busService, AuthUtils authUtils) {
        super(authUtils);
        this.busService = busService;
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageResponse<BusResponse>>> getBuses(
            @ModelAttribute BusParamsRequest params) {
        return ok("Fetched buses", busService.getBuses(params, getCurrentTenantId()));
    }

    @PostMapping
    public ResponseEntity<GeneralResponse<BusResponse>> createBus(
            @Valid @RequestBody BusUpsertRequest request) {
        return created("Created bus",
                busService.createBus(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<BusResponse>> getBus(@PathVariable Long id) {
        return ok("Fetched bus", busService.getBusResponse(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponse<BusResponse>> updateBus(@PathVariable Long id,
            @Valid @RequestBody BusUpsertRequest request) {
        return ok("Updated bus",
                busService.updateBus(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<Void>> deleteBus(@PathVariable Long id) {
        busService.deleteBus(id, getCurrentTenantId(), getCurrentUserId());
        return ok("Deleted bus");
    }
}
