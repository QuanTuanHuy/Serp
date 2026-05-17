/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.OptimizationRunWarningModel;

import java.util.List;

@Repository
public interface IOptimizationRunWarningRepository extends JpaRepository<OptimizationRunWarningModel, Long> {
    List<OptimizationRunWarningModel> findAllByTenantIdAndRunId(Long tenantId, Long runId);
}
