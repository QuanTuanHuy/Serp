/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.board;

import com.fasterxml.jackson.annotation.JsonInclude;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardStatusProjection;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkItemBoardColumnView(
        Long statusId,
        String statusKey,
        String statusName,
        String statusDescription,
        String statusIconUrl,
        StatusCategorySummaryView statusCategory,
        List<WorkItemBoardCardView> items,
        long total
) {

    public static WorkItemBoardColumnView from(WorkItemBoardStatusProjection projection,
                                               List<WorkItemBoardCardView> items) {
        return new WorkItemBoardColumnView(
                projection.statusId(),
                projection.statusKey(),
                projection.statusName(),
                projection.statusDescription(),
                projection.statusIconUrl(),
                new StatusCategorySummaryView(
                        projection.statusCategoryId(),
                        projection.statusCategoryKey(),
                        projection.statusCategoryName()
                ),
                items,
                items.size()
        );
    }

    public record StatusCategorySummaryView(
            Long id,
            String key,
            String name
    ) {
    }
}
