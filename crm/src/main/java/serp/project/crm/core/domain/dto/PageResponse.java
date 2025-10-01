/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {

    private List<T> content;

    private PaginationInfo pagination;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationInfo {
        private Integer page;
        private Integer size;
        private Long totalElements;
        private Integer totalPages;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }

    public static <T> PageResponse<T> of(List<T> content, PageRequest pageRequest, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageRequest.getSize());

        PaginationInfo pagination = PaginationInfo.builder()
                .page(pageRequest.getPage())
                .size(pageRequest.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(pageRequest.getPage() < totalPages)
                .hasPrevious(pageRequest.getPage() > 1)
                .build();

        return PageResponse.<T>builder()
                .content(content)
                .pagination(pagination)
                .build();
    }
}
