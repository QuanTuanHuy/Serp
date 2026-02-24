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

/**
 * Rich typed filter request for work item search.
 * Supports Jira-style filtering: IN, NOT IN, IS NULL, date ranges, text search,
 * junction table filtering (sprints, components, labels, versions), and computed conditions.
 * <p>
 * All list fields use AND semantics (all conditions must match).
 * Null fields are silently skipped (not applied to the query).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkItemFilterRequest extends BaseGetParams {

    // ---- Scope ----

    /**
     * Filter by project. Almost always required in practice.
     */
    private Long projectId;

    // ---- Direct IN filters (match any in list) ----

    private List<Long> statusIds;
    private List<Long> priorityIds;
    private List<Long> issueTypeIds;
    private List<Long> assigneeIds;
    private List<Long> reporterIds;
    private List<Long> resolutionIds;

    /**
     * Exact match on parent work item (epic link / sub-task parent).
     */
    private Long parentId;

    // ---- Exclusion filters (NOT IN) ----

    private List<Long> excludeStatusIds;
    private List<Long> excludeIssueTypeIds;

    // ---- Null checks ----

    /**
     * If true: assignee_id IS NULL
     */
    private Boolean unassigned;

    /**
     * If true: resolution_id IS NULL (unresolved issues)
     */
    private Boolean unresolved;

    // ---- Date range filters (epoch milliseconds) ----

    private Long dueDateFrom;
    private Long dueDateTo;
    private Long createdFrom;
    private Long createdTo;
    private Long updatedFrom;
    private Long updatedTo;

    // ---- Text search ----

    /**
     * ILIKE search across summary and key columns.
     */
    private String keyword;

    // ---- Junction table filters (EXISTS subquery) ----

    /**
     * Filter work items belonging to any of these sprints (active assignment).
     */
    private List<Long> sprintIds;

    /**
     * Filter work items linked to any of these components.
     */
    private List<Long> componentIds;

    /**
     * Filter work items linked to any of these fix versions.
     */
    private List<Long> fixVersionIds;

    /**
     * Filter work items that have any of these labels.
     */
    private List<Long> labelIds;

    // ---- Computed / derived filters ----

    /**
     * If true: due_date < NOW() AND resolution_id IS NULL
     */
    private Boolean isOverdue;

    /**
     * If true: time_spent > 0
     */
    private Boolean hasTimeLogged;

    // ---- Sort override ----

    /**
     * Custom sort specification. Falls back to rank ASC if null.
     */
    private SortField sort;

    // ---- Enrichment control ----

    /**
     * If true, the query will LEFT JOIN related tables (issue_types, priorities, statuses)
     * to include their names/colors in the result.
     * Defaults to false for lightweight list queries.
     */
    private Boolean enriched;

    // ---- Convenience methods ----

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean isEnriched() {
        return Boolean.TRUE.equals(enriched);
    }
}
