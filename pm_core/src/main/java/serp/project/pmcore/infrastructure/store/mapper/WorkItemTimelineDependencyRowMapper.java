/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineDependencyProjection;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class WorkItemTimelineDependencyRowMapper extends BaseRowMapper implements RowMapper<WorkItemTimelineDependencyProjection> {

    @Override
    public WorkItemTimelineDependencyProjection mapRow(ResultSet rs, int rowNum) throws SQLException {
        return WorkItemTimelineDependencyProjection.builder()
                .linkId(rs.getLong("link_id"))
                .sourceId(rs.getLong("source_id"))
                .targetId(rs.getLong("target_id"))
                .linkTypeId(rs.getLong("link_type_id"))
                .linkTypeName(rs.getString("link_type_name"))
                .description(rs.getString("description"))
                .build();
    }
}
