/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.pagination;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@SuperBuilder
public class PageCriteria {
    private Integer page;
    private Integer pageSize;
    private String sortBy;
    private String sortDirection;

    public Integer getPage() {
        return page != null ? page : 0;
    }

    public Integer getPageSize() {
        return pageSize != null ? pageSize : 10;
    }

    public String getSortBy() {
        return sortBy != null ? sortBy : "id";
    }

    public String getSortDirection() {
        if (sortDirection != null
                && (sortDirection.equalsIgnoreCase("asc") || sortDirection.equalsIgnoreCase("desc"))) {
            return sortDirection;
        }
        return "desc";
    }
}
