/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.RoleEntity;

public interface IRolePort {
    RoleEntity save(RoleEntity role);
    RoleEntity getRoleByName(String name);
}
