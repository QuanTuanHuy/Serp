/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service;

import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;

public interface IProjectPermissionEvaluationService {
    boolean hasPermission(ProjectEntity project,
                          ProjectPermissionEvaluationContext context,
                          String permissionKey);

    void checkPermission(ProjectEntity project,
                         ProjectPermissionEvaluationContext context,
                         String permissionKey);
}
