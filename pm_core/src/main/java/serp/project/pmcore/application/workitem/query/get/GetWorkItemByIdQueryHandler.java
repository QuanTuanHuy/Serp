/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.get;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetWorkItemByIdQueryHandler implements IQueryHandler<GetWorkItemByIdQuery, WorkItemDetailView> {

    private final IWorkItemReadPort workItemReadPort;

    private final WorkItemDetailAssembler workItemDetailAssembler;

    private final IProjectService projectService;
    private final IProjectPermissionEvaluationService permissionEvaluationService;

    @Override
    @Transactional(readOnly = true)
    public WorkItemDetailView handle(GetWorkItemByIdQuery query) {

        ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
        var evaluationContext = ProjectPermissionEvaluationContext.builder()
                .userId(query.userId())
                .build();
        permissionEvaluationService.checkPermission(
                project,
                evaluationContext,
                ProjectPermissionKeys.BROWSE_PROJECTS
        );

        WorkItemEntity workItem = workItemReadPort.getWorkItemById(query.workItemId(), query.tenantId())
                .orElseThrow(() -> {
                    log.error("[GetWorkItemByIdQueryHandler] Work item not found: workItemId={}", query.workItemId());
                    return ResourceNotFoundException.workItem(query.workItemId());
                });
        if (!workItem.getProjectId().equals(query.projectId())) {
            log.error("[GetWorkItemByIdQueryHandler] Work item {} does not belong to project {}", query.workItemId(), query.projectId());
            throw ResourceNotFoundException.workItem(query.workItemId());
        }


        return workItemDetailAssembler.toView(workItem);
    }
}
