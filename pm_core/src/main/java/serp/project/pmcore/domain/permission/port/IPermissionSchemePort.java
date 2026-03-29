/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.permission.port;

import java.util.Optional;

import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntity;

public interface IPermissionSchemePort {
    PermissionSchemeEntity createPermissionScheme(PermissionSchemeEntity scheme);

    Optional<PermissionSchemeEntity> getPermissionSchemeById(Long schemeId, Long tenantId);

    Optional<PermissionSchemeEntity> getPermissionSchemeByIdIncludingSystem(Long schemeId, Long tenantId);

    Optional<PermissionSchemeEntity> getPermissionSchemeByNameIncludingSystem(String name, Long tenantId);
}
