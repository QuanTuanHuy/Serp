package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import serp.project.first_mile.domain.ImportHistory;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.enums.ImportType;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.mapper.LocationMapper;
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
        return toImportHistoryResponse(importHistory);
    }

    private ImportHistoryResponse toImportHistoryResponse(ImportHistory importHistory) {
        return ImportHistoryResponse.builder()
                .id(importHistory.getId())
                .fileId(importHistory.getFileId())
                .fileName(importHistory.getFileName())
                .status(importHistory.getStatus())
                .totalRecords(importHistory.getTotalRecords())
                .successRecords(importHistory.getSuccessRecords())
                .failedRecords(importHistory.getFailedRecords())
                .errorMessage(importHistory.getErrorMessage())
                .startedAt(importHistory.getStartedAt())
                .finishedAt(importHistory.getFinishedAt())
                .type(importHistory.getType())
                .build();
    }

    @Override
    public PageResponse<ImportHistoryResponse> getAllImportHistory(int page, int size, ImportType type, Long tenantId) {
        Pageable pageable = PageRequest.of(page, size);

        var importHistoryPage = (type == null
            ? importHistoryRepository.findAllByTenantId(tenantId, pageable)
            : importHistoryRepository.findAllByTenantIdAndType(tenantId, type, pageable))
                .map(this::toImportHistoryResponse);
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
