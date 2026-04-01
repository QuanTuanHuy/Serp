/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.request.PostOfficeImportDTO;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;

public interface PostOfficeImportExcelService {
    ValidateImportFileDTO<PostOfficeImportDTO> validateImportFile(MultipartFile file, Long tenantId);

    ImportHistoryResponse importPostOfficesAsync(MultipartFile file, Long tenantId);

    ImportHistoryResponse getImportHistory(Long importHistoryId, Long tenantId);
}
