package com.example.ttcrs.controller;

import com.example.ttcrs.dto.request.CreateContainerDTO;
import com.example.ttcrs.dto.request.CreateDriverDTO;
import com.example.ttcrs.dto.request.CreateTrailerDTO;
import com.example.ttcrs.dto.request.CreateTruckDTO;
import com.example.ttcrs.dto.response.ApiResponse;
import com.example.ttcrs.dto.response.ContainerResponseDTO;
import com.example.ttcrs.dto.response.DriverResponseDTO;
import com.example.ttcrs.dto.response.TrailerResponseDTO;
import com.example.ttcrs.dto.response.TruckResponseDTO;
import com.example.ttcrs.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for fleet resource management (containers, trucks, trailers, drivers).
 *
 * <p>Base URL: {@code /ttcrs/api/v1/dispatcher/resources}
 */
@Slf4j
@RestController
@RequestMapping("/ttcrs/api/v1/dispatcher/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    // ── Containers ────────────────────────────────────────────────────────

    @GetMapping("/containers")
    public ResponseEntity<ApiResponse<List<ContainerResponseDTO>>> getContainers() {
        log.info("GET /ttcrs/api/v1/dispatcher/resources/containers");
        return ResponseEntity.ok(ApiResponse.ok(resourceService.getContainers()));
    }

    @PostMapping("/containers")
    public ResponseEntity<ApiResponse<ContainerResponseDTO>> createContainer(
            @Valid @RequestBody CreateContainerDTO dto) {
        log.info("POST /ttcrs/api/v1/dispatcher/resources/containers - code={}", dto.getCode());
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Container created successfully", resourceService.createContainer(dto)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Trucks ────────────────────────────────────────────────────────────

    @GetMapping("/trucks")
    public ResponseEntity<ApiResponse<List<TruckResponseDTO>>> getTrucks() {
        log.info("GET /ttcrs/api/v1/dispatcher/resources/trucks");
        return ResponseEntity.ok(ApiResponse.ok(resourceService.getTrucks()));
    }

    @PostMapping("/trucks")
    public ResponseEntity<ApiResponse<TruckResponseDTO>> createTruck(
            @Valid @RequestBody CreateTruckDTO dto) {
        log.info("POST /ttcrs/api/v1/dispatcher/resources/trucks - code={}", dto.getCode());
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Truck created successfully", resourceService.createTruck(dto)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Trailers ──────────────────────────────────────────────────────────

    @GetMapping("/trailers")
    public ResponseEntity<ApiResponse<List<TrailerResponseDTO>>> getTrailers() {
        log.info("GET /ttcrs/api/v1/dispatcher/resources/trailers");
        return ResponseEntity.ok(ApiResponse.ok(resourceService.getTrailers()));
    }

    @PostMapping("/trailers")
    public ResponseEntity<ApiResponse<TrailerResponseDTO>> createTrailer(
            @Valid @RequestBody CreateTrailerDTO dto) {
        log.info("POST /ttcrs/api/v1/dispatcher/resources/trailers - code={}", dto.getCode());
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Trailer created successfully", resourceService.createTrailer(dto)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Drivers ───────────────────────────────────────────────────────────

    @GetMapping("/drivers")
    public ResponseEntity<ApiResponse<List<DriverResponseDTO>>> getDrivers() {
        log.info("GET /ttcrs/api/v1/dispatcher/resources/drivers");
        return ResponseEntity.ok(ApiResponse.ok(resourceService.getDrivers()));
    }

    @PostMapping("/drivers")
    public ResponseEntity<ApiResponse<DriverResponseDTO>> createDriver(
            @Valid @RequestBody CreateDriverDTO dto) {
        log.info("POST /ttcrs/api/v1/dispatcher/resources/drivers - name={}", dto.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Driver created successfully", resourceService.createDriver(dto)));
    }
}
