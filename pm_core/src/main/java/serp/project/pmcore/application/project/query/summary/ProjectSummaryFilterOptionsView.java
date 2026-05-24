/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.summary;

import serp.project.pmcore.application.shared.dto.user.UserSummary;

import java.util.List;

public record ProjectSummaryFilterOptionsView(
        List<UserSummary> assignees,
        List<ProjectSummaryParentOptionView> parents,
        List<ProjectSummaryBreakdownItemView> priorities,
        List<ProjectSummaryBreakdownItemView> statuses,
        List<ProjectSummaryBreakdownItemView> issueTypes
) {
}
