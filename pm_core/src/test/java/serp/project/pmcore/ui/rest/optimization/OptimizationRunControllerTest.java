/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.optimization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import serp.project.pmcore.application.optimization.command.apply.ApplyOptimizationRunCommandHandler;
import serp.project.pmcore.application.optimization.command.discard.DiscardOptimizationRunCommandHandler;
import serp.project.pmcore.application.optimization.command.generate.GenerateOptimizationRunCommandHandler;
import serp.project.pmcore.application.optimization.command.update.BatchUpdateOptimizationRunItemDecisionsCommand;
import serp.project.pmcore.application.optimization.command.update.BatchUpdateOptimizationRunItemDecisionsCommandHandler;
import serp.project.pmcore.application.optimization.query.get.GetOptimizationRunQueryHandler;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.optimization.dto.request.BatchUpdateOptimizationRunItemDecisionsRequest;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OptimizationRunControllerTest {
    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long RUN_ID = 20L;
    private static final Long WORK_ITEM_ID = 30L;

    @Mock
    private AuthUtils authUtils;
    @Mock
    private ResponseUtils responseUtils;
    @Mock
    private GenerateOptimizationRunCommandHandler generateOptimizationRunCommandHandler;
    @Mock
    private GetOptimizationRunQueryHandler getOptimizationRunQueryHandler;
    @Mock
    private BatchUpdateOptimizationRunItemDecisionsCommandHandler batchUpdateOptimizationRunItemDecisionsCommandHandler;
    @Mock
    private ApplyOptimizationRunCommandHandler applyOptimizationRunCommandHandler;
    @Mock
    private DiscardOptimizationRunCommandHandler discardOptimizationRunCommandHandler;

    private OptimizationRunController controller;

    @BeforeEach
    void setUp() {
        controller = new OptimizationRunController(
                authUtils,
                responseUtils,
                generateOptimizationRunCommandHandler,
                getOptimizationRunQueryHandler,
                batchUpdateOptimizationRunItemDecisionsCommandHandler,
                applyOptimizationRunCommandHandler,
                discardOptimizationRunCommandHandler
        );
    }

    @Test
    void updateOptimizationRunItemDecisionsShouldDelegateBatchCommand() {
        OptimizationRunReviewView view = OptimizationRunReviewView.builder().id(RUN_ID).build();
        GeneralResponse<OptimizationRunReviewView> body = GeneralResponse.<OptimizationRunReviewView>builder()
                .data(view)
                .build();
        BatchUpdateOptimizationRunItemDecisionsRequest request = request();
        when(authUtils.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(authUtils.getCurrentTenantId()).thenReturn(Optional.of(TENANT_ID));
        when(batchUpdateOptimizationRunItemDecisionsCommandHandler.handle(org.mockito.ArgumentMatchers.any()))
                .thenReturn(view);
        when(responseUtils.success(view)).thenReturn(body);

        ResponseEntity<GeneralResponse<OptimizationRunReviewView>> response =
                controller.updateOptimizationRunItemDecisions(PROJECT_ID, RUN_ID, request);

        assertSame(body, response.getBody());
        assertEquals(200, response.getStatusCode().value());
        ArgumentCaptor<BatchUpdateOptimizationRunItemDecisionsCommand> commandCaptor =
                ArgumentCaptor.forClass(BatchUpdateOptimizationRunItemDecisionsCommand.class);
        verify(batchUpdateOptimizationRunItemDecisionsCommandHandler).handle(commandCaptor.capture());
        BatchUpdateOptimizationRunItemDecisionsCommand command = commandCaptor.getValue();
        assertEquals(TENANT_ID, command.tenantId());
        assertEquals(USER_ID, command.userId());
        assertEquals(PROJECT_ID, command.projectId());
        assertEquals(RUN_ID, command.runId());
        assertEquals(1, command.items().size());
        assertEquals(WORK_ITEM_ID, command.items().get(0).workItemId());
        assertEquals(OptimizationDecision.ACCEPTED, command.items().get(0).assignmentDecision());
    }

    private BatchUpdateOptimizationRunItemDecisionsRequest request() {
        BatchUpdateOptimizationRunItemDecisionsRequest request =
                new BatchUpdateOptimizationRunItemDecisionsRequest();
        BatchUpdateOptimizationRunItemDecisionsRequest.ItemDecisionRequest item =
                new BatchUpdateOptimizationRunItemDecisionsRequest.ItemDecisionRequest();
        item.setWorkItemId(WORK_ITEM_ID);
        item.setAssignmentDecision(OptimizationDecision.ACCEPTED);
        request.setItems(List.of(item));
        return request;
    }
}
