/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import org.springframework.data.util.Pair;
import serp.project.account.core.domain.dto.request.CreateUserDto;
import serp.project.account.core.domain.dto.request.CreateUserForOrgRequest;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.UserProfileResponse;
import serp.project.account.core.domain.dto.response.UserStatsResponse;
import serp.project.account.core.domain.entity.PasswordResetRequestEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.UserStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUserService {
    UserEntity createUser(CreateUserDto request);

    UserEntity createUser(Long organizationId, CreateUserForOrgRequest request);

    UserEntity updateUser(Long userId, UserEntity update);

    UserEntity getUserByEmail(String email);

    UserEntity getUserById(Long userId);

    UserProfileResponse getUserProfile(Long userId);

    List<UserEntity> getUsersByOrganizationId(Long organizationId);

    List<UserEntity> getUsersByIds(List<Long> userIds);

    List<UserEntity> getUsersByOrganizationIdAndIds(Long organizationId, List<Long> userIds);

    List<UserProfileResponse> getUserProfilesByIds(List<Long> userIds);

    void addRolesToUser(Long userId, List<Long> roleIds);

    void removeRolesFromUser(Long userId, List<Long> roleIds);

    void updateKeycloakUser(Long userId, String keycloakId);

    Pair<Long, List<UserEntity>> getUsers(GetUserParams params);

    Integer countUsersByOrganizationId(Long organizationId);

    UserStatsResponse getUserStats(Long organizationId);

    Long countUsers();

    Long countUsersByStatus(UserStatus status);

    Map<Long, Long> countUsersByOrganizationIds(List<Long> organizationIds);

    Optional<PasswordResetRequestEntity> getPasswordResetRequestByTokenHash(String tokenHash);

    void invalidatePendingPasswordResetByUserId(Long userId);

    PasswordResetRequestEntity createPasswordResetRequest(
            Long userId,
            Long organizationId,
            String email,
            Long requestedBy,
            String tokenHash,
            Long expiresAt);

    PasswordResetRequestEntity updatePasswordResetRequest(PasswordResetRequestEntity entity);
}
