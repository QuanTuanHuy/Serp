/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.user.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.UpdateUserInfoRequest;
import serp.project.account.core.domain.dto.response.UserProfileResponse;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.UserType;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IUserService;
import serp.project.account.core.usecase.user.query.UserProfileAssembler;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserAccountCommandService {
    private final IUserService userService;
    private final IOrganizationService organizationService;
    private final UserProfileAssembler userProfileAssembler;

    @Transactional(rollbackFor = Exception.class)
    public UserProfileResponse updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        var updated = userService.updateUser(userId, request.toEntity());
        return userProfileAssembler.assembleBasic(updated);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long organizationId, Long updatedBy, Long userId, String status, boolean isSystemAdmin) {
        var user = getUserOrThrow(userId);
        ensureUserInOrganization(user, organizationId);

        if (!isSystemAdmin) {
            var organization = organizationService.getOrganizationById(organizationId);
            if (organization == null || !Objects.equals(organization.getOwnerId(), updatedBy)) {
                throw new AppException(Constants.ErrorMessage.FORBIDDEN, Constants.HttpStatusCode.FORBIDDEN);
            }
        }

        applyStatus(user, status);
        userService.updateUser(userId, user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateType(Long organizationId, Long userId, String userType) {
        var user = getUserOrThrow(userId);
        ensureUserInOrganization(user, organizationId);

        var newType = UserType.valueOf(userType.toUpperCase());
        var patch = UserEntity.builder().userType(newType).build();
        userService.updateUser(userId, patch);
    }

    private UserEntity getUserOrThrow(Long userId) {
        var user = userService.getUserById(userId);
        if (user == null) {
            throw new AppException(Constants.ErrorMessage.USER_NOT_FOUND);
        }
        return user;
    }

    private void ensureUserInOrganization(UserEntity user, Long organizationId) {
        if (!Objects.equals(user.getPrimaryOrganizationId(), organizationId)) {
            throw new AppException(Constants.ErrorMessage.USER_NOT_IN_ORGANIZATION,
                    Constants.HttpStatusCode.FORBIDDEN);
        }
    }

    private void applyStatus(UserEntity user, String status) {
        switch (status.toUpperCase()) {
            case "ACTIVE":
                user.activate();
                break;
            case "INACTIVE":
                user.deactivate();
                break;
            case "SUSPENDED":
                user.suspend();
                break;
            default:
                throw new IllegalArgumentException("Invalid status: " + status);
        }
    }
}
