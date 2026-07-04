package com.example.ttcrs.controller;

import com.example.ttcrs.dto.request.transportplan.SaveTransportPlanDTO;
import com.example.ttcrs.dto.response.ApiResponse;
import com.example.ttcrs.dto.response.TransportPlanResponseDTO;
import com.example.ttcrs.service.TransportPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ttcrs/api/v1/dispatcher/manual-routes")
@RequiredArgsConstructor
public class ManualRouteController {

    private final TransportPlanService transportPlanService;

    /**
     * POST /ttcrs/api/v1/dispatcher/manual-routes
     *
     * <p>Saves dispatcher-built manual routes and updates linked requests to PLANNED.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<List<TransportPlanResponseDTO>>> createManualRoute(
            @Valid @RequestBody SaveTransportPlanDTO dto
    ) {
        log.info("POST /ttcrs/api/v1/dispatcher/manual-routes - {} plans", dto.getPlans().size());
        List<TransportPlanResponseDTO> result = transportPlanService.saveManualRoute(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Manual route saved successfully", result));
    }
}

