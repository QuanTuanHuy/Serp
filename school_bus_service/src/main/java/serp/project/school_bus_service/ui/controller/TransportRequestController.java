package serp.project.school_bus_service.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.application.dto.request.RejectRequest;
import serp.project.school_bus_service.application.dto.request.TransportRequestUpsertRequest;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.core.service.ITransportRequestService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/school-bus/api/v1/transport-requests")
public class TransportRequestController extends AbstractBaseController {

    private final ITransportRequestService transportRequestService;

    public TransportRequestController(ITransportRequestService transportRequestService, AuthUtils authUtils) {
        super(authUtils);
        this.transportRequestService = transportRequestService;
    }

    @GetMapping
    public ResponseEntity<?> getTransportRequests() {
        return ok("Fetched transport requests", transportRequestService.getTransportRequests(getCurrentTenantId()));
    }

    @PostMapping
    public ResponseEntity<?> createTransportRequest(@Valid @RequestBody TransportRequestUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER", "SCHOOL_BUS_PARENT");
        return created("Created transport request",
                transportRequestService.createTransportRequest(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransportRequest(@PathVariable Long id) {
        return ok("Fetched transport request", transportRequestService.getTransportRequest(id, getCurrentTenantId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTransportRequest(@PathVariable Long id,
            @Valid @RequestBody TransportRequestUpsertRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER", "SCHOOL_BUS_PARENT");
        return ok("Updated transport request",
                transportRequestService.updateTransportRequest(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveTransportRequest(@PathVariable Long id) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return ok("Approved transport request",
                transportRequestService.approveTransportRequest(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectTransportRequest(@PathVariable Long id,
            @Valid @RequestBody RejectRequest request) {
        requireAnyRole("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");
        return ok("Rejected transport request",
                transportRequestService.rejectTransportRequest(id, request, getCurrentTenantId(), getCurrentUserId()));
    }
}
