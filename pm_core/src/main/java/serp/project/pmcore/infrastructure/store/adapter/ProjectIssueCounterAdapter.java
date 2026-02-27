/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.core.port.store.IProjectIssueCounterPort;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectIssueCounterAdapter implements IProjectIssueCounterPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String SELECT_FOR_UPDATE_SQL =
            "SELECT counter FROM project_issue_counters " +
            "WHERE project_id = :projectId AND tenant_id = :tenantId " +
            "FOR UPDATE";

    private static final String UPDATE_COUNTER_SQL =
            "UPDATE project_issue_counters SET counter = :newCounter " +
            "WHERE project_id = :projectId AND tenant_id = :tenantId";

    private static final String INSERT_COUNTER_SQL =
            "INSERT INTO project_issue_counters (tenant_id, project_id, counter) " +
            "VALUES (:tenantId, :projectId, 1)";

    @Override
    public Long getNextIssueNo(Long projectId, Long tenantId) {
        var params = new MapSqlParameterSource()
                .addValue("projectId", projectId)
                .addValue("tenantId", tenantId);

        Long currentCounter;
        try {
            currentCounter = jdbcTemplate.queryForObject(SELECT_FOR_UPDATE_SQL, params, Long.class);
        } catch (Exception e) {
            // Counter row doesn't exist yet
            log.info("Creating issue counter for project {} in tenant {}", projectId, tenantId);
            jdbcTemplate.update(INSERT_COUNTER_SQL, params);
            return 1L;
        }

        if (currentCounter == null) {
            throw new AppException(ErrorCode.ISSUE_COUNTER_NOT_FOUND);
        }

        long nextCounter = currentCounter + 1;
        params.addValue("newCounter", nextCounter);
        jdbcTemplate.update(UPDATE_COUNTER_SQL, params);

        return nextCounter;
    }
}
