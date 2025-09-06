/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.CreateUserDto;
import serp.project.account.core.domain.dto.request.LoginRequest;
import serp.project.account.core.domain.enums.RoleEnum;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IKeycloakUserService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.ITokenService;
import serp.project.account.core.service.IUserService;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUseCase {
    private final IUserService userService;
    private final IKeycloakUserService keycloakUserService;
    private final IRoleService roleService;
    private final ITokenService tokenService;

    private final ResponseUtils responseUtils;

    private final UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> registerUser(CreateUserDto request) {
        try {
            var role = roleService.getRoleByName(RoleEnum.USER.getRole());
            if (role == null) {
                log.error("Role USER not found");
                return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
            }

            request.setRoleIds(List.of(role.getId()));
            var user = userService.createUser(request);
            user.setRoles(List.of(role));

            var createUserKeycloak = userMapper.createUserMapper(user, request);
            String keycloakId = keycloakUserService.createUser(createUserKeycloak);
            userService.updateKeycloakUser(user.getId(), keycloakId);

            return responseUtils.success(user);
        } catch (AppException e) {
            log.error("Register user failed: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected register user error: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> login(LoginRequest request) {
        try {
            var user = userService.getUserByEmail(request.getEmail());
            if (user == null) {
                return responseUtils.badRequest(Constants.ErrorMessage.WRONG_EMAIL_OR_PASSWORD);
            }
            var loginResponse = tokenService.getUserToken(user.getEmail(), request.getPassword());
            return responseUtils.success(loginResponse);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getUserToken(String username, String password) {
        try {
            var tokenResponse = tokenService.getUserToken(username, password);
            return responseUtils.success(tokenResponse);
        } catch (AppException e) {
            log.error("Error getting user token: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when getting user token: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }
}
