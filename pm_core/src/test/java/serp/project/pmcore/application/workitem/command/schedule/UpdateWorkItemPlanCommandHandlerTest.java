/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanAllocationEntity;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.enums.WorkItemPlanSource;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanAllocationPort;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanPort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateWorkItemPlanCommandHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long WORK_ITEM_ID = 30L;
    private static final Long ASSIGNEE_ID = 40L;
    private static final Long START = 1_714_876_800_000L;
    private static final Long END = 1_714_905_600_000L;

    @Mock
    private IProjectService projectService;
    @Mock
    private IWorkItemReadPort workItemReadPort;
    @Mock
    private IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    @Mock
    private IIssueSecurityService issueSecurityService;
    @Mock
    private IWorkItemPlanPort workItemPlanPort;
    @Mock
    private IWorkItemPlanAllocationPort workItemPlanAllocationPort;

    private UpdateWorkItemPlanCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateWorkItemPlanCommandHandler(
                projectService,
                workItemReadPort,
                workItemAuthorizationSupportService,
                issueSecurityService,
                workItemPlanPort,
                workItemPlanAllocationPort
        );
    }

    @Test
    void handleShouldSaveManualLockedPlanAndReplaceAllocations() {
        ProjectPermissionEvaluationContext actorContext = actorContext();
        stubAuthorizedProjectAndWorkItem(actorContext);
        when(workItemPlanPort.upsertActivePlan(any())).thenReturn(savedPlan(true));
        when(workItemPlanAllocationPort.replaceForPlan(eq(TENANT_ID), eq(700L), any()))
                .thenReturn(List.of(savedAllocation()));

        UpdateWorkItemPlanResult result = handler.handle(command(true, allocation(START, START + 3_600_000L)));

        ArgumentCaptor<WorkItemPlanEntity> planCaptor = ArgumentCaptor.forClass(WorkItemPlanEntity.class);
        verify(workItemPlanPort).upsertActivePlan(planCaptor.capture());
        WorkItemPlanEntity plan = planCaptor.getValue();
        assertEquals(TENANT_ID, plan.getTenantId());
        assertEquals(PROJECT_ID, plan.getProjectId());
        assertEquals(WORK_ITEM_ID, plan.getWorkItemId());
        assertEquals(START, plan.getPlannedStart());
        assertEquals(END, plan.getPlannedEnd());
        assertEquals(WorkItemPlanSource.MANUAL, plan.getSource());
        assertNull(plan.getSourceRunId());
        assertEquals(true, plan.getLocked());

        ArgumentCaptor<List<WorkItemPlanAllocationEntity>> allocationCaptor = ArgumentCaptor.forClass(List.class);
        verify(workItemPlanAllocationPort).replaceForPlan(eq(TENANT_ID), eq(700L), allocationCaptor.capture());
        assertEquals(1, allocationCaptor.getValue().size());
        WorkItemPlanAllocationEntity allocation = allocationCaptor.getValue().getFirst();
        assertEquals(TENANT_ID, allocation.getTenantId());
        assertEquals(PROJECT_ID, allocation.getProjectId());
        assertEquals(700L, allocation.getWorkItemPlanId());
        assertEquals(WORK_ITEM_ID, allocation.getWorkItemId());
        assertEquals(ASSIGNEE_ID, allocation.getAssigneeId());
        assertEquals(START, allocation.getStartTime());
        assertEquals(START + 3_600_000L, allocation.getEndTime());
        assertEquals(3_600_000L, allocation.getEffortMillis());
        assertEquals(WorkItemPlanSource.MANUAL, allocation.getSource());
        assertNull(allocation.getSourceRunId());
        assertNull(allocation.getSourceRunItemId());

        assertEquals(700L, result.id());
        assertEquals(WORK_ITEM_ID, result.workItemId());
        assertEquals(PROJECT_ID, result.projectId());
        assertEquals(WorkItemPlanSource.MANUAL.name(), result.source());
        assertEquals(true, result.locked());
        assertEquals(1, result.allocations().size());
        assertEquals(701L, result.allocations().getFirst().id());
    }

    @Test
    void handleShouldDefaultLockedToTrue() {
        stubAuthorizedProjectAndWorkItem(actorContext());
        when(workItemPlanPort.upsertActivePlan(any())).thenReturn(savedPlan(true));
        when(workItemPlanAllocationPort.replaceForPlan(eq(TENANT_ID), eq(700L), any())).thenReturn(List.of());

        handler.handle(command(null, allocation(START, START + 3_600_000L)));

        ArgumentCaptor<WorkItemPlanEntity> planCaptor = ArgumentCaptor.forClass(WorkItemPlanEntity.class);
        verify(workItemPlanPort).upsertActivePlan(planCaptor.capture());
        assertEquals(true, planCaptor.getValue().getLocked());
    }

    @Test
    void handleShouldRejectInvalidPlanRange() {
        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> handler.handle(new UpdateWorkItemPlanCommand(
                        TENANT_ID,
                        USER_ID,
                        PROJECT_ID,
                        WORK_ITEM_ID,
                        END,
                        START,
                        true,
                        List.of(allocation(START, START + 3_600_000L)),
                        Set.of("pm")
                ))
        );

        assertEquals(DomainErrorCode.WORK_ITEM_SCHEDULE_INVALID, exception.getErrorCode());
        verify(workItemPlanPort, never()).upsertActivePlan(any());
    }

    @Test
    void handleShouldRejectAllocationOutsidePlanRange() {
        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> handler.handle(command(true, allocation(START - 1_000L, START + 3_600_000L)))
        );

        assertEquals(DomainErrorCode.WORK_ITEM_SCHEDULE_INVALID, exception.getErrorCode());
        verify(workItemPlanPort, never()).upsertActivePlan(any());
    }

    private void stubAuthorizedProjectAndWorkItem(ProjectPermissionEvaluationContext actorContext) {
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project());
        when(workItemReadPort.getWorkItemById(WORK_ITEM_ID, TENANT_ID)).thenReturn(Optional.of(workItem()));
        when(workItemAuthorizationSupportService.buildActorContext(USER_ID, Set.of("pm"), 101L, ASSIGNEE_ID))
                .thenReturn(actorContext);
    }

    private UpdateWorkItemPlanCommand command(Boolean locked, UpdateWorkItemPlanAllocationCommand allocation) {
        return new UpdateWorkItemPlanCommand(
                TENANT_ID,
                USER_ID,
                PROJECT_ID,
                WORK_ITEM_ID,
                START,
                END,
                locked,
                List.of(allocation),
                Set.of("pm")
        );
    }

    private UpdateWorkItemPlanAllocationCommand allocation(Long start, Long end) {
        return new UpdateWorkItemPlanAllocationCommand(
                ASSIGNEE_ID,
                start,
                end,
                end - start
        );
    }

    private ProjectPermissionEvaluationContext actorContext() {
        return ProjectPermissionEvaluationContext.builder()
                .userId(USER_ID)
                .groupKeys(Set.of("pm"))
                .reporterUserId(101L)
                .assigneeUserId(ASSIGNEE_ID)
                .build();
    }

    private ProjectEntity project() {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .leadUserId(USER_ID)
                .isArchived(false)
                .build();
    }

    private WorkItemEntity workItem() {
        return WorkItemEntity.builder()
                .id(WORK_ITEM_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .reporterId(101L)
                .assigneeId(ASSIGNEE_ID)
                .build();
    }

    private WorkItemPlanEntity savedPlan(Boolean locked) {
        return WorkItemPlanEntity.builder()
                .id(700L)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .workItemId(WORK_ITEM_ID)
                .plannedStart(START)
                .plannedEnd(END)
                .source(WorkItemPlanSource.MANUAL)
                .locked(locked)
                .build();
    }

    private WorkItemPlanAllocationEntity savedAllocation() {
        return WorkItemPlanAllocationEntity.builder()
                .id(701L)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .workItemPlanId(700L)
                .workItemId(WORK_ITEM_ID)
                .assigneeId(ASSIGNEE_ID)
                .startTime(START)
                .endTime(START + 3_600_000L)
                .effortMillis(3_600_000L)
                .source(WorkItemPlanSource.MANUAL)
                .build();
    }
}
