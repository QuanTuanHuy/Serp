/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.port.store;

import serp.project.pmcore.domain.shared.entity.TenantSchemeMappingEntity;
import serp.project.pmcore.domain.shared.enums.SchemeType;

import java.util.Optional;

public interface ITenantSchemeMappingPort {

    Optional<TenantSchemeMappingEntity> getMapping(Long tenantId, SchemeType schemeType, Long sourceSchemeId);

    TenantSchemeMappingEntity saveMapping(TenantSchemeMappingEntity mapping);
}
