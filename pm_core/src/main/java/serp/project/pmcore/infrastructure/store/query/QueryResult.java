/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.query;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public record QueryResult(
        String dataSql,
        String countSql,
        MapSqlParameterSource params
) {
}
