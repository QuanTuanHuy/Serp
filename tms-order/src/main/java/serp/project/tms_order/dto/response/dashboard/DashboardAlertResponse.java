/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

public record DashboardAlertResponse(
        String id,
        String type,
        String severity,
        String title,
        String description,
        String entityType,
        Long entityId,
        String entityCode,
        String statusCode,
        String leg,
        String occurredAt,
        String dueAt,
        String actionHref
) {
}
