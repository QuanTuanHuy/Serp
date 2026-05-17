/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.apply;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewAssembler;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.application.optimization.support.OptimizationRunGuard;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationApplyStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.enums.WorkItemPlanSource;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanPort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplyOptimizationRunCommandHandlerTest {
    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long RUN_ID = 20L;
    private static final Long WORK_ITEM_ID = 30L;
    private static final Long SNAPSHOT_UPDATED_AT = 1_714_876_800_000L;

    @Mock
    private IOptimizationRunPort optimizationRunPort;
    @Mock
    private IOptimizationRunItemPort optimizationRunItemPort;
    @Mock
    private IOptimizationRunWarningPort optimizationRunWarningPort;
    @Mock
    private IWorkItemPlanPort workItemPlanPort;
    @Mock
    private IWorkItemReadPort workItemReadPort;
    @Mock
    private IWorkItemService workItemService;
    @Mock
    private IProjectService projectService;
    @Mock
    private IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    @Mock
    private IIssueSecurityService issueSecurityService;
    @Mock
    private OptimizationRunGuard optimizationRunGuard;
    @Mock
    private OptimizationRunReviewAssembler optimizationRunReviewAssembler;
    @Mock
    private JsonUtils jsonUtils;

    private ApplyOptimizationRunCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApplyOptimizationRunCommandHandler(
                optimizationRunPort,
                optimizationRunItemPort,
                optimizationRunWarningPort,
                workItemPlanPort,
                workItemReadPort,
                workItemService,
                projectService,
                workItemAuthorizationSupportService,
                issueSecurityService,
                optimizationRunGuard,
                optimizationRunReviewAssembler,
                jsonUtils
        );
    }

    @Test
    void handleShouldUpsertSchedulePlanForAcceptedItem() {
        OptimizationRunEntity run = run();
        OptimizationRunItemEntity item = runItem();
        WorkItemEntity workItem = workItem(SNAPSHOT_UPDATED_AT, 100L);
        ProjectEntity project = project();
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(USER_ID)
                .groupKeys(Set.of("pm"))
                .reporterUserId(101L)
                .assigneeUserId(100L)
                .build();

        stubCommon(run, item, project, workItem);
        when(workItemAuthorizationSupportService.buildActorContext(USER_ID, Set.of("pm"), 101L, 100L)).thenReturn(actorContext);
        when(optimizationRunReviewAssembler.toView(any(), any(), any()))
                .thenReturn(OptimizationRunReviewView.builder().id(RUN_ID).build());

        handler.handle(new ApplyOptimizationRunCommand(
                TENANT_ID, USER_ID, PROJECT_ID, RUN_ID, false, true, List.of(WORK_ITEM_ID), Set.of("pm")));

        ArgumentCaptor<WorkItemPlanEntity> planCaptor = ArgumentCaptor.forClass(WorkItemPlanEntity.class);
        verify(workItemPlanPort).upsertActivePlan(planCaptor.capture());
        assertEquals(WorkItemPlanSource.OPTIMIZATION, planCaptor.getValue().getSource());
        assertEquals(RUN_ID, planCaptor.getValue().getSourceRunId());

        ArgumentCaptor<List<OptimizationRunItemEntity>> itemCaptor = ArgumentCaptor.forClass(List.class);
        verify(optimizationRunItemPort).saveAll(itemCaptor.capture());
        assertEquals(OptimizationApplyStatus.APPLIED, itemCaptor.getValue().get(0).getScheduleApplyStatus());
        verify(optimizationRunPort).save(any());
    }

    @Test
    void handleShouldSkipStaleAssignment() {
        OptimizationRunEntity run = run();
        OptimizationRunItemEntity item = runItem();
        item.setAssignmentDecision(OptimizationDecision.ACCEPTED);
        item.setSuggestedAssigneeId(200L);
        WorkItemEntity workItem = workItem(SNAPSHOT_UPDATED_AT + 1_000L, 100L);
        ProjectEntity project = project();

        stubCommon(run, item, project, workItem);
        when(jsonUtils.toJson(any())).thenReturn("{}");
        when(optimizationRunReviewAssembler.toView(any(), any(), any()))
                .thenReturn(OptimizationRunReviewView.builder().id(RUN_ID).build());

        handler.handle(new ApplyOptimizationRunCommand(
                TENANT_ID, USER_ID, PROJECT_ID, RUN_ID, true, false, List.of(WORK_ITEM_ID), Set.of()));

        ArgumentCaptor<List<OptimizationRunItemEntity>> itemCaptor = ArgumentCaptor.forClass(List.class);
        verify(optimizationRunItemPort).saveAll(itemCaptor.capture());
        assertEquals(OptimizationApplyStatus.SKIPPED, itemCaptor.getValue().get(0).getAssignmentApplyStatus());
        assertEquals("STALE_ITEM", itemCaptor.getValue().get(0).getAssignmentSkippedReason());
        verify(workItemService, never()).updateWorkItem(any(), any());
        verify(optimizationRunWarningPort).saveAll(any());
    }

    private void stubCommon(OptimizationRunEntity run,
                            OptimizationRunItemEntity item,
                            ProjectEntity project,
                            WorkItemEntity workItem) {
        when(optimizationRunGuard.requireRunInProject(TENANT_ID, PROJECT_ID, RUN_ID)).thenReturn(run);
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(optimizationRunItemPort.listByRunId(TENANT_ID, RUN_ID)).thenReturn(List.of(item));
        when(workItemReadPort.listActiveByWorkItemIds(TENANT_ID, List.of(WORK_ITEM_ID))).thenReturn(List.of(workItem));
        when(workItemPlanPort.listActivePlansByWorkItemIds(TENANT_ID, List.of(WORK_ITEM_ID))).thenReturn(List.of());
        when(optimizationRunWarningPort.listByRunId(TENANT_ID, RUN_ID)).thenReturn(List.of());
    }

    private OptimizationRunEntity run() {
        return OptimizationRunEntity.builder()
                .id(RUN_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .status(OptimizationRunStatus.GENERATED)
                .build();
    }

    private OptimizationRunItemEntity runItem() {
        return OptimizationRunItemEntity.builder()
                .id(100L)
                .tenantId(TENANT_ID)
                .runId(RUN_ID)
                .projectId(PROJECT_ID)
                .workItemId(WORK_ITEM_ID)
                .workItemUpdatedAtSnapshot(SNAPSHOT_UPDATED_AT)
                .currentAssigneeId(100L)
                .suggestedAssigneeId(100L)
                .suggestedPlannedStart(1_714_876_800_000L)
                .suggestedPlannedEnd(1_714_905_600_000L)
                .assignmentDecision(OptimizationDecision.REJECTED)
                .scheduleDecision(OptimizationDecision.ACCEPTED)
                .assignmentApplyStatus(OptimizationApplyStatus.NOT_APPLIED)
                .scheduleApplyStatus(OptimizationApplyStatus.NOT_APPLIED)
                .build();
    }

    private ProjectEntity project() {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .leadUserId(2L)
                .isArchived(false)
                .build();
    }

    private WorkItemEntity workItem(Long updatedAt, Long assigneeId) {
        return WorkItemEntity.builder()
                .id(WORK_ITEM_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .reporterId(101L)
                .assigneeId(assigneeId)
                .updatedAt(updatedAt)
                .build();
    }
}
