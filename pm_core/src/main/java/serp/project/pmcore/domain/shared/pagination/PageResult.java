/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> items, long total) {
    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        List<R> mappedItems = new ArrayList<>(items.size());
        for (T item : items) {
            mappedItems.add(mapper.apply(item));
        }
        return new PageResult<>(mappedItems, total);
    }
}
