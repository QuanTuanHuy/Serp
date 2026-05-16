/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.update;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewAssembler;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;
import serp.project.pmcore.domain.optimization.service.IOptimizationProjectModelBuilder;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateOptimizationRunItemDecisionCommandHandlerTest {
    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long RUN_ID = 20L;
    private static final Long WORK_ITEM_ID = 30L;

    @Mock
    private IOptimizationRunPort optimizationRunPort;
    @Mock
    private IOptimizationRunItemPort optimizationRunItemPort;
    @Mock
    private IOptimizationRunWarningPort optimizationRunWarningPort;
    @Mock
    private IOptimizationProjectModelBuilder optimizationProjectModelBuilder;
    @Mock
    private OptimizationRunReviewAssembler optimizationRunReviewAssembler;
    @Mock
    private JsonUtils jsonUtils;

    private UpdateOptimizationRunItemDecisionCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateOptimizationRunItemDecisionCommandHandler(
                optimizationRunPort,
                optimizationRunItemPort,
                optimizationRunWarningPort,
                optimizationProjectModelBuilder,
                optimizationRunReviewAssembler,
                jsonUtils
        );
    }

    @Test
    void handleShouldPersistAcceptedDecisions() {
        OptimizationRunEntity run = run();
        OptimizationRunItemEntity item = runItem();
        OptimizationRunReviewView view = OptimizationRunReviewView.builder().id(RUN_ID).build();
        when(optimizationRunPort.getById(TENANT_ID, RUN_ID)).thenReturn(Optional.of(run));
        when(optimizationRunItemPort.listByRunId(TENANT_ID, RUN_ID)).thenReturn(List.of(item));
        when(optimizationRunWarningPort.listByRunId(TENANT_ID, RUN_ID)).thenReturn(List.of());
        when(optimizationRunReviewAssembler.toView(any(), any(), any())).thenReturn(view);

        OptimizationRunReviewView result = handler.handle(new UpdateOptimizationRunItemDecisionCommand(
                TENANT_ID,
                USER_ID,
                PROJECT_ID,
                RUN_ID,
                WORK_ITEM_ID,
                OptimizationDecision.ACCEPTED,
                OptimizationDecision.REJECTED,
                null,
                null,
                null
        ));

        assertEquals(RUN_ID, result.getId());
        ArgumentCaptor<OptimizationRunItemEntity> itemCaptor = ArgumentCaptor.forClass(OptimizationRunItemEntity.class);
        verify(optimizationRunItemPort).save(itemCaptor.capture());
        assertEquals(OptimizationDecision.ACCEPTED, itemCaptor.getValue().getAssignmentDecision());
        assertEquals(OptimizationDecision.REJECTED, itemCaptor.getValue().getScheduleDecision());
    }

    private OptimizationRunEntity run() {
        return OptimizationRunEntity.builder()
                .id(RUN_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .status(OptimizationRunStatus.GENERATED)
                .planningStart(1_714_876_800_000L)
                .planningEnd(1_715_308_800_000L)
                .mode("BALANCED_WORKLOAD")
                .allowReassignment(true)
                .allowScheduleChanges(true)
                .build();
    }

    private OptimizationRunItemEntity runItem() {
        return OptimizationRunItemEntity.builder()
                .id(100L)
                .tenantId(TENANT_ID)
                .runId(RUN_ID)
                .projectId(PROJECT_ID)
                .workItemId(WORK_ITEM_ID)
                .assignmentDecision(OptimizationDecision.PENDING)
                .scheduleDecision(OptimizationDecision.PENDING)
                .build();
    }
}
