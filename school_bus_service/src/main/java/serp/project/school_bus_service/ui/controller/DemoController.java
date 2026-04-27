package serp.project.school_bus_service.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.application.dto.request.DemoSpeedRequest;
import serp.project.school_bus_service.application.dto.response.DemoEventLogResponse;
import serp.project.school_bus_service.application.dto.response.DemoSessionResponse;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.core.service.IDemoSimulationService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

import java.util.List;

@RestController
@RequestMapping("/demo/trips")
public class DemoController extends AbstractBaseController {

    private final IDemoSimulationService demoSimulationService;

    public DemoController(IDemoSimulationService demoSimulationService, AuthUtils authUtils) {
        super(authUtils);
        this.demoSimulationService = demoSimulationService;
    }

    @PostMapping("/{tripId}/start")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.demo.operate')")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> start(@PathVariable Long tripId) {
        return ok("Started demo session",
                demoSimulationService.startDemo(tripId, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{tripId}/pause")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.demo.operate')")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> pause(@PathVariable Long tripId) {
        return ok("Paused demo session",
                demoSimulationService.pauseDemo(tripId, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{tripId}/resume")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.demo.operate')")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> resume(@PathVariable Long tripId) {
        return ok("Resumed demo session",
                demoSimulationService.resumeDemo(tripId, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{tripId}/stop")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.demo.operate')")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> stop(@PathVariable Long tripId) {
        return ok("Stopped demo session",
                demoSimulationService.stopDemo(tripId, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{tripId}/speed")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.demo.operate')")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> speed(
            @PathVariable Long tripId,
            @Valid @RequestBody DemoSpeedRequest request) {
        return ok("Updated demo speed",
                demoSimulationService.changeSpeed(tripId, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{tripId}/state")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.demo.read')")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> state(@PathVariable Long tripId) {
        return ok("Fetched demo state", demoSimulationService.getState(tripId, getCurrentTenantId()));
    }

    @GetMapping("/{tripId}/events")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.demo.read')")
    public ResponseEntity<GeneralResponse<List<DemoEventLogResponse>>> events(@PathVariable Long tripId) {
        return ok("Fetched demo events", demoSimulationService.getEvents(tripId, getCurrentTenantId()));
    }
}
