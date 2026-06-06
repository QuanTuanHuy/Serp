/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.permission.query;

import serp.project.pmcore.application.project.permission.ProjectPermissionDefinitionView;
import serp.project.pmcore.application.project.permission.ProjectPermissionGrantView;
import serp.project.pmcore.application.project.permission.ProjectPermissionSchemeView;

import java.util.List;

public record ProjectPermissionSettingsView(
        ProjectPermissionSchemeView scheme,
        List<ProjectPermissionDefinitionView> permissions,
        List<ProjectPermissionGrantView> grants
) {
}
