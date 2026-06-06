/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.tms_order.dto.request.OrderImportDTO;
import serp.project.tms_order.dto.response.ImportHistoryResponse;
import serp.project.tms_order.dto.response.ValidateImportFileDTO;

public interface OrderImportExcelService {
    ValidateImportFileDTO<OrderImportDTO> validateImportFile(MultipartFile file, Long tenantId);

    ImportHistoryResponse importOrdersAsync(MultipartFile file, Long tenantId);

}

