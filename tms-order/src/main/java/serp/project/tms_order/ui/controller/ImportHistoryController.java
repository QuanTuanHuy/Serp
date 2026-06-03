/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.tms_order.dto.ApiResponse;
import serp.project.tms_order.dto.PageResponse;
import serp.project.tms_order.dto.response.ImportHistoryResponse;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.exception.MessageService;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.service.ImportHistoryService;

@RestController
@RequestMapping("/api/v1/import-history")
@RequiredArgsConstructor
@Slf4j
public class ImportHistoryController {
    private final AuthUtils authUtils;
    private final ImportHistoryService importHistoryService;
    private final MessageService messageService;

    @GetMapping("/{importHistoryId}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ImportHistoryResponse getImportHistory(@PathVariable Long importHistoryId) {
        Long tenantId = getCurrentTenantId();
        log.info("REST request to get order import history {} for tenant {}", importHistoryId, tenantId);
        return importHistoryService.getOrderImportHistory(importHistoryId, tenantId);
    }

    @GetMapping
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<PageResponse<ImportHistoryResponse>> getImportHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<PageResponse<ImportHistoryResponse>>builder()
                .message(messageService.getMessage("success.all_import_history"))
                .result(importHistoryService.getAllOrderImportHistory(page, size, tenantId))
                .build();
    }

    private Long getCurrentTenantId() {
        return authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
    }
}
