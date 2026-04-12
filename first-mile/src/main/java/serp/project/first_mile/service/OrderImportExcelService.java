/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.request.OrderImportDTO;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;

public interface OrderImportExcelService {
    ValidateImportFileDTO<OrderImportDTO> validateImportFile(MultipartFile file, Long tenantId);

    ImportHistoryResponse importOrdersAsync(MultipartFile file, Long tenantId);

}
