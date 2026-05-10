/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardStatusProjection;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class WorkItemBoardStatusRowMapper extends BaseRowMapper implements RowMapper<WorkItemBoardStatusProjection> {

    @Override
    public WorkItemBoardStatusProjection mapRow(ResultSet rs, int rowNum) throws SQLException {
        return WorkItemBoardStatusProjection.builder()
                .statusId(rs.getLong("status_id"))
                .statusKey(rs.getString("status_key"))
                .statusName(rs.getString("status_name"))
                .statusDescription(rs.getString("status_description"))
                .statusIconUrl(rs.getString("status_icon_url"))
                .statusCategoryId(getNullableLong(rs, "status_category_id"))
                .statusCategoryKey(rs.getString("status_category_key"))
                .statusCategoryName(rs.getString("status_category_name"))
                .build();
    }
}
