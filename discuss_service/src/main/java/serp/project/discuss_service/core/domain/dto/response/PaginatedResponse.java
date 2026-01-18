/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Generic paginated response DTO
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedResponse<T> {
    
    private List<T> items;
    private Integer page;
    private Integer pageSize;
    private Long totalItems;
    private Integer totalPages;
    private Boolean hasNext;
    private Boolean hasPrevious;

    public static <T> PaginatedResponse<T> of(List<T> items, int page, int pageSize, long totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        return PaginatedResponse.<T>builder()
                .items(items)
                .page(page)
                .pageSize(pageSize)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }

    public static <T> PaginatedResponse<T> empty(int page, int pageSize) {
        return PaginatedResponse.<T>builder()
                .items(List.of())
                .page(page)
                .pageSize(pageSize)
                .totalItems(0L)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}
