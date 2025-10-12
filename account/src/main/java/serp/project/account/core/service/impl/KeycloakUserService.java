/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateKeycloakUserDto;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.client.IKeycloakPort;
import serp.project.account.core.service.IKeycloakUserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserService implements IKeycloakUserService {

    private final IKeycloakPort keycloakPort;

    @Override
    public String createUser(CreateKeycloakUserDto request) {
        try {
            if (keycloakPort.isUserExists(request.getUsername())) {
                log.error("User {} already exists", request.getUsername());
                throw new AppException(Constants.ErrorMessage.USER_ALREADY_EXISTS);
            }
            return keycloakPort.createUser(request);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void assignRealmRoles(String userId, List<String> roleNames) {
        try {
            log.info("Rolenames: {}", roleNames);
            keycloakPort.assignRealmRoles(userId, roleNames);
        } catch (AppException e) {
            log.error("Error assigning realm roles to user {}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error assigning realm roles to user {}: {}", userId, e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void assignClientRoles(String userId, String clientId, List<String> roleNames) {
        try {
            keycloakPort.assignClientRoles(userId, clientId, roleNames);
        } catch (AppException e) {
            log.error("Error assigning roles to user {} for client {}: {}", userId, clientId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error assigning roles to user {} for client {}: {}", userId, clientId,
                    e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteUser(String userId) {
        try {
            keycloakPort.deleteUser(userId);
        } catch (Exception e) {
            log.error("Error deleting user {}: {}", userId, e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
