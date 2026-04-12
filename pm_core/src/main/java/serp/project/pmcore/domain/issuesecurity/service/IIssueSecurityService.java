package serp.project.pmcore.domain.issuesecurity.service;

import serp.project.pmcore.domain.issuesecurity.dto.IssueSecurityAccessContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;

public interface IIssueSecurityService {
    void checkSecurityAccessIfNeeded(IssueSecurityAccessContext accessContext,
                                     ProjectPermissionEvaluationContext actorContext);

    Long resolveDefaultSecurityLevelId(Long issueSecuritySchemeId, Long tenantId);

    Long validateSecurityLevelId(Long issueSecuritySchemeId, Long requestedSecurityLevelId, Long tenantId);
}
