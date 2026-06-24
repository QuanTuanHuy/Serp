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
import serp.project.school_bus_service.dto.params.StudentSubscriptionParamsRequest;
import serp.project.school_bus_service.dto.request.StudentSubscriptionUpsertRequest;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.StudentSubscriptionResponse;
import serp.project.school_bus_service.service.IStudentSubscriptionService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController extends AbstractBaseController {

    private final IStudentSubscriptionService subscriptionService;

    public SubscriptionController(IStudentSubscriptionService subscriptionService, AuthUtils authUtils) {
        super(authUtils);
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.subscription.read')")
    public ResponseEntity<GeneralResponse<PageResponse<StudentSubscriptionResponse>>> getSubscriptions(
            @ModelAttribute StudentSubscriptionParamsRequest params) {
        return ok("Fetched subscriptions", subscriptionService.getSubscriptions(params, getCurrentTenantId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.subscription.read')")
    public ResponseEntity<GeneralResponse<StudentSubscriptionResponse>> getSubscription(@PathVariable Long id) {
        return ok("Fetched subscription", subscriptionService.getSubscription(id, getCurrentTenantId()));
    }

    @PostMapping
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.subscription.write')")
    public ResponseEntity<GeneralResponse<StudentSubscriptionResponse>> createSubscription(
            @Valid @RequestBody StudentSubscriptionUpsertRequest request) {
        return created("Created subscription",
                subscriptionService.createSubscription(request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.subscription.write')")
    public ResponseEntity<GeneralResponse<StudentSubscriptionResponse>> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody StudentSubscriptionUpsertRequest request) {
        return ok("Updated subscription",
                subscriptionService.updateSubscription(id, request, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.subscription.write')")
    public ResponseEntity<GeneralResponse<StudentSubscriptionResponse>> activateSubscription(@PathVariable Long id) {
        return ok("Activated subscription",
                subscriptionService.activateSubscription(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.subscription.write')")
    public ResponseEntity<GeneralResponse<StudentSubscriptionResponse>> pauseSubscription(@PathVariable Long id) {
        return ok("Paused subscription",
                subscriptionService.pauseSubscription(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/stop")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.subscription.write')")
    public ResponseEntity<GeneralResponse<StudentSubscriptionResponse>> stopSubscription(@PathVariable Long id) {
        return ok("Stopped subscription",
                subscriptionService.stopSubscription(id, getCurrentTenantId(), getCurrentUserId()));
    }

}
