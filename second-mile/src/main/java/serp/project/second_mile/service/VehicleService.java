/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.CreateVehicleRequest;
import serp.project.second_mile.dto.request.UpdateVehicleRequest;
import serp.project.second_mile.dto.request.VehicleFilterRequest;
import serp.project.second_mile.dto.response.VehicleResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VehicleService {
    PageResponse<VehicleResponse> getVehicles(int page, int size, VehicleFilterRequest filterRequest);

    VehicleResponse getVehicleById(Long id);

    VehicleResponse createVehicle(CreateVehicleRequest request);

    VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request);

    VehicleResponse uploadImage(Long id, MultipartFile file);

    void deleteVehicle(Long id);
}

