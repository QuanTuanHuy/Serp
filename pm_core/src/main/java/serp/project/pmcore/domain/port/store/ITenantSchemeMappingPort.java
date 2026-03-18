/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.TenantSchemeMappingEntity;
import serp.project.pmcore.domain.enums.SchemeType;

import java.util.Optional;

public interface ITenantSchemeMappingPort {

    Optional<TenantSchemeMappingEntity> getMapping(Long tenantId, SchemeType schemeType, Long sourceSchemeId);

    TenantSchemeMappingEntity saveMapping(TenantSchemeMappingEntity mapping);
}
