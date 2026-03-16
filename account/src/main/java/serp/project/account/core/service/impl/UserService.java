/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateUserDto;
import serp.project.account.core.domain.dto.request.CreateUserForOrgRequest;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.UserProfileResponse;
import serp.project.account.core.domain.dto.response.UserStatsResponse;
import serp.project.account.core.domain.entity.PasswordResetRequestEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.entity.UserRoleEntity;
import serp.project.account.core.domain.enums.ResetPassStatus;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IPasswordResetRequestPort;
import serp.project.account.core.port.store.IUserPort;
import serp.project.account.core.port.store.IUserRolePort;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.IUserService;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.kernel.utils.CollectionUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final IUserPort userPort;
    private final IUserRolePort userRolePort;
    private final IPasswordResetRequestPort passwordResetRequestPort;

    private final IRoleService roleService;

    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserEntity createUser(CreateUserDto request) {
        UserEntity existedUser = userPort.getUserByEmail(request.getEmail());
        if (existedUser != null) {
            throw new AppException(Constants.ErrorMessage.USER_ALREADY_EXISTS);
        }

        UserEntity user = userMapper.createUserMapper(request);
        user = userPort.save(user);
        final long userId = user.getId();

        if (!CollectionUtils.isEmpty(request.getRoleIds())) {
            List<UserRoleEntity> userRoles = request.getRoleIds().stream()
                    .map(roleId -> UserRoleEntity.builder()
                            .userId(userId)
                            .roleId(roleId)
                            .build())
                    .collect(Collectors.toList());
            userRolePort.saveAll(userRoles);
        }
        return user;
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        UserEntity user = userPort.getUserByEmail(email);
        if (user == null) {
            return null;
        }
        var userRoles = userRolePort.getUserRolesByUserId(user.getId());
        var roleMap = roleService.getAllRoles().stream()
                .collect(Collectors.toMap(RoleEntity::getId, Function.identity()));
        user.setRoles(userRoles.stream().map(ur -> roleMap.get(ur.getRoleId())).toList());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateKeycloakUser(Long userId, String keycloakId) {
        UserEntity user = userPort.getUserById(userId);
        if (user == null) {
            throw new AppException(Constants.ErrorMessage.USER_NOT_FOUND);
        }
        user.setKeycloakId(keycloakId);
        userPort.save(user);
    }

    @Override
    public Pair<Long, List<UserEntity>> getUsers(GetUserParams params) {
        var result = userPort.getUsers(params);
        if (CollectionUtils.isEmpty(result.getSecond())) {
            return result;
        }

        var users = result.getSecond();
        List<Long> userIds = users.stream()
                .map(UserEntity::getId).toList();
        var userRoles = userRolePort.getUserRolesByUserIds(userIds);
        var roleMap = roleService.getAllRoles().stream()
                .collect(Collectors.toMap(RoleEntity::getId, Function.identity()));

        users.forEach(user -> {
            var roles = userRoles.stream()
                    .filter(userRole -> userRole.getUserId().equals(user.getId()))
                    .map(userRole -> roleMap.get(userRole.getRoleId()))
                    .toList();
            user.setRoles(roles);
        });

        return result;
    }

    @Override
    public UserEntity getUserById(Long userId) {
        UserEntity user = userPort.getUserById(userId);
        if (user == null) {
            return null;
        }
        var userRoles = userRolePort.getUserRolesByUserId(user.getId());
        var roleMap = roleService.getAllRoles().stream()
                .collect(Collectors.toMap(RoleEntity::getId, Function.identity()));
        user.setRoles(userRoles.stream().map(ur -> roleMap.get(ur.getRoleId())).toList());
        return user;
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        return userMapper.toProfileResponse(getUserById(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRolesToUser(Long userId, List<Long> roleIds) {
        UserEntity user = userPort.getUserById(userId);
        if (user == null) {
            throw new AppException(Constants.ErrorMessage.USER_NOT_FOUND);
        }

        List<RoleEntity> roles = roleService.getAllRoles().stream()
                .filter(role -> roleIds.contains(role.getId()))
                .toList();

        List<Long> existedRoleIds = userRolePort.getUserRolesByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
        List<UserRoleEntity> newUserRoles = roles.stream()
                .filter(role -> !existedRoleIds.contains(role.getId()))
                .map(role -> UserRoleEntity.builder()
                        .userId(userId)
                        .roleId(role.getId())
                        .createdAt(Instant.now().toEpochMilli())
                        .updatedAt(Instant.now().toEpochMilli())
                        .build())
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(newUserRoles)) {
            userRolePort.saveAll(newUserRoles);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserEntity updateUser(Long userId, UserEntity update) {
        UserEntity user = userPort.getUserById(userId);
        if (user == null) {
            throw new AppException(Constants.ErrorMessage.USER_NOT_FOUND);
        }
        user = userMapper.updateUserMapper(user, update);
        return userPort.save(user);
    }

    @Override
    public List<UserProfileResponse> getUserProfilesByIds(List<Long> userIds) {
        List<UserEntity> users = userPort.getUsersByIds(userIds);
        return users.stream()
                .map(userMapper::toProfileResponse)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserEntity createUser(Long organizationId, CreateUserForOrgRequest request) {
        var existedUser = userPort.getUserByEmail(request.getEmail());
        if (existedUser != null) {
            throw new AppException(Constants.ErrorMessage.USER_ALREADY_EXISTS);
        }
        var user = userMapper.createUserForOrgMapper(request, organizationId);
        return userPort.save(user);
    }

    @Override
    public void removeRolesFromUser(Long userId, List<Long> roleIds) {
        userRolePort.deleteUserRolesByUserIdAndRoleIds(userId, roleIds);
    }

    @Override
    public List<UserEntity> getUsersByOrganizationId(Long organizationId) {
        return userPort.getUsersByOrganizationId(organizationId);
    }

    @Override
    public Integer countUsersByOrganizationId(Long organizationId) {
        return userPort.countUsersByOrganizationId(organizationId);
    }

    @Override
    public List<UserEntity> getUsersByIds(List<Long> userIds) {
        return userPort.getUsersByIds(userIds);
    }

    @Override
    public List<UserEntity> getUsersByOrganizationIdAndIds(Long organizationId, List<Long> userIds) {
        return userPort.getUsersByOrganizationIdAndIds(organizationId, userIds);
    }

    @Override
    public UserStatsResponse getUserStats(Long organizationId) {
        int total = userPort.countUsersByOrganizationId(organizationId);

        Map<String, Integer> statusCounts = userPort.countUsersByStatusForOrganization(organizationId);
        int active = statusCounts.getOrDefault(UserStatus.ACTIVE.name(), 0);
        int inactive = statusCounts.getOrDefault(UserStatus.INACTIVE.name(), 0);
        int suspended = statusCounts.getOrDefault(UserStatus.SUSPENDED.name(), 0);
        int invited = statusCounts.getOrDefault(UserStatus.INVITED.name(), 0);

        int adminUsers = userPort.countAdminUsersByOrganizationId(organizationId);

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfThisMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime startOfNextMonth = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime startOfLastMonth = currentMonth.minusMonths(1).atDay(1).atStartOfDay();

        int newThisMonth = userPort.countUsersByOrganizationIdAndCreatedBetween(
                organizationId, startOfThisMonth, startOfNextMonth);
        int newLastMonth = userPort.countUsersByOrganizationIdAndCreatedBetween(
                organizationId, startOfLastMonth, startOfThisMonth);

        return UserStatsResponse.builder()
                .totalUsers(total)
                .activeUsers(active)
                .inactiveUsers(inactive)
                .suspendedUsers(suspended)
                .invitedUsers(invited)
                .adminUsers(adminUsers)
                .newUsersThisMonth(newThisMonth)
                .newUsersLastMonth(newLastMonth)
                .build();
    }

    @Override
    public Optional<PasswordResetRequestEntity> getPasswordResetRequestByTokenHash(String tokenHash) {
        return passwordResetRequestPort.getByTokenHash(tokenHash);
    }

    @Override
    public void invalidatePendingPasswordResetByUserId(Long userId) {
        passwordResetRequestPort.invalidatePendingByUserId(userId);
    }

    @Override
    public PasswordResetRequestEntity createPasswordResetRequest(
            Long userId,
            Long organizationId,
            String email,
            Long requestedBy,
            String tokenHash,
            Long expiresAt) {
        PasswordResetRequestEntity entity = PasswordResetRequestEntity.builder()
                .userId(userId)
                .organizationId(organizationId)
                .email(email)
                .tokenHash(tokenHash)
                .status(ResetPassStatus.PENDING)
                .requestedBy(requestedBy)
                .expiresAt(expiresAt)
                .build();
        return passwordResetRequestPort.save(entity);
    }

    @Override
    public PasswordResetRequestEntity updatePasswordResetRequest(PasswordResetRequestEntity entity) {
        return passwordResetRequestPort.save(entity);
    }
}
