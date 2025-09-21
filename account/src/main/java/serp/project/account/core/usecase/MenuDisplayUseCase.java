/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

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
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuDisplayUseCase {
    private final IMenuDisplayService menuDisplayService;
    private final IModuleService moduleService;

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
}
