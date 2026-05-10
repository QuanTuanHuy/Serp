/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.board;

import java.util.List;

public record WorkItemBoardView(
        Long projectId,
        List<WorkItemBoardColumnView> columns
) {
}
