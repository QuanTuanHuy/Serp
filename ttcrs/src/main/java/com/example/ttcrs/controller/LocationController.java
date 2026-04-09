package com.example.ttcrs.controller;

import com.example.ttcrs.dto.response.ApiResponse;
import com.example.ttcrs.dto.response.LocationResponseDTO;
import com.example.ttcrs.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller cung cấp danh sách Locations cho Dispatcher (dùng để điền form tạo request).
 *
 * <p>Base URL: {@code /ttcrs/api/v1/dispatcher/locations}
 */
@Slf4j
@RestController
@RequestMapping("/ttcrs/api/v1/dispatcher/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    /**
     * Lấy danh sách tất cả Location trong tenant của Dispatcher hiện tại.
     *
     * <p>Dùng để populate dropdown "Điểm đi" và "Điểm đến" trong form tạo request.
     *
     * @return {@code 200 OK} với danh sách Location
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationResponseDTO>>> getLocations() {
        log.info("GET /ttcrs/api/v1/dispatcher/locations");
        List<LocationResponseDTO> locations = locationService.getLocationsForCurrentTenant();
        return ResponseEntity.ok(ApiResponse.ok(locations));
    }
}
