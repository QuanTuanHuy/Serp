package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.dto.params.RoutePlanParamsRequest;
import serp.project.school_bus_service.dto.request.AddRouteStopRequest;
import serp.project.school_bus_service.dto.request.AddStudentToStopRequest;
import serp.project.school_bus_service.dto.request.ManualDispatchRequest;
import serp.project.school_bus_service.dto.request.MoveStudentRequest;
import serp.project.school_bus_service.dto.request.ReorderStopsRequest;
import serp.project.school_bus_service.dto.request.RouteAssignmentRequest;
import serp.project.school_bus_service.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.dto.response.AssignmentHistoryResponse;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.dto.response.RouteDetailResponse;
import serp.project.school_bus_service.dto.response.RoutePathResponse;
import serp.project.school_bus_service.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.dto.request.RoutingPointRequest;
import serp.project.school_bus_service.dto.response.RoutingMatrixResponse;
import serp.project.school_bus_service.service.domain.IRoutingMatrixService;
import serp.project.school_bus_service.dto.response.RoutePlanStudentResponse;
import serp.project.school_bus_service.dto.response.RouteStopResponse;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteCalculationTraceService;
import serp.project.school_bus_service.mapper.RouteMapper;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;
import serp.project.school_bus_service.dto.response.RouteManualValidationResponse;
import serp.project.school_bus_service.service.IRouteManualValidationService;
import serp.project.school_bus_service.shared.export.IExportService;
import serp.project.school_bus_service.shared.export.ExportRequest;
import serp.project.school_bus_service.shared.export.ExportResult;
import serp.project.school_bus_service.shared.export.ExportCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController extends AbstractBaseController {

    private final IRouteService routeService;
    private final IRoutingMatrixService routingMatrixService;
    private final IRouteCalculationTraceService routeCalculationTraceService;
    private final RouteMapper routeMapper;
    private final IExportService exportService;
    private final IRouteManualValidationService routeManualValidationService;
    private final serp.project.school_bus_service.service.IRouteObjectiveScoringService objectiveScoringService;

    public RouteController(
            IRouteService routeService,
            IRoutingMatrixService routingMatrixService,
            IRouteCalculationTraceService routeCalculationTraceService,
            RouteMapper routeMapper,
            IExportService exportService,
            IRouteManualValidationService routeManualValidationService,
            serp.project.school_bus_service.service.IRouteObjectiveScoringService objectiveScoringService,
            AuthUtils authUtils) {
        super(authUtils);
        this.routeService = routeService;
        this.routingMatrixService = routingMatrixService;
        this.routeCalculationTraceService = routeCalculationTraceService;
        this.routeMapper = routeMapper;
        this.exportService = exportService;
        this.routeManualValidationService = routeManualValidationService;
        this.objectiveScoringService = objectiveScoringService;
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<GeneralResponse<RouteManualValidationResponse>> validateRoute(@PathVariable Long id) {
        return ok("Validated route feasibility", routeManualValidationService.validateRoute(id, getCurrentTenantId()));
    }

    @PostMapping("/matrix")
    public ResponseEntity<GeneralResponse<RoutingMatrixResponse>> getMatrix(
            @Valid @RequestBody List<RoutingPointRequest> points) {
        return ok("Computed routing matrix", routingMatrixService.buildMatrix(getCurrentTenantId(), points));
    }

    @GetMapping
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.read')")
    public ResponseEntity<GeneralResponse<PageResponse<RoutePlanResponse>>> getRoutes(
            @ModelAttribute RoutePlanParamsRequest params) {
        return ok("Fetched routes", routeService.getRoutes(params, getCurrentTenantId()));
    }

    @GetMapping("/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.read')")
    public ResponseEntity<GeneralResponse<RouteDetailResponse>> getRoute(@PathVariable Long id) {
        return ok("Fetched route", routeService.getRoute(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.write')")
    public ResponseEntity<GeneralResponse<RoutePlanResponse>> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RoutePlanUpsertRequest request) {
        return ok("Updated route", routeService.updateRoute(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/assign")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.assign')")
    public ResponseEntity<GeneralResponse<RouteAssignmentResponse>> assignRoute(
            @PathVariable Long id,
            @Valid @RequestBody RouteAssignmentRequest request) {
        return ok("Assigned route", routeService.assignRoute(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/manual-dispatch")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.assign')")
    public ResponseEntity<GeneralResponse<RouteAssignmentResponse>> manualDispatchRoute(
            @PathVariable Long id,
            @Valid @RequestBody ManualDispatchRequest request) {
        return ok("Manually dispatched route",
                routeService.manualDispatchRoute(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PatchMapping("/{id}/stops/reorder")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.write')")
    public ResponseEntity<GeneralResponse<List<RouteStopResponse>>> reorderRouteStops(
            @PathVariable Long id,
            @Valid @RequestBody ReorderStopsRequest request) {
        return ok("Reordered route stops",
                routeService.reorderRouteStops(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/compute-path")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.write')")
    public ResponseEntity<GeneralResponse<RoutePathResponse>> computePath(@PathVariable Long id) {
        return ok("Computed route path", routeService.computePath(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}/path")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.read')")
    public ResponseEntity<GeneralResponse<RoutePathResponse>> getPath(@PathVariable Long id) {
        return ok("Fetched route path", routeService.getRoutePath(id, getCurrentTenantId()));
    }

    @GetMapping("/{id}/assignment-history")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.read')")
    public ResponseEntity<GeneralResponse<List<AssignmentHistoryResponse>>> getAssignmentHistory(
            @PathVariable Long id) {
        return ok("Fetched assignment history", routeService.getAssignmentHistory(id, getCurrentTenantId()));
    }

    // ── Manual editing endpoints ─────────────────────────────────────────

    @PostMapping("/{id}/stops")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.write')")
    public ResponseEntity<GeneralResponse<RouteStopResponse>> addStop(
            @PathVariable Long id,
            @Valid @RequestBody AddRouteStopRequest request) {
        return created("Added stop", routeService.addStop(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @DeleteMapping("/{id}/stops/{stopId}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.write')")
    public ResponseEntity<GeneralResponse<Void>> removeStop(
            @PathVariable Long id,
            @PathVariable Long stopId) {
        routeService.removeStop(id, stopId, getCurrentTenantId(), getCurrentUserId());
        return ok("Removed stop", null);
    }

    @PostMapping("/{id}/stops/{stopId}/students")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.write')")
    public ResponseEntity<GeneralResponse<RoutePlanStudentResponse>> addStudentToStop(
            @PathVariable Long id,
            @PathVariable Long stopId,
            @Valid @RequestBody AddStudentToStopRequest request) {
        return created("Added student to stop",
                routeService.addStudentToStop(id, stopId, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/students/assign")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.write')")
    public ResponseEntity<GeneralResponse<RoutePlanStudentResponse>> assignStudentToRoute(
            @PathVariable Long id,
            @Valid @RequestBody AddStudentToStopRequest request) {
        return created("Assigned student to route",
                routeService.assignStudentToRoute(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/students/move")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.write')")
    public ResponseEntity<GeneralResponse<Void>> moveStudent(
            @PathVariable Long id,
            @Valid @RequestBody MoveStudentRequest request) {
        routeService.moveStudent(id, request, getCurrentTenantId(), getCurrentUserId());
        return ok("Moved student", null);
    }

    @DeleteMapping("/{id}/students/{studentId}")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.route.write')")
    public ResponseEntity<GeneralResponse<Void>> removeStudent(
            @PathVariable Long id,
            @PathVariable Long studentId,
            @RequestParam Long subscriptionId) {
        routeService.removeStudent(id, studentId, subscriptionId, getCurrentTenantId(), getCurrentUserId());
        return ok("Removed student", null);
    }

    @GetMapping("/{routePlanId}/calculation-traces/latest")
    public ResponseEntity<GeneralResponse<serp.project.school_bus_service.dto.response.RouteCalculationTraceResponse>> getLatestCalculationTrace(
            @PathVariable Long routePlanId) {
        return ok("Fetched latest calculation trace",
                routeCalculationTraceService.findLatestByRoutePlanId(routePlanId)
                        .map(routeMapper::toRouteCalculationTraceResponse)
                        .orElseThrow(() -> new serp.project.school_bus_service.shared.exception.AppException(
                                serp.project.school_bus_service.shared.exception.AppErrorCode.NOT_FOUND,
                                "No calculation trace found for route: " + routePlanId)));
    }

    @GetMapping("/{routePlanId}/calculation-traces")
    public ResponseEntity<GeneralResponse<List<serp.project.school_bus_service.dto.response.RouteCalculationTraceResponse>>> getCalculationTraceHistory(
            @PathVariable Long routePlanId) {
        return ok("Fetched calculation trace history",
                routeMapper.toRouteCalculationTraceResponseList(
                        routeCalculationTraceService.findHistoryByRoutePlanId(routePlanId)));
    }

    /**
     * TODO Phase 5/7:
     * Persist and export planning-context full N x N matrix before route generation.
     * This will be used by greedy route generation and experiment benchmark.
     *
     * Tiếng Việt:
     * TODO Phase 5/7:
     * Lưu và export ma trận N x N theo planning context trước khi tạo route.
     * Ma trận này phục vụ thuật toán greedy và benchmark thực nghiệm.
     */
    @GetMapping("/{routePlanId}/calculation-traces/latest/export")
    public ResponseEntity<byte[]> exportLatestCalculationTrace(@PathVariable Long routePlanId) {
        ExportResult result = exportService.export(
                ExportRequest.builder()
                        .exportCode(ExportCode.ROUTING_TRACE)
                        .routePlanId(routePlanId)
                        .build()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.getFileName() + "\"")
                .body(result.getContent());
    }

    @GetMapping("/{routePlanId}/calculation-traces/{traceId}/export")
    public ResponseEntity<byte[]> exportCalculationTrace(
            @PathVariable Long routePlanId,
            @PathVariable Long traceId) {
        ExportResult result = exportService.export(
                ExportRequest.builder()
                        .exportCode(ExportCode.ROUTING_TRACE)
                        .routePlanId(routePlanId)
                        .traceId(traceId)
                        .build()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.getFileName() + "\"")
                .body(result.getContent());
    }

    @GetMapping("/{id}/objective-score")
    public ResponseEntity<GeneralResponse<serp.project.school_bus_service.dto.response.ObjectiveScoreResponse>> getRouteObjectiveScore(
            @PathVariable Long id) {
        return ok("Fetched route objective score",
                objectiveScoringService.calculateRouteScore(id, getCurrentTenantId()));
    }

    @PostMapping("/{id}/objective-score/recalculate")
    public ResponseEntity<GeneralResponse<serp.project.school_bus_service.dto.response.ObjectiveScoreResponse>> recalculateRouteObjectiveScore(
            @PathVariable Long id) {
        return ok("Recalculated route objective score",
                objectiveScoringService.calculateRouteScore(id, getCurrentTenantId()));
    }
}
