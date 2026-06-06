/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.settings;

import java.util.List;

public record ProjectSettingsOverviewView(
        ProjectView project,
        PeopleView people,
        ComponentsView components,
        List<SchemeBindingView> schemes
) {
    public record ProjectView(
            Long id,
            String key,
            String name,
            String description,
            String url,
            String projectTypeKey,
            Boolean isArchived,
            Long archivedAt,
            Long leadUserId,
            String leadUserName,
            CategoryView category
    ) {
    }

    public record CategoryView(Long id, String name) {
    }

    public record PeopleView(
            Long leadUserId,
            String leadUserName,
            long memberCount,
            long roleCount
    ) {
    }

    public record ComponentsView(
            long totalCount,
            List<ComponentPreviewView> preview
    ) {
    }

    public record ComponentPreviewView(
            Long id,
            String name,
            String description,
            Long issueCount,
            String assigneeType
    ) {
    }

    public record SchemeBindingView(
            String type,
            String label,
            Long schemeId,
            String schemeName,
            String globalSection,
            boolean available
    ) {
    }
}
