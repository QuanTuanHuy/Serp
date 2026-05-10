/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.second_mile.dto.request.HubImportDTO;
import serp.project.second_mile.dto.response.ImportHistoryResponse;
import serp.project.second_mile.dto.response.ValidateImportFileDTO;

public interface HubImportExcelService {
    ValidateImportFileDTO<HubImportDTO> validateImportFile(MultipartFile file, Long tenantId);

    ImportHistoryResponse importHubsAsync(MultipartFile file, Long tenantId);
}
