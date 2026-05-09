/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineDependencyProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineItemProjection;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.dto.WorkItemDetailProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemSearchCriteria;
import serp.project.pmcore.infrastructure.store.mapper.ProjectComponentMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemTimelineDependencyRowMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemTimelineItemRowMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemRowMapper;
import serp.project.pmcore.infrastructure.store.model.WorkItemModel;
import serp.project.pmcore.infrastructure.store.query.WorkItemQueryBuilder;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemComponentRepository;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkItemReadAdapter implements IWorkItemReadPort {

    private final IWorkItemRepository workItemRepository;
    private final IWorkItemComponentRepository workItemComponentRepository;
    private final WorkItemMapper workItemMapper;
    private final ProjectComponentMapper projectComponentMapper;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final WorkItemQueryBuilder queryBuilder;
    private final WorkItemRowMapper rowMapper;
    private final WorkItemTimelineItemRowMapper timelineItemRowMapper;
    private final WorkItemTimelineDependencyRowMapper timelineDependencyRowMapper;

    @Override
    public Optional<WorkItemEntity> getWorkItemById(Long id, Long tenantId) {
        return workItemRepository.findByIdAndTenantId(id, tenantId)
                .map(workItemMapper::toEntity);
    }

    @Override
    public List<WorkItemEntity> getWorkItemsByProjectId(Long projectId, Long tenantId) {
        return workItemMapper.toEntities(
                workItemRepository.findAllByTenantIdAndProjectId(tenantId, projectId)
        );
    }

    @Override
    public List<WorkItemEntity> getWorkItemsByIssueTypeId(Long issueTypeId, Long tenantId) {
        return workItemMapper.toEntities(
                workItemRepository.findAllByTenantIdAndIssueTypeId(tenantId, issueTypeId)
        );
    }

    @Override
    public List<WorkItemEntity> getWorkItemsByPriorityId(Long priorityId, Long tenantId) {
        return workItemMapper.toEntities(
                workItemRepository.findAllByTenantIdAndPriorityId(tenantId, priorityId)
        );
    }

    @Override
    public boolean existsActiveWorkItemByStatusId(Long statusId, Long tenantId) {
        return workItemRepository.existsByTenantIdAndStatusId(tenantId, statusId);
    }

    @Override
    public List<Long> getActiveIssueTypeIdsInUseByProjectIds(Long tenantId, List<Long> projectIds, List<Long> issueTypeIds) {
        if (projectIds == null || projectIds.isEmpty() || issueTypeIds == null || issueTypeIds.isEmpty()) {
            return List.of();
        }
        return workItemRepository.findDistinctIssueTypeIdsInUseByProjectIds(tenantId, projectIds, issueTypeIds);
    }

    @Override
    public List<Long> getActivePriorityIdsInUseByProjectIds(Long tenantId, List<Long> projectIds, List<Long> priorityIds) {
        if (projectIds == null || projectIds.isEmpty() || priorityIds == null || priorityIds.isEmpty()) {
            return List.of();
        }
        return workItemRepository.findDistinctPriorityIdsInUseByProjectIds(tenantId, projectIds, priorityIds);
    }

    @Override
    public Optional<String> getLastRankByProjectId(Long projectId, Long tenantId) {
        return workItemRepository.findFirstByTenantIdAndProjectIdOrderByRankDescIdDesc(tenantId, projectId)
                .map(WorkItemModel::getRank);
    }

    @Override
    public PageResult<WorkItemEntity> searchWorkItems(Long tenantId, WorkItemSearchCriteria criteria) {
        var qr = queryBuilder.build(tenantId, criteria);
        log.debug("WorkItem search SQL: {}", qr.dataSql());
        log.debug("WorkItem count SQL: {}", qr.countSql());

        List<WorkItemEntity> data = jdbcTemplate.query(qr.dataSql(), qr.params(), rowMapper);
        Long total = jdbcTemplate.queryForObject(qr.countSql(), qr.params(), Long.class);
        return new PageResult<>(data, total != null ? total : 0L);
    }

    @Override
    public Optional<WorkItemDetailProjection> getWorkItemDetailById(Long id, Long tenantId) {
        return workItemRepository.findWorkItemDetailById(id, tenantId);
    }

    @Override
    public List<WorkItemEntity> getActiveChildrenByParentId(Long parentId, Long tenantId) {
        return workItemMapper.toEntities(
                workItemRepository.findAllByTenantIdAndParentId(tenantId, parentId)
        );
    }

    @Override
    public List<ProjectComponentEntity> getActiveComponentsByWorkItemId(Long workItemId, Long tenantId) {
        return projectComponentMapper.toEntities(
                workItemComponentRepository.findActiveComponentsByWorkItemId(workItemId, tenantId)
        );
    }

    @Override
    public PageResult<WorkItemTimelineItemProjection> listTimelineWorkItems(Long tenantId, WorkItemTimelineCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("projectId", criteria.getProjectId())
                .addValue("limit", criteria.getPageSize())
                .addValue("offset", criteria.getPage() * criteria.getPageSize());

        String scopedCte = buildTimelineScopedCte(criteria, params);
        String fromSql = buildTimelineFromSql(criteria);
        String whereSql = buildTimelineWhereSql(criteria, params);

        String dataSql = scopedCte + """
                SELECT
                    w.id,
                    w.project_id,
                    w.parent_id,
                    w.key,
                    w.summary,
                    w.assignee_id,
                    w.start_date,
                    w.due_date,
                    (w.start_date IS NULL AND w.due_date IS NULL) AS is_unscheduled,
                    EXISTS (
                        SELECT 1
                        FROM work_items child
                        WHERE child.tenant_id = w.tenant_id
                          AND child.project_id = w.project_id
                          AND child.parent_id = w.id
                          AND child.deleted_at IS NULL
                    ) AS has_children,
                    w.rank,
                    it.id AS issue_type_id,
                    it.name AS issue_type_name,
                    it.icon_url AS issue_type_icon_url,
                    it.hierarchy_level AS issue_type_hierarchy_level,
                    st.id AS status_id,
                    st.name AS status_name,
                    pr.id AS priority_id,
                    pr.name AS priority_name,
                    pr.color AS priority_color
                """ + fromSql + whereSql + """
                 ORDER BY COALESCE(w.start_date, w.due_date) ASC NULLS LAST, w.rank ASC NULLS LAST, w.id ASC
                 LIMIT :limit OFFSET :offset
                """;
        String countSql = scopedCte + "SELECT COUNT(*) " + fromSql + whereSql;

        List<WorkItemTimelineItemProjection> items = jdbcTemplate.query(dataSql, params, timelineItemRowMapper);
        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        return new PageResult<>(items, total != null ? total : 0L);
    }

    @Override
    public List<WorkItemTimelineDependencyProjection> listTimelineDependencies(Long tenantId,
                                                                               Long projectId,
                                                                               List<Long> workItemIds) {
        if (workItemIds == null || workItemIds.isEmpty()) {
            return List.of();
        }

        String sql = """
                SELECT
                    il.id AS link_id,
                    il.source_id,
                    il.target_id,
                    il.link_type_id,
                    ilt.name AS link_type_name,
                    ilt.outward_desc AS description
                FROM issue_links il
                JOIN issue_link_types ilt
                  ON ilt.id = il.link_type_id
                 AND ilt.deleted_at IS NULL
                JOIN work_items source_wi
                  ON source_wi.id = il.source_id
                 AND source_wi.deleted_at IS NULL
                JOIN work_items target_wi
                  ON target_wi.id = il.target_id
                 AND target_wi.deleted_at IS NULL
                WHERE il.tenant_id = :tenantId
                  AND il.deleted_at IS NULL
                  AND source_wi.project_id = :projectId
                  AND target_wi.project_id = :projectId
                  AND il.source_id IN (:workItemIds)
                  AND il.target_id IN (:workItemIds)
                ORDER BY il.id DESC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("projectId", projectId)
                .addValue("workItemIds", workItemIds);

        return jdbcTemplate.query(sql, params, timelineDependencyRowMapper);
    }

    private String buildTimelineScopedCte(WorkItemTimelineCriteria criteria, MapSqlParameterSource params) {
        if (criteria.getParentId() == null) {
            return "";
        }

        params.addValue("parentId", criteria.getParentId());
        params.addValue("depth", criteria.getEffectiveDepth());
        return """
                WITH RECURSIVE scoped_items AS (
                    SELECT w.id, 0 AS depth
                    FROM work_items w
                    WHERE w.tenant_id = :tenantId
                      AND w.project_id = :projectId
                      AND w.id = :parentId
                      AND w.deleted_at IS NULL
                    UNION ALL
                    SELECT child.id, scoped.depth + 1 AS depth
                    FROM work_items child
                    JOIN scoped_items scoped
                      ON scoped.id = child.parent_id
                    WHERE child.tenant_id = :tenantId
                      AND child.project_id = :projectId
                      AND child.deleted_at IS NULL
                      AND scoped.depth < :depth
                )
                """;
    }

    private String buildTimelineFromSql(WorkItemTimelineCriteria criteria) {
        StringBuilder fromSql = new StringBuilder("""
                FROM work_items w
                LEFT JOIN issue_types it ON it.id = w.issue_type_id AND it.deleted_at IS NULL
                LEFT JOIN statuses st ON st.id = w.status_id AND st.deleted_at IS NULL
                LEFT JOIN priorities pr ON pr.id = w.priority_id AND pr.deleted_at IS NULL
                """);
        if (criteria.getParentId() != null) {
            fromSql.append("\nJOIN scoped_items scoped ON scoped.id = w.id");
        }
        return fromSql.toString();
    }

    private String buildTimelineWhereSql(WorkItemTimelineCriteria criteria, MapSqlParameterSource params) {
        StringBuilder whereSql = new StringBuilder("""
                WHERE w.tenant_id = :tenantId
                  AND w.project_id = :projectId
                  AND w.deleted_at IS NULL
                """);

        appendInFilter(whereSql, params, "w.status_id", "statusIds", criteria.getStatusIds());
        appendInFilter(whereSql, params, "w.assignee_id", "assigneeIds", criteria.getAssigneeIds());
        appendInFilter(whereSql, params, "w.issue_type_id", "issueTypeIds", criteria.getIssueTypeIds());
        appendInFilter(whereSql, params, "w.priority_id", "priorityIds", criteria.getPriorityIds());

        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            params.addValue("keyword", "%" + criteria.getKeyword().trim().toLowerCase() + "%");
            whereSql.append("""
                    
                      AND (
                          LOWER(w.key) LIKE :keyword
                          OR LOWER(w.summary) LIKE :keyword
                      )
                    """);
        }

        appendTimelineViewportFilter(whereSql, params, criteria);
        return whereSql.toString();
    }

    private void appendInFilter(StringBuilder whereSql,
                                MapSqlParameterSource params,
                                String column,
                                String paramName,
                                List<Long> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        params.addValue(paramName, values);
        whereSql.append("\n  AND ").append(column).append(" IN (:").append(paramName).append(")");
    }

    private void appendTimelineViewportFilter(StringBuilder whereSql,
                                              MapSqlParameterSource params,
                                              WorkItemTimelineCriteria criteria) {
        Timestamp viewportStart = toTimestamp(criteria.getViewportStart());
        Timestamp viewportEnd = toTimestamp(criteria.getViewportEnd());
        boolean includeUnscheduled = criteria.isIncludeUnscheduled();

        if (viewportStart == null && viewportEnd == null) {
            if (!includeUnscheduled) {
                whereSql.append("\n  AND (w.start_date IS NOT NULL OR w.due_date IS NOT NULL)");
            }
            return;
        }

        if (viewportStart != null) {
            params.addValue("viewportStart", viewportStart);
        }
        if (viewportEnd != null) {
            params.addValue("viewportEnd", viewportEnd);
        }

        whereSql.append("\n  AND (");
        if (includeUnscheduled) {
            whereSql.append("(w.start_date IS NULL AND w.due_date IS NULL) OR ");
        }
        whereSql.append("(");
        boolean appendedCondition = false;
        if (viewportStart != null) {
            whereSql.append("COALESCE(w.due_date, w.start_date) >= :viewportStart");
            appendedCondition = true;
        }
        if (viewportEnd != null) {
            if (appendedCondition) {
                whereSql.append(" AND ");
            }
            whereSql.append("COALESCE(w.start_date, w.due_date) <= :viewportEnd");
        }
        whereSql.append("))");
    }

    private Timestamp toTimestamp(Long epochMillis) {
        return epochMillis == null ? null : new Timestamp(epochMillis);
    }
}
