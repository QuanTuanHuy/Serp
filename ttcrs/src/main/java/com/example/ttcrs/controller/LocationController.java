package com.example.ttcrs.controller;

import com.example.ttcrs.dto.request.CreateLocationDTO;
import com.example.ttcrs.dto.response.ApiResponse;
import com.example.ttcrs.dto.response.LocationResponseDTO;
import com.example.ttcrs.service.LocationService;
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
 * Controller cho Location API (dành cho Dispatcher).
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
     * @return {@code 200 OK} với danh sách Location
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationResponseDTO>>> getLocations() {
        log.info("GET /ttcrs/api/v1/dispatcher/locations");
        List<LocationResponseDTO> locations = locationService.getLocationsForCurrentTenant();
        return ResponseEntity.ok(ApiResponse.ok(locations));
    }

    /**
     * Tạo mới một Location cho tenant hiện tại.
     *
     * <p><b>Request body:</b>
     * <ul>
     *   <li>{@code locationCode} — mã định danh duy nhất (bắt buộc)</li>
     *   <li>{@code type}         — loại location (bắt buộc): PORT | WAREHOUSE | DEPOT_CONTAINER | DEPOT_TRUCK | DEPOT_TRAILER</li>
     *   <li>{@code lat}          — vĩ độ từ bản đồ (bắt buộc)</li>
     *   <li>{@code lng}          — kinh độ từ bản đồ (bắt buộc)</li>
     * </ul>
     *
     * @param dto tham số tạo location
     * @return {@code 201 Created} với location vừa tạo, hoặc {@code 409 Conflict} nếu locationCode đã tồn tại
     */
    @PostMapping
    public ResponseEntity<ApiResponse<LocationResponseDTO>> createLocation(
            @Valid @RequestBody CreateLocationDTO dto
    ) {
        log.info("POST /ttcrs/api/v1/dispatcher/locations - code={}, type={}", dto.getLocationCode(), dto.getType());
        try {
            LocationResponseDTO created = locationService.createLocation(dto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Location created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
