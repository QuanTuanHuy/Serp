/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.TenantSchemeMappingEntity;
import serp.project.pmcore.core.domain.enums.SchemeType;

import java.util.Optional;

public interface ITenantSchemeMappingPort {

    Optional<TenantSchemeMappingEntity> getMapping(Long tenantId, SchemeType schemeType, Long sourceSchemeId);

    TenantSchemeMappingEntity saveMapping(TenantSchemeMappingEntity mapping);
}
