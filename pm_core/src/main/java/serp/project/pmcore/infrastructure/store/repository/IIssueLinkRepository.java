/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.IssueLinkModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IIssueLinkRepository extends JpaRepository<IssueLinkModel, Long> {

    Optional<IssueLinkModel> findByIdAndTenantId(Long id, Long tenantId);

    Optional<IssueLinkModel> findFirstByTenantIdAndSourceIdAndTargetIdAndLinkTypeIdOrderByIdAsc(
            Long tenantId,
            Long sourceId,
            Long targetId,
            Long linkTypeId
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            DELETE FROM IssueLinkModel il
            WHERE il.id = :id
              AND il.tenantId = :tenantId
            """)
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query(value = """
            SELECT
                il.id AS link_id,
                il.source_id,
                il.target_id,
                il.link_type_id,
                ilt.name AS link_type_name,
                ilt.dependency_behavior,
                ilt.outward_desc,
                ilt.inward_desc,
                CASE
                    WHEN il.source_id = :workItemId THEN il.target_id
                    ELSE il.source_id
                END AS related_work_item_id,
                wi.project_id AS related_project_id,
                wi.key AS related_work_item_key,
                wi.summary AS related_work_item_summary,
                wi.issue_type_id AS related_issue_type_id,
                it.name AS related_issue_type_name,
                wi.status_id AS related_status_id,
                st.name AS related_status_name,
                il.created_at,
                il.created_by
            FROM issue_links il
            JOIN issue_link_types ilt
              ON ilt.id = il.link_type_id
             AND ilt.deleted_at IS NULL
            JOIN work_items wi
              ON wi.id = CASE
                    WHEN il.source_id = :workItemId THEN il.target_id
                    ELSE il.source_id
                END
             AND wi.deleted_at IS NULL
            JOIN issue_types it
              ON it.id = wi.issue_type_id
             AND it.deleted_at IS NULL
            JOIN statuses st
              ON st.id = wi.status_id
             AND st.deleted_at IS NULL
            WHERE il.tenant_id = :tenantId
              AND il.deleted_at IS NULL
              AND (il.source_id = :workItemId OR il.target_id = :workItemId)
            ORDER BY il.id DESC
            """, nativeQuery = true)
    List<IssueLinkDetailRow> findIssueLinkDetailsByWorkItemId(@Param("tenantId") Long tenantId,
                                                              @Param("workItemId") Long workItemId);

    @Query(value = """
            SELECT
                il.id AS link_id,
                il.source_id,
                il.target_id,
                il.link_type_id,
                ilt.name AS link_type_name,
                ilt.dependency_behavior,
                ilt.outward_desc,
                ilt.inward_desc,
                CASE
                    WHEN il.source_id IN (:workItemIds) THEN il.target_id
                    ELSE il.source_id
                END AS related_work_item_id,
                wi.project_id AS related_project_id,
                wi.key AS related_work_item_key,
                wi.summary AS related_work_item_summary,
                wi.issue_type_id AS related_issue_type_id,
                it.name AS related_issue_type_name,
                wi.status_id AS related_status_id,
                st.name AS related_status_name,
                il.created_at,
                il.created_by
            FROM issue_links il
            JOIN issue_link_types ilt
              ON ilt.id = il.link_type_id
             AND ilt.deleted_at IS NULL
            JOIN work_items wi
              ON wi.id = CASE
                    WHEN il.source_id IN (:workItemIds) THEN il.target_id
                    ELSE il.source_id
                END
             AND wi.deleted_at IS NULL
            JOIN issue_types it
              ON it.id = wi.issue_type_id
             AND it.deleted_at IS NULL
            JOIN statuses st
              ON st.id = wi.status_id
             AND st.deleted_at IS NULL
            WHERE il.tenant_id = :tenantId
              AND il.deleted_at IS NULL
              AND (il.source_id IN (:workItemIds) OR il.target_id IN (:workItemIds))
            ORDER BY il.id DESC
            """, nativeQuery = true)
    List<IssueLinkDetailRow> findIssueLinkDetailsByWorkItemIds(@Param("tenantId") Long tenantId,
                                                               @Param("workItemIds") List<Long> workItemIds);

    interface IssueLinkDetailRow {
        Long getLinkId();

        Long getSourceId();

        Long getTargetId();

        Long getLinkTypeId();

        String getLinkTypeName();

        String getDependencyBehavior();

        String getOutwardDesc();

        String getInwardDesc();

        Long getRelatedWorkItemId();

        Long getRelatedProjectId();

        String getRelatedWorkItemKey();

        String getRelatedWorkItemSummary();

        Long getRelatedIssueTypeId();

        String getRelatedIssueTypeName();

        Long getRelatedStatusId();

        String getRelatedStatusName();

        java.sql.Timestamp getCreatedAt();

        Long getCreatedBy();
    }
}
