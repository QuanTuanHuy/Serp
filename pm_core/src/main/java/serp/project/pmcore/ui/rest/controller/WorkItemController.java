/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.core.domain.dto.request.CreateWorkItemRequest;
import serp.project.pmcore.core.domain.dto.response.GeneralResponse;
import serp.project.pmcore.core.domain.dto.response.WorkItemResponse;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.core.usecase.WorkItemUseCase;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.kernel.utils.ResponseUtils;

@RestController
@RequestMapping("/api/v1/work-items")
@RequiredArgsConstructor
@Slf4j
public class WorkItemController {

    private final WorkItemUseCase workItemUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @PostMapping
    public ResponseEntity<GeneralResponse<?>> createWorkItem(
            @Valid @RequestBody CreateWorkItemRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        WorkItemResponse response = workItemUseCase.createWorkItem(request, tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }
}
