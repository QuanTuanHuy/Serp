/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import serp.project.tms_order.domain.ImportHistory;
import serp.project.tms_order.dto.PageResponse;
import serp.project.tms_order.dto.response.ImportHistoryResponse;
import serp.project.tms_order.enums.ImportType;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kernel.utils.ImportHistoryResponseUtils;
import serp.project.tms_order.repository.ImportHistoryRepository;
import serp.project.tms_order.service.ImportHistoryService;

@Service
@RequiredArgsConstructor
public class ImportHistoryServiceImpl implements ImportHistoryService {
    private final ImportHistoryRepository importHistoryRepository;

    @Override
    public ImportHistoryResponse getOrderImportHistory(Long importHistoryId, Long tenantId) {
        ImportHistory importHistory = importHistoryRepository
                .findByIdAndTenantIdAndType(importHistoryId, tenantId, ImportType.ORDER)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
        return ImportHistoryResponseUtils.toResponse(importHistory, true);
    }

    @Override
    public PageResponse<ImportHistoryResponse> getAllOrderImportHistory(int page, int size, Long tenantId) {
        Pageable pageable = PageRequest.of(page, size);

        var importHistoryPage = importHistoryRepository
                .findAllByTenantIdAndType(tenantId, ImportType.ORDER, pageable)
                .map(importHistory -> ImportHistoryResponseUtils.toResponse(importHistory, true));

        return PageResponse.<ImportHistoryResponse>builder()
                .items(importHistoryPage.getContent())
                .page(importHistoryPage.getNumber())
                .size(importHistoryPage.getSize())
                .totalElements(importHistoryPage.getTotalElements())
                .totalPages(importHistoryPage.getTotalPages())
                .hasNext(importHistoryPage.hasNext())
                .hasPrevious(importHistoryPage.hasPrevious())
                .build();
    }
}
