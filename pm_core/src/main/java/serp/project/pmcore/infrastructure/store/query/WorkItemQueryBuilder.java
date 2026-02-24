/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.query;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.dto.filter.FilterOperator;
import serp.project.pmcore.core.domain.dto.filter.SortField;
import serp.project.pmcore.core.domain.dto.filter.WorkItemFilterRequest;

import java.util.Set;

/**
 * Builds dynamic SQL for work item search queries.
 * <p>
 * Delegates all primitive operations to {@link BaseQueryBuilder} and only contains
 * work-item-specific logic: column mappings, allowed sort columns, optional JOINs
 * for enrichment, and the base SELECT/FROM structure.
 * <p>
 * Supports two modes:
 * <ul>
 *   <li><b>Lightweight</b> (default): SELECT only from work_items, returns FK IDs</li>
 *   <li><b>Enriched</b> ({@code enriched=true}): LEFT JOINs issue_types, priorities, statuses
 *       to include display names, colors, and icons in the result</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class WorkItemQueryBuilder {

    private final BaseQueryBuilder base;

    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of(
            "id", "key", "summary", "status_id", "priority_id", "assignee_id",
            "reporter_id", "issue_type_id", "due_date", "created_at",
            "updated_at", "rank", "issue_no", "resolution_id", "parent_id"
    );

    private static final String DEFAULT_SORT = "rank";

    private static final String BASE_COLUMNS = """
            w.id, w.tenant_id, w.project_id, w.issue_type_id,
            w.issue_no, w.key, w.summary, w.description,
            w.status_id, w.priority_id, w.resolution_id,
            w.assignee_id, w.reporter_id, w.parent_id,
            w.security_level_id, w.due_date, w.rank,
            w.time_original_estimate, w.time_remaining_estimate, w.time_spent,
            w.created_at, w.updated_at, w.created_by, w.updated_by""";

    private static final String ENRICHED_COLUMNS = """
            ,
            it.name AS issue_type_name, it.icon_url AS issue_type_icon_url,
            it.hierarchy_level AS issue_type_hierarchy_level,
            pr.name AS priority_name, pr.icon_url AS priority_icon_url,
            pr.color AS priority_color, pr.sequence AS priority_sequence""";

    private static final String BASE_FROM = "\nFROM work_items w";

    private static final String BASE_WHERE = "\nWHERE w.tenant_id = :tenantId AND w.deleted_at IS NULL";

    /**
     * Build a paginated search query with optional enrichment JOINs.
     *
     * @param tenantId tenant scope
     * @param f        filter request (null fields are skipped)
     * @return query result with data SQL, count SQL, and named parameters
     */
    public QueryResult build(Long tenantId, WorkItemFilterRequest f) {
        var params = new MapSqlParameterSource("tenantId", tenantId);
        var where = new StringBuilder();
        var joins = new StringBuilder();

        boolean enriched = f.isEnriched();
        if (enriched) {
            base.appendLeftJoin(joins, "issue_types", "it",
                    "w.issue_type_id", "it.id", "it.tenant_id = w.tenant_id");
            base.appendLeftJoin(joins, "priorities", "pr",
                    "w.priority_id", "pr.id", "pr.tenant_id = w.tenant_id");
        }

        base.appendScalar(where, params, "w.project_id", "projectId", FilterOperator.EQ, f.getProjectId());
        base.appendList(where, params, "w.status_id", "statusIds", FilterOperator.IN, f.getStatusIds());
        base.appendList(where, params, "w.priority_id", "priorityIds", FilterOperator.IN, f.getPriorityIds());
        base.appendList(where, params, "w.issue_type_id", "issueTypeIds", FilterOperator.IN, f.getIssueTypeIds());
        base.appendList(where, params, "w.assignee_id", "assigneeIds", FilterOperator.IN, f.getAssigneeIds());
        base.appendList(where, params, "w.reporter_id", "reporterIds", FilterOperator.IN, f.getReporterIds());
        base.appendList(where, params, "w.resolution_id", "resolutionIds", FilterOperator.IN, f.getResolutionIds());
        base.appendScalar(where, params, "w.parent_id", "parentId", FilterOperator.EQ, f.getParentId());

        base.appendList(where, params, "w.status_id", "exStatusIds",
                FilterOperator.NOT_IN, f.getExcludeStatusIds());
        base.appendList(where, params, "w.issue_type_id", "exTypeIds",
                FilterOperator.NOT_IN, f.getExcludeIssueTypeIds());

        if (Boolean.TRUE.equals(f.getUnassigned())) {
            base.appendNullCheck(where, "w.assignee_id", true);
        }
        if (Boolean.TRUE.equals(f.getUnresolved())) {
            base.appendNullCheck(where, "w.resolution_id", true);
        }

        base.appendEpochRange(where, params, "w.due_date", "dueDate",
                f.getDueDateFrom(), f.getDueDateTo());
        base.appendEpochRange(where, params, "w.created_at", "created",
                f.getCreatedFrom(), f.getCreatedTo());
        base.appendEpochRange(where, params, "w.updated_at", "updated",
                f.getUpdatedFrom(), f.getUpdatedTo());

        base.appendLike(where, params,
                new String[]{"w.summary", "w.key"}, "keyword", f.getKeyword());

        base.appendExistsSubquery(where, params,
                "work_item_sprints", "w", "id", "work_item_id",
                "sprint_id", "sprintIds", f.getSprintIds(), "_j.is_active = true");
        base.appendExistsSubquery(where, params,
                "work_item_components", "w", "id", "work_item_id",
                "component_id", "componentIds", f.getComponentIds(), null);
        base.appendExistsSubquery(where, params,
                "work_item_fix_versions", "w", "id", "work_item_id",
                "version_id", "fixVersionIds", f.getFixVersionIds(), null);

        // base.appendExistsSubquery(where, params,
        //         "work_item_labels", "w", "id", "work_item_id",
        //         "label_id", "labelIds", f.getLabelIds(), null);

        if (Boolean.TRUE.equals(f.getIsOverdue())) {
            base.appendRaw(where, "w.due_date < CURRENT_TIMESTAMP AND w.resolution_id IS NULL");
        }
        if (Boolean.TRUE.equals(f.getHasTimeLogged())) {
            base.appendRaw(where, "w.time_spent > 0");
        }

        SortField sort = (f.getSort() != null) ? f.getSort()
                : SortField.builder().field(DEFAULT_SORT).direction("ASC").build();
        String orderAndPage = base.buildOrderAndPagination(params, "w", sort,
                f.getPage(), f.getPageSize(), ALLOWED_SORT_COLUMNS, DEFAULT_SORT);

        String selectCols = enriched ? BASE_COLUMNS + ENRICHED_COLUMNS : BASE_COLUMNS;

        String dataSql = "SELECT " + selectCols + BASE_FROM + joins + BASE_WHERE + where + orderAndPage;
        String countSql = "SELECT COUNT(*)" + BASE_FROM + BASE_WHERE + where;

        return new QueryResult(dataSql, countSql, params);
    }

}
