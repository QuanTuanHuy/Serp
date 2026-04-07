/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CreateVehicleRequest;
import serp.project.first_mile.dto.request.UpdateVehicleRequest;
import serp.project.first_mile.dto.response.VehicleResponse;
import serp.project.first_mile.exception.MessageService;
import serp.project.first_mile.service.VehicleService;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final MessageService messageService;

    @GetMapping
    public ApiResponse<PageResponse<VehicleResponse>> getVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.<PageResponse<VehicleResponse>>builder()
                .message(messageService.getMessage("success.vehicles.list"))
                .result(vehicleService.getVehicles(page, size, keyword))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<VehicleResponse> getVehicleById(@PathVariable Long id) {
        return ApiResponse.<VehicleResponse>builder()
                .message(messageService.getMessage("success.vehicles.detail"))
                .result(vehicleService.getVehicleById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<VehicleResponse> createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        return ApiResponse.<VehicleResponse>builder()
                .message(messageService.getMessage("success.vehicles.create"))
                .result(vehicleService.createVehicle(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<VehicleResponse> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleRequest request
    ) {
        return ApiResponse.<VehicleResponse>builder()
                .message(messageService.getMessage("success.vehicles.update"))
                .result(vehicleService.updateVehicle(id, request))
                .build();
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<VehicleResponse> uploadVehicleImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.<VehicleResponse>builder()
                .message(messageService.getMessage("success.vehicles.image.upload"))
                .result(vehicleService.uploadImage(id, file))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ApiResponse.<Void>builder()
                .message(messageService.getMessage("success.vehicles.delete"))
                .build();
    }

    @PutMapping("/{id}/ownership/post-office/{postOfficeId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<VehicleResponse> assignVehicleToPostOffice(
            @PathVariable Long id,
            @PathVariable Long postOfficeId
    ) {
        return ApiResponse.<VehicleResponse>builder()
                .message(messageService.getMessage("success.vehicles.assign.post_office"))
                .result(vehicleService.assignVehicleToPostOffice(id, postOfficeId))
                .build();
    }

    @PutMapping("/{id}/ownership/courier/{postOfficeStaffId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<VehicleResponse> assignVehicleToCourier(
            @PathVariable Long id,
            @PathVariable Long postOfficeStaffId
    ) {
        return ApiResponse.<VehicleResponse>builder()
                .message(messageService.getMessage("success.vehicles.assign.courier"))
                .result(vehicleService.assignVehicleToCourier(id, postOfficeStaffId))
                .build();
    }
}
