/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.worklog.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.pagination.PageCriteria;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorklogListCriteria extends PageCriteria {
    private Long workItemId;
    private Long authorId;

    @Override
    public Integer getPageSize() {
        Integer pageSize = super.getPageSize();
        return pageSize == null ? 20 : pageSize;
    }

    @Override
    public String getSortBy() {
        String sortBy = super.getSortBy();
        return sortBy == null || sortBy.isBlank() ? "start_date" : sortBy.trim();
    }

    @Override
    public String getSortDirection() {
        String sortDirection = super.getSortDirection();
        return sortDirection == null || sortDirection.isBlank()
                ? "DESC"
                : sortDirection.trim();
    }
}
