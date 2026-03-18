/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.TenantWorkflowMappingEntity;

import java.util.Optional;

public interface ITenantWorkflowMappingPort {

    Optional<TenantWorkflowMappingEntity> getMapping(Long tenantId, Long sourceWorkflowId);

    TenantWorkflowMappingEntity saveMapping(TenantWorkflowMappingEntity mapping);
}
