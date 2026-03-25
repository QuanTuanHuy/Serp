/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service;

import serp.project.pmcore.domain.dto.project.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.entity.project.ProjectEntity;

public interface IProjectPermissionEvaluationService {
    boolean hasPermission(ProjectEntity project,
                          ProjectPermissionEvaluationContext context,
                          String permissionKey);

    void checkPermission(ProjectEntity project,
                         ProjectPermissionEvaluationContext context,
                         String permissionKey);
}
