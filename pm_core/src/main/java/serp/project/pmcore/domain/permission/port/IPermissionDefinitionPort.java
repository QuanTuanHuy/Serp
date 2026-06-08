/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.permission.port;

import java.util.List;

import serp.project.pmcore.domain.permission.entity.PermissionDefinitionEntity;

public interface IPermissionDefinitionPort {
    List<PermissionDefinitionEntity> getPermissionDefinitionsIncludingSystem(Long tenantId);
}
