/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.request;

import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@SuperBuilder
public class BaseGetParams {
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
        return sortDirection != null ? sortDirection : "desc";
    }
}
