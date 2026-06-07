/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.optimization;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.optimization.command.apply.ApplyOptimizationRunCommand;
import serp.project.pmcore.application.optimization.command.apply.ApplyOptimizationRunCommandHandler;
import serp.project.pmcore.application.optimization.command.discard.DiscardOptimizationRunCommand;
import serp.project.pmcore.application.optimization.command.discard.DiscardOptimizationRunCommandHandler;
import serp.project.pmcore.application.optimization.command.generate.GenerateOptimizationRunCommand;
import serp.project.pmcore.application.optimization.command.generate.GenerateOptimizationRunCommandHandler;
import serp.project.pmcore.application.optimization.command.update.BatchUpdateOptimizationRunItemDecisionsCommand;
import serp.project.pmcore.application.optimization.command.update.BatchUpdateOptimizationRunItemDecisionsCommandHandler;
import serp.project.pmcore.application.optimization.query.get.GetOptimizationRunQuery;
import serp.project.pmcore.application.optimization.query.get.GetOptimizationRunQueryHandler;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.optimization.dto.request.ApplyOptimizationRunRequest;
import serp.project.pmcore.ui.rest.optimization.dto.request.BatchUpdateOptimizationRunItemDecisionsRequest;
import serp.project.pmcore.ui.rest.optimization.dto.request.GenerateOptimizationRunRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

import java.util.List;

@RestController
@RequestMapping(PathConstants.PROJECT_OPTIMIZATION_RUNS)
@RequiredArgsConstructor
public class OptimizationRunController {
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final GenerateOptimizationRunCommandHandler generateOptimizationRunCommandHandler;
    private final GetOptimizationRunQueryHandler getOptimizationRunQueryHandler;
    private final BatchUpdateOptimizationRunItemDecisionsCommandHandler batchUpdateOptimizationRunItemDecisionsCommandHandler;
    private final ApplyOptimizationRunCommandHandler applyOptimizationRunCommandHandler;
    private final DiscardOptimizationRunCommandHandler discardOptimizationRunCommandHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<OptimizationRunReviewView>> generateOptimizationRun(
            @PathVariable Long projectId,
            @Valid @RequestBody GenerateOptimizationRunRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        OptimizationRunReviewView response = generateOptimizationRunCommandHandler.handle(new GenerateOptimizationRunCommand(
                tenantId,
                userId,
                projectId,
                request.getScope(),
                request.getAlgorithmKey(),
                request.getObjective(),
                request.getChangeScope(),
                request.getPlanningStart(),
                request.getPlanningEnd(),
                request.getSelectedWorkItemIds()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @GetMapping("/{runId}")
    public ResponseEntity<GeneralResponse<OptimizationRunReviewView>> getOptimizationRun(
            @PathVariable Long projectId,
            @PathVariable Long runId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        OptimizationRunReviewView response = getOptimizationRunQueryHandler.handle(new GetOptimizationRunQuery(
                tenantId,
                projectId,
                runId
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PatchMapping("/{runId}/items/decisions")
    public ResponseEntity<GeneralResponse<OptimizationRunReviewView>> updateOptimizationRunItemDecisions(
            @PathVariable Long projectId,
            @PathVariable Long runId,
            @Valid @RequestBody BatchUpdateOptimizationRunItemDecisionsRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        List<BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision> items = request.getItems().stream()
                .map(item -> new BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision(
                        item.getWorkItemId(),
                        item.getAssignmentDecision(),
                        item.getScheduleDecision(),
                        item.getOverrideAssigneeId(),
                        item.getOverridePlannedStart(),
                        item.getOverridePlannedEnd()
                ))
                .toList();

        OptimizationRunReviewView response = batchUpdateOptimizationRunItemDecisionsCommandHandler.handle(
                new BatchUpdateOptimizationRunItemDecisionsCommand(
                        tenantId,
                        userId,
                        projectId,
                        runId,
                        items
                )
        );

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PostMapping("/{runId}/apply")
    public ResponseEntity<GeneralResponse<OptimizationRunReviewView>> applyOptimizationRun(
            @PathVariable Long projectId,
            @PathVariable Long runId,
            @Valid @RequestBody ApplyOptimizationRunRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        OptimizationRunReviewView response = applyOptimizationRunCommandHandler.handle(new ApplyOptimizationRunCommand(
                tenantId,
                userId,
                projectId,
                runId,
                request.getApplyAssignment(),
                request.getApplySchedule(),
                request.getWorkItemIds(),
                authUtils.getCurrentGroups()
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PostMapping("/{runId}/discard")
    public ResponseEntity<GeneralResponse<OptimizationRunReviewView>> discardOptimizationRun(
            @PathVariable Long projectId,
            @PathVariable Long runId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        OptimizationRunReviewView response = discardOptimizationRunCommandHandler.handle(new DiscardOptimizationRunCommand(
                tenantId,
                userId,
                projectId,
                runId
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }
}
