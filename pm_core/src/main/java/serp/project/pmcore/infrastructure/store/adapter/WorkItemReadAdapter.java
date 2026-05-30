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
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryActivityProjection;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryBreakdownProjection;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryCriteria;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryMetricsProjection;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryParentOptionProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemScheduleAllocationCalendarProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemScheduleCalendarCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardItemProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardStatusProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemChildProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineDependencyProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineItemProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemActivityProjection;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.dto.WorkItemDetailProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemLinkProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemSearchCriteria;
import serp.project.pmcore.infrastructure.store.mapper.ProjectComponentMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemActivityRowMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemBoardItemRowMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemBoardStatusRowMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemChildRowMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemLinkRowMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemScheduleAllocationCalendarRowMapper;
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
    private final WorkItemBoardStatusRowMapper boardStatusRowMapper;
    private final WorkItemBoardItemRowMapper boardItemRowMapper;
    private final WorkItemChildRowMapper childRowMapper;
    private final WorkItemLinkRowMapper linkRowMapper;
    private final WorkItemActivityRowMapper activityRowMapper;
    private final WorkItemScheduleAllocationCalendarRowMapper scheduleAllocationCalendarRowMapper;

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
    public List<WorkItemEntity> listActiveByWorkItemIds(Long tenantId, List<Long> workItemIds) {
        if (workItemIds == null || workItemIds.isEmpty()) {
            return List.of();
        }
        return workItemMapper.toEntities(workItemRepository.findAllByTenantIdAndIdIn(tenantId, workItemIds));
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
    public List<WorkItemEntity> getWorkItemsByResolutionId(Long resolutionId, Long tenantId) {
        return workItemMapper.toEntities(
                workItemRepository.findAllByTenantIdAndResolutionId(tenantId, resolutionId)
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
    public Optional<WorkItemDetailProjection> getWorkItemDetailById(Long projectId, Long id, Long tenantId) {
        return workItemRepository.findWorkItemDetailById(projectId, id, tenantId);
    }

    @Override
    public List<WorkItemEntity> getActiveChildrenByParentId(Long parentId, Long tenantId) {
        return workItemMapper.toEntities(
                workItemRepository.findAllByTenantIdAndParentId(tenantId, parentId)
        );
    }

    @Override
    public List<WorkItemChildProjection> listChildrenByParentId(Long projectId, Long parentId, Long tenantId) {
        String sql = """
                SELECT
                    child.id,
                    child.project_id,
                    child.parent_id,
                    child.key,
                    child.summary,
                    child.assignee_id,
                    it.id AS issue_type_id,
                    it.name AS issue_type_name,
                    it.icon_url AS issue_type_icon_url,
                    it.hierarchy_level AS issue_type_hierarchy_level,
                    s.id AS status_id,
                    s.status_key,
                    s.name AS status_name,
                    p.id AS priority_id,
                    p.name AS priority_name,
                    p.color AS priority_color,
                    child.rank
                FROM work_items child
                LEFT JOIN issue_types it ON child.issue_type_id = it.id
                    AND it.deleted_at IS NULL
                LEFT JOIN statuses s ON child.status_id = s.id
                    AND s.deleted_at IS NULL
                LEFT JOIN priorities p ON child.priority_id = p.id
                    AND p.deleted_at IS NULL
                WHERE child.tenant_id = :tenantId
                  AND child.project_id = :projectId
                  AND child.parent_id = :parentId
                  AND child.deleted_at IS NULL
                ORDER BY child.rank ASC NULLS LAST, child.id ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("projectId", projectId)
                .addValue("parentId", parentId);
        return jdbcTemplate.query(sql, params, childRowMapper);
    }

    @Override
    public long countActiveChildrenByParentId(Long projectId, Long parentId, Long tenantId) {
        return workItemRepository.countByTenantIdAndProjectIdAndParentId(tenantId, projectId, parentId);
    }

    @Override
    public long countDoneChildrenByParentId(Long projectId, Long parentId, Long tenantId) {
        return workItemRepository.countDoneChildrenByParentId(projectId, parentId, tenantId);
    }

    @Override
    public long countActiveLinksByWorkItemId(Long workItemId, Long tenantId) {
        String sql = """
                SELECT COUNT(*)
                FROM issue_links il
                WHERE il.tenant_id = :tenantId
                  AND il.deleted_at IS NULL
                  AND (il.source_id = :workItemId OR il.target_id = :workItemId)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("workItemId", workItemId);
        Long total = jdbcTemplate.queryForObject(sql, params, Long.class);
        return total != null ? total : 0L;
    }

    @Override
    public List<WorkItemLinkProjection> listLinksByWorkItemId(Long workItemId, Long tenantId) {
        String sql = """
                SELECT
                    il.id,
                    il.source_id,
                    il.target_id,
                    il.link_type_id,
                    ilt.name AS link_type_name,
                    ilt.outward_desc,
                    ilt.inward_desc,
                    related.id AS related_work_item_id,
                    related.project_id AS related_project_id,
                    related.key AS related_work_item_key,
                    related.summary AS related_work_item_summary,
                    s.id AS related_status_id,
                    s.status_key AS related_status_key,
                    s.name AS related_status_name,
                    p.id AS related_priority_id,
                    p.name AS related_priority_name,
                    p.color AS related_priority_color
                FROM issue_links il
                JOIN issue_link_types ilt ON il.link_type_id = ilt.id
                    AND ilt.deleted_at IS NULL
                JOIN work_items related ON related.id = CASE
                        WHEN il.source_id = :workItemId THEN il.target_id
                        ELSE il.source_id
                    END
                    AND related.tenant_id = il.tenant_id
                    AND related.deleted_at IS NULL
                LEFT JOIN statuses s ON related.status_id = s.id
                    AND s.deleted_at IS NULL
                LEFT JOIN priorities p ON related.priority_id = p.id
                    AND p.deleted_at IS NULL
                WHERE il.tenant_id = :tenantId
                  AND il.deleted_at IS NULL
                  AND (il.source_id = :workItemId OR il.target_id = :workItemId)
                ORDER BY il.id DESC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("workItemId", workItemId);
        return jdbcTemplate.query(sql, params, linkRowMapper);
    }

    @Override
    public PageResult<WorkItemActivityProjection> listWorkItemActivities(
            Long workItemId,
            Long tenantId,
            String type,
            int page,
            int size) {
        String normalizedType = type == null || type.isBlank() ? "ALL" : type.trim().toUpperCase();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        String activitySql = """
                FROM (
                    SELECT
                        CONCAT('comment-', c.id) AS activity_id,
                        'COMMENT' AS activity_type,
                        c.id AS sort_id,
                        c.author_id AS actor_id,
                        c.body,
                        CAST(NULL AS VARCHAR) AS field_key,
                        CAST(NULL AS VARCHAR) AS field_name,
                        CAST(NULL AS VARCHAR) AS from_value,
                        CAST(NULL AS VARCHAR) AS to_value,
                        c.created_at
                    FROM work_item_comments c
                    WHERE c.tenant_id = :tenantId
                      AND c.work_item_id = :workItemId
                      AND c.deleted_at IS NULL
                    UNION ALL
                    SELECT
                        CONCAT('history-', h.id) AS activity_id,
                        'HISTORY' AS activity_type,
                        h.id AS sort_id,
                        h.actor_id,
                        CAST(NULL AS TEXT) AS body,
                        h.field_key,
                        h.field_name,
                        h.from_display_value AS from_value,
                        h.to_display_value AS to_value,
                        h.created_at
                    FROM work_item_history h
                    WHERE h.tenant_id = :tenantId
                      AND h.work_item_id = :workItemId
                      AND h.deleted_at IS NULL
                ) activity
                WHERE (:activityType = 'ALL' OR activity.activity_type = :activityType)
                """;
        String dataSql = "SELECT * " + activitySql + """
                ORDER BY created_at DESC NULLS LAST, sort_id DESC
                LIMIT :limit OFFSET :offset
                """;
        String countSql = "SELECT COUNT(*) " + activitySql;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("workItemId", workItemId)
                .addValue("activityType", normalizedType)
                .addValue("limit", safeSize)
                .addValue("offset", safePage * safeSize);

        var items = jdbcTemplate.query(dataSql, params, activityRowMapper);
        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        return new PageResult<>(items, total != null ? total : 0L);
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

    @Override
    public PageResult<WorkItemScheduleAllocationCalendarProjection> listScheduleAllocationCalendarItems(
            Long tenantId,
            WorkItemScheduleCalendarCriteria criteria
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("projectId", criteria.getProjectId())
                .addValue("limit", criteria.getPageSize())
                .addValue("offset", criteria.getPage() * criteria.getPageSize());

        String fromSql = buildScheduleCalendarFromSql();
        String whereSql = buildScheduleCalendarWhereSql(criteria, params);
        String dataSql = """
                SELECT
                    a.id AS allocation_id,
                    a.work_item_plan_id,
                    a.work_item_id,
                    a.project_id,
                    w.key,
                    w.summary,
                    a.assignee_id,
                    CAST(NULL AS VARCHAR) AS assignee_name,
                    CAST(NULL AS VARCHAR) AS assignee_avatar_url,
                    a.start_time,
                    a.end_time,
                    a.effort_millis,
                    a.source,
                    a.source_run_id,
                    a.source_run_item_id,
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
                 ORDER BY a.start_time ASC, a.end_time ASC, w.rank ASC NULLS LAST, a.id ASC
                 LIMIT :limit OFFSET :offset
                """;
        String countSql = "SELECT COUNT(*) " + fromSql + whereSql;

        List<WorkItemScheduleAllocationCalendarProjection> items = jdbcTemplate.query(
                dataSql,
                params,
                scheduleAllocationCalendarRowMapper
        );
        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        return new PageResult<>(items, total != null ? total : 0L);
    }

    @Override
    public List<WorkItemBoardStatusProjection> listBoardStatuses(Long tenantId, WorkItemBoardCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("projectId", criteria.getProjectId());

        StringBuilder sql = new StringBuilder("""
                WITH scheme_workflows AS (
                    SELECT scheme.default_workflow_id AS workflow_id
                    FROM projects p
                    JOIN workflow_schemes scheme
                      ON scheme.id = p.workflow_scheme_id
                     AND scheme.tenant_id = p.tenant_id
                     AND scheme.deleted_at IS NULL
                    WHERE p.tenant_id = :tenantId
                      AND p.id = :projectId
                      AND p.deleted_at IS NULL
                      AND scheme.default_workflow_id IS NOT NULL
                    UNION
                    SELECT scheme_item.workflow_id
                    FROM projects p
                    JOIN workflow_schemes scheme
                      ON scheme.id = p.workflow_scheme_id
                     AND scheme.tenant_id = p.tenant_id
                     AND scheme.deleted_at IS NULL
                    JOIN workflow_scheme_items scheme_item
                      ON scheme_item.scheme_id = scheme.id
                     AND scheme_item.tenant_id = p.tenant_id
                     AND scheme_item.deleted_at IS NULL
                    WHERE p.tenant_id = :tenantId
                      AND p.id = :projectId
                      AND p.deleted_at IS NULL
                )
                SELECT
                    st.id AS status_id,
                    st.status_key,
                    st.name AS status_name,
                    st.description AS status_description,
                    st.icon_url AS status_icon_url,
                    sc.id AS status_category_id,
                    sc.key AS status_category_key,
                    sc.name AS status_category_name
                FROM scheme_workflows scheme_workflow
                JOIN workflows wf
                  ON wf.id = scheme_workflow.workflow_id
                 AND wf.tenant_id = :tenantId
                 AND wf.deleted_at IS NULL
                JOIN workflow_versions wv
                  ON wv.id = wf.current_published_version_id
                 AND wv.tenant_id = :tenantId
                 AND wv.deleted_at IS NULL
                JOIN workflow_steps step
                  ON step.workflow_version_id = wv.id
                 AND step.tenant_id = :tenantId
                 AND step.deleted_at IS NULL
                JOIN statuses st
                  ON st.id = step.status_id
                 AND st.tenant_id = :tenantId
                 AND st.deleted_at IS NULL
                LEFT JOIN status_categories sc
                  ON sc.id = st.category_id
                 AND sc.tenant_id = :tenantId
                 AND sc.deleted_at IS NULL
                WHERE 1 = 1
                """);
        appendInFilter(sql, params, "st.id", "statusIds", criteria.getStatusIds());
        sql.append("""
                GROUP BY st.id, st.status_key, st.name, st.description, st.icon_url, sc.id, sc.key, sc.name
                ORDER BY MIN(step.step_order) ASC NULLS LAST, MIN(step.id) ASC
                """);

        return jdbcTemplate.query(sql.toString(), params, boardStatusRowMapper);
    }

    @Override
    public List<WorkItemBoardItemProjection> listBoardWorkItems(Long tenantId, WorkItemBoardCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("projectId", criteria.getProjectId());

        StringBuilder sql = new StringBuilder("""
                SELECT
                    w.id,
                    w.project_id,
                    w.parent_id,
                    w.key,
                    w.summary,
                    w.description,
                    w.assignee_id,
                    CAST(NULL AS VARCHAR) AS assignee_name,
                    CAST(NULL AS VARCHAR) AS assignee_avatar_url,
                    w.reporter_id,
                    w.start_date,
                    w.due_date,
                    w.rank,
                    it.id AS issue_type_id,
                    it.name AS issue_type_name,
                    it.icon_url AS issue_type_icon_url,
                    it.hierarchy_level AS issue_type_hierarchy_level,
                    st.id AS status_id,
                    st.status_key,
                    st.name AS status_name,
                    pr.id AS priority_id,
                    pr.name AS priority_name,
                    pr.icon_url AS priority_icon_url,
                    pr.color AS priority_color
                FROM work_items w
                LEFT JOIN issue_types it ON it.id = w.issue_type_id AND it.deleted_at IS NULL
                LEFT JOIN statuses st ON st.id = w.status_id AND st.deleted_at IS NULL
                LEFT JOIN priorities pr ON pr.id = w.priority_id AND pr.deleted_at IS NULL
                WHERE w.tenant_id = :tenantId
                  AND w.project_id = :projectId
                  AND w.deleted_at IS NULL
                """);

        appendInFilter(sql, params, "w.status_id", "statusIds", criteria.getStatusIds());
        appendInFilter(sql, params, "w.assignee_id", "assigneeIds", criteria.getAssigneeIds());
        appendInFilter(sql, params, "w.issue_type_id", "issueTypeIds", criteria.getIssueTypeIds());
        appendInFilter(sql, params, "w.priority_id", "priorityIds", criteria.getPriorityIds());
        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            params.addValue("keyword", "%" + criteria.getKeyword().trim().toLowerCase() + "%");
            sql.append("""
                    
                      AND (
                          LOWER(w.key) LIKE :keyword
                          OR LOWER(w.summary) LIKE :keyword
                      )
                    """);
        }
        sql.append("\nORDER BY w.status_id ASC, w.rank ASC NULLS LAST, w.id ASC");

        return jdbcTemplate.query(sql.toString(), params, boardItemRowMapper);
    }

    @Override
    public ProjectSummaryMetricsProjection getProjectSummaryMetrics(Long tenantId,
                                                                    ProjectSummaryCriteria criteria,
                                                                    Long now,
                                                                    Long sevenDaysAgo,
                                                                    Long sevenDaysAhead) {
        MapSqlParameterSource params = buildProjectSummaryParams(tenantId, criteria)
                .addValue("now", toTimestamp(now))
                .addValue("sevenDaysAgo", toTimestamp(sevenDaysAgo))
                .addValue("sevenDaysAhead", toTimestamp(sevenDaysAhead));

        StringBuilder sql = new StringBuilder("""
                SELECT
                    COUNT(*) FILTER (
                        WHERE sc.key = 'done'
                          AND w.updated_at >= :sevenDaysAgo
                          AND w.updated_at <= :now
                    ) AS completed_last_7_days,
                    COUNT(*) FILTER (
                        WHERE w.updated_at >= :sevenDaysAgo
                          AND w.updated_at <= :now
                    ) AS updated_last_7_days,
                    COUNT(*) FILTER (
                        WHERE w.created_at >= :sevenDaysAgo
                          AND w.created_at <= :now
                    ) AS created_last_7_days,
                    COUNT(*) FILTER (
                        WHERE w.due_date >= :now
                          AND w.due_date <= :sevenDaysAhead
                          AND w.resolution_id IS NULL
                          AND COALESCE(sc.key, '') <> 'done'
                    ) AS due_soon_next_7_days
                FROM work_items w
                LEFT JOIN statuses st ON st.id = w.status_id
                    AND st.tenant_id = w.tenant_id
                    AND st.deleted_at IS NULL
                LEFT JOIN status_categories sc ON sc.id = st.category_id
                    AND sc.tenant_id = w.tenant_id
                    AND sc.deleted_at IS NULL
                WHERE w.tenant_id = :tenantId
                  AND w.project_id = :projectId
                  AND w.deleted_at IS NULL
                """);
        appendProjectSummaryFilters(sql, params, criteria, "w");

        return jdbcTemplate.queryForObject(sql.toString(), params, (rs, rowNum) -> new ProjectSummaryMetricsProjection(
                rs.getLong("completed_last_7_days"),
                rs.getLong("updated_last_7_days"),
                rs.getLong("created_last_7_days"),
                rs.getLong("due_soon_next_7_days")
        ));
    }

    @Override
    public List<ProjectSummaryBreakdownProjection> listProjectSummaryStatuses(Long tenantId, ProjectSummaryCriteria criteria) {
        MapSqlParameterSource params = buildProjectSummaryParams(tenantId, criteria);
        StringBuilder filteredWhere = new StringBuilder("""
                WHERE w.tenant_id = :tenantId
                  AND w.project_id = :projectId
                  AND w.deleted_at IS NULL
                """);
        appendProjectSummaryFilters(filteredWhere, params, criteria, "w");

        String sql = """
                WITH scheme_workflows AS (
                    SELECT scheme.default_workflow_id AS workflow_id
                    FROM projects p
                    JOIN workflow_schemes scheme
                      ON scheme.id = p.workflow_scheme_id
                     AND scheme.tenant_id = p.tenant_id
                     AND scheme.deleted_at IS NULL
                    WHERE p.tenant_id = :tenantId
                      AND p.id = :projectId
                      AND p.deleted_at IS NULL
                      AND scheme.default_workflow_id IS NOT NULL
                    UNION
                    SELECT scheme_item.workflow_id
                    FROM projects p
                    JOIN workflow_schemes scheme
                      ON scheme.id = p.workflow_scheme_id
                     AND scheme.tenant_id = p.tenant_id
                     AND scheme.deleted_at IS NULL
                    JOIN workflow_scheme_items scheme_item
                      ON scheme_item.scheme_id = scheme.id
                     AND scheme_item.tenant_id = p.tenant_id
                     AND scheme_item.deleted_at IS NULL
                    WHERE p.tenant_id = :tenantId
                      AND p.id = :projectId
                      AND p.deleted_at IS NULL
                ),
                status_options AS (
                    SELECT
                        st.id,
                        st.status_key,
                        st.name,
                        st.icon_url,
                        sc.key AS category_key,
                        sc.name AS category_name,
                        MIN(step.step_order) AS sequence
                    FROM scheme_workflows scheme_workflow
                    JOIN workflows wf ON wf.id = scheme_workflow.workflow_id
                     AND wf.tenant_id = :tenantId
                     AND wf.deleted_at IS NULL
                    JOIN workflow_versions wv ON wv.id = wf.current_published_version_id
                     AND wv.tenant_id = :tenantId
                     AND wv.deleted_at IS NULL
                    JOIN workflow_steps step ON step.workflow_version_id = wv.id
                     AND step.tenant_id = :tenantId
                     AND step.deleted_at IS NULL
                    JOIN statuses st ON st.id = step.status_id
                     AND st.tenant_id = :tenantId
                     AND st.deleted_at IS NULL
                    LEFT JOIN status_categories sc ON sc.id = st.category_id
                     AND sc.tenant_id = :tenantId
                     AND sc.deleted_at IS NULL
                    GROUP BY st.id, st.status_key, st.name, st.icon_url, sc.key, sc.name
                ),
                filtered_counts AS (
                    SELECT w.status_id, COUNT(*) AS item_count
                    FROM work_items w
                    %s
                    GROUP BY w.status_id
                )
                SELECT
                    option.id,
                    option.status_key AS item_key,
                    option.name,
                    option.icon_url,
                    CAST(NULL AS VARCHAR) AS color,
                    option.sequence,
                    option.category_key,
                    option.category_name,
                    COALESCE(counts.item_count, 0) AS item_count
                FROM status_options option
                LEFT JOIN filtered_counts counts ON counts.status_id = option.id
                ORDER BY option.sequence ASC NULLS LAST, option.id ASC
                """.formatted(filteredWhere);

        return jdbcTemplate.query(sql, params, this::mapProjectSummaryBreakdown);
    }

    @Override
    public List<ProjectSummaryBreakdownProjection> listProjectSummaryPriorities(Long tenantId, ProjectSummaryCriteria criteria) {
        MapSqlParameterSource params = buildProjectSummaryParams(tenantId, criteria);
        StringBuilder filteredWhere = new StringBuilder("""
                WHERE w.tenant_id = :tenantId
                  AND w.project_id = :projectId
                  AND w.deleted_at IS NULL
                """);
        appendProjectSummaryFilters(filteredWhere, params, criteria, "w");

        String sql = """
                WITH filtered_counts AS (
                    SELECT w.priority_id, COUNT(*) AS item_count
                    FROM work_items w
                    %s
                    GROUP BY w.priority_id
                )
                SELECT
                    pr.id,
                    pr.priority_key AS item_key,
                    pr.name,
                    pr.icon_url,
                    pr.color,
                    COALESCE(psi.sequence, pr.sequence) AS sequence,
                    CAST(NULL AS VARCHAR) AS category_key,
                    CAST(NULL AS VARCHAR) AS category_name,
                    COALESCE(counts.item_count, 0) AS item_count
                FROM projects project
                JOIN priority_scheme_items psi ON psi.scheme_id = project.priority_scheme_id
                 AND psi.tenant_id = project.tenant_id
                 AND psi.deleted_at IS NULL
                JOIN priorities pr ON pr.id = psi.priority_id
                 AND pr.tenant_id = project.tenant_id
                 AND pr.deleted_at IS NULL
                LEFT JOIN filtered_counts counts ON counts.priority_id = pr.id
                WHERE project.tenant_id = :tenantId
                  AND project.id = :projectId
                  AND project.deleted_at IS NULL
                ORDER BY COALESCE(psi.sequence, pr.sequence) ASC NULLS LAST, pr.id ASC
                """.formatted(filteredWhere);

        return jdbcTemplate.query(sql, params, this::mapProjectSummaryBreakdown);
    }

    @Override
    public List<ProjectSummaryBreakdownProjection> listProjectSummaryIssueTypes(Long tenantId, ProjectSummaryCriteria criteria) {
        MapSqlParameterSource params = buildProjectSummaryParams(tenantId, criteria);
        StringBuilder filteredWhere = new StringBuilder("""
                WHERE w.tenant_id = :tenantId
                  AND w.project_id = :projectId
                  AND w.deleted_at IS NULL
                """);
        appendProjectSummaryFilters(filteredWhere, params, criteria, "w");

        String sql = """
                WITH filtered_counts AS (
                    SELECT w.issue_type_id, COUNT(*) AS item_count
                    FROM work_items w
                    %s
                    GROUP BY w.issue_type_id
                )
                SELECT
                    it.id,
                    it.type_key AS item_key,
                    it.name,
                    it.icon_url,
                    CAST(NULL AS VARCHAR) AS color,
                    COALESCE(itsi.sequence, it.hierarchy_level) AS sequence,
                    CAST(NULL AS VARCHAR) AS category_key,
                    CAST(NULL AS VARCHAR) AS category_name,
                    COALESCE(counts.item_count, 0) AS item_count
                FROM projects project
                JOIN issue_type_scheme_items itsi ON itsi.scheme_id = project.issue_type_scheme_id
                 AND itsi.tenant_id = project.tenant_id
                 AND itsi.deleted_at IS NULL
                JOIN issue_types it ON it.id = itsi.issue_type_id
                 AND it.tenant_id = project.tenant_id
                 AND it.deleted_at IS NULL
                LEFT JOIN filtered_counts counts ON counts.issue_type_id = it.id
                WHERE project.tenant_id = :tenantId
                  AND project.id = :projectId
                  AND project.deleted_at IS NULL
                ORDER BY COALESCE(itsi.sequence, it.hierarchy_level) ASC NULLS LAST, it.id ASC
                """.formatted(filteredWhere);

        return jdbcTemplate.query(sql, params, this::mapProjectSummaryBreakdown);
    }

    @Override
    public PageResult<ProjectSummaryActivityProjection> listProjectSummaryActivities(Long tenantId, ProjectSummaryCriteria criteria) {
        MapSqlParameterSource params = buildProjectSummaryParams(tenantId, criteria)
                .addValue("limit", criteria.getActivitySize())
                .addValue("offset", criteria.getActivityPage() * criteria.getActivitySize());
        StringBuilder filteredWhere = new StringBuilder("""
                WHERE w.tenant_id = :tenantId
                  AND w.project_id = :projectId
                  AND w.deleted_at IS NULL
                """);
        appendProjectSummaryFilters(filteredWhere, params, criteria, "w");

        String activitySql = """
                WITH filtered_items AS (
                    SELECT
                        w.id,
                        w.key,
                        w.summary,
                        st.id AS status_id,
                        st.status_key,
                        st.name AS status_name
                    FROM work_items w
                    LEFT JOIN statuses st ON st.id = w.status_id
                     AND st.tenant_id = w.tenant_id
                     AND st.deleted_at IS NULL
                    %s
                ),
                activity AS (
                    SELECT
                        CONCAT('comment-', c.id) AS activity_id,
                        'COMMENT' AS activity_type,
                        c.id AS sort_id,
                        c.author_id AS actor_id,
                        item.id AS work_item_id,
                        item.key AS work_item_key,
                        item.summary AS work_item_summary,
                        item.status_id,
                        item.status_key,
                        item.status_name,
                        c.body,
                        CAST(NULL AS VARCHAR) AS field_key,
                        CAST(NULL AS VARCHAR) AS field_name,
                        CAST(NULL AS VARCHAR) AS from_value,
                        CAST(NULL AS VARCHAR) AS to_value,
                        c.created_at
                    FROM filtered_items item
                    JOIN work_item_comments c ON c.work_item_id = item.id
                     AND c.tenant_id = :tenantId
                     AND c.deleted_at IS NULL
                    UNION ALL
                    SELECT
                        CONCAT('history-', h.id) AS activity_id,
                        'HISTORY' AS activity_type,
                        h.id AS sort_id,
                        h.actor_id,
                        item.id AS work_item_id,
                        item.key AS work_item_key,
                        item.summary AS work_item_summary,
                        item.status_id,
                        item.status_key,
                        item.status_name,
                        CAST(NULL AS TEXT) AS body,
                        h.field_key,
                        h.field_name,
                        h.from_display_value AS from_value,
                        h.to_display_value AS to_value,
                        h.created_at
                    FROM filtered_items item
                    JOIN work_item_history h ON h.work_item_id = item.id
                     AND h.tenant_id = :tenantId
                     AND h.deleted_at IS NULL
                )
                """.formatted(filteredWhere);
        String dataSql = activitySql + """
                SELECT *
                FROM activity
                ORDER BY created_at DESC NULLS LAST, sort_id DESC
                LIMIT :limit OFFSET :offset
                """;
        String countSql = activitySql + """
                SELECT COUNT(*)
                FROM activity
                """;

        List<ProjectSummaryActivityProjection> items = jdbcTemplate.query(dataSql, params, (rs, rowNum) -> new ProjectSummaryActivityProjection(
                rs.getString("activity_id"),
                rs.getString("activity_type"),
                getNullableLong(rs, "actor_id"),
                getNullableLong(rs, "work_item_id"),
                rs.getString("work_item_key"),
                rs.getString("work_item_summary"),
                getNullableLong(rs, "status_id"),
                rs.getString("status_key"),
                rs.getString("status_name"),
                rs.getString("body"),
                rs.getString("field_key"),
                rs.getString("field_name"),
                rs.getString("from_value"),
                rs.getString("to_value"),
                toEpochMilli(rs.getTimestamp("created_at"))
        ));
        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        return new PageResult<>(items, total != null ? total : 0L);
    }

    @Override
    public List<Long> listProjectSummaryAssigneeIds(Long tenantId, Long projectId) {
        String sql = """
                SELECT DISTINCT w.assignee_id
                FROM work_items w
                WHERE w.tenant_id = :tenantId
                  AND w.project_id = :projectId
                  AND w.assignee_id IS NOT NULL
                  AND w.deleted_at IS NULL
                ORDER BY w.assignee_id ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("projectId", projectId);
        return jdbcTemplate.queryForList(sql, params, Long.class);
    }

    @Override
    public List<ProjectSummaryParentOptionProjection> listProjectSummaryParentOptions(Long tenantId, Long projectId) {
        String sql = """
                SELECT DISTINCT parent.id, parent.key, parent.summary
                FROM work_items child
                JOIN work_items parent ON parent.id = child.parent_id
                 AND parent.tenant_id = child.tenant_id
                 AND parent.project_id = child.project_id
                 AND parent.deleted_at IS NULL
                WHERE child.tenant_id = :tenantId
                  AND child.project_id = :projectId
                  AND child.parent_id IS NOT NULL
                  AND child.deleted_at IS NULL
                ORDER BY parent.key ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("projectId", projectId);
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new ProjectSummaryParentOptionProjection(
                getNullableLong(rs, "id"),
                rs.getString("key"),
                rs.getString("summary")
        ));
    }

    private MapSqlParameterSource buildProjectSummaryParams(Long tenantId, ProjectSummaryCriteria criteria) {
        return new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("projectId", criteria.getProjectId());
    }

    private void appendProjectSummaryFilters(StringBuilder whereSql,
                                             MapSqlParameterSource params,
                                             ProjectSummaryCriteria criteria,
                                             String alias) {
        appendInFilter(whereSql, params, alias + ".status_id", "summaryStatusIds", criteria.getStatusIds());
        appendInFilter(whereSql, params, alias + ".assignee_id", "summaryAssigneeIds", criteria.getAssigneeIds());
        appendInFilter(whereSql, params, alias + ".issue_type_id", "summaryIssueTypeIds", criteria.getIssueTypeIds());
        appendInFilter(whereSql, params, alias + ".priority_id", "summaryPriorityIds", criteria.getPriorityIds());
        if (criteria.getParentId() != null) {
            params.addValue("summaryParentId", criteria.getParentId());
            whereSql.append("\n  AND ").append(alias).append(".parent_id = :summaryParentId");
        }
        appendTimestampRange(whereSql, params, alias + ".created_at", "summaryCreated",
                criteria.getCreatedFrom(), criteria.getCreatedTo());
        appendTimestampRange(whereSql, params, alias + ".updated_at", "summaryUpdated",
                criteria.getUpdatedFrom(), criteria.getUpdatedTo());
        appendTimestampRange(whereSql, params, alias + ".due_date", "summaryDueDate",
                criteria.getDueDateFrom(), criteria.getDueDateTo());
    }

    private void appendTimestampRange(StringBuilder whereSql,
                                      MapSqlParameterSource params,
                                      String column,
                                      String paramPrefix,
                                      Long from,
                                      Long to) {
        Timestamp fromTimestamp = toTimestamp(from);
        Timestamp toTimestamp = toTimestamp(to);
        if (fromTimestamp != null) {
            params.addValue(paramPrefix + "From", fromTimestamp);
            whereSql.append("\n  AND ").append(column).append(" >= :").append(paramPrefix).append("From");
        }
        if (toTimestamp != null) {
            params.addValue(paramPrefix + "To", toTimestamp);
            whereSql.append("\n  AND ").append(column).append(" <= :").append(paramPrefix).append("To");
        }
    }

    private ProjectSummaryBreakdownProjection mapProjectSummaryBreakdown(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new ProjectSummaryBreakdownProjection(
                getNullableLong(rs, "id"),
                rs.getString("item_key"),
                rs.getString("name"),
                rs.getString("icon_url"),
                rs.getString("color"),
                getNullableInt(rs, "sequence"),
                rs.getString("category_key"),
                rs.getString("category_name"),
                rs.getLong("item_count")
        );
    }

    private Long getNullableLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private Integer getNullableInt(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private Long toEpochMilli(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.getTime();
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

    private String buildScheduleCalendarFromSql() {
        return """
                FROM work_item_plan_allocations a
                JOIN work_item_plans p
                  ON p.id = a.work_item_plan_id
                 AND p.tenant_id = a.tenant_id
                 AND p.project_id = a.project_id
                 AND p.work_item_id = a.work_item_id
                 AND p.deleted_at IS NULL
                JOIN work_items w
                  ON w.id = a.work_item_id
                 AND w.tenant_id = a.tenant_id
                 AND w.project_id = a.project_id
                 AND w.deleted_at IS NULL
                LEFT JOIN issue_types it ON it.id = w.issue_type_id AND it.deleted_at IS NULL
                LEFT JOIN statuses st ON st.id = w.status_id AND st.deleted_at IS NULL
                LEFT JOIN priorities pr ON pr.id = w.priority_id AND pr.deleted_at IS NULL
                """;
    }

    private String buildScheduleCalendarWhereSql(WorkItemScheduleCalendarCriteria criteria,
                                                 MapSqlParameterSource params) {
        StringBuilder whereSql = new StringBuilder("""
                WHERE a.tenant_id = :tenantId
                  AND a.project_id = :projectId
                """);

        appendInFilter(whereSql, params, "w.status_id", "statusIds", criteria.getStatusIds());
        appendInFilter(whereSql, params, "a.assignee_id", "assigneeIds", criteria.getAssigneeIds());
        appendInFilter(whereSql, params, "w.issue_type_id", "issueTypeIds", criteria.getIssueTypeIds());
        appendKeywordFilter(whereSql, params, "w", criteria.getKeyword());
        appendScheduleCalendarViewportFilter(whereSql, params, criteria);
        return whereSql.toString();
    }

    private void appendKeywordFilter(StringBuilder whereSql,
                                     MapSqlParameterSource params,
                                     String workItemAlias,
                                     String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        params.addValue("keyword", "%" + keyword.trim().toLowerCase() + "%");
        whereSql.append("\n  AND (LOWER(")
                .append(workItemAlias)
                .append(".key) LIKE :keyword OR LOWER(")
                .append(workItemAlias)
                .append(".summary) LIKE :keyword)");
    }

    private void appendScheduleCalendarViewportFilter(StringBuilder whereSql,
                                                      MapSqlParameterSource params,
                                                      WorkItemScheduleCalendarCriteria criteria) {
        Timestamp viewportStart = toTimestamp(criteria.getViewportStart());
        Timestamp viewportEnd = toTimestamp(criteria.getViewportEnd());
        if (viewportStart != null) {
            params.addValue("scheduleViewportStart", viewportStart);
            whereSql.append("\n  AND a.end_time > :scheduleViewportStart");
        }
        if (viewportEnd != null) {
            params.addValue("scheduleViewportEnd", viewportEnd);
            whereSql.append("\n  AND a.start_time < :scheduleViewportEnd");
        }
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
