/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.port.store.IProjectCategoryPort;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProjectDetailViewFactory {
    private final IProjectCategoryPort projectCategoryPort;

    public ProjectDetailView toView(ProjectEntity project,
                                    Long tenantId,
                                    Set<ProjectExpandOption> expand) {
        CategorySummaryView category = expand.contains(ProjectExpandOption.CATEGORY)
                ? resolveCategory(project.getCategoryId(), tenantId)
                : null;
        return ProjectDetailView.from(project, category);
    }

    private CategorySummaryView resolveCategory(Long categoryId, Long tenantId) {
        if (categoryId == null) {
            return null;
        }

        return projectCategoryPort.getCategoryById(categoryId, tenantId)
                .map(category -> new CategorySummaryView(category.getId(), category.getName()))
                .orElse(null);
    }
}
