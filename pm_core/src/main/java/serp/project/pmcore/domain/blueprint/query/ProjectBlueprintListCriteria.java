/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.blueprint.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.pagination.SearchPageCriteria;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProjectBlueprintListCriteria extends SearchPageCriteria {
    private String projectTypeKey;
    private Boolean isSystem;

    public String getProjectTypeKey() {
        if (projectTypeKey == null) {
            return null;
        }
        String trimmed = projectTypeKey.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public String getSortBy() {
        String sortBy = getRawSortBy();
        if (sortBy == null || sortBy.isBlank()) {
            return "name";
        }
        return sortBy.trim();
    }

    public String getSortDirection() {
        String sortDirection = getRawSortDirection();
        if (sortDirection == null || sortDirection.isBlank()) {
            return "ASC";
        }
        return sortDirection.trim();
    }
}
