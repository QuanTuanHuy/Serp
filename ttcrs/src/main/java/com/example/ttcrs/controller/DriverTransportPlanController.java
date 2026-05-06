package com.example.ttcrs.controller;

import com.example.ttcrs.dto.response.ApiResponse;
import com.example.ttcrs.dto.response.TransportPlanDetailDTO;
import com.example.ttcrs.dto.response.TransportPlanResponseDTO;
import com.example.ttcrs.service.TransportPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ttcrs/api/v1/driver/transport-plans")
@RequiredArgsConstructor
public class DriverTransportPlanController {

    private final TransportPlanService transportPlanService;

    /** GET /ttcrs/api/v1/driver/transport-plans — list plans assigned to the current driver */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransportPlanResponseDTO>>> getMyPlans() {
        log.info("GET /ttcrs/api/v1/driver/transport-plans");
        return ResponseEntity.ok(ApiResponse.ok(transportPlanService.getPlansByDriver()));
    }

    /** GET /ttcrs/api/v1/driver/transport-plans/{id} — detail, validates ownership */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransportPlanDetailDTO>> getMyPlanDetail(@PathVariable Long id) {
        log.info("GET /ttcrs/api/v1/driver/transport-plans/{}", id);
        try {
            return ResponseEntity.ok(ApiResponse.ok(transportPlanService.getPlanDetailForDriver(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
