/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.account.core.service;

import java.util.List;

import serp.project.account.core.domain.dto.request.CreatePermissionDto;
import serp.project.account.core.domain.entity.PermissionEntity;

public interface IPermissionService {
    PermissionEntity createPermission(CreatePermissionDto request);
    PermissionEntity getPermissionByName(String name);
    List<PermissionEntity> getPermissionsByIds(List<Long> ids);
    List<PermissionEntity> getAllPermissions();
}
