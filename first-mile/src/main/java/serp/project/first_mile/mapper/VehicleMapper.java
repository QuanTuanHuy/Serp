/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.mapper;

import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.Vehicle;
import serp.project.first_mile.dto.request.CreateVehicleRequest;
import serp.project.first_mile.dto.request.UpdateVehicleRequest;
import serp.project.first_mile.dto.response.VehicleResponse;

public final class VehicleMapper {

    private VehicleMapper() {
    }

    public static Vehicle toEntity(CreateVehicleRequest request, PostOffice postOffice, Long postOfficeStaffId) {
        return Vehicle.builder()
                .licensePlate(request.getLicensePlate() == null ? null : request.getLicensePlate().trim())
                .maxWeight(request.getMaxWeight())
                .maxVolume(request.getMaxVolume())
                .postOffice(postOffice)
                .postOfficeStaffId(postOfficeStaffId)
                .status(request.getStatus())
            .vehicleType(request.getVehicleType())
                .build();
    }

    public static void mapForUpdate(
            UpdateVehicleRequest request,
            Vehicle vehicle,
            PostOffice postOffice,
            Long postOfficeStaffId
    ) {
        vehicle.setLicensePlate(request.getLicensePlate() == null ? null : request.getLicensePlate().trim());
        vehicle.setMaxWeight(request.getMaxWeight());
        vehicle.setMaxVolume(request.getMaxVolume());
        vehicle.setPostOffice(postOffice);
        vehicle.setPostOfficeStaffId(postOfficeStaffId);
        vehicle.setStatus(request.getStatus());
        vehicle.setVehicleType(request.getVehicleType());
    }

    public static VehicleResponse toResponse(Vehicle vehicle) {
        Long postOfficeId = null;
        String postOfficeCode = null;
        String postOfficeName = null;

        if (vehicle.getPostOffice() != null) {
            postOfficeId = vehicle.getPostOffice().getId();
            postOfficeCode = vehicle.getPostOffice().getCode();
            postOfficeName = vehicle.getPostOffice().getName();
        }

        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getMaxWeight(),
                vehicle.getMaxVolume(),
                vehicle.getImageUrl(),
                postOfficeId,
                postOfficeCode,
                postOfficeName,
                vehicle.getPostOfficeStaffId(),
                vehicle.getStatus(),
                vehicle.getVehicleType(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt(),
                vehicle.getCreatedBy(),
                vehicle.getUpdatedBy(),
                vehicle.getTenantId()
        );
    }
}
