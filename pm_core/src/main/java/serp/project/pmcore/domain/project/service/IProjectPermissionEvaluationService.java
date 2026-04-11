/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service;

import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;

public interface IProjectPermissionEvaluationService {
    boolean hasPermission(ProjectPermissionSubject subject,
                          ProjectPermissionEvaluationContext context,
                          String permissionKey);

    void checkPermission(ProjectPermissionSubject subject,
                         ProjectPermissionEvaluationContext context,
                         String permissionKey);
}
