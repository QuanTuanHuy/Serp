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

import serp.project.pmcore.infrastructure.store.model.WorkItemModel;
import serp.project.pmcore.domain.workitem.dto.WorkItemDetailProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWorkItemRepository extends JpaRepository<WorkItemModel, Long> {
    Optional<WorkItemModel> findByIdAndTenantId(Long id, Long tenantId);

    List<WorkItemModel> findAllByTenantIdAndProjectId(Long tenantId, Long projectId);

    List<WorkItemModel> findAllByTenantIdAndIssueTypeId(Long tenantId, Long issueId);

    List<WorkItemModel> findAllByTenantIdAndPriorityId(Long tenantId, Long priorityId);

    @Query("SELECT DISTINCT w.issueTypeId FROM WorkItemModel w WHERE w.tenantId = :tenantId AND w.projectId IN :projectIds AND w.issueTypeId IN :issueTypeIds AND w.deletedAt IS NULL")
    List<Long> findDistinctIssueTypeIdsInUseByProjectIds(@Param("tenantId") Long tenantId,
                                                         @Param("projectIds") List<Long> projectIds,
                                                         @Param("issueTypeIds") List<Long> issueTypeIds);

    List<WorkItemModel> findAllByTenantIdAndParentId(Long tenantId, Long parentId);

    Optional<WorkItemModel> findFirstByTenantIdAndProjectIdOrderByRankDescIdDesc(Long tenantId, Long projectId);

    @Modifying
    @Query("UPDATE WorkItemModel w SET w.deletedAt = CURRENT_TIMESTAMP WHERE w.id = :id AND w.tenantId = :tenantId AND w.deletedAt IS NULL")
    void deleteByIdAndTenantId(Long id, Long tenantId);

    @Query(value = """
            SELECT
                w.id AS id,
                w.project_id AS projectId,
                w.issue_no AS issueNo,
                w.key AS key,
                w.summary AS summary,
                w.description AS description,
                w.resolution_id AS resolutionId,
                w.parent_id AS parentId,
                w.security_level_id AS securityLevelId,
                w.due_date AS dueDate,
                w.rank AS rank,
                w.time_original_estimate AS timeOriginalEstimate,
                w.time_remaining_estimate AS timeRemainingEstimate,
                w.time_spent AS timeSpent,
                w.created_at AS createdAt,
                w.created_by AS createdBy,
                w.updated_at AS updatedAt,
                w.updated_by AS updatedBy,
                w.assignee_id AS assigneeId,
                w.reporter_id AS reporterId,
                it.id AS issueTypeId,
                it.name AS issueTypeName,
                p.id AS priorityId,
                p.name AS priorityName,
                p.color AS priorityColor,
                s.id AS statusId,
                s.name AS statusName,
                ws.id AS workflowStepId,
                ws.name AS workflowStepName
            FROM work_items w
            LEFT JOIN issue_types it ON w.issue_type_id = it.id
            LEFT JOIN priorities p ON w.priority_id = p.id
            LEFT JOIN statuses s ON w.status_id = s.id
            LEFT JOIN workflow_steps ws ON w.workflow_step_id = ws.id
            WHERE w.id = :workItemId
                AND w.tenant_id = :tenantId
                AND w.deleted_at IS NULL
           """, nativeQuery = true)
    Optional<WorkItemDetailProjection> findWorkItemDetailById(@Param("workItemId") Long workItemId,
                                                              @Param("tenantId") Long tenantId);
}
