/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CreateVehicleRequest;
import serp.project.first_mile.dto.request.UpdateVehicleRequest;
import serp.project.first_mile.dto.request.VehicleImportDTO;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;
import serp.project.first_mile.dto.response.VehicleResponse;
import serp.project.first_mile.enums.VehicleStatus;
import serp.project.first_mile.enums.VehicleType;

public interface VehicleService {
    byte[] exportTemplate();

    ValidateImportFileDTO<VehicleImportDTO> validateImportFile(MultipartFile file, Long tenantId);

    ImportHistoryResponse importVehiclesAsync(MultipartFile file, Long tenantId);

    PageResponse<VehicleResponse> getVehicles(
            int page,
            int size,
            String keyword,
            VehicleType vehicleType,
            VehicleStatus status,
            String postOfficeKeyword,
            String courierKeyword
    );

    VehicleResponse getVehicleById(Long id);

    VehicleResponse createVehicle(CreateVehicleRequest request);

    VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request);

    VehicleResponse uploadImage(Long id, MultipartFile file);

    void deleteVehicle(Long id);

    VehicleResponse assignVehicleToPostOffice(Long id, Long postOfficeId);

    VehicleResponse assignVehicleToCourier(Long id, Long postOfficeStaffId);
}
