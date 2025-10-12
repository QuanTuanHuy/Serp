package serp.project.account.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.UserModuleAccessEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IModulePort;
import serp.project.account.core.port.store.IUserModuleAccessPort;
import serp.project.account.core.service.IUserModuleAccessService;
import serp.project.account.infrastructure.store.mapper.UserModuleAccessMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserModuleAccessService implements IUserModuleAccessService {

    private final IUserModuleAccessPort userModuleAccessPort;
    private final IModulePort modulePort;
    private final UserModuleAccessMapper userModuleAccessMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserModuleAccessEntity registerUserToModule(Long userId, Long moduleId, Long organizationId,
            Long grantedBy) {
        try {
            ModuleEntity module = modulePort.getModuleById(moduleId);
            if (module == null) {
                throw new AppException(Constants.ErrorMessage.MODULE_NOT_FOUND);
            }

            if (!module.isAvailable()) {
                throw new AppException("Module is not available for registration");
            }

            UserModuleAccessEntity existingAccess = userModuleAccessPort
                    .getUserModuleAccess(userId, moduleId, organizationId);

            if (existingAccess != null) {
                if (!existingAccess.isActiveAccess()) {
                    existingAccess.activate(grantedBy);
                    return userModuleAccessPort.save(existingAccess);
                }
                log.info("User {} already has active access to module {} in org {}",
                        userId, moduleId, organizationId);
                return existingAccess;
            }

            String description = "User registered to module: " + module.getModuleName();
            UserModuleAccessEntity newAccess = userModuleAccessMapper.buildNewAccess(
                    userId, moduleId, organizationId, grantedBy, description);

            UserModuleAccessEntity savedAccess = userModuleAccessPort.save(newAccess);
            log.info("Successfully registered user {} to module {} in org {}",
                    userId, moduleId, organizationId);

            return savedAccess;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error registering user to module: {}", e.getMessage(), e);
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeUserModuleAccess(Long userId, Long moduleId, Long organizationId) {
        try {
            UserModuleAccessEntity access = userModuleAccessPort
                    .getUserModuleAccess(userId, moduleId, organizationId);

            if (access == null) {
                throw new AppException(Constants.ErrorMessage.USER_MODULE_ACCESS_NOT_FOUND);
            }

            access.deactivate();
            userModuleAccessPort.save(access);

            log.info("Successfully revoked module access for user {} to module {} in org {}",
                    userId, moduleId, organizationId);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error revoking user module access: {}", e.getMessage(), e);
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean hasAccess(Long userId, Long moduleId, Long organizationId) {
        try {
            return userModuleAccessPort.hasAccess(userId, moduleId, organizationId);
        } catch (Exception e) {
            log.error("Error checking user module access: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<UserModuleAccessEntity> getUserModuleAccesses(Long userId, Long organizationId) {
        try {
            if (organizationId != null) {
                return userModuleAccessPort.getUserModuleAccessesByUserIdAndOrgId(userId, organizationId);
            }
            return userModuleAccessPort.getUserModuleAccessesByUserId(userId);
        } catch (Exception e) {
            log.error("Error getting user module accesses: {}", e.getMessage(), e);
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
