/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.CreateMenuDisplayDto;
import serp.project.account.core.domain.dto.request.GetMenuDisplayParams;
import serp.project.account.core.domain.dto.request.UpdateMenuDisplayDto;
import serp.project.account.core.domain.dto.response.MenuDisplayResponse;
import serp.project.account.core.domain.entity.MenuDisplayEntity;
import serp.project.account.core.domain.entity.MenuDisplayRoleEntity;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.enums.MenuType;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IMenuDisplayService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.PaginationUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuDisplayUseCase {
    private final IMenuDisplayService menuDisplayService;
    private final IModuleService moduleService;
    private final IRoleService roleService;
    private final IUserService userService;

    private final ResponseUtils responseUtils;
    private final PaginationUtils paginationUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createMenuDisplay(CreateMenuDisplayDto request) {
        try {
            validateCreateMenuDisplayRequest(request);

            var module = moduleService.getModuleById(request.getModuleId());
            if (module == null) {
                throw new AppException(Constants.ErrorMessage.MODULE_NOT_FOUND);
            }
            var existedMenuDisplay = menuDisplayService.getMenuDisplayByModuleIdAndName(request.getModuleId(),
                    request.getName());
            if (existedMenuDisplay != null) {
                throw new AppException(Constants.ErrorMessage.MENU_DISPLAY_ALREADY_EXISTS);
            }

            var menuDisplay = menuDisplayService.createMenuDisplay(request);
            return responseUtils.success(menuDisplay);
        } catch (AppException e) {
            log.error("Error creating menu display: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected Error creating menu display: {}", e.getMessage());
            throw e;
        }
    }

    private void validateCreateMenuDisplayRequest(CreateMenuDisplayDto request) {
        try {
            MenuType.valueOf(request.getMenuType());
        } catch (IllegalArgumentException e) {
            throw new AppException(Constants.ErrorMessage.INVALID_MENU_TYPE);
        }
        if (request.getParentId() != null) {
            menuDisplayService.getMenuDisplayById(request.getParentId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateMenuDisplay(Long id, UpdateMenuDisplayDto request) {
        try {
            var menuDisplay = menuDisplayService.updateMenuDisplay(id, request);
            return responseUtils.success(menuDisplay);
        } catch (AppException e) {
            log.error("Error updating menu display: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected Error updating menu display: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> deleteMenuDisplay(Long id) {
        try {
            menuDisplayService.deleteMenuDisplay(id);
            return responseUtils.success("Delete menu display successfully");
        } catch (AppException e) {
            log.error("Error deleting menu display: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected Error deleting menu display: {}", e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> getMenuDisplaysByModuleId(Long moduleId) {
        try {
            var module = moduleService.getModuleById(moduleId);
            if (module == null) {
                throw new AppException(Constants.ErrorMessage.MODULE_NOT_FOUND);
            }

            var menuDisplays = menuDisplayService.getMenuDisplaysByModuleId(moduleId);
            return responseUtils.success(menuDisplays);
        } catch (AppException e) {
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected Error fetching menu displays: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> assignMenuDisplaysToRole(Long roleId, List<Long> menuDisplayIds) {
        try {
            var role = roleService.getRoleByIdFromCache(roleId);
            if (!role.canAssignToMenuDisplays()) {
                throw new AppException(Constants.ErrorMessage.ROLE_CANNOT_ASSIGN_MENU_DISPLAYS);
            }
            List<MenuDisplayEntity> menuDisplays = menuDisplayService.getByIds(menuDisplayIds).stream()
                    .filter(md -> role.isSystemRole() || md.getModuleId().equals(role.getModuleId()))
                    .toList();
            if (menuDisplays.isEmpty()) {
                throw new AppException(Constants.ErrorMessage.NO_VALID_MENU_DISPLAYS_TO_ASSIGN);
            }
            menuDisplayIds = menuDisplays.stream()
                    .map(MenuDisplayEntity::getId)
                    .distinct().toList();

            menuDisplayService.assignMenuDisplaysToRole(roleId, menuDisplayIds);
            return responseUtils.success("Assigned menu displays successfully");
        } catch (AppException e) {
            log.error("Error assigning menu displays to role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected Error assigning menu displays to role: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> unassignMenuDisplaysFromRole(Long roleId, List<Long> menuDisplayIds) {
        try {
            roleService.getRoleById(roleId);

            menuDisplayService.unassignMenuDisplaysFromRole(roleId, menuDisplayIds);
            return responseUtils.success("Unassigned menu displays successfully");
        } catch (AppException e) {
            log.error("Error unassigning menu displays from role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected Error unassigning menu displays from role: {}", e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> getMenuDisplaysByRoleIds(List<Long> roleIds) {
        try {
            var roles = roleService.getAllRoles().stream()
                    .filter(r -> roleIds.contains(r.getId()))
                    .toList();
            List<Long> validRoleIds = roles.stream()
                    .map(RoleEntity::getId)
                    .toList();

            var menuDisplaysByRoleIds = menuDisplayService.getMenuDisplaysByRoleIds(validRoleIds);
            return responseUtils.success(menuDisplaysByRoleIds);
        } catch (AppException e) {
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected Error fetching menu displays by role ids: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getAllMenuDisplays(GetMenuDisplayParams params) {
        try {
            var pair = menuDisplayService.getAllMenuDisplays(params);
            var menuDisplayEntities = pair.getFirst();

            if (menuDisplayEntities.isEmpty()) {
                return responseUtils.success(paginationUtils.getResponse(
                        pair.getSecond(), params.getPage(), params.getPageSize(), Collections.emptyList()));
            }

            List<Long> moduleIds = menuDisplayEntities.stream()
                    .map(MenuDisplayEntity::getModuleId)
                    .distinct().toList();
            var idToModule = moduleService.getModulesByIds(moduleIds).stream()
                    .collect(Collectors.toMap(ModuleEntity::getId, Function.identity()));

            var menuDisplayRoles = menuDisplayService.getMenuDisplayRolesByMenuDisplayIds(
                    menuDisplayEntities.stream()
                            .map(MenuDisplayEntity::getId)
                            .toList());
            var menuDisplayIdToRoles = menuDisplayRoles.stream()
                    .collect(Collectors.groupingBy(
                            MenuDisplayRoleEntity::getMenuDisplayId,
                            Collectors.mapping(
                                    menuDisplayRole -> roleService.getRoleByIdFromCache(menuDisplayRole.getRoleId()),
                                    Collectors.filtering(
                                            Objects::nonNull,
                                            Collectors.toList()))));

            var responses = menuDisplayEntities.stream()
                    .map(md -> {
                        var module = idToModule.get(md.getModuleId());
                        var roles = menuDisplayIdToRoles.getOrDefault(md.getId(), Collections.emptyList());
                        return MenuDisplayResponse.fromEnity(md, module, roles);
                    })
                    .toList();

            return responseUtils.success(paginationUtils.getResponse(
                    pair.getSecond(), params.getPage(), params.getPageSize(), responses));
        } catch (AppException e) {
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected Error fetching all menu displays: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getMenuDisplaysByModuleIdAndUserId(Long moduleId, Long userId) {
        try {
            var module = moduleService.getModuleByIdFromCache(moduleId);
            if (!module.isAvailable()) {
                throw new AppException(Constants.ErrorMessage.MODULE_NOT_FOUND);
            }
            if (userId == null) {
                throw new AppException(Constants.ErrorMessage.USER_NOT_FOUND);
            }
            var user = userService.getUserById(userId);
            if (user == null || !user.isActive()) {
                throw new AppException(Constants.ErrorMessage.USER_NOT_FOUND);
            }

            var userRoles = user.getRoles();
            var menuDisplays = menuDisplayService.getMenuDisplaysByModuleId(moduleId).stream()
                    .filter(MenuDisplayEntity::getIsVisible)
                    .toList();
            var responses = menuDisplays.stream()
                    .map(md -> {
                        List<Long> roleCanAccessIds = md.getAssignedRoles().stream()
                                .map(MenuDisplayRoleEntity::getRoleId)
                                .toList();
                        List<RoleEntity> rolesCanAccess = userRoles.stream()
                                .filter(r -> roleCanAccessIds.contains(r.getId()))
                                .toList();
                        if (rolesCanAccess.isEmpty()) {
                            return null;
                        }
                        return MenuDisplayResponse.fromEnity(md, module, rolesCanAccess);
                    })
                    .filter(Objects::nonNull)
                    .toList();

            return responseUtils.success(responses);
        } catch (AppException e) {
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected Error fetching menu displays by module id and user id: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }
}
