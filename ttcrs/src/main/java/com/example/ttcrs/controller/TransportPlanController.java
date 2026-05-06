package com.example.ttcrs.controller;

import com.example.ttcrs.dto.request.transportplan.SaveTransportPlanDTO;
import com.example.ttcrs.dto.response.ApiResponse;
import com.example.ttcrs.dto.response.TransportPlanDetailDTO;
import com.example.ttcrs.dto.response.TransportPlanResponseDTO;
import com.example.ttcrs.service.TransportPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ttcrs/api/v1/dispatcher/transport-plans")
@RequiredArgsConstructor
public class TransportPlanController {

    private final TransportPlanService transportPlanService;

    /** GET /ttcrs/api/v1/dispatcher/transport-plans — list all plans for the tenant */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransportPlanResponseDTO>>> getPlans() {
        log.info("GET /ttcrs/api/v1/dispatcher/transport-plans");
        return ResponseEntity.ok(ApiResponse.ok(transportPlanService.getPlans()));
    }

    /** GET /ttcrs/api/v1/dispatcher/transport-plans/{id} — full detail with stops */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransportPlanDetailDTO>> getPlanDetail(@PathVariable Long id) {
        log.info("GET /ttcrs/api/v1/dispatcher/transport-plans/{}", id);
        try {
            return ResponseEntity.ok(ApiResponse.ok(transportPlanService.getPlanDetail(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /ttcrs/api/v1/dispatcher/transport-plans
     */
    @PostMapping
    public ResponseEntity<ApiResponse<List<TransportPlanResponseDTO>>> savePlans(
            @Valid @RequestBody SaveTransportPlanDTO dto
    ) {
        log.info("POST /ttcrs/api/v1/dispatcher/transport-plans - {} plans", dto.getPlans().size());
        List<TransportPlanResponseDTO> result = transportPlanService.savePlans(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Transport plans saved successfully", result));
    }
}
