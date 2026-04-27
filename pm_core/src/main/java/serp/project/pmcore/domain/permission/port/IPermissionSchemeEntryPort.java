/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.permission.port;

import java.util.List;

import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntryEntity;

public interface IPermissionSchemeEntryPort {
    List<PermissionSchemeEntryEntity> createPermissionSchemeEntries(List<PermissionSchemeEntryEntity> entries);

    List<PermissionSchemeEntryEntity> getPermissionSchemeEntriesBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    List<PermissionSchemeEntryEntity> getPermissionSchemeEntriesBySchemeId(Long schemeId, Long tenantId);

    boolean existsByProjectRoleId(Long tenantId, Long roleId);
}
