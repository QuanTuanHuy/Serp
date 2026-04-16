/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCategoryListCriteria {
    private String search;
    private Integer page;
    private Integer pageSize;
    private String sortBy;
    private String sortDirection;

    public int getPage() {
        return page != null ? page : 0;
    }

    public int getPageSize() {
        return pageSize != null ? pageSize : 10;
    }

    public String getSearch() {
        if (search == null) {
            return null;
        }
        String trimmed = search.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public String getSortBy() {
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }
        return sortBy.trim();
    }

    public String getSortDirection() {
        if (sortDirection == null || sortDirection.isBlank()) {
            return "DESC";
        }
        return sortDirection.trim();
    }
}
