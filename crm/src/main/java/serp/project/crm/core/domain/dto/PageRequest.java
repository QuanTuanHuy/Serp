/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.constant.Constants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageRequest {

    @Builder.Default
    private Integer page = Constants.Pagination.DEFAULT_PAGE;

    @Builder.Default
    private Integer size = Constants.Pagination.DEFAULT_SIZE;

    private String sortBy;

    @Builder.Default
    private String sortDirection = "DESC";

    public void validate() {
        if (page == null || page < 1) {
            page = Constants.Pagination.DEFAULT_PAGE;
        }

        if (size == null || size < Constants.Pagination.MIN_SIZE) {
            size = Constants.Pagination.DEFAULT_SIZE;
        }

        if (size > Constants.Pagination.MAX_SIZE) {
            size = Constants.Pagination.MAX_SIZE;
        }

        if (sortDirection == null ||
                (!sortDirection.equalsIgnoreCase("ASC") && !sortDirection.equalsIgnoreCase("DESC"))) {
            sortDirection = "ASC";
        }
    }

    public int getOffset() {
        return (page - 1) * size;
    }
}
