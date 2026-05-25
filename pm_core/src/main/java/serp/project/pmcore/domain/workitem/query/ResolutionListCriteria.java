/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.query;

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
public class ResolutionListCriteria extends SearchPageCriteria {
    private Boolean isSystem;

    public String getSortBy() {
        String sortBy = getRawSortBy();
        if (sortBy == null || sortBy.isBlank()) {
            return "sequence";
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
