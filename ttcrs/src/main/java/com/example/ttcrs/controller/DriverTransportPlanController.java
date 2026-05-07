package com.example.ttcrs.controller;

import com.example.ttcrs.dto.request.driver.CancelRouteRequest;
import com.example.ttcrs.dto.request.driver.CompleteStopRequest;
import com.example.ttcrs.dto.response.ApiResponse;
import com.example.ttcrs.dto.response.TransportPlanDetailDTO;
import com.example.ttcrs.dto.response.TransportPlanResponseDTO;
import com.example.ttcrs.service.EvidenceStorageService;
import com.example.ttcrs.service.TransportPlanService;
import com.example.ttcrs.util.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ttcrs/api/v1/driver/transport-plans")
@RequiredArgsConstructor
public class DriverTransportPlanController {

    private final TransportPlanService transportPlanService;
    private final EvidenceStorageService evidenceStorageService;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransportPlanResponseDTO>>> getMyPlans() {
        return ResponseEntity.ok(ApiResponse.ok(transportPlanService.getPlansByDriver()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransportPlanDetailDTO>> getMyPlanDetail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(transportPlanService.getPlanDetailForDriver(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    /** Start route: CREATED → EXECUTING */
    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<TransportPlanDetailDTO>> startRoute(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(transportPlanService.startRoute(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        }
    }

    /** Cancel route: CREATED → CANCELLED */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<TransportPlanDetailDTO>> cancelRoute(
            @PathVariable Long id,
            @Valid @RequestBody CancelRouteRequest body) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(transportPlanService.cancelRoute(id, body.getReason())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        }
    }

    /** Driver arrived at stop N */
    @PatchMapping("/{id}/stops/{seq}/arrive")
    public ResponseEntity<ApiResponse<TransportPlanDetailDTO>> arriveAtStop(
            @PathVariable Long id,
            @PathVariable Integer seq) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(transportPlanService.arriveAtStop(id, seq)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        }
    }

    /** Complete stop N (with optional evidence URL) */
    @PatchMapping("/{id}/stops/{seq}/complete")
    public ResponseEntity<ApiResponse<TransportPlanDetailDTO>> completeStop(
            @PathVariable Long id,
            @PathVariable Integer seq,
            @RequestBody CompleteStopRequest body) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    transportPlanService.completeStop(id, seq, body.getEvidenceUrl(), body.getTotalDistance())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        }
    }

    /** Restore route: CANCELLED → CREATED */
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<TransportPlanDetailDTO>> restoreRoute(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(transportPlanService.restoreRoute(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        }
    }

    /** Upload evidence photo → MinIO → return URL */
    @PostMapping(value = "/evidence/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadEvidence(
            @RequestPart("file") MultipartFile file) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Tenant not found in JWT"));
        try {
            String url = evidenceStorageService.upload(file, tenantId);
            return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url)));
        } catch (Exception e) {
            log.error("Evidence upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }
}
