/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import java.util.List;
import java.util.Map;

import serp.project.account.core.domain.dto.request.CreateMenuDisplayDto;
import serp.project.account.core.domain.dto.request.UpdateMenuDisplayDto;
import serp.project.account.core.domain.entity.MenuDisplayEntity;

public interface IMenuDisplayService {
    MenuDisplayEntity createMenuDisplay(CreateMenuDisplayDto request);

    MenuDisplayEntity updateMenuDisplay(Long id, UpdateMenuDisplayDto request);

    MenuDisplayEntity getMenuDisplayByModuleIdAndName(Long moduleId, String name);

    void deleteMenuDisplay(Long id);

    List<MenuDisplayEntity> getMenuDisplaysByModuleId(Long moduleId);

    void assignMenuDisplaysToRole(Long roleId, List<Long> menuDisplayIds);

    void unassignMenuDisplaysFromRole(Long roleId, List<Long> menuDisplayIds);

    Map<Long, List<MenuDisplayEntity>> getMenuDisplaysByRoleIds(List<Long> roleIds);
}
