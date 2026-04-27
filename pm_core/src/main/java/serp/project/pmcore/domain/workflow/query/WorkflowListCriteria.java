/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.query;

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
public class WorkflowListCriteria extends SearchPageCriteria {
    private Boolean isActive;
    private Boolean isSystem;

    public String getSortBy() {
        String sortBy = getRawSortBy();
        if (sortBy == null || sortBy.isBlank()) {
            return "created_at";
        }
        return sortBy.trim();
    }

    public String getSortDirection() {
        String sortDirection = getRawSortDirection();
        if (sortDirection == null || sortDirection.isBlank()) {
            return "DESC";
        }
        return sortDirection.trim();
    }
}
