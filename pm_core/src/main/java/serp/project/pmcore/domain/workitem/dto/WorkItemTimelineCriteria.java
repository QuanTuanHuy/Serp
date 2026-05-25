/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.pagination.PageCriteria;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkItemTimelineCriteria extends PageCriteria {

    private static final int DEFAULT_DEPTH = 2;
    private static final int MAX_DEPTH = 5;

    private Long projectId;
    private Long viewportStart;
    private Long viewportEnd;
    private Boolean includeUnscheduled;
    private Long parentId;
    private Integer depth;
    private List<Long> statusIds;
    private List<Long> assigneeIds;
    private List<Long> issueTypeIds;
    private List<Long> priorityIds;
    private String keyword;

    public boolean isIncludeUnscheduled() {
        return !Boolean.FALSE.equals(includeUnscheduled);
    }

    public int getEffectiveDepth() {
        if (depth == null) {
            return DEFAULT_DEPTH;
        }
        if (depth < 0) {
            return 0;
        }
        return Math.min(depth, MAX_DEPTH);
    }
}
