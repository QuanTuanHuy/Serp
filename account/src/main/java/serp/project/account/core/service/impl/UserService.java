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
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.UserProfileResponse;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.entity.UserRoleEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IUserPort;
import serp.project.account.core.port.store.IUserRolePort;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.IUserService;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.kernel.utils.CollectionUtils;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final IUserPort userPort;
    private final IUserRolePort userRolePort;

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
}
