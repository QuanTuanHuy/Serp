/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.port.store;

import java.util.List;

import serp.project.pmcore.domain.shared.entity.TenantSchemeDefaultEntity;

public interface ITenantSchemeDefaultPort {
    List<TenantSchemeDefaultEntity> getDefaultsByTenantId(Long tenantId);

    List<TenantSchemeDefaultEntity> getDefaultsByTenantIdIncludingSystem(Long tenantId);
}
