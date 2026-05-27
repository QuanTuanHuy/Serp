/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.settings;

import java.util.List;

public record PrioritySettingsOverviewView(
        List<PriorityView> priorities,
        List<PrioritySchemeView> prioritySchemes
) {
    public record PriorityView(
            Long id,
            Long tenantId,
            String priorityKey,
            String name,
            String description,
            String iconUrl,
            String color,
            Integer sequence,
            boolean isSystem,
            boolean readOnly,
            List<SchemeRefView> relatedSchemes,
            Long createdAt,
            Long createdBy,
            Long updatedAt,
            Long updatedBy
    ) {
    }

    public record PrioritySchemeView(
            Long id,
            Long tenantId,
            String name,
            String description,
            Long defaultPriorityId,
            boolean isSystem,
            boolean readOnly,
            List<PriorityOptionView> priorities,
            List<ProjectRefView> spaces,
            Long createdAt,
            Long createdBy,
            Long updatedAt,
            Long updatedBy
    ) {
    }

    public record SchemeRefView(
            Long id,
            String name,
            boolean isSystem
    ) {
    }

    public record PriorityOptionView(
            Long id,
            String priorityKey,
            String name,
            String description,
            String iconUrl,
            String color,
            Integer sequence,
            boolean isDefault
    ) {
    }

    public record ProjectRefView(
            Long id,
            String key,
            String name
    ) {
    }
}
