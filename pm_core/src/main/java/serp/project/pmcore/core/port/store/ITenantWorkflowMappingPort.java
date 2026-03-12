/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.TenantWorkflowMappingEntity;

import java.util.Optional;

public interface ITenantWorkflowMappingPort {

    Optional<TenantWorkflowMappingEntity> getMapping(Long tenantId, Long sourceWorkflowId);

    TenantWorkflowMappingEntity saveMapping(TenantWorkflowMappingEntity mapping);
}
