/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateMenuDisplayDto;
import serp.project.account.core.domain.dto.request.GetMenuDisplayParams;
import serp.project.account.core.domain.dto.request.UpdateMenuDisplayDto;
import serp.project.account.core.domain.entity.MenuDisplayEntity;
import serp.project.account.core.domain.entity.MenuDisplayRoleEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IMenuDisplayPort;
import serp.project.account.core.port.store.IMenuDisplayRolePort;
import serp.project.account.core.service.IMenuDisplayService;
import serp.project.account.infrastructure.store.mapper.MenuDisplayMapper;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.DataUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuDisplayService implements IMenuDisplayService {

    private final IMenuDisplayPort menuDisplayPort;
    private final IMenuDisplayRolePort menuDisplayRolePort;

    private final MenuDisplayMapper menuDisplayMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuDisplayEntity createMenuDisplay(CreateMenuDisplayDto request) {
        try {
            if (!DataUtils.isNullOrEmpty(request.getParentId())) {
                var parentMenuDisplay = menuDisplayPort.getByIds(List.of(request.getParentId()))
                        .stream().findFirst().orElse(null);
                if (parentMenuDisplay == null) {
                    throw new AppException(Constants.ErrorMessage.PARENT_MENU_DISPLAY_NOT_FOUND);
                }

            }

            var menuDisplay = menuDisplayMapper.createMenuDisplayMapper(request);
            return menuDisplayPort.save(menuDisplay);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating menu display: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.CREATE_MENU_DISPLAY_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuDisplayEntity updateMenuDisplay(Long id, UpdateMenuDisplayDto request) {
        try {
            var menuDisplay = menuDisplayPort.getByIds(List.of(id))
                    .stream().findFirst().orElse(null);
            if (menuDisplay == null) {
                throw new AppException(Constants.ErrorMessage.MENU_DISPLAY_NOT_FOUND);
            }
            menuDisplay = menuDisplayMapper.updateMenuDisplayMapper(menuDisplay, request);
            return menuDisplayPort.save(menuDisplay);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating menu display: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.UPDATE_MENU_DISPLAY_FAILED);
        }
    }

    @Override
    public MenuDisplayEntity getMenuDisplayByModuleIdAndName(Long moduleId, String name) {
        try {
            return menuDisplayPort.getByModuleIdAndName(moduleId, name);
        } catch (Exception e) {
            log.error("Error getting menu display by moduleId and name: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.GET_MENU_DISPLAY_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenuDisplay(Long id) {
        try {
            var menuDisplay = menuDisplayPort.getByIds(List.of(id))
                    .stream().findFirst().orElse(null);
            if (menuDisplay == null) {
                throw new AppException(Constants.ErrorMessage.MENU_DISPLAY_NOT_FOUND);
            }
            menuDisplayPort.deleteMenuDisplay(id);
        } catch (Exception e) {
            log.error("Error deleting menu display: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.DELETE_MENU_DISPLAY_FAILED);
        }
    }

    @Override
    public List<MenuDisplayEntity> getMenuDisplaysByModuleId(Long moduleId) {
        try {
            List<MenuDisplayEntity> menuDisplays = menuDisplayPort.getByModuleId(moduleId);
            List<Long> menuDisplayIds = menuDisplays.stream()
                    .map(MenuDisplayEntity::getId)
                    .distinct().toList();
            List<MenuDisplayRoleEntity> menuDisplayRoles = menuDisplayRolePort.getByMenuDisplayIds(menuDisplayIds);
            Map<Long, List<MenuDisplayRoleEntity>> menuDisplayIdToRoles = menuDisplayRoles.stream()
                    .collect(Collectors.groupingBy(MenuDisplayRoleEntity::getMenuDisplayId));
            menuDisplays.forEach(menuDisplay -> {
                List<MenuDisplayRoleEntity> assignedRoles = menuDisplayIdToRoles.get(menuDisplay.getId());
                menuDisplay.setAssignedRoles(assignedRoles != null ? assignedRoles : List.of());
            });
            return menuDisplays;
        } catch (Exception e) {
            log.error("Error getting menu displays by moduleId: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.GET_MENU_DISPLAY_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenuDisplaysToRole(Long roleId, List<Long> menuDisplayIds) {
        try {
            var assignedMenuDisplayIds = menuDisplayRolePort.getByRoleIds(List.of(roleId))
                    .stream().map(MenuDisplayRoleEntity::getId)
                    .toList();
            var newMenuDisplayIds = menuDisplayIds.stream()
                    .filter(id -> !assignedMenuDisplayIds.contains(id))
                    .toList();
            if (!CollectionUtils.isEmpty(newMenuDisplayIds)) {
                List<MenuDisplayRoleEntity> newMenuDisplayRoles = newMenuDisplayIds.stream()
                        .map(menuDisplayId -> MenuDisplayRoleEntity.builder()
                                .roleId(roleId)
                                .menuDisplayId(menuDisplayId)
                                .createdAt(Instant.now().toEpochMilli())
                                .updatedAt(Instant.now().toEpochMilli())
                                .build())
                        .collect(Collectors.toList());
                menuDisplayRolePort.save(newMenuDisplayRoles);
            }
            log.info("Assigned menu displays successfully to role: {}, menu displays: {}", roleId, newMenuDisplayIds);
        } catch (Exception e) {
            log.error("Error assigning menu displays to role: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.ASSIGN_MENU_DISPLAY_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unassignMenuDisplaysFromRole(Long roleId, List<Long> menuDisplayIds) {
        try {
            if (roleId == null || CollectionUtils.isEmpty(menuDisplayIds)) {
                return;
            }
            menuDisplayRolePort.deleteByRoleIdAndMenuDisplayIds(roleId, menuDisplayIds);
            log.info("Unassigned menu displays successfully from role: {}, menu displays: {}", roleId, menuDisplayIds);
        } catch (Exception e) {
            log.error("Error unassigning menu displays from role: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.UNASSIGN_MENU_DISPLAY_FAILED);
        }
    }

    @Override
    public Map<Long, List<MenuDisplayEntity>> getMenuDisplaysByRoleIds(List<Long> roleIds) {
        var menuDisplayRoles = menuDisplayRolePort.getByRoleIds(roleIds);
        var menuDisplayIds = menuDisplayRoles.stream()
                .map(MenuDisplayRoleEntity::getMenuDisplayId)
                .distinct()
                .toList();
        var menuDisplays = menuDisplayPort.getByIds(menuDisplayIds);
        return menuDisplayRoles.stream()
                .collect(Collectors.groupingBy(
                        MenuDisplayRoleEntity::getRoleId,
                        Collectors.mapping(
                                menuDisplayRole -> menuDisplays.stream()
                                        .filter(menuDisplay -> menuDisplay.getId()
                                                .equals(menuDisplayRole.getMenuDisplayId()))
                                        .findFirst()
                                        .orElse(null),
                                Collectors.filtering(
                                        Objects::nonNull,
                                        Collectors.toList()))));
    }

    @Override
    public List<MenuDisplayEntity> getByIds(List<Long> ids) {
        return menuDisplayPort.getByIds(ids);
    }

    @Override
    public Pair<List<MenuDisplayEntity>, Long> getAllMenuDisplays(GetMenuDisplayParams params) {
        return menuDisplayPort.getAllMenuDisplays(params);
    }

    @Override
    public List<MenuDisplayRoleEntity> getMenuDisplayRolesByMenuDisplayIds(List<Long> menuDisplayIds) {
        return menuDisplayRolePort.getByMenuDisplayIds(menuDisplayIds);
    }

    @Override
    public MenuDisplayEntity getMenuDisplayById(Long id) {
        return Optional.ofNullable(menuDisplayPort.getById(id))
                .orElseThrow(() -> new AppException(Constants.ErrorMessage.MENU_DISPLAY_NOT_FOUND));
    }

}
