package serp.project.school_bus_service.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.core.service.IDashboardService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/school-bus/api/v1")
public class DashboardController extends AbstractBaseController {

    private final IDashboardService dashboardService;

    public DashboardController(IDashboardService dashboardService, AuthUtils authUtils) {
        super(authUtils);
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<?> getSummary() {
        return ok("Fetched dashboard summary", dashboardService.getSummary(getCurrentTenantId()));
    }

    @GetMapping("/reports/operations-summary")
    public ResponseEntity<?> getOperationsReport() {
        return ok("Fetched operations report", dashboardService.getOperationsReport(getCurrentTenantId()));
    }
}
