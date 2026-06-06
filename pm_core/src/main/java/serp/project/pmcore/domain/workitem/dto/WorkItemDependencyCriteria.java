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
public class WorkItemDependencyCriteria extends PageCriteria {
    private static final int DEFAULT_DEPTH = 2;
    private static final int MAX_DEPTH = 5;

    private Long projectId;
    private String keyword;
    private Long parentId;
    private List<Long> statusIds;
    private List<Long> assigneeIds;
    private List<Long> issueTypeIds;
    private List<Long> priorityIds;
    private List<Long> componentIds;
    private Boolean includeOutside;
    private Boolean includeRelatedLinks;
    private Integer depth;

    public boolean isIncludeOutside() {
        return !Boolean.FALSE.equals(includeOutside);
    }

    public boolean isIncludeRelatedLinks() {
        return Boolean.TRUE.equals(includeRelatedLinks);
    }

    public int getEffectiveDepth() {
        if (depth == null) {
            return DEFAULT_DEPTH;
        }
        if (depth < 1) {
            return 1;
        }
        return Math.min(depth, MAX_DEPTH);
    }
}
