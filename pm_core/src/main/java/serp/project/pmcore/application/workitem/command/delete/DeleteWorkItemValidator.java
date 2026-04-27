/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.delete;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteWorkItemValidator {

    private final IProjectService projectService;

    public void validateCommand(DeleteWorkItemCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Delete work item command is required");
        }
        if (command.projectId() == null || command.projectId() <= 0) {
            throw new IllegalArgumentException("projectId must be positive");
        }
        if (command.workItemId() == null || command.workItemId() <= 0) {
            throw new IllegalArgumentException("workItemId must be positive");
        }
        if (command.tenantId() == null || command.tenantId() <= 0) {
            throw new IllegalArgumentException("tenantId must be positive");
        }
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
    }

    public ProjectEntity validateWritableProject(Long projectId, Long tenantId) {
        ProjectEntity project = projectService.getProjectById(projectId, tenantId);
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            log.error("[DeleteWorkItemValidator] Project {} is archived", projectId);
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
        return project;
    }
}
