/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.TenantSchemeDefaultEntity;

import java.util.List;

public interface ITenantSchemeDefaultPort {
    List<TenantSchemeDefaultEntity> getDefaultsByTenantId(Long tenantId);

    List<TenantSchemeDefaultEntity> getDefaultsByTenantIdIncludingSystem(Long tenantId);
}
