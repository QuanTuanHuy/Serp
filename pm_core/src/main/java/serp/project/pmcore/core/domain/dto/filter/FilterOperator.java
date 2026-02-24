/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.dto.filter;

/**
 * Reusable filter operators for dynamic query building.
 * Covers the essential subset of Jira-style JQL operators.
 */
public enum FilterOperator {
    EQ,           // column = :param
    NEQ,          // column != :param
    IN,           // column IN (:param)
    NOT_IN,       // column NOT IN (:param)
    GT,           // column > :param
    GTE,          // column >= :param
    LT,           // column < :param
    LTE,          // column <= :param
    LIKE,         // column ILIKE :param  (auto-wrap with %)
    IS_NULL,      // column IS NULL       (no param needed)
    IS_NOT_NULL,  // column IS NOT NULL   (no param needed)
    BETWEEN       // column BETWEEN :paramFrom AND :paramTo
}
