/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.PermissionSchemeEntryEntity;

import java.util.List;

public interface IPermissionSchemeEntryPort {
    List<PermissionSchemeEntryEntity> createPermissionSchemeEntries(List<PermissionSchemeEntryEntity> entries);

    List<PermissionSchemeEntryEntity> getPermissionSchemeEntriesBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    List<PermissionSchemeEntryEntity> getPermissionSchemeEntriesBySchemeId(Long schemeId, Long tenantId);
}
