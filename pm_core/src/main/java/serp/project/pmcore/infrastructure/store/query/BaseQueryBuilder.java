/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.query;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.dto.filter.FieldFilter;
import serp.project.pmcore.core.domain.dto.filter.FilterOperator;
import serp.project.pmcore.core.domain.dto.filter.SortField;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/**
 * Reusable SQL query builder for NamedParameterJdbcTemplate.
 * <p>
 * Provides composable primitives to construct WHERE clauses safely using named parameters,
 * preventing SQL injection while supporting Jira-style complex filtering.
 * <p>
 * Designed for reuse across any entity — WorkItem, Sprint, Component, etc.
 * Entity-specific query builders should inject this class and delegate to its methods.
 *
 * <h3>Supported operations:</h3>
 * <ul>
 *   <li>Scalar comparisons: =, !=, >, >=, <, <=</li>
 *   <li>List operations: IN, NOT IN</li>
 *   <li>Text search: ILIKE with auto-wrapped %</li>
 *   <li>Date/timestamp ranges with epoch ms conversion</li>
 *   <li>NULL checks: IS NULL, IS NOT NULL</li>
 *   <li>EXISTS subqueries for junction/join tables</li>
 *   <li>Optional LEFT JOINs for enrichment</li>
 *   <li>Sort with column whitelist + NULLS FIRST/LAST</li>
 *   <li>Pagination with LIMIT/OFFSET</li>
 *   <li>Generic {@link FieldFilter} dispatch</li>
 * </ul>
 */
@Component
public class BaseQueryBuilder {

    /**
     * Append a single scalar condition: {@code column OP :paramName}.
     * Supports EQ, NEQ, GT, GTE, LT, LTE. Silently skips if value is null.
     */
    public void appendScalar(StringBuilder where, MapSqlParameterSource params,
                             String column, String paramName, FilterOperator op, Object value) {
        if (value == null) {
            return;
        }
        FilterOperator safeOp = (op != null) ? op : FilterOperator.EQ;
        String sqlOp = switch (safeOp) {
            case EQ -> "=";
            case NEQ -> "!=";
            case GT -> ">";
            case GTE -> ">=";
            case LT -> "<";
            case LTE -> "<=";
            default -> "=";
        };
        where.append(" AND ").append(column).append(" ").append(sqlOp)
                .append(" :").append(paramName);
        params.addValue(paramName, value);
    }

    /**
     * Append an IN or NOT IN clause. Silently skips if values list is null or empty.
     */
    public void appendList(StringBuilder where, MapSqlParameterSource params,
                           String column, String paramName,
                           FilterOperator op, List<?> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        String keyword = (op == FilterOperator.NOT_IN) ? "NOT IN" : "IN";
        where.append(" AND ").append(column).append(" ").append(keyword)
                .append(" (:").append(paramName).append(")");
        params.addValue(paramName, values);
    }


    /**
     * Append an ILIKE search across one or more columns (OR-combined).
     * Auto-wraps the keyword with {@code %}. Silently skips if keyword is blank.
     *
     * @param columns   one or more column references (e.g. "w.summary", "w.key")
     * @param paramName named parameter key
     * @param keyword   the search text (not yet wrapped with %)
     */
    public void appendLike(StringBuilder where, MapSqlParameterSource params,
                           String[] columns, String paramName, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        if (columns.length == 1) {
            where.append(" AND ").append(columns[0]).append(" ILIKE :").append(paramName);
        } else {
            where.append(" AND (");
            for (int i = 0; i < columns.length; i++) {
                if (i > 0) {
                    where.append(" OR ");
                }
                where.append(columns[i]).append(" ILIKE :").append(paramName);
            }
            where.append(")");
        }
        params.addValue(paramName, pattern);
    }

    /**
     * Append a date/timestamp range: {@code column >= :prefixFrom AND column <= :prefixTo}.
     * Either bound can be null (open-ended range).
     */
    public void appendDateRange(StringBuilder where, MapSqlParameterSource params,
                                String column, String prefix,
                                LocalDateTime from, LocalDateTime to) {
        if (from != null) {
            where.append(" AND ").append(column).append(" >= :").append(prefix).append("From");
            params.addValue(prefix + "From", from);
        }
        if (to != null) {
            where.append(" AND ").append(column).append(" <= :").append(prefix).append("To");
            params.addValue(prefix + "To", to);
        }
    }

    /**
     * Append a date range converting epoch milliseconds to LocalDateTime first.
     */
    public void appendEpochRange(StringBuilder where, MapSqlParameterSource params,
                                 String column, String prefix, Long fromMs, Long toMs) {
        appendDateRange(where, params, column, prefix,
                fromMs != null ? epochToLdt(fromMs) : null,
                toMs != null ? epochToLdt(toMs) : null);
    }


    /**
     * Append IS NULL or IS NOT NULL condition.
     */
    public void appendNullCheck(StringBuilder where, String column, boolean isNull) {
        where.append(" AND ").append(column)
                .append(isNull ? " IS NULL" : " IS NOT NULL");
    }

    /**
     * Append an EXISTS subquery for filtering via junction/association tables.
     * <p>
     * Example output:
     * <pre>
     * AND EXISTS (SELECT 1 FROM work_item_sprints _j
     *   WHERE _j.work_item_id = w.id
     *   AND _j.tenant_id = w.tenant_id
     *   AND _j.deleted_at IS NULL
     *   AND _j.sprint_id IN (:sprintIds)
     *   AND _j.is_active = true)
     * </pre>
     *
     * @param junctionTable  the junction table name
     * @param parentAlias    alias of the parent table in the main query (e.g. "w")
     * @param parentPk       PK column of the parent table (e.g. "id")
     * @param fkToParent     FK column in junction table pointing to parent (e.g. "work_item_id")
     * @param filterFk       the column in junction table to filter on (e.g. "sprint_id")
     * @param paramName      named parameter key for the IN values
     * @param values         list of IDs to filter on
     * @param extraCondition optional extra SQL condition (e.g. "_j.is_active = true"), or null
     */
    public void appendExistsSubquery(StringBuilder where, MapSqlParameterSource params,
                                     String junctionTable, String parentAlias,
                                     String parentPk, String fkToParent,
                                     String filterFk, String paramName,
                                     List<?> values, String extraCondition) {
        if (values == null || values.isEmpty()) {
            return;
        }
        where.append(" AND EXISTS (SELECT 1 FROM ").append(junctionTable).append(" _j")
                .append(" WHERE _j.").append(fkToParent).append(" = ")
                .append(parentAlias).append(".").append(parentPk)
                .append(" AND _j.tenant_id = ").append(parentAlias).append(".tenant_id")
                .append(" AND _j.deleted_at IS NULL")
                .append(" AND _j.").append(filterFk).append(" IN (:").append(paramName).append(")");
        if (extraCondition != null && !extraCondition.isEmpty()) {
            where.append(" AND ").append(extraCondition);
        }
        where.append(")");
        params.addValue(paramName, values);
    }


    /**
     * Append a raw SQL condition fragment. Use for computed/complex conditions
     * like {@code w.due_date < CURRENT_TIMESTAMP AND w.resolution_id IS NULL}.
     * <p>
     * <b>Warning:</b> The condition string is appended as-is. Never pass user input directly.
     */
    public void appendRaw(StringBuilder where, String condition) {
        if (condition != null && !condition.isEmpty()) {
            where.append(" AND ").append(condition);
        }
    }


    /**
     * Append a LEFT JOIN clause for enrichment (e.g. fetching status name alongside work items).
     * <p>
     * Example: {@code appendLeftJoin(joins, "statuses", "st", "w.status_id", "st.id", "st.tenant_id = w.tenant_id")}
     * <p>
     * Produces: {@code LEFT JOIN statuses st ON w.status_id = st.id AND st.tenant_id = w.tenant_id AND st.deleted_at IS NULL}
     *
     * @param joins          StringBuilder collecting JOIN clauses
     * @param table          the table to join
     * @param alias          alias for the joined table
     * @param leftKey        the FK in the main table (e.g. "w.status_id")
     * @param rightKey       the PK in the joined table (e.g. "st.id")
     * @param extraCondition additional join condition (e.g. tenant matching), or null
     */
    public void appendLeftJoin(StringBuilder joins, String table, String alias,
                               String leftKey, String rightKey, String extraCondition) {
        joins.append(" LEFT JOIN ").append(table).append(" ").append(alias)
                .append(" ON ").append(leftKey).append(" = ").append(rightKey)
                .append(" AND ").append(alias).append(".deleted_at IS NULL");
        if (extraCondition != null && !extraCondition.isEmpty()) {
            joins.append(" AND ").append(extraCondition);
        }
    }

    /**
     * Build ORDER BY + LIMIT/OFFSET clause with sort column whitelist validation.
     * <p>
     * The sort column is validated against a whitelist to prevent SQL injection.
     * Falls back to {@code defaultSort} if the requested column is not in the whitelist.
     *
     * @param params              parameter source to add limit/offset values
     * @param tableAlias          alias of the main table (used as column prefix)
     * @param sort                the requested sort specification (nullable)
     * @param page                zero-based page number
     * @param pageSize            number of items per page
     * @param allowedSortColumns  whitelist of allowed column names
     * @param defaultSort         fallback sort column
     * @return the ORDER BY + LIMIT OFFSET SQL fragment
     */
    public String buildOrderAndPagination(MapSqlParameterSource params,
                                          String tableAlias,
                                          SortField sort, int page, int pageSize,
                                          Set<String> allowedSortColumns,
                                          String defaultSort) {
        String sortCol = (sort != null && sort.getField() != null
                && allowedSortColumns.contains(sort.getField()))
                ? sort.getField() : defaultSort;

        String sortDir = (sort != null && "DESC".equalsIgnoreCase(sort.getDirection()))
                ? "DESC" : "ASC";

        String nulls = (sort != null && "FIRST".equalsIgnoreCase(sort.getNullsPosition()))
                ? "NULLS FIRST" : "NULLS LAST";

        int safePage = Math.max(page, 0);
        int safeSize = (pageSize > 0 && pageSize <= 200) ? pageSize : 20;

        params.addValue("_limit", safeSize);
        params.addValue("_offset", safePage * safeSize);

        return " ORDER BY " + tableAlias + "." + sortCol + " " + sortDir + " " + nulls
                + " LIMIT :_limit OFFSET :_offset";
    }


    /**
     * Build a complete query from parts, exposing the WHERE clause separately
     * so aggregation queries can reuse the same filter logic with a different SELECT.
     * <p>
     * Usage pattern:
     * <pre>
     *   var where = new StringBuilder();
     *   // ... append conditions to where ...
     *   String aggregateSql = "SELECT status_id, COUNT(*) FROM work_items w"
     *       + " WHERE w.tenant_id = :tenantId AND w.deleted_at IS NULL"
     *       + where
     *       + " GROUP BY status_id";
     * </pre>
     *
     * @return the WHERE clause fragment (without the base WHERE, just the AND conditions)
     */
    public String extractWhereClause(StringBuilder where) {
        return where.toString();
    }

    /**
     * Apply a generic {@link FieldFilter} to the query by dispatching to the
     * appropriate method based on the operator.
     */
    public <T> void applyFieldFilter(StringBuilder where, MapSqlParameterSource params,
                                     String column, String paramName, FieldFilter<T> filter) {
        if (filter == null) {
            return;
        }
        FilterOperator op = (filter.getOperator() != null) ? filter.getOperator() : FilterOperator.EQ;

        switch (op) {
            case EQ, NEQ, GT, GTE, LT, LTE ->
                    appendScalar(where, params, column, paramName, op, filter.getValue());
            case IN ->
                    appendList(where, params, column, paramName, FilterOperator.IN,
                            filter.getValues());
            case NOT_IN ->
                    appendList(where, params, column, paramName, FilterOperator.NOT_IN,
                            filter.getValues());
            case LIKE ->
                    appendLike(where, params, new String[]{column}, paramName,
                            filter.getValue() != null ? filter.getValue().toString() : null);
            case IS_NULL ->
                    appendNullCheck(where, column, true);
            case IS_NOT_NULL ->
                    appendNullCheck(where, column, false);
            case BETWEEN -> {
                if (filter.getFrom() instanceof LocalDateTime from
                        && filter.getTo() instanceof LocalDateTime to) {
                    appendDateRange(where, params, column, paramName, from, to);
                } else if (filter.getFrom() instanceof Long fromMs) {
                    Long toMs = (filter.getTo() instanceof Long t) ? t : null;
                    appendEpochRange(where, params, column, paramName, fromMs, toMs);
                }
            }
        }
    }

    protected LocalDateTime epochToLdt(Long epochMs) {
        if (epochMs == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault());
    }
}
