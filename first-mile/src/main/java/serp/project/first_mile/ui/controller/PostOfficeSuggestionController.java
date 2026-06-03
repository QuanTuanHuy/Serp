/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.first_mile.dto.request.PostOfficeSuggestionRequest;
import serp.project.first_mile.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.first_mile.service.PostOfficeReservationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal/post-office-suggestions")
@RequiredArgsConstructor
@Slf4j
public class PostOfficeSuggestionController {

    private final PostOfficeReservationService postOfficeReservationService;

    @PostMapping("/drop-off")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public List<OrderDropOffPostOfficeSuggestionResponse> suggestDropOffPostOffices(
            @RequestBody PostOfficeSuggestionRequest request
    ) {
        log.info("REST request to suggest drop-off post offices");
        return postOfficeReservationService.suggestDropOffPostOffices(request);
    }
}
