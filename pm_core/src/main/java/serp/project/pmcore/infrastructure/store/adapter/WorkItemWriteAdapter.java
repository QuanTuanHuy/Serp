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
import serp.project.pmcore.infrastructure.store.repository.IWorkItemRepository;

import java.sql.Timestamp;
import java.time.Instant;
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
//    private static final String SOFT_DELETE_WORKLOGS_SQL = """
//            UPDATE worklogs
//               SET deleted_at = :deletedAt,
//                   updated_at = :deletedAt,
//                   updated_by = :userId
//             WHERE tenant_id = :tenantId
//               AND work_item_id IN (:workItemIds)
//               AND deleted_at IS NULL
//            """;
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
//    private static final String SOFT_DELETE_ISSUE_LINKS_SQL = """
//            UPDATE issue_links
//               SET deleted_at = :deletedAt,
//                   updated_at = :deletedAt,
//                   updated_by = :userId
//             WHERE tenant_id = :tenantId
//               AND (source_id IN (:workItemIds) OR target_id IN (:workItemIds))
//               AND deleted_at IS NULL
//            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final IWorkItemRepository workItemRepository;
    private final WorkItemMapper workItemMapper;

    @Override
    public WorkItemEntity saveWorkItem(WorkItemEntity workItem) {
        return workItemMapper.toEntity(
                workItemRepository.save(workItemMapper.toModel(workItem))
        );
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
//        int deletedWorklogCount = jdbcTemplate.update(SOFT_DELETE_WORKLOGS_SQL, params);
//        int deletedCustomFieldValueCount = jdbcTemplate.update(SOFT_DELETE_CUSTOM_FIELD_VALUES_SQL, params);
//        int deletedLinkCount = jdbcTemplate.update(SOFT_DELETE_ISSUE_LINKS_SQL, params);

        return WorkItemDeleteExecutionResult.builder()
                .deletedWorkItemCount(deletedWorkItemCount)
                .deletedRelationCount(0
                )
                .deletedLinkCount(0)
                .build();
    }
}
