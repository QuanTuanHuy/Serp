package serp.project.first_mile.service;

import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.enums.ImportType;

public interface ImportHistoryService {
    ImportHistoryResponse getImportHistory(Long importHistoryId, Long tenantId);

    PageResponse<ImportHistoryResponse> getAllImportHistory(int page, int size, ImportType type, Long tenantId);
}
