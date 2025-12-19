package serp.project.logistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {

    private long totalItems;

    private long totalPages;

    private long currentPage;

    private List<T> items;


    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber() + 1)
                .items(page.getContent())
                .build();
    }
}
