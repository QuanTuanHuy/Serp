/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service;

import serp.project.tms_order.dto.PageResponse;
import serp.project.tms_order.dto.response.ImportHistoryResponse;

public interface ImportHistoryService {
    ImportHistoryResponse getOrderImportHistory(Long importHistoryId, Long tenantId);

    PageResponse<ImportHistoryResponse> getAllOrderImportHistory(int page, int size, Long tenantId);
}
