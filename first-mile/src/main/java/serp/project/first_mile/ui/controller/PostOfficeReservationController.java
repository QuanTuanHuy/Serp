/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.first_mile.dto.request.ReserveDestinationPostOfficeRequest;
import serp.project.first_mile.dto.request.ReserveOriginPostOfficeRequest;
import serp.project.first_mile.dto.response.DestinationPostOfficeReservationResponse;
import serp.project.first_mile.dto.response.OriginPostOfficeReservationResponse;
import serp.project.first_mile.service.PostOfficeReservationService;

@RestController
@RequestMapping("/api/v1/internal/post-office-reservations")
@RequiredArgsConstructor
@Slf4j
public class PostOfficeReservationController {

    private final PostOfficeReservationService postOfficeReservationService;

    @PostMapping("/origin/best")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public OriginPostOfficeReservationResponse reserveBestOriginPostOffice(
            @Valid @RequestBody ReserveOriginPostOfficeRequest request
    ) {
        log.info("REST request to reserve best origin post office");
        return postOfficeReservationService.reserveBestOriginPostOffice(request);
    }

    @PostMapping("/destination/best")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER', 'TMS_POSTOFFICER_MANAGER')")
    public DestinationPostOfficeReservationResponse reserveBestDestinationPostOffice(
            @Valid @RequestBody ReserveDestinationPostOfficeRequest request
    ) {
        log.info("REST request to reserve best destination post office");
        return postOfficeReservationService.reserveBestDestinationPostOffice(request);
    }

    @PostMapping("/origin/{postOfficeId}/drop-off")
    @PreAuthorize("hasRole('TMS_POSTOFFICER_MANAGER')")
    public OriginPostOfficeReservationResponse reserveDropOffOriginPostOffice(
            @PathVariable Long postOfficeId,
            @Valid @RequestBody ReserveOriginPostOfficeRequest request
    ) {
        log.info("REST request to reserve drop-off origin post office {}", postOfficeId);
        return postOfficeReservationService.reserveDropOffOriginPostOffice(postOfficeId, request);
    }

    @GetMapping("/origin/{postOfficeId}/managed")
    @PreAuthorize("hasRole('TMS_POSTOFFICER_MANAGER')")
    public OriginPostOfficeReservationResponse validateManagedPostOffice(@PathVariable Long postOfficeId) {
        log.info("REST request to validate managed origin post office {}", postOfficeId);
        return postOfficeReservationService.validateManagedPostOffice(postOfficeId);
    }

}
