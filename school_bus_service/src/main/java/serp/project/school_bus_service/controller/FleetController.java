package serp.project.school_bus_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.dto.response.FleetSummaryResponse;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.service.IFleetSummaryService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/fleet")
public class FleetController extends AbstractBaseController {

    private final IFleetSummaryService fleetSummaryService;

    public FleetController(IFleetSummaryService fleetSummaryService, AuthUtils authUtils) {
        super(authUtils);
        this.fleetSummaryService = fleetSummaryService;
    }

    @GetMapping("/summary")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.fleet.read')")
    public ResponseEntity<GeneralResponse<FleetSummaryResponse>> getSummary() {
        return ok("Fetched fleet summary", fleetSummaryService.getSummary(getCurrentTenantId()));
    }
}
