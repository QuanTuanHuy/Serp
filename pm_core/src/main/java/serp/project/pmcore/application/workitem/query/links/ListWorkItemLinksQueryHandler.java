/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.links;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.dto.WorkItemLinkProjection;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListWorkItemLinksQueryHandler implements IQueryHandler<ListWorkItemLinksQuery, List<WorkItemLinkView>> {

    private final IWorkItemReadPort workItemReadPort;
    private final IProjectService projectService;
    private final IProjectPermissionEvaluationService permissionEvaluationService;

    @Override
    @Transactional(readOnly = true)
    public List<WorkItemLinkView> handle(ListWorkItemLinksQuery query) {
        ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
        permissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                buildEvaluationContext(query.userId(), query.groupKeys()),
                ProjectPermissionKeys.BROWSE_PROJECTS
        );
        workItemReadPort.getWorkItemDetailById(query.projectId(), query.workItemId(), query.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.workItem(query.workItemId()));

        List<WorkItemLinkProjection> links = workItemReadPort.listLinksByWorkItemId(query.workItemId(), query.tenantId());
        return links.stream()
                .map(link -> WorkItemLinkView.from(query.workItemId(), link))
                .toList();
    }

    private ProjectPermissionEvaluationContext buildEvaluationContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys == null ? Set.of() : groupKeys)
                .build();
    }
}
