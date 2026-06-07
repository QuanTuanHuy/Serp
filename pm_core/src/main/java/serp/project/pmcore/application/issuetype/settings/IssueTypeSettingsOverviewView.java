/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.settings;

import java.util.List;

public record IssueTypeSettingsOverviewView(
        List<WorkTypeView> workTypes,
        List<WorkTypeSchemeView> workTypeSchemes
) {
    public record WorkTypeView(
            Long id,
            Long tenantId,
            String typeKey,
            String name,
            String description,
            String iconUrl,
            Integer hierarchyLevel,
            boolean isSystem,
            boolean readOnly,
            List<SchemeRefView> relatedSchemes,
            Long createdAt,
            Long createdBy,
            Long updatedAt,
            Long updatedBy
    ) {
    }

    public record WorkTypeSchemeView(
            Long id,
            Long tenantId,
            String name,
            String description,
            Long defaultIssueTypeId,
            boolean isSystem,
            boolean readOnly,
            List<WorkTypeOptionView> workTypes,
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

    public record WorkTypeOptionView(
            Long id,
            String typeKey,
            String name,
            String description,
            String iconUrl,
            Integer hierarchyLevel,
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
