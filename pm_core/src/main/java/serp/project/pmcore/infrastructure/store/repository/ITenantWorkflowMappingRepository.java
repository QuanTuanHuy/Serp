/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.TenantWorkflowMappingModel;

import java.util.Optional;

@Repository
public interface ITenantWorkflowMappingRepository extends JpaRepository<TenantWorkflowMappingModel, Long> {

    Optional<TenantWorkflowMappingModel> findByTenantIdAndSourceWorkflowId(Long tenantId, Long sourceWorkflowId);
}
