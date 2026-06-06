/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.query.get;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.optimization.support.OptimizationRunGuard;
import serp.project.pmcore.application.shared.dto.user.UserSummary;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunWarningEntity;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.user.service.IUserService;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOptimizationRunQueryHandlerTest {
    private static final Long TENANT_ID = 1L;
    private static final Long PROJECT_ID = 100L;
    private static final Long RUN_ID = 500L;

    @Mock
    private OptimizationRunGuard optimizationRunGuard;
    @Mock
    private IOptimizationRunItemPort optimizationRunItemPort;
    @Mock
    private IOptimizationRunWarningPort optimizationRunWarningPort;
    @Mock
    private OptimizationRunReviewAssembler optimizationRunReviewAssembler;
    @Mock
    private IWorkItemReadPort workItemReadPort;
    @Mock
    private IUserService userService;

    private GetOptimizationRunQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetOptimizationRunQueryHandler(
                optimizationRunGuard,
                optimizationRunItemPort,
                optimizationRunWarningPort,
                optimizationRunReviewAssembler,
                workItemReadPort,
                userService
        );
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void handleShouldEnrichRunItemsWithWorkItemAndAssigneeSummaries() {
        OptimizationRunEntity run = OptimizationRunEntity.builder()
                .id(RUN_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .build();
        OptimizationRunItemEntity item = OptimizationRunItemEntity.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .runId(RUN_ID)
                .workItemId(10L)
                .currentAssigneeId(100L)
                .suggestedAssigneeId(200L)
                .overrideAssigneeId(300L)
                .build();
        List<OptimizationRunItemEntity> items = List.of(item);
        List<OptimizationRunWarningEntity> warnings = List.of();
        WorkItemEntity workItem = WorkItemEntity.builder()
                .id(10L)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .key("SERP-10")
                .summary("Review generated optimization")
                .issueTypeName("Task")
                .statusName("In Progress")
                .priorityName("High")
                .build();
        OptimizationRunReviewView expectedView = OptimizationRunReviewView.builder()
                .id(RUN_ID)
                .build();

        when(optimizationRunGuard.requireRunInProject(TENANT_ID, PROJECT_ID, RUN_ID))
                .thenReturn(run);
        when(optimizationRunItemPort.listByRunId(TENANT_ID, RUN_ID))
                .thenReturn(items);
        when(optimizationRunWarningPort.listByRunId(TENANT_ID, RUN_ID))
                .thenReturn(warnings);
        when(workItemReadPort.listActiveByWorkItemIds(TENANT_ID, List.of(10L)))
                .thenReturn(List.of(workItem));
        when(userService.getUserProfilesByIds(List.of(100L, 200L, 300L)))
                .thenReturn(List.of(
                        user(100L, "Current", "User", "/current.png"),
                        user(200L, "Suggested", "User", "/suggested.png"),
                        user(300L, "Override", "User", "/override.png")
                ));

        ArgumentCaptor<Map> workItemsCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map> usersCaptor = ArgumentCaptor.forClass(Map.class);
        when(optimizationRunReviewAssembler.toView(
                eq(run),
                eq(items),
                eq(warnings),
                workItemsCaptor.capture(),
                usersCaptor.capture()
        )).thenReturn(expectedView);

        OptimizationRunReviewView actualView = handler.handle(new GetOptimizationRunQuery(
                TENANT_ID,
                PROJECT_ID,
                RUN_ID
        ));

        assertEquals(expectedView, actualView);
        Map<Long, OptimizationWorkItemSummaryView> workItemsById = workItemsCaptor.getValue();
        Map<Long, UserSummary> usersById = usersCaptor.getValue();
        assertEquals("SERP-10", workItemsById.get(10L).key());
        assertEquals("Review generated optimization", workItemsById.get(10L).summary());
        assertEquals("Current User", usersById.get(100L).displayName());
        assertEquals("Suggested User", usersById.get(200L).displayName());
        assertEquals("Override User", usersById.get(300L).displayName());
        verify(workItemReadPort).listActiveByWorkItemIds(TENANT_ID, List.of(10L));
        verify(userService).getUserProfilesByIds(List.of(100L, 200L, 300L));
    }

    private UserProfileDto user(Long id, String firstName, String lastName, String avatarUrl) {
        return UserProfileDto.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .avatarUrl(avatarUrl)
                .build();
    }
}
