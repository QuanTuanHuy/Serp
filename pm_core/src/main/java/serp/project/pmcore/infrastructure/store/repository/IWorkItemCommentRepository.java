/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.WorkItemCommentModel;

import java.util.Optional;

@Repository
public interface IWorkItemCommentRepository extends JpaRepository<WorkItemCommentModel, Long> {

    Optional<WorkItemCommentModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("""
            SELECT c
            FROM WorkItemCommentModel c
            WHERE c.tenantId = :tenantId
              AND c.workItemId = :workItemId
              AND c.deletedAt IS NULL
            """)
    Page<WorkItemCommentModel> findAllActiveByWorkItemId(@Param("tenantId") Long tenantId,
                                                         @Param("workItemId") Long workItemId,
                                                         Pageable pageable);

    @Query("""
            SELECT COUNT(c.id)
            FROM WorkItemCommentModel c
            WHERE c.tenantId = :tenantId
              AND c.workItemId = :workItemId
              AND c.deletedAt IS NULL
            """)
    long countActiveByWorkItemId(@Param("tenantId") Long tenantId,
                                 @Param("workItemId") Long workItemId);
}
