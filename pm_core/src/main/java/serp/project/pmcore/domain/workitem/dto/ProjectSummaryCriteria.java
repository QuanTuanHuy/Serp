/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryCriteria {
    private Long projectId;
    private List<Long> assigneeIds;
    private List<Long> priorityIds;
    private List<Long> statusIds;
    private List<Long> issueTypeIds;
    private Long parentId;
    private Long createdFrom;
    private Long createdTo;
    private Long updatedFrom;
    private Long updatedTo;
    private Long dueDateFrom;
    private Long dueDateTo;
    private Integer activityPage;
    private Integer activitySize;

    public int getActivityPage() {
        return activityPage == null ? 0 : Math.max(activityPage, 0);
    }

    public int getActivitySize() {
        return activitySize == null ? 20 : Math.min(Math.max(activitySize, 1), 100);
    }
}
