/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.support;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import serp.project.pmcore.domain.shared.pagination.PageCriteria;

public final class PageableUtils {

    private PageableUtils() {
    }

    public static Pageable of(PageCriteria criteria, Sort sort) {
        int page = criteria.getPage();
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }

        int pageSize = criteria.getPageSize();
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize must be between 1 and 100");
        }

        return PageRequest.of(page, pageSize, sort);
    }

    public static Sort.Direction resolveDirection(String sortDirection) {
        return switch (sortDirection.toUpperCase()) {
            case "ASC" -> Sort.Direction.ASC;
            case "DESC" -> Sort.Direction.DESC;
            default -> throw new IllegalArgumentException("sortDirection must be ASC or DESC");
        };
    }
}
