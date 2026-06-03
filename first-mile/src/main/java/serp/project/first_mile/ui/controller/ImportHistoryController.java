package serp.project.first_mile.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.enums.ImportType;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.exception.MessageService;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.service.ImportHistoryService;

@RestController
@RequestMapping("/api/v1/import-history")
@RequiredArgsConstructor
@Slf4j
public class ImportHistoryController {
    private final AuthUtils authUtils;
    private final ImportHistoryService  importHistoryService;
    private final MessageService messageService;

    @GetMapping("/{importHistoryId}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ImportHistoryResponse getImportHistory(
            @PathVariable Long importHistoryId
    ) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to get import history {} for tenant {}", importHistoryId, tenantId);
        return importHistoryService.getImportHistory(importHistoryId, tenantId);
    }

    @GetMapping
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<PageResponse<ImportHistoryResponse>> getImportHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ImportType type
    ) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        return ApiResponse.<PageResponse<ImportHistoryResponse>>builder()
                .message(messageService.getMessage("success.all_import_history"))
                .result(importHistoryService.getAllImportHistory(page, size, type, tenantId))
                .build();
    }
}
