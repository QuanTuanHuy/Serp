/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuelink.IssueLinkListItemView;
import serp.project.pmcore.application.issuelink.IssueLinkValidator;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkAuthorizationService;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ListIssueLinksQueryHandler implements IQueryHandler<ListIssueLinksQuery, List<IssueLinkListItemView>> {

    private final IssueLinkValidator issueLinkValidator;
    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IIssueLinkAuthorizationService issueLinkAuthorizationService;
    private final IIssueLinkService issueLinkService;

    @Override
    @Transactional(readOnly = true)
    public List<IssueLinkListItemView> handle(ListIssueLinksQuery query) {
        issueLinkValidator.validateProjectScopedRequest(
                query.projectId(),
                query.workItemId(),
                query.tenantId(),
                query.userId()
        );

        ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
        WorkItemEntity ownerWorkItem = workItemService.getWorkItemById(query.workItemId(), query.tenantId());
        ensureWorkItemBelongsToProject(ownerWorkItem, project);

        ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService.buildActorContext(
                query.userId(),
                query.groupKeys(),
                ownerWorkItem.getReporterId(),
                ownerWorkItem.getAssigneeId()
        );
        issueLinkAuthorizationService.checkReadAccess(project, ownerWorkItem, actorContext);

        return issueLinkService.listByWorkItemId(query.tenantId(), ownerWorkItem.getId()).stream()
                .map(item -> IssueLinkListItemView.from(item, ownerWorkItem.getId()))
                .toList();
    }

    private void ensureWorkItemBelongsToProject(WorkItemEntity workItem, ProjectEntity project) {
        if (!Objects.equals(workItem.getProjectId(), project.getId())) {
            throw ResourceNotFoundException.workItem(workItem.getId());
        }
    }
}
