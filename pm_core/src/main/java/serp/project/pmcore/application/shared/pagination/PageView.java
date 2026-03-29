/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.shared.pagination;

import java.util.List;

public record PageView<T>(
        List<T> items,
        long totalItems,
        int totalPages,
        int currentPage,
        int pageSize
) {
}
