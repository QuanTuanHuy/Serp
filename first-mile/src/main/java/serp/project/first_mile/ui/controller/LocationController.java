/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.GeocodeAddressRequest;
import serp.project.first_mile.dto.response.GeocodeAddressResponse;
import serp.project.first_mile.dto.response.ProvinceResponse;
import serp.project.first_mile.dto.response.WardResponse;
import serp.project.first_mile.exception.MessageService;
import serp.project.first_mile.kernel.ratelimit.RateLimit;
import serp.project.first_mile.service.GeocodeService;
import serp.project.first_mile.service.LocationService;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
    private final GeocodeService geocodeService;
    private final MessageService messageService;

    @GetMapping("/provinces")
    public ApiResponse<PageResponse<ProvinceResponse>> getProvinces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.<PageResponse<ProvinceResponse>>builder()
                .message(messageService.getMessage("success.locations.provinces"))
                .result(locationService.getProvinces(page, size))
                .build();
    }

    @GetMapping("/provinces/{provinceCode}/wards")
    public ApiResponse<PageResponse<WardResponse>> getWardsByProvinceCode(
            @PathVariable String provinceCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.<PageResponse<WardResponse>>builder()
                .message(messageService.getMessage("success.locations.wards"))
                .result(locationService.getWardsByProvinceCode(provinceCode, page, size))
                .build();
    }

    @PostMapping("/geocode")
    @RateLimit(key = "locations-geocode", permitsPerSecondProperty = "geocode.rate-limit.tps")
    public ApiResponse<GeocodeAddressResponse> geocodeAddress(
            @Valid @RequestBody GeocodeAddressRequest request
    ) {
        return ApiResponse.<GeocodeAddressResponse>builder()
                .message(messageService.getMessage("success.locations.geocode"))
                .result(geocodeService.geocodeAddress(request.getAddress()))
                .build();
    }
}
