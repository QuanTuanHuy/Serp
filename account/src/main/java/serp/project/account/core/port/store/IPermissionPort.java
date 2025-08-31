/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import java.util.List;

import serp.project.account.core.domain.entity.PermissionEntity;

public interface IPermissionPort {
    PermissionEntity save(PermissionEntity permission);
    PermissionEntity getPermissionByName(String name);
    List<PermissionEntity> getAllPermissions();
}
