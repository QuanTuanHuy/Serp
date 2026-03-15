/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.query;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public record SqlQueryResult(
        String dataSql,
        String countSql,
        MapSqlParameterSource params) {
}
