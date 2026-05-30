package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.dto.request.CreateDemoSessionRequest;
import serp.project.school_bus_service.dto.response.DemoEventLogResponse;
import serp.project.school_bus_service.dto.response.DemoSessionResponse;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.entity.DemoSessionEntity;
import serp.project.school_bus_service.enums.DemoEventType;
import serp.project.school_bus_service.service.IDemoEventLogService;
import serp.project.school_bus_service.service.IDemoPlaybackService;
import serp.project.school_bus_service.service.IDemoSessionService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

import java.util.List;

@RestController
@RequestMapping("/demo-sessions")
public class DemoSessionController extends AbstractBaseController {

    private final IDemoSessionService demoSessionService;
    private final IDemoPlaybackService demoPlaybackService;
    private final IDemoEventLogService demoEventLogService;

    public DemoSessionController(IDemoSessionService demoSessionService,
                                 IDemoPlaybackService demoPlaybackService,
                                 IDemoEventLogService demoEventLogService,
                                 AuthUtils authUtils) {
        super(authUtils);
        this.demoSessionService = demoSessionService;
        this.demoPlaybackService = demoPlaybackService;
        this.demoEventLogService = demoEventLogService;
    }

    @PostMapping("/from-trip/{tripId}")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> createFromTrip(
            @PathVariable Long tripId,
            @Valid @RequestBody(required = false) CreateDemoSessionRequest request) {
        CreateDemoSessionRequest req = request != null ? request : new CreateDemoSessionRequest();
        DemoSessionEntity session = demoSessionService.createFromTrip(
                tripId,
                req.getDurationSeconds(),
                req.getAutoAdvanceStops(),
                req.getAutoAttendance(),
                getCurrentTenantId(),
                getCurrentUserId());
        demoEventLogService.record(session, DemoEventType.DEMO_CREATED, null,
                getCurrentTenantId(), getCurrentUserId());
        return ok("Demo session created", demoSessionService.toResponse(session));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> getSession(@PathVariable Long id) {
        DemoSessionEntity session = demoSessionService.getById(id, getCurrentTenantId());
        return ok("Fetched demo session", demoSessionService.toResponse(session));
    }

    @GetMapping("/by-trip/{tripId}")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> getByTrip(@PathVariable Long tripId) {
        DemoSessionEntity session = demoSessionService.getByTripId(tripId, getCurrentTenantId());
        return ok("Fetched demo session for trip", demoSessionService.toResponse(session));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> start(@PathVariable Long id) {
        return ok("Demo started", demoPlaybackService.start(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> pause(@PathVariable Long id) {
        return ok("Demo paused", demoPlaybackService.pause(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> resume(@PathVariable Long id) {
        return ok("Demo resumed", demoPlaybackService.resume(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/tick")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> tick(@PathVariable Long id) {
        return ok("Demo ticked", demoPlaybackService.tick(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<GeneralResponse<DemoSessionResponse>> stop(@PathVariable Long id) {
        return ok("Demo stopped", demoPlaybackService.stop(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<GeneralResponse<List<DemoEventLogResponse>>> events(@PathVariable Long id) {
        return ok("Fetched demo events", demoEventLogService.getEvents(id, getCurrentTenantId()));
    }
}
