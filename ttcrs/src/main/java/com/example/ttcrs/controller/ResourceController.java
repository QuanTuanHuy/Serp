package com.example.ttcrs.controller;

import com.example.ttcrs.dto.request.resource.*;
import com.example.ttcrs.dto.response.*;
import com.example.ttcrs.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Fleet resource management: containers, trucks, trailers, drivers.
 * Base URL: /ttcrs/api/v1/dispatcher/resources
 *
 * Drivers are Account users with role TTCRS_DRIVER.
 * Create/delete of drivers is managed in Account service, not here.
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
        return ResponseEntity.ok(ApiResponse.ok(resourceService.getContainers()));
    }

    @PostMapping("/containers")
    public ResponseEntity<ApiResponse<ContainerResponseDTO>> createContainer(
            @Valid @RequestBody CreateContainerDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Container created successfully", resourceService.createContainer(dto)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/containers/{id}")
    public ResponseEntity<ApiResponse<ContainerResponseDTO>> updateContainer(
            @PathVariable Long id, @Valid @RequestBody UpdateContainerDTO dto) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Container updated successfully",
                    resourceService.updateContainer(id, dto)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/containers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContainer(@PathVariable Long id) {
        try {
            resourceService.deleteContainer(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Trucks ────────────────────────────────────────────────────────────

    @GetMapping("/trucks")
    public ResponseEntity<ApiResponse<List<TruckResponseDTO>>> getTrucks() {
        return ResponseEntity.ok(ApiResponse.ok(resourceService.getTrucks()));
    }

    @PostMapping("/trucks")
    public ResponseEntity<ApiResponse<TruckResponseDTO>> createTruck(
            @Valid @RequestBody CreateTruckDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Truck created successfully", resourceService.createTruck(dto)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/trucks/{id}")
    public ResponseEntity<ApiResponse<TruckResponseDTO>> updateTruck(
            @PathVariable Long id, @Valid @RequestBody UpdateTruckDTO dto) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Truck updated successfully",
                    resourceService.updateTruck(id, dto)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/trucks/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTruck(@PathVariable Long id) {
        try {
            resourceService.deleteTruck(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Trailers ──────────────────────────────────────────────────────────

    @GetMapping("/trailers")
    public ResponseEntity<ApiResponse<List<TrailerResponseDTO>>> getTrailers() {
        return ResponseEntity.ok(ApiResponse.ok(resourceService.getTrailers()));
    }

    @PostMapping("/trailers")
    public ResponseEntity<ApiResponse<TrailerResponseDTO>> createTrailer(
            @Valid @RequestBody CreateTrailerDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Trailer created successfully", resourceService.createTrailer(dto)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/trailers/{id}")
    public ResponseEntity<ApiResponse<TrailerResponseDTO>> updateTrailer(
            @PathVariable Long id, @Valid @RequestBody UpdateTrailerDTO dto) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Trailer updated successfully",
                    resourceService.updateTrailer(id, dto)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/trailers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTrailer(@PathVariable Long id) {
        try {
            resourceService.deleteTrailer(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Drivers (Account users with TTCRS_DRIVER role) ────────────────────

    @GetMapping("/drivers")
    public ResponseEntity<ApiResponse<List<DriverDTO>>> getDrivers() {
        return ResponseEntity.ok(ApiResponse.ok(resourceService.getDrivers()));
    }

    @PutMapping("/drivers/{userId}")
    public ResponseEntity<ApiResponse<DriverDTO>> updateDriverStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateDriverStatusDTO dto) {
        log.info("PUT /ttcrs/api/v1/dispatcher/resources/drivers/{}", userId);
        try {
            return ResponseEntity.ok(ApiResponse.ok("Driver status updated successfully",
                    resourceService.updateDriverStatus(userId, dto)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
