/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.shared.pagination;

import serp.project.pmcore.domain.shared.pagination.PageCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.function.Function;

public final class PageViews {

    private PageViews() {
    }

    public static <T> PageView<T> from(PageResult<T> result, PageCriteria criteria) {
        return new PageView<>(
                result.items(),
                result.total(),
                totalPages(result.total(), criteria.getPageSize()),
                criteria.getPage(),
                criteria.getPageSize()
        );
    }

    public static <S, T> PageView<T> from(PageResult<S> result,
                                          PageCriteria criteria,
                                          Function<? super S, ? extends T> mapper) {
        return from(result.map(mapper), criteria);
    }

    public static int totalPages(long totalItems, int pageSize) {
        return pageSize <= 0 ? 0 : (int) Math.ceil((double) totalItems / pageSize);
    }
}
