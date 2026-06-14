/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

import java.util.List;

public record DashboardScopeResponse(
        String accessLevel,
        List<Long> hubIds,
        List<String> postOfficeCodes,
        List<String> roleCodes
) {
}
