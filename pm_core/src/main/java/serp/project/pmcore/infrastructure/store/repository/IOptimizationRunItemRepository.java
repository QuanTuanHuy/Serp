/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.OptimizationRunItemModel;

import java.util.List;

@Repository
public interface IOptimizationRunItemRepository extends JpaRepository<OptimizationRunItemModel, Long> {
    List<OptimizationRunItemModel> findAllByTenantIdAndRunId(Long tenantId, Long runId);
}
