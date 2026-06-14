/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

public record DashboardTopEntityResponse(
        String code,
        String name,
        long count,
        double percentage
) {
}
