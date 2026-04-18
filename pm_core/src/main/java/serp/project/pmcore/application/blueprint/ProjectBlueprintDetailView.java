/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint;

import serp.project.pmcore.domain.blueprint.entity.BlueprintSchemeDefaultEntity;
import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;

import java.util.List;

public record ProjectBlueprintDetailView(
        Long id,
        Long tenantId,
        String name,
        String description,
        String projectTypeKey,
        String avatarUrl,
        Boolean isSystem,
        boolean readOnly,
        List<BlueprintSchemeDefaultView> schemeDefaults,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static ProjectBlueprintDetailView from(ProjectBlueprintEntity entity,
                                                  List<BlueprintSchemeDefaultEntity> defaults) {
        return new ProjectBlueprintDetailView(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getTypeKey(),
                entity.getAvatarUrl(),
                entity.getIsSystem(),
                entity.isSystem(),
                defaults.stream().map(BlueprintSchemeDefaultView::from).toList(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
