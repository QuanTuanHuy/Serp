/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.entity.UserRoleEntity;
import serp.project.account.core.port.store.IUserRolePort;
import serp.project.account.core.service.ICombineRoleService;
import serp.project.account.core.service.IKeycloakUserService;
import serp.project.account.kernel.utils.CollectionUtils;

@RequiredArgsConstructor
@Service
@Slf4j
public class CombineRoleService implements ICombineRoleService {
	private final IKeycloakUserService keycloakUserService;

	private final IUserRolePort userRolePort;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void assignRolesToUser(UserEntity user, List<RoleEntity> roles) {
		validateInput(user, roles);
		log.info("Assign {} roles to users {}", roles.size(), user.getId());

		List<Long> existedRoleIds = userRolePort.getUserRolesByUserId(user.getId()).stream()
				.map(UserRoleEntity::getRoleId)
				.toList();
		List<RoleEntity> rolesToAssign = roles.stream()
				.filter(role -> !existedRoleIds.contains(role.getId()))
				.toList();
		if (!CollectionUtils.isEmpty(rolesToAssign)) {
			List<UserRoleEntity> newUserRoles = rolesToAssign.stream()
					.map(role -> UserRoleEntity.builder()
							.userId(user.getId())
							.roleId(role.getId())
							.createdAt(Instant.now().toEpochMilli())
							.updatedAt(Instant.now().toEpochMilli())
							.build())
					.collect(Collectors.toList());
			userRolePort.saveAll(newUserRoles);
			assignKeycloakRoles(user, rolesToAssign);
		}
	}

	private void assignKeycloakRoles(UserEntity user, List<RoleEntity> rolesToAssign) {
		if (CollectionUtils.isEmpty(rolesToAssign)) {
			return;
		}
		List<String> realmRoles = rolesToAssign.stream()
				.filter(role -> role.getKeycloakClientId() == null)
				.map(RoleEntity::getName)
				.toList();
		if (!CollectionUtils.isEmpty(realmRoles)) {
			keycloakUserService.assignRealmRoles(user.getKeycloakId(), realmRoles);
			log.info("Assign {} realm roles to user {}", realmRoles.size(), user.getId());
		}
		List<RoleEntity> clientRoles = rolesToAssign.stream()
				.filter(role -> role.getKeycloakClientId() != null)
				.toList();
		if (CollectionUtils.isEmpty(clientRoles)) {
			return;
		}
		var clientRolesByClientId = clientRoles.stream()
				.collect(Collectors.groupingBy(RoleEntity::getKeycloakClientId));
		clientRolesByClientId.forEach((clientId, rolesList) -> {
			keycloakUserService.assignClientRoles(user.getKeycloakId(), clientId,
					rolesList.stream().map(RoleEntity::getName).toList());
		});
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void assignRolesToUsers(List<UserEntity> users, List<RoleEntity> roles) {
		if (CollectionUtils.isEmpty(users) || CollectionUtils.isEmpty(roles)) {
			return;
		}

		List<Long> userIds = users.stream()
				.map(UserEntity::getId)
				.toList();
		Map<Long, List<Long>> existedRoleIdsByUserId = userRolePort.getUserRolesByUserIds(userIds).stream()
				.collect(Collectors.groupingBy(
						UserRoleEntity::getUserId,
						Collectors.mapping(UserRoleEntity::getRoleId, Collectors.toList())));

		List<UserRoleEntity> newUserRoles = new ArrayList<>();
		Map<Long, List<RoleEntity>> rolesToAssignByUserId = new HashMap<>();
		long now = Instant.now().toEpochMilli();
		for (var user : users) {
			List<Long> existedRoleIds = existedRoleIdsByUserId.getOrDefault(user.getId(), List.of());
			List<RoleEntity> rolesToAssign = roles.stream()
					.filter(role -> !existedRoleIds.contains(role.getId()))
					.toList();
			if (CollectionUtils.isEmpty(rolesToAssign)) {
				continue;
			}
			rolesToAssignByUserId.put(user.getId(), rolesToAssign);
			rolesToAssign.forEach(role -> newUserRoles.add(UserRoleEntity.builder()
					.userId(user.getId())
					.roleId(role.getId())
					.createdAt(now)
					.updatedAt(now)
					.build()));
		}

		if (!CollectionUtils.isEmpty(newUserRoles)) {
			userRolePort.saveAll(newUserRoles);
		}
		users.forEach(user -> assignKeycloakRoles(user, rolesToAssignByUserId.getOrDefault(user.getId(), List.of())));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeRolesFromUser(UserEntity user, List<RoleEntity> roles) {
		validateInput(user, roles);
		removeRolesFromUsers(List.of(user), roles);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeRolesFromUsers(List<UserEntity> users, List<RoleEntity> roles) {
		if (CollectionUtils.isEmpty(users) || CollectionUtils.isEmpty(roles)) {
			return;
		}

		List<Long> userIds = users.stream()
				.map(UserEntity::getId)
				.toList();
		List<Long> roleIdsToRemove = roles.stream()
				.map(RoleEntity::getId)
				.toList();

		userRolePort.deleteUserRolesByUserIdsAndRoleIds(userIds, roleIdsToRemove);
		users.forEach(user -> revokeKeycloakClientRoles(user, roles));
	}

	private void revokeKeycloakClientRoles(UserEntity user, List<RoleEntity> roles) {
		List<RoleEntity> clientRolesToRemove = roles.stream()
				.filter(role -> role.getKeycloakClientId() != null)
				.toList();
		if (CollectionUtils.isEmpty(clientRolesToRemove)) {
			return;
		}
		var clientRolesByClientId = clientRolesToRemove.stream()
				.collect(Collectors.groupingBy(RoleEntity::getKeycloakClientId));
		clientRolesByClientId.forEach((clientId, rolesList) -> {
			keycloakUserService.revokeClientRoles(user.getKeycloakId(), clientId,
					rolesList.stream().map(RoleEntity::getName).toList());
		});
	}

	private void validateInput(UserEntity user, List<RoleEntity> roles) {
		if (user == null || CollectionUtils.isEmpty(roles)) {
			throw new IllegalArgumentException("User and roles must not be null or empty");
		}
	}

}
