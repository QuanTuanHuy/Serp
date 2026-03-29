/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.create;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.project.dto.ProjectSchemeBindings;
import serp.project.pmcore.domain.shared.enums.ProvisioningMode;

public record CreateProjectCommand(
        String name,
        String key,
        String description,
        String projectTypeKey,
        Long leadUserId,
        Long categoryId,
        Long blueprintId,
        String url,
        Long avatarId,
        Long issueTypeSchemeId,
        Long workflowSchemeId,
        Long fieldConfigSchemeId,
        Long issueTypeScreenSchemeId,
        Long permissionSchemeId,
        Long notificationSchemeId,
        Long prioritySchemeId,
        Long issueSecuritySchemeId,
        ProvisioningMode provisioningMode,
        Long tenantId,
        Long userId
) implements ICommand<CreateProjectResult> {

    public ProjectSchemeBindings toSchemeBindings() {
        return ProjectSchemeBindings.builder()
                .issueTypeSchemeId(issueTypeSchemeId)
                .workflowSchemeId(workflowSchemeId)
                .fieldConfigSchemeId(fieldConfigSchemeId)
                .issueTypeScreenSchemeId(issueTypeScreenSchemeId)
                .permissionSchemeId(permissionSchemeId)
                .notificationSchemeId(notificationSchemeId)
                .prioritySchemeId(prioritySchemeId)
                .issueSecuritySchemeId(issueSecuritySchemeId)
                .build();
    }
}
