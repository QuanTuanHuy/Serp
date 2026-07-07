package serp.project.school_bus_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.dto.params.TransportRequestParamsRequest;
import serp.project.school_bus_service.dto.request.RejectRequest;
import serp.project.school_bus_service.dto.request.TransportRequestUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.TransportRequestDetailResponse;
import serp.project.school_bus_service.dto.response.TransportRequestResponse;
import serp.project.school_bus_service.dto.response.TransportRequestSummaryResponse;
import serp.project.school_bus_service.service.ITransportRequestService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/transport-requests")
public class TransportRequestController extends AbstractBaseController {

    private final ITransportRequestService transportRequestService;

    public TransportRequestController(ITransportRequestService transportRequestService, AuthUtils authUtils) {
        super(authUtils);
        this.transportRequestService = transportRequestService;
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.request.read')")
    public ResponseEntity<GeneralResponse<PageResponse<TransportRequestResponse>>> getTransportRequests(
            @ModelAttribute TransportRequestParamsRequest params) {
        return ok("Fetched transport requests",
                transportRequestService.getTransportRequests(params, getCurrentTenantId()));
    }

    @GetMapping("/summary")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.request.read')")
    public ResponseEntity<GeneralResponse<TransportRequestSummaryResponse>> getSummary() {
        return ok("Fetched transport request summary", transportRequestService.getSummary(getCurrentTenantId()));
    }

    @PostMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.request.write')")
    public ResponseEntity<GeneralResponse<TransportRequestResponse>> createTransportRequest(@Valid @RequestBody TransportRequestUpsertRequest request) {
        return created("Created transport request",
                transportRequestService.createTransportRequest(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.request.read')")
    public ResponseEntity<GeneralResponse<TransportRequestDetailResponse>> getTransportRequest(@PathVariable Long id) {
        return ok("Fetched transport request", transportRequestService.getTransportRequest(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.request.write')")
    public ResponseEntity<GeneralResponse<TransportRequestResponse>> updateTransportRequest(@PathVariable Long id,
            @Valid @RequestBody TransportRequestUpsertRequest request) {
        return ok("Updated transport request",
                transportRequestService.updateTransportRequest(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.request.approve')")
    public ResponseEntity<GeneralResponse<TransportRequestResponse>> approveTransportRequest(@PathVariable Long id) {
        return ok("Approved transport request",
                transportRequestService.approveTransportRequest(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.request.approve')")
    public ResponseEntity<GeneralResponse<TransportRequestResponse>> rejectTransportRequest(@PathVariable Long id,
            @Valid @RequestBody RejectRequest request) {
        return ok("Rejected transport request",
                transportRequestService.rejectTransportRequest(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.request.write')")
    public ResponseEntity<GeneralResponse<TransportRequestResponse>> cancelTransportRequest(@PathVariable Long id) {
        return ok("Cancelled transport request",
                transportRequestService.cancelTransportRequest(id, getCurrentTenantId(), getCurrentUserId()));
    }
}
