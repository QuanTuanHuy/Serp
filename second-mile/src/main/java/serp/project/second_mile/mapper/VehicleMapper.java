/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.mapper;

import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.request.CreateVehicleRequest;
import serp.project.second_mile.dto.request.UpdateVehicleRequest;
import serp.project.second_mile.dto.response.VehicleResponse;

public final class VehicleMapper {
    private VehicleMapper() {
    }

    public static Vehicle toEntity(CreateVehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setMaxWeight(request.getMaxWeight());
        vehicle.setMaxVolume(request.getMaxVolume());
        vehicle.setMaxBags(request.getMaxBags());
        vehicle.setImageUrl(request.getImageUrl());
        vehicle.setHubId(request.getHubId());
        vehicle.setAssignedStaffId(request.getAssignedStaffId());
        vehicle.setStatus(request.getStatus());
        return vehicle;
    }

    public static void mapForUpdate(UpdateVehicleRequest request, Vehicle vehicle) {
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setMaxWeight(request.getMaxWeight());
        vehicle.setMaxVolume(request.getMaxVolume());
        vehicle.setMaxBags(request.getMaxBags());
        vehicle.setImageUrl(request.getImageUrl());
        vehicle.setHubId(request.getHubId());
        vehicle.setAssignedStaffId(request.getAssignedStaffId());
        vehicle.setStatus(request.getStatus());
    }

    public static VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getVehicleType(),
                vehicle.getMaxWeight(),
                vehicle.getMaxVolume(),
                vehicle.getMaxBags(),
                vehicle.getImageUrl(),
                vehicle.getHubId(),
                vehicle.getAssignedStaffId(),
                vehicle.getStatus(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt(),
                vehicle.getCreatedBy(),
                vehicle.getUpdatedBy(),
                vehicle.getTenantId()
        );
    }
}

