/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workitem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.workitem.command.assign.AssignWorkItemCommandHandler;
import serp.project.pmcore.application.workitem.command.comment.CreateWorkItemCommentCommandHandler;
import serp.project.pmcore.application.workitem.command.comment.DeleteWorkItemCommentCommandHandler;
import serp.project.pmcore.application.workitem.command.comment.UpdateWorkItemCommentCommandHandler;
import serp.project.pmcore.application.workitem.command.component.ManageWorkItemComponentsCommandHandler;
import serp.project.pmcore.application.workitem.command.component.RemoveWorkItemComponentCommandHandler;
import serp.project.pmcore.application.workitem.command.create.CreateWorkItemCommandHandler;
import serp.project.pmcore.application.workitem.command.delete.DeleteWorkItemCommandHandler;
import serp.project.pmcore.application.workitem.command.schedule.UpdateWorkItemPlanAllocationCommand;
import serp.project.pmcore.application.workitem.command.schedule.UpdateWorkItemPlanCommand;
import serp.project.pmcore.application.workitem.command.schedule.UpdateWorkItemPlanCommandHandler;
import serp.project.pmcore.application.workitem.command.schedule.UpdateWorkItemPlanResult;
import serp.project.pmcore.application.workitem.command.transition.TransitionWorkItemCommandHandler;
import serp.project.pmcore.application.workitem.command.update.UpdateWorkItemCommandHandler;
import serp.project.pmcore.application.workitem.query.activity.ListWorkItemActivitiesQueryHandler;
import serp.project.pmcore.application.workitem.query.board.ListWorkItemBoardQueryHandler;
import serp.project.pmcore.application.workitem.query.children.ListWorkItemChildrenQueryHandler;
import serp.project.pmcore.application.workitem.query.comment.ListWorkItemCommentsQueryHandler;
import serp.project.pmcore.application.workitem.query.component.ListWorkItemComponentsQueryHandler;
import serp.project.pmcore.application.workitem.query.createmeta.GetWorkItemCreateMetaQueryHandler;
import serp.project.pmcore.application.workitem.query.get.GetWorkItemByIdQueryHandler;
import serp.project.pmcore.application.workitem.query.search.SearchWorkItemsQueryHandler;
import serp.project.pmcore.application.workitem.query.transition.ListWorkItemTransitionsQueryHandler;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;
import serp.project.pmcore.ui.rest.workitem.dto.request.UpdateWorkItemPlanRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemControllerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long WORK_ITEM_ID = 30L;
    private static final Long ASSIGNEE_ID = 40L;
    private static final Long START = 1_714_876_800_000L;
    private static final Long END = 1_714_905_600_000L;

    @Mock
    private AuthUtils authUtils;
    @Mock
    private ResponseUtils responseUtils;
    @Mock
    private AssignWorkItemCommandHandler assignWorkItemCommandHandler;
    @Mock
    private CreateWorkItemCommandHandler createWorkItemCommandHandler;
    @Mock
    private DeleteWorkItemCommandHandler deleteWorkItemCommandHandler;
    @Mock
    private UpdateWorkItemCommandHandler updateWorkItemCommandHandler;
    @Mock
    private UpdateWorkItemPlanCommandHandler updateWorkItemPlanCommandHandler;
    @Mock
    private TransitionWorkItemCommandHandler transitionWorkItemCommandHandler;
    @Mock
    private ManageWorkItemComponentsCommandHandler manageWorkItemComponentsCommandHandler;
    @Mock
    private RemoveWorkItemComponentCommandHandler removeWorkItemComponentCommandHandler;
    @Mock
    private CreateWorkItemCommentCommandHandler createWorkItemCommentCommandHandler;
    @Mock
    private UpdateWorkItemCommentCommandHandler updateWorkItemCommentCommandHandler;
    @Mock
    private DeleteWorkItemCommentCommandHandler deleteWorkItemCommentCommandHandler;
    @Mock
    private SearchWorkItemsQueryHandler searchWorkItemsQueryHandler;
    @Mock
    private ListWorkItemBoardQueryHandler listWorkItemBoardQueryHandler;
    @Mock
    private GetWorkItemCreateMetaQueryHandler getWorkItemCreateMetaQueryHandler;
    @Mock
    private GetWorkItemByIdQueryHandler getWorkItemByIdQueryHandler;
    @Mock
    private ListWorkItemChildrenQueryHandler listWorkItemChildrenQueryHandler;
    @Mock
    private ListWorkItemCommentsQueryHandler listWorkItemCommentsQueryHandler;
    @Mock
    private ListWorkItemActivitiesQueryHandler listWorkItemActivitiesQueryHandler;
    @Mock
    private ListWorkItemComponentsQueryHandler listWorkItemComponentsQueryHandler;
    @Mock
    private ListWorkItemTransitionsQueryHandler listWorkItemTransitionsQueryHandler;

    private WorkItemController controller;

    @BeforeEach
    void setUp() {
        controller = new WorkItemController(
                authUtils,
                responseUtils,
                assignWorkItemCommandHandler,
                createWorkItemCommandHandler,
                deleteWorkItemCommandHandler,
                updateWorkItemCommandHandler,
                updateWorkItemPlanCommandHandler,
                transitionWorkItemCommandHandler,
                manageWorkItemComponentsCommandHandler,
                removeWorkItemComponentCommandHandler,
                createWorkItemCommentCommandHandler,
                updateWorkItemCommentCommandHandler,
                deleteWorkItemCommentCommandHandler,
                searchWorkItemsQueryHandler,
                listWorkItemBoardQueryHandler,
                getWorkItemCreateMetaQueryHandler,
                getWorkItemByIdQueryHandler,
                listWorkItemChildrenQueryHandler,
                listWorkItemCommentsQueryHandler,
                listWorkItemActivitiesQueryHandler,
                listWorkItemComponentsQueryHandler,
                listWorkItemTransitionsQueryHandler
        );
    }

    @Test
    void updateWorkItemSchedulePlanShouldDelegateCommand() {
        UpdateWorkItemPlanRequest request = request();
        UpdateWorkItemPlanResult result = UpdateWorkItemPlanResult.builder()
                .id(700L)
                .workItemId(WORK_ITEM_ID)
                .projectId(PROJECT_ID)
                .allocations(List.of())
                .build();
        GeneralResponse<UpdateWorkItemPlanResult> body = GeneralResponse.<UpdateWorkItemPlanResult>builder()
                .data(result)
                .build();
        when(authUtils.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(authUtils.getCurrentTenantId()).thenReturn(Optional.of(TENANT_ID));
        when(authUtils.getCurrentGroups()).thenReturn(Set.of("pm"));
        when(updateWorkItemPlanCommandHandler.handle(any())).thenReturn(result);
        when(responseUtils.success(result)).thenReturn(body);

        ResponseEntity<GeneralResponse<UpdateWorkItemPlanResult>> response =
                controller.updateWorkItemSchedulePlan(PROJECT_ID, WORK_ITEM_ID, request);

        assertSame(body, response.getBody());
        assertEquals(200, response.getStatusCode().value());
        ArgumentCaptor<UpdateWorkItemPlanCommand> commandCaptor =
                ArgumentCaptor.forClass(UpdateWorkItemPlanCommand.class);
        verify(updateWorkItemPlanCommandHandler).handle(commandCaptor.capture());
        UpdateWorkItemPlanCommand command = commandCaptor.getValue();
        assertEquals(TENANT_ID, command.tenantId());
        assertEquals(USER_ID, command.userId());
        assertEquals(PROJECT_ID, command.projectId());
        assertEquals(WORK_ITEM_ID, command.workItemId());
        assertEquals(START, command.plannedStart());
        assertEquals(END, command.plannedEnd());
        assertEquals(true, command.locked());
        assertEquals(Set.of("pm"), command.groupKeys());
        assertEquals(1, command.allocations().size());
        UpdateWorkItemPlanAllocationCommand allocation = command.allocations().getFirst();
        assertEquals(ASSIGNEE_ID, allocation.assigneeId());
        assertEquals(START, allocation.start());
        assertEquals(START + 3_600_000L, allocation.end());
        assertEquals(3_600_000L, allocation.effortMillis());
    }

    private UpdateWorkItemPlanRequest request() {
        UpdateWorkItemPlanRequest request = new UpdateWorkItemPlanRequest();
        request.setPlannedStart(START);
        request.setPlannedEnd(END);
        request.setLocked(true);
        UpdateWorkItemPlanRequest.AllocationRequest allocation =
                new UpdateWorkItemPlanRequest.AllocationRequest();
        allocation.setAssigneeId(ASSIGNEE_ID);
        allocation.setStart(START);
        allocation.setEnd(START + 3_600_000L);
        allocation.setEffortMillis(3_600_000L);
        request.setAllocations(List.of(allocation));
        return request;
    }
}
