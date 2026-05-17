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
import serp.project.pmcore.infrastructure.store.model.ProjectComponentModel;
import serp.project.pmcore.infrastructure.store.model.WorkItemComponentModel;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface IWorkItemComponentRepository extends JpaRepository<WorkItemComponentModel, Long> {

    @Query("""
            SELECT wc.componentId
            FROM WorkItemComponentModel wc
            WHERE wc.tenantId = :tenantId
              AND wc.workItemId = :workItemId
              AND wc.componentId IN :componentIds
              AND wc.deletedAt IS NULL
            """)
    List<Long> findActiveComponentIds(@Param("workItemId") Long workItemId,
                                      @Param("tenantId") Long tenantId,
                                      @Param("componentIds") Collection<Long> componentIds);

    @Query("""
            SELECT c
            FROM WorkItemComponentModel wc
            JOIN ProjectComponentModel c ON c.id = wc.componentId
            WHERE wc.tenantId = :tenantId
              AND wc.workItemId = :workItemId
              AND wc.deletedAt IS NULL
              AND c.deletedAt IS NULL
            ORDER BY COALESCE(wc.sequence, 2147483647), c.id
            """)
    List<ProjectComponentModel> findActiveComponentsByWorkItemId(@Param("workItemId") Long workItemId,
                                                                  @Param("tenantId") Long tenantId);

    @Query("""
            SELECT wc
            FROM WorkItemComponentModel wc
            WHERE wc.tenantId = :tenantId
              AND wc.workItemId IN :workItemIds
              AND wc.deletedAt IS NULL
            ORDER BY wc.workItemId, COALESCE(wc.sequence, 2147483647), wc.componentId
            """)
    List<WorkItemComponentModel> findActiveLinksByTenantIdAndWorkItemIdIn(@Param("tenantId") Long tenantId,
                                                                          @Param("workItemIds") Collection<Long> workItemIds);

    @Modifying
    @Query("""
            UPDATE WorkItemComponentModel wc
            SET wc.deletedAt = :deletedAt,
                wc.updatedAt = :deletedAt,
                wc.updatedBy = :userId
            WHERE wc.tenantId = :tenantId
              AND wc.workItemId = :workItemId
              AND wc.componentId = :componentId
              AND wc.deletedAt IS NULL
            """)
    int softDeleteActiveLink(@Param("workItemId") Long workItemId,
                             @Param("componentId") Long componentId,
                             @Param("tenantId") Long tenantId,
                             @Param("userId") Long userId,
                             @Param("deletedAt") LocalDateTime deletedAt);
}
