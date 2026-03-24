/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.PermissionSchemeEntity;

import java.util.Optional;

public interface IPermissionSchemePort {
    PermissionSchemeEntity createPermissionScheme(PermissionSchemeEntity scheme);

    Optional<PermissionSchemeEntity> getPermissionSchemeById(Long schemeId, Long tenantId);

    Optional<PermissionSchemeEntity> getPermissionSchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
