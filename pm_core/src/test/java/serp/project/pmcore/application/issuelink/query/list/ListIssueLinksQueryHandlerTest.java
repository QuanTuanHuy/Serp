/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink.query.list;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.issuelink.IssueLinkListItemView;
import serp.project.pmcore.application.issuelink.IssueLinkValidator;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkDetailEntity;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkAuthorizationService;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListIssueLinksQueryHandlerTest {

    @Mock
    private IssueLinkValidator issueLinkValidator;
    @Mock
    private IProjectService projectService;
    @Mock
    private IWorkItemService workItemService;
    @Mock
    private IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    @Mock
    private IIssueLinkAuthorizationService issueLinkAuthorizationService;
    @Mock
    private IIssueLinkService issueLinkService;

    private ListIssueLinksQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListIssueLinksQueryHandler(
                issueLinkValidator,
                projectService,
                workItemService,
                workItemAuthorizationSupportService,
                issueLinkAuthorizationService,
                issueLinkService
        );
    }

    @Test
    void handleShouldReturnInwardAndOutwardViews() {
        ListIssueLinksQuery query = new ListIssueLinksQuery(10L, 20L, 1L, 99L, Set.of("dev-team"));
        ProjectEntity project = ProjectEntity.builder().id(10L).tenantId(1L).build();
        WorkItemEntity owner = WorkItemEntity.builder()
                .id(20L)
                .tenantId(1L)
                .projectId(10L)
                .reporterId(77L)
                .assigneeId(88L)
                .build();
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(99L)
                .groupKeys(Set.of("dev-team"))
                .reporterUserId(77L)
                .assigneeUserId(88L)
                .build();
        IssueLinkDetailEntity outward = IssueLinkDetailEntity.builder()
                .linkId(1L)
                .sourceId(20L)
                .targetId(30L)
                .linkTypeId(40L)
                .linkTypeName("Blocks")
                .outwardDescription("blocks")
                .inwardDescription("is blocked by")
                .relatedWorkItemId(30L)
                .relatedProjectId(11L)
                .relatedWorkItemKey("OPS-7")
                .relatedWorkItemSummary("Target item")
                .relatedIssueTypeId(301L)
                .relatedIssueTypeName("Task")
                .relatedStatusId(201L)
                .relatedStatusName("In Progress")
                .build();
        IssueLinkDetailEntity inward = IssueLinkDetailEntity.builder()
                .linkId(2L)
                .sourceId(31L)
                .targetId(20L)
                .linkTypeId(41L)
                .linkTypeName("Clones")
                .outwardDescription("clones")
                .inwardDescription("is cloned by")
                .relatedWorkItemId(31L)
                .relatedProjectId(12L)
                .relatedWorkItemKey("OPS-8")
                .relatedWorkItemSummary("Source item")
                .relatedIssueTypeId(302L)
                .relatedIssueTypeName("Bug")
                .relatedStatusId(200L)
                .relatedStatusName("Backlog")
                .build();

        when(projectService.getProjectById(10L, 1L)).thenReturn(project);
        when(workItemService.getWorkItemById(20L, 1L)).thenReturn(owner);
        when(workItemAuthorizationSupportService.buildActorContext(99L, Set.of("dev-team"), 77L, 88L))
                .thenReturn(actorContext);
        when(issueLinkService.listByWorkItemId(1L, 20L)).thenReturn(List.of(outward, inward));

        List<IssueLinkListItemView> result = handler.handle(query);

        assertEquals(2, result.size());
        assertEquals("OUTWARD", result.get(0).direction());
        assertEquals("blocks", result.get(0).description());
        assertEquals("INWARD", result.get(1).direction());
        assertEquals("is cloned by", result.get(1).description());
        verify(issueLinkAuthorizationService).checkReadAccess(project, owner, actorContext);
    }
}
