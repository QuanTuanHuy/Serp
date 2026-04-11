/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.update.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.screen.service.IScreenService;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;
import serp.project.pmcore.domain.workitem.service.IWorkItemFieldResolver;

@Service
@RequiredArgsConstructor
public class UpdateWorkItemFieldRulesResolver {

    private final IScreenService screenService;
    private final IWorkItemFieldResolver workItemFieldResolver;

    public WorkItemFieldRules resolveEditFieldRules(ProjectEntity project, Long issueTypeId, Long tenantId) {
        Long editScreenId = screenService.resolveScreenIdForOperation(
                project,
                issueTypeId,
                WorkItemFieldConstants.EDIT_OPERATION_KEY,
                tenantId
        );

        return workItemFieldResolver.resolveFieldRules(project, issueTypeId, editScreenId, tenantId);
    }
}
