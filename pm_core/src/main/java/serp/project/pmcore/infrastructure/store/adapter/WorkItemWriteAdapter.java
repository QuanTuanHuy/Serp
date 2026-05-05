/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workitem.dto.WorkItemDeleteExecutionResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemWritePort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemComponentRepository;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WorkItemWriteAdapter implements IWorkItemWritePort {

    private static final String SOFT_DELETE_WORK_ITEMS_SQL = """
            UPDATE work_items
               SET deleted_at = :deletedAt,
                   updated_at = :deletedAt,
                   updated_by = :userId
             WHERE tenant_id = :tenantId
               AND project_id = :projectId
               AND id IN (:workItemIds)
               AND deleted_at IS NULL
            """;

//    private static final String SOFT_DELETE_WORK_ITEM_COMPONENTS_SQL = """
//            UPDATE work_item_components
//               SET deleted_at = :deletedAt,
//                   updated_at = :deletedAt,
//                   updated_by = :userId
//             WHERE tenant_id = :tenantId
//               AND work_item_id IN (:workItemIds)
//               AND deleted_at IS NULL
//            """;
//
//    private static final String SOFT_DELETE_WORK_ITEM_FIX_VERSIONS_SQL = """
//            UPDATE work_item_fix_versions
//               SET deleted_at = :deletedAt,
//                   updated_at = :deletedAt,
//                   updated_by = :userId
//             WHERE tenant_id = :tenantId
//               AND work_item_id IN (:workItemIds)
//               AND deleted_at IS NULL
//            """;
//
//    private static final String SOFT_DELETE_WORK_ITEM_SPRINTS_SQL = """
//            UPDATE work_item_sprints
//               SET deleted_at = :deletedAt,
//                   updated_at = :deletedAt,
//                   updated_by = :userId
//             WHERE tenant_id = :tenantId
//               AND work_item_id IN (:workItemIds)
//               AND deleted_at IS NULL
//            """;
//
    private static final String SOFT_DELETE_WORK_ITEM_COMPONENTS_SQL = """
            UPDATE work_item_components
               SET deleted_at = :deletedAt,
                   updated_at = :deletedAt,
                   updated_by = :userId
             WHERE tenant_id = :tenantId
               AND work_item_id IN (:workItemIds)
               AND deleted_at IS NULL
            """;

    private static final String SOFT_DELETE_WORKLOGS_SQL = """
            UPDATE worklogs
               SET deleted_at = :deletedAt,
                   updated_at = :deletedAt,
                   updated_by = :userId
             WHERE tenant_id = :tenantId
               AND work_item_id IN (:workItemIds)
               AND deleted_at IS NULL
            """;
//
//    private static final String SOFT_DELETE_CUSTOM_FIELD_VALUES_SQL = """
//            UPDATE work_item_custom_field_values
//               SET deleted_at = :deletedAt,
//                   updated_at = :deletedAt,
//                   updated_by = :userId
//             WHERE tenant_id = :tenantId
//               AND work_item_id IN (:workItemIds)
//               AND deleted_at IS NULL
//            """;
//
    private static final String SOFT_DELETE_ISSUE_LINKS_SQL = """
            UPDATE issue_links
               SET deleted_at = :deletedAt,
                   updated_at = :deletedAt,
                   updated_by = :userId
             WHERE tenant_id = :tenantId
               AND (source_id IN (:workItemIds) OR target_id IN (:workItemIds))
               AND deleted_at IS NULL
            """;

    private static final String INSERT_WORK_ITEM_COMPONENT_SQL = """
            INSERT INTO work_item_components (
                tenant_id,
                work_item_id,
                component_id,
                created_at,
                created_by,
                updated_at,
                updated_by
            )
            VALUES (
                :tenantId,
                :workItemId,
                :componentId,
                :createdAt,
                :createdBy,
                :updatedAt,
                :updatedBy
            )
            ON CONFLICT (tenant_id, work_item_id, component_id) WHERE deleted_at IS NULL DO NOTHING
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final IWorkItemRepository workItemRepository;
    private final IWorkItemComponentRepository workItemComponentRepository;
    private final WorkItemMapper workItemMapper;

    @Override
    public WorkItemEntity saveWorkItem(WorkItemEntity workItem) {
        var model = workItemMapper.toModel(workItem);
        if (model == null) {
            throw new IllegalArgumentException("workItem must not be null");
        }
        return workItemMapper.toEntity(workItemRepository.save(model));
    }

    @Override
    public WorkItemDeleteExecutionResult softDeleteWorkItems(Long projectId,
                                                             Long tenantId,
                                                             Set<Long> workItemIds,
                                                             Long userId,
                                                             Long deletedAt) {
        if (workItemIds == null || workItemIds.isEmpty()) {
            return WorkItemDeleteExecutionResult.builder()
                    .deletedWorkItemCount(0)
                    .deletedRelationCount(0)
                    .deletedLinkCount(0)
                    .build();
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("projectId", projectId)
                .addValue("tenantId", tenantId)
                .addValue("workItemIds", workItemIds)
                .addValue("userId", userId)
                .addValue("deletedAt", Timestamp.from(Instant.ofEpochMilli(deletedAt)));

        int deletedWorkItemCount = jdbcTemplate.update(SOFT_DELETE_WORK_ITEMS_SQL, params);
//        int deletedComponentCount = jdbcTemplate.update(SOFT_DELETE_WORK_ITEM_COMPONENTS_SQL, params);
//        int deletedFixVersionCount = jdbcTemplate.update(SOFT_DELETE_WORK_ITEM_FIX_VERSIONS_SQL, params);
//        int deletedSprintCount = jdbcTemplate.update(SOFT_DELETE_WORK_ITEM_SPRINTS_SQL, params);
        int deletedComponentCount = jdbcTemplate.update(SOFT_DELETE_WORK_ITEM_COMPONENTS_SQL, params);
        int deletedWorklogCount = jdbcTemplate.update(SOFT_DELETE_WORKLOGS_SQL, params);
//        int deletedCustomFieldValueCount = jdbcTemplate.update(SOFT_DELETE_CUSTOM_FIELD_VALUES_SQL, params);
        int deletedLinkCount = jdbcTemplate.update(SOFT_DELETE_ISSUE_LINKS_SQL, params);

        return WorkItemDeleteExecutionResult.builder()
                .deletedWorkItemCount(deletedWorkItemCount)
                .deletedRelationCount(deletedComponentCount + deletedWorklogCount)
                .deletedLinkCount(deletedLinkCount)
                .build();
    }

    @Override
    public void addWorkItemComponents(Long workItemId,
                                      Long tenantId,
                                      Long userId,
                                      List<Long> componentIds) {
        if (componentIds == null || componentIds.isEmpty()) {
            return;
        }

        List<Long> normalizedIds = new ArrayList<>(new LinkedHashSet<>(componentIds));
        LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), java.time.ZoneOffset.UTC);

        for (Long componentId : normalizedIds) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("tenantId", tenantId)
                    .addValue("workItemId", workItemId)
                    .addValue("componentId", componentId)
                    .addValue("createdAt", now)
                    .addValue("createdBy", userId)
                    .addValue("updatedAt", now)
                    .addValue("updatedBy", userId);
            jdbcTemplate.update(INSERT_WORK_ITEM_COMPONENT_SQL, params);
        }
    }

    @Override
    public boolean removeWorkItemComponent(Long workItemId,
                                           Long componentId,
                                           Long tenantId,
                                           Long userId,
                                           Long deletedAt) {
        return workItemComponentRepository.softDeleteActiveLink(
                workItemId,
                componentId,
                tenantId,
                userId,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(deletedAt), java.time.ZoneOffset.UTC)
        ) > 0;
    }
}
