/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.update;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewAssembler;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.application.optimization.support.OptimizationRunGuard;
import serp.project.pmcore.application.optimization.support.OptimizationRunWarningAuditService;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationChangeScope;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;
import serp.project.pmcore.domain.optimization.service.IOptimizationProjectModelBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchUpdateOptimizationRunItemDecisionsCommandHandlerTest {
    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long RUN_ID = 20L;
    private static final Long WORK_ITEM_ID = 30L;
    private static final Long SECOND_WORK_ITEM_ID = 31L;

    @Mock
    private OptimizationRunGuard optimizationRunGuard;
    @Mock
    private IOptimizationRunItemPort optimizationRunItemPort;
    @Mock
    private IOptimizationRunWarningPort optimizationRunWarningPort;
    @Mock
    private IOptimizationProjectModelBuilder optimizationProjectModelBuilder;
    @Mock
    private OptimizationRunReviewAssembler optimizationRunReviewAssembler;
    @Mock
    private OptimizationRunWarningAuditService optimizationRunWarningAuditService;

    private BatchUpdateOptimizationRunItemDecisionsCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new BatchUpdateOptimizationRunItemDecisionsCommandHandler(
                optimizationRunGuard,
                optimizationRunItemPort,
                optimizationRunWarningPort,
                optimizationProjectModelBuilder,
                optimizationRunReviewAssembler,
                optimizationRunWarningAuditService
        );
    }

    @Test
    void handleShouldPersistMultipleAcceptedDecisions() {
        OptimizationRunEntity run = run();
        OptimizationRunItemEntity firstItem = runItem(WORK_ITEM_ID);
        OptimizationRunItemEntity secondItem = runItem(SECOND_WORK_ITEM_ID);
        OptimizationRunReviewView view = OptimizationRunReviewView.builder().id(RUN_ID).build();
        when(optimizationRunGuard.requireRunInProject(TENANT_ID, PROJECT_ID, RUN_ID)).thenReturn(run);
        when(optimizationRunItemPort.listByRunId(TENANT_ID, RUN_ID))
                .thenReturn(List.of(firstItem, secondItem));
        when(optimizationRunWarningPort.listByRunId(TENANT_ID, RUN_ID)).thenReturn(List.of());
        when(optimizationRunReviewAssembler.toView(any(), any(), any())).thenReturn(view);

        OptimizationRunReviewView result = handler.handle(new BatchUpdateOptimizationRunItemDecisionsCommand(
                TENANT_ID,
                USER_ID,
                PROJECT_ID,
                RUN_ID,
                List.of(
                        new BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision(
                                WORK_ITEM_ID,
                                OptimizationDecision.ACCEPTED,
                                OptimizationDecision.REJECTED,
                                null,
                                null,
                                null
                        ),
                        new BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision(
                                SECOND_WORK_ITEM_ID,
                                OptimizationDecision.ACCEPTED,
                                OptimizationDecision.ACCEPTED,
                                null,
                                null,
                                null
                        )
                )
        ));

        assertEquals(RUN_ID, result.getId());
        assertEquals(OptimizationDecision.ACCEPTED, firstItem.getAssignmentDecision());
        assertEquals(OptimizationDecision.REJECTED, firstItem.getScheduleDecision());
        assertEquals(OptimizationDecision.ACCEPTED, secondItem.getAssignmentDecision());
        assertEquals(OptimizationDecision.ACCEPTED, secondItem.getScheduleDecision());
        verify(optimizationRunItemPort).saveAll(List.of(firstItem, secondItem));
    }

    @Test
    void handleShouldRejectDuplicateWorkItemIds() {
        BatchUpdateOptimizationRunItemDecisionsCommand command = new BatchUpdateOptimizationRunItemDecisionsCommand(
                TENANT_ID,
                USER_ID,
                PROJECT_ID,
                RUN_ID,
                List.of(
                        new BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision(
                                WORK_ITEM_ID,
                                OptimizationDecision.ACCEPTED,
                                null,
                                null,
                                null,
                                null
                        ),
                        new BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision(
                                WORK_ITEM_ID,
                                OptimizationDecision.REJECTED,
                                null,
                                null,
                                null,
                                null
                        )
                )
        );

        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));

        verifyNoInteractions(optimizationRunGuard, optimizationRunItemPort);
    }

    @Test
    void handleShouldRejectInvalidOverridePayload() {
        OptimizationRunEntity run = run();
        OptimizationRunItemEntity item = runItem(WORK_ITEM_ID);
        when(optimizationRunGuard.requireRunInProject(TENANT_ID, PROJECT_ID, RUN_ID)).thenReturn(run);
        when(optimizationRunItemPort.listByRunId(TENANT_ID, RUN_ID)).thenReturn(List.of(item));

        BatchUpdateOptimizationRunItemDecisionsCommand command = new BatchUpdateOptimizationRunItemDecisionsCommand(
                TENANT_ID,
                USER_ID,
                PROJECT_ID,
                RUN_ID,
                List.of(new BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision(
                        WORK_ITEM_ID,
                        OptimizationDecision.OVERRIDDEN,
                        null,
                        null,
                        null,
                        null
                ))
        );

        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));

        verify(optimizationRunWarningAuditService).recordInvalidOverrideWarning(
                TENANT_ID,
                USER_ID,
                RUN_ID,
                WORK_ITEM_ID,
                "overrideAssigneeId is required when assignmentDecision is OVERRIDDEN"
        );
        verify(optimizationRunItemPort, never()).saveAll(any());
    }

    private OptimizationRunEntity run() {
        return OptimizationRunEntity.builder()
                .id(RUN_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .status(OptimizationRunStatus.GENERATED)
                .planningStart(1_714_876_800_000L)
                .planningEnd(1_715_308_800_000L)
                .objective(OptimizationObjective.BALANCED_WORKLOAD.name())
                .changeScope(OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE.name())
                .algorithmKey(OptimizationAlgorithmKeys.GREEDY_BALANCED)
                .build();
    }

    private OptimizationRunItemEntity runItem(Long workItemId) {
        return OptimizationRunItemEntity.builder()
                .id(workItemId + 100L)
                .tenantId(TENANT_ID)
                .runId(RUN_ID)
                .projectId(PROJECT_ID)
                .workItemId(workItemId)
                .assignmentDecision(OptimizationDecision.PENDING)
                .scheduleDecision(OptimizationDecision.PENDING)
                .currentPlannedStart(1_714_876_800_000L)
                .currentPlannedEnd(1_714_963_200_000L)
                .suggestedPlannedStart(1_714_963_200_000L)
                .suggestedPlannedEnd(1_715_049_600_000L)
                .build();
    }
}
