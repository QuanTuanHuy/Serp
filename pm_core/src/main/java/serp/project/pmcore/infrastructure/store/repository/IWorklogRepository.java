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
import serp.project.pmcore.infrastructure.store.model.WorklogModel;

import java.util.Optional;

@Repository
public interface IWorklogRepository extends JpaRepository<WorklogModel, Long> {
    Optional<WorklogModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("""
            SELECT w
            FROM WorklogModel w
            WHERE w.tenantId = :tenantId
              AND w.workItemId = :workItemId
              AND (:authorId IS NULL OR w.authorId = :authorId)
            """)
    Page<WorklogModel> findAllByWorkItemIdWithFilters(@Param("tenantId") Long tenantId,
                                                      @Param("workItemId") Long workItemId,
                                                      @Param("authorId") Long authorId,
                                                      Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(w.timeSpent), 0)
            FROM WorklogModel w
            WHERE w.tenantId = :tenantId
              AND w.workItemId = :workItemId
              AND w.deletedAt IS NULL
            """)
    Long sumActiveTimeSpentByWorkItemId(@Param("workItemId") Long workItemId,
                                        @Param("tenantId") Long tenantId);
}
