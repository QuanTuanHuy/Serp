/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.dto.PageRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BaseFilterRequest {
    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer size = 20;

    private String sortBy;

    @Builder.Default
    private String sortDirection = "DESC";

    public void normalize() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 20;
        }
        if (size > 200) {
            size = 200;
        }
        if (sortDirection == null ||
                (!"ASC".equalsIgnoreCase(sortDirection) && !"DESC".equalsIgnoreCase(sortDirection))) {
            sortDirection = "DESC";
        }
    }

    public PageRequest toPageRequest() {
        normalize();
        return PageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
