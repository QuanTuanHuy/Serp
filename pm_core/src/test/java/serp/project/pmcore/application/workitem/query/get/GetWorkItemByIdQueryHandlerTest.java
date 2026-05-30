/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.get;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.shared.dto.user.UserSummary;
import serp.project.pmcore.application.workitem.WorkItemComponentView;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.enums.WorkItemPlanSource;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanPort;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.workitem.dto.WorkItemDetailProjection;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemCommentReadPort;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.user.service.IUserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWorkItemByIdQueryHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 99L;
    private static final Long PROJECT_ID = 10L;
    private static final Long WORK_ITEM_ID = 101L;

    @Mock
    private IWorkItemReadPort workItemReadPort;
    @Mock
    private IWorkItemCommentReadPort workItemCommentReadPort;
    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectPermissionEvaluationService permissionEvaluationService;
    @Mock
    private IUserService userService;
    @Mock
    private IWorkItemPlanPort workItemPlanPort;

    private GetWorkItemByIdQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetWorkItemByIdQueryHandler(
                workItemReadPort,
                workItemCommentReadPort,
                workItemPlanPort,
                projectService,
                permissionEvaluationService,
                userService
        );
    }

    @Test
    void handleShouldExposeCurrentPlanSummaryInDetailView() {
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .build());
        when(workItemReadPort.getWorkItemDetailById(PROJECT_ID, WORK_ITEM_ID, TENANT_ID)).thenReturn(Optional.of(
                WorkItemDetailProjectionFixture.workItemDetail(PROJECT_ID, WORK_ITEM_ID)
        ));
        when(workItemReadPort.getActiveComponentsByWorkItemId(WORK_ITEM_ID, TENANT_ID)).thenReturn(List.of());
        when(workItemReadPort.countActiveChildrenByParentId(PROJECT_ID, WORK_ITEM_ID, TENANT_ID)).thenReturn(0L);
        when(workItemReadPort.countDoneChildrenByParentId(PROJECT_ID, WORK_ITEM_ID, TENANT_ID)).thenReturn(0L);
        when(workItemReadPort.countActiveLinksByWorkItemId(WORK_ITEM_ID, TENANT_ID)).thenReturn(0L);
        when(workItemCommentReadPort.countByWorkItemId(WORK_ITEM_ID, TENANT_ID)).thenReturn(0L);
        when(workItemPlanPort.getActivePlanByWorkItemId(TENANT_ID, WORK_ITEM_ID)).thenReturn(Optional.of(
                WorkItemPlanEntity.builder()
                        .tenantId(TENANT_ID)
                        .projectId(PROJECT_ID)
                        .workItemId(WORK_ITEM_ID)
                        .plannedStart(1_700_000_000_000L)
                        .plannedEnd(1_710_000_000_000L)
                        .source(WorkItemPlanSource.OPTIMIZATION)
                        .sourceRunId(501L)
                        .locked(false)
                        .build()
        ));

        GetWorkItemByIdQuery query = new GetWorkItemByIdQuery(
                TENANT_ID,
                USER_ID,
                PROJECT_ID,
                WORK_ITEM_ID
        );

        WorkItemDetailView result = handler.handle(query);

        assertNotNull(result.schedule());
        assertEquals(1_700_000_000_000L, result.schedule().plannedStart());
        assertEquals(1_710_000_000_000L, result.schedule().plannedEnd());
        assertEquals("OPTIMIZATION", result.schedule().source());
        assertEquals(Boolean.FALSE, result.schedule().locked());
        assertEquals(501L, result.schedule().sourceRunId());
        verify(workItemPlanPort).getActivePlanByWorkItemId(TENANT_ID, WORK_ITEM_ID);
        verify(permissionEvaluationService).checkPermission(any(), any(), any());
    }

    static final class WorkItemDetailProjectionFixture {
        private WorkItemDetailProjectionFixture() {
        }

        static WorkItemDetailProjection workItemDetail(Long projectId, Long workItemId) {
            return new WorkItemDetailProjection() {
                @Override
                public Long getId() {
                    return workItemId;
                }

                @Override
                public Long getProjectId() {
                    return projectId;
                }

                @Override
                public Long getIssueNo() {
                    return 7L;
                }

                @Override
                public String getKey() {
                    return "SERP-101";
                }

                @Override
                public String getSummary() {
                    return "Build schedule summary";
                }

                @Override
                public String getDescription() {
                    return null;
                }

                @Override
                public Long getResolutionId() {
                    return null;
                }

                @Override
                public Long getParentId() {
                    return null;
                }

                @Override
                public String getParentKey() {
                    return null;
                }

                @Override
                public String getParentSummary() {
                    return null;
                }

                @Override
                public Long getSecurityLevelId() {
                    return null;
                }

                @Override
                public java.time.Instant getStartDate() {
                    return null;
                }

                @Override
                public java.time.Instant getDueDate() {
                    return null;
                }

                @Override
                public String getRank() {
                    return "0|a";
                }

                @Override
                public Long getTimeOriginalEstimate() {
                    return null;
                }

                @Override
                public Long getTimeRemainingEstimate() {
                    return null;
                }

                @Override
                public Long getTimeSpent() {
                    return null;
                }

                @Override
                public Long getAssigneeId() {
                    return null;
                }

                @Override
                public Long getReporterId() {
                    return null;
                }

                @Override
                public java.time.Instant getCreatedAt() {
                    return null;
                }

                @Override
                public Long getCreatedBy() {
                    return null;
                }

                @Override
                public java.time.Instant getUpdatedAt() {
                    return null;
                }

                @Override
                public Long getUpdatedBy() {
                    return null;
                }

                @Override
                public Long getIssueTypeId() {
                    return null;
                }

                @Override
                public String getIssueTypeName() {
                    return null;
                }

                @Override
                public String getIssueTypeIconUrl() {
                    return null;
                }

                @Override
                public Integer getIssueTypeHierarchyLevel() {
                    return null;
                }

                @Override
                public Long getPriorityId() {
                    return null;
                }

                @Override
                public String getPriorityName() {
                    return null;
                }

                @Override
                public String getPriorityColor() {
                    return null;
                }

                @Override
                public Long getStatusId() {
                    return null;
                }

                @Override
                public String getStatusName() {
                    return null;
                }

                @Override
                public String getStatusKey() {
                    return null;
                }

                @Override
                public Long getWorkflowStepId() {
                    return null;
                }

                @Override
                public String getWorkflowStepName() {
                    return null;
                }
            };
        }
    }
}
