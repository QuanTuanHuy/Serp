package serp.project.first_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.request.VehicleImportDTO;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;

public interface VehicleImportExcelService {
    byte[] exportTemplate();

    ValidateImportFileDTO<VehicleImportDTO> validateImportFile(MultipartFile file, Long tenantId);

    ImportHistoryResponse importVehiclesAsync(MultipartFile file, Long tenantId);
}
