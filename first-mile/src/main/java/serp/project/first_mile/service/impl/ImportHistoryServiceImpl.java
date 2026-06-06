package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import serp.project.first_mile.domain.ImportHistory;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.enums.ImportType;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.ImportHistoryResponseUtils;
import serp.project.first_mile.repository.ImportHistoryRepository;
import serp.project.first_mile.service.ImportHistoryService;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImportHistoryServiceImpl implements ImportHistoryService {
    private final ImportHistoryRepository importHistoryRepository;
    @Override
    public ImportHistoryResponse getImportHistory(Long importHistoryId, Long tenantId) {
        ImportHistory importHistory = importHistoryRepository.findByIdAndTenantId(importHistoryId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
        if (ImportType.ORDER.equals(importHistory.getType())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return ImportHistoryResponseUtils.toResponse(importHistory, true);
    }

    @Override
    public PageResponse<ImportHistoryResponse> getAllImportHistory(int page, int size, ImportType type, Long tenantId) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ImportHistory> sourcePage;
        if (type == null) {
            sourcePage = importHistoryRepository.findAllByTenantIdAndTypeNot(tenantId, ImportType.ORDER, pageable);
        } else if (ImportType.ORDER.equals(type)) {
            sourcePage = Page.empty(pageable);
        } else {
            sourcePage = importHistoryRepository.findAllByTenantIdAndType(tenantId, type, pageable);
        }

        var importHistoryPage = sourcePage
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
