package serp.project.pmcore.domain.issuesecurity.service;

import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

public interface IIssueSecurityService {
    void checkSecurityAccessIfNeeded(ProjectEntity project,
                                     WorkItemEntity workItem,
                                     ProjectPermissionEvaluationContext actorContext,
                                     Long tenantId);
}
