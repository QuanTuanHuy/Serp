/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.CreateMenuDisplayDto;
import serp.project.account.core.domain.dto.request.UpdateMenuDisplayDto;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IMenuDisplayService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuDisplayUseCase {
    private final IMenuDisplayService menuDisplayService;
    private final IModuleService moduleService;
    private final IRoleService roleService;

    private final ResponseUtils responseUtils;

    public GeneralResponse<?> createMenuDisplay(CreateMenuDisplayDto request) {
        try {
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
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected Error creating menu display: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> updateMenuDisplay(Long id, UpdateMenuDisplayDto request) {
        try {
            var menuDisplay = menuDisplayService.updateMenuDisplay(id, request);
            return responseUtils.success(menuDisplay);
        } catch (AppException e) {
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected Error updating menu display: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> deleteMenuDisplay(Long id) {
        try {
            menuDisplayService.deleteMenuDisplay(id);
            return responseUtils.success("Delete menu display successfully");
        } catch (AppException e) {
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected Error deleting menu display: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
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

    public GeneralResponse<?> assignMenuDisplaysToRole(Long roleId, List<Long> menuDisplayIds) {
        try {
            var role = roleService.getAllRoles().stream()
                    .filter(r -> r.getId().equals(roleId))
                    .findFirst()
                    .orElse(null);
            if (role == null) {
                throw new AppException(Constants.ErrorMessage.ROLE_NOT_FOUND);
            }

            menuDisplayService.assignMenuDisplaysToRole(roleId, menuDisplayIds);
            return responseUtils.success("Assigned menu displays successfully");
        } catch (AppException e) {
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected Error assigning menu displays to role: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> unassignMenuDisplaysFromRole(Long roleId, List<Long> menuDisplayIds) {
        try {
            var role = roleService.getAllRoles().stream()
                    .filter(r -> r.getId().equals(roleId))
                    .findFirst()
                    .orElse(null);
            if (role == null) {
                throw new AppException(Constants.ErrorMessage.ROLE_NOT_FOUND);
            }

            menuDisplayService.unassignMenuDisplaysFromRole(roleId, menuDisplayIds);
            return responseUtils.success("Unassigned menu displays successfully");
        } catch (AppException e) {
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected Error unassigning menu displays from role: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getMenuDisplaysByRoleIds(List<Long> roleIds) {
        try {
            var roles = roleService.getAllRoles().stream()
                    .filter(r -> roleIds.contains(r.getId()))
                    .toList();
            if (roles.size() != roleIds.size()) {
                throw new AppException(Constants.ErrorMessage.ROLE_NOT_FOUND);
            }

            var menuDisplaysByRoleIds = menuDisplayService.getMenuDisplaysByRoleIds(roleIds);
            return responseUtils.success(menuDisplaysByRoleIds);
        } catch (AppException e) {
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected Error fetching menu displays by role ids: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }
}
