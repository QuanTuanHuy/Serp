/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Generic typed filter condition. Pairs a {@link FilterOperator} with value(s).
 * <p>
 * Usage examples:
 * <pre>
 *   // Single value: status = 1
 *   FieldFilter.of(FilterOperator.EQ, 1L)
 *
 *   // Multi value: priority IN (1, 2, 3)
 *   FieldFilter.in(List.of(1L, 2L, 3L))
 *
 *   // Range: created BETWEEN from AND to
 *   FieldFilter.between(fromDate, toDate)
 *
 *   // Null check: assignee IS NULL
 *   FieldFilter.isNull()
 * </pre>
 *
 * @param <T> the value type (Long, String, LocalDateTime, etc.)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldFilter<T> {

    private FilterOperator operator;

    /**
     * Single value — used with EQ, NEQ, GT, GTE, LT, LTE, LIKE
     */
    private T value;

    /**
     * Multi values — used with IN, NOT_IN
     */
    private List<T> values;

    /**
     * Range start — used with BETWEEN
     */
    private T from;

    /**
     * Range end — used with BETWEEN
     */
    private T to;

    // ---- Factory methods for convenience ----

    public static <T> FieldFilter<T> of(FilterOperator op, T value) {
        return FieldFilter.<T>builder().operator(op).value(value).build();
    }

    public static <T> FieldFilter<T> eq(T value) {
        return of(FilterOperator.EQ, value);
    }

    public static <T> FieldFilter<T> in(List<T> values) {
        return FieldFilter.<T>builder().operator(FilterOperator.IN).values(values).build();
    }

    public static <T> FieldFilter<T> notIn(List<T> values) {
        return FieldFilter.<T>builder().operator(FilterOperator.NOT_IN).values(values).build();
    }

    public static <T> FieldFilter<T> between(T from, T to) {
        return FieldFilter.<T>builder().operator(FilterOperator.BETWEEN).from(from).to(to).build();
    }

    public static <T> FieldFilter<T> isNull() {
        return FieldFilter.<T>builder().operator(FilterOperator.IS_NULL).build();
    }

    public static <T> FieldFilter<T> isNotNull() {
        return FieldFilter.<T>builder().operator(FilterOperator.IS_NOT_NULL).build();
    }
}
