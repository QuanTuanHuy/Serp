/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink.command.create;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.issuelink.IssueLinkValidator;
import serp.project.pmcore.application.issuelink.IssueLinkView;
import serp.project.pmcore.application.issuelink.command.IssueLinkOutboxPublisher;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkEntity;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkAuthorizationService;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkService;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkTypeService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateIssueLinkCommandHandlerTest {

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
    private IIssueLinkTypeService issueLinkTypeService;
    @Mock
    private IIssueLinkService issueLinkService;
    @Mock
    private IssueLinkOutboxPublisher issueLinkOutboxPublisher;

    private CreateIssueLinkCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateIssueLinkCommandHandler(
                issueLinkValidator,
                projectService,
                workItemService,
                workItemAuthorizationSupportService,
                issueLinkAuthorizationService,
                issueLinkTypeService,
                issueLinkService,
                issueLinkOutboxPublisher
        );
    }

    @Test
    void handleShouldCreateCrossProjectIssueLinkAndPublishOutbox() {
        CreateIssueLinkCommand command = new CreateIssueLinkCommand(
                10L,
                20L,
                30L,
                40L,
                1L,
                99L,
                Set.of("dev-team")
        );
        ProjectEntity sourceProject = ProjectEntity.builder().id(10L).tenantId(1L).build();
        WorkItemEntity sourceWorkItem = WorkItemEntity.builder()
                .id(20L)
                .tenantId(1L)
                .projectId(10L)
                .reporterId(77L)
                .assigneeId(88L)
                .build();
        WorkItemEntity targetWorkItem = WorkItemEntity.builder()
                .id(30L)
                .tenantId(1L)
                .projectId(11L)
                .build();
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(99L)
                .groupKeys(Set.of("dev-team"))
                .reporterUserId(77L)
                .assigneeUserId(88L)
                .build();
        IssueLinkEntity created = IssueLinkEntity.builder()
                .id(50L)
                .tenantId(1L)
                .sourceId(20L)
                .targetId(30L)
                .linkTypeId(40L)
                .createdBy(99L)
                .build();

        when(projectService.getProjectById(10L, 1L)).thenReturn(sourceProject);
        when(workItemService.getWorkItemById(20L, 1L)).thenReturn(sourceWorkItem);
        when(workItemService.getWorkItemById(30L, 1L)).thenReturn(targetWorkItem);
        when(workItemAuthorizationSupportService.buildActorContext(99L, Set.of("dev-team"), 77L, 88L))
                .thenReturn(actorContext);
        when(issueLinkService.create(any(IssueLinkEntity.class), eq(1L), eq(99L))).thenReturn(created);

        IssueLinkView result = handler.handle(command);

        assertEquals(50L, result.id());
        assertEquals(20L, result.sourceId());
        assertEquals(30L, result.targetId());
        verify(issueLinkAuthorizationService).checkWriteAccess(sourceProject, sourceWorkItem, actorContext);
        verify(issueLinkTypeService).getVisibleById(40L, 1L);
        verify(issueLinkOutboxPublisher).publishIssueLinkCreated(eq(1L), any());
    }
}
