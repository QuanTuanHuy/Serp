/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.dto;

import serp.project.pmcore.domain.project.entity.ProjectEntity;

public record ProjectPermissionSubject(
        Long projectId,
        Long tenantId,
        Long leadUserId,
        Long permissionSchemeId
) {
    public static ProjectPermissionSubject from(ProjectEntity project) {
        return new ProjectPermissionSubject(
                project.getId(),
                project.getTenantId(),
                project.getLeadUserId(),
                project.getPermissionSchemeId()
        );
    }
}
