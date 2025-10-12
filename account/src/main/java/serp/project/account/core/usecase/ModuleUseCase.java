package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.CreateModuleDto;
import serp.project.account.core.domain.dto.request.UpdateModuleDto;
import serp.project.account.core.domain.entity.UserModuleAccessEntity;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.IUserModuleAccessService;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleUseCase {
    private final IModuleService moduleService;
    private final IUserModuleAccessService userModuleAccessService;

    private final ResponseUtils responseUtils;

    public GeneralResponse<?> createModule(CreateModuleDto request) {
        try {
            var module = moduleService.createModule(request);
            return responseUtils.success(module);
        } catch (Exception e) {
            log.error("Error creating module: {}", e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> getModuleById(Long moduleId) {
        try {
            var module = moduleService.getModuleById(moduleId);
            if (module == null) {
                return responseUtils.notFound(Constants.ErrorMessage.MODULE_NOT_FOUND);
            }
            return responseUtils.success(module);
        } catch (Exception e) {
            log.error("Error getting module: {}", e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> updateModule(Long moduleId, UpdateModuleDto request) {
        try {
            var module = moduleService.updateModule(moduleId, request);
            return responseUtils.success(module);
        } catch (Exception e) {
            log.error("Error updating module: {}", e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> getAllModules() {
        try {
            var modules = moduleService.getAllModules();
            return responseUtils.success(modules);
        } catch (Exception e) {
            log.error("Error retrieving modules: {}", e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> userRegisterModule(Long userId, Long moduleId, Long organizationId) {
        try {
            // User tự đăng ký, grantedBy chính là userId
            UserModuleAccessEntity access = userModuleAccessService
                    .registerUserToModule(userId, moduleId, organizationId, userId);

            return responseUtils.success(access);
        } catch (Exception e) {
            log.error("Error registering user to module: {}", e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> getUserModules(Long userId, Long organizationId) {
        try {
            var accesses = userModuleAccessService.getUserModuleAccesses(userId, organizationId);
            return responseUtils.success(accesses);
        } catch (Exception e) {
            log.error("Error getting user modules: {}", e.getMessage());
            throw e;
        }
    }
}
