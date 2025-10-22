/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.dto.request.BaseGetParams;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaginationUtils {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;

    public Pageable getPageable(BaseGetParams params) {
        int page = (params.getPage() != null && params.getPage() >= 0) ? params.getPage() : DEFAULT_PAGE;
        int size = (params.getPageSize() != null && params.getPageSize() > 0) ? params.getPageSize()
                : DEFAULT_PAGE_SIZE;

        String sortBy = (!DataUtils.isNullOrEmpty(params.getSortBy()) ? params.getSortBy() : "id");

        Sort.Direction direction = (!DataUtils.isNullOrEmpty(params.getSortDirection()) ||
                params.getSortDirection().equalsIgnoreCase("desc"))
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }

    public Map<String, Object> getResponse(long totalItems, Object items) {
        return Map.of(
                "totalItems", totalItems,
                "items", items);
    }

    public Map<String, Object> getResponse(long totalItems, int page, int pageSize, Object items) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        return Map.of(
                "totalItems", totalItems,
                "totalPages", totalPages,
                "currentPage", page,
                "items", items);
    }
}
