/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.core.domain.dto.request.BaseGetParams;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkItemFilterRequest extends BaseGetParams {

    private Long projectId;

    private List<Long> statusIds;
    private List<Long> priorityIds;
    private List<Long> issueTypeIds;
    private List<Long> assigneeIds;
    private List<Long> reporterIds;
    private List<Long> resolutionIds;

    private Long parentId;

    private List<Long> excludeStatusIds;
    private List<Long> excludeIssueTypeIds;


    private Boolean unassigned;

    private Boolean unresolved;


    private Long dueDateFrom;
    private Long dueDateTo;
    private Long createdFrom;
    private Long createdTo;
    private Long updatedFrom;
    private Long updatedTo;

    private String keyword;

    private List<Long> sprintIds;
    private List<Long> componentIds;
    private List<Long> fixVersionIds;

    private List<Long> labelIds;

    // ---- Computed / derived filters ----
    private Boolean isOverdue;

    /**
     * If true: time_spent > 0
     */
    private Boolean hasTimeLogged;

    // ---- Sort override ----

    private SortField sort;

    // ---- Enrichment control ----

    private Boolean enriched;


    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean isEnriched() {
        return Boolean.TRUE.equals(enriched);
    }
}
