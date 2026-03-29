/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.user.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateUserForOrgRequest;
import serp.project.account.core.domain.dto.message.SyncUserFirstMileEvent;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.client.IKafkaProducer;
import serp.project.account.core.service.ICombineRoleService;
import serp.project.account.core.service.IKeycloakUserService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IUserService;
import serp.project.account.core.usecase.support.OrganizationRoleResolver;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.kernel.property.KafkaTopicProperties;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.CollectionUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProvisioningCoordinator {
    private static final String ROLE_TMS_POSTOFFICER_MANAGER = "TMS_POSTOFFICER_MANAGER";
    private static final String ROLE_TMS_POSTOFFICER = "TMS_POSTOFFICER";
    private static final Set<String> FIRST_MILE_SYNC_ROLES = Set.of(
            ROLE_TMS_POSTOFFICER_MANAGER,
            ROLE_TMS_POSTOFFICER
    );

    private final IUserService userService;
    private final IKeycloakUserService keycloakUserService;
    private final IOrganizationService organizationService;
    private final ICombineRoleService combineRoleService;
    private final OrganizationRoleResolver organizationRoleResolver;
    private final UserMapper userMapper;
    private final IKafkaProducer kafkaProducer;
    private final KafkaTopicProperties kafkaTopicProperties;

    @Transactional(rollbackFor = Exception.class)
    public UserEntity createOrganizationUser(OrganizationEntity organization, CreateUserForOrgRequest request) {
        String keycloakUserId = null;
        try {
            var user = userService.createUser(organization.getId(), request);
            var keycloakUser = userMapper.createUserMapper(user, organization.getId(), request.getPassword());
            keycloakUserId = keycloakUserService.createUser(keycloakUser);

            user = activateProvisionedUser(user, keycloakUserId);

            List<RoleEntity> roles = organizationRoleResolver.resolveRequestedOrAutoAssignedRoles(
                    organization.getId(),
                    request.getRoleIds());
            if (CollectionUtils.isEmpty(roles)) {
                log.error("No roles found to assign to user with id {} in organization with id {}", user.getId(),
                        organization.getId());
                throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
            }

            combineRoleService.assignRolesToUser(user, roles);
            assignOrganizationRoles(organization.getId(), user.getId(), roles);
            publishUserSyncForFirstMile(organization.getId(), user, roles);
            return user;
        } catch (Exception e) {
            cleanupKeycloakUser(keycloakUserId);
            throw e;
        }
    }

    private UserEntity activateProvisionedUser(UserEntity user, String keycloakUserId) {
        user.setKeycloakId(keycloakUserId);
        user.setStatus(UserStatus.ACTIVE);
        return userService.updateUser(user.getId(), user);
    }

    private void assignOrganizationRoles(Long organizationId, Long userId, List<RoleEntity> roles) {
        roles.stream()
                .filter(RoleEntity::isOrganizationRole)
                .forEach(role -> organizationService.assignOrganizationToUser(organizationId, userId, role.getId(), true));
    }

    private void cleanupKeycloakUser(String keycloakUserId) {
        if (keycloakUserId == null) {
            return;
        }
        try {
            keycloakUserService.deleteUser(keycloakUserId);
        } catch (Exception cleanupException) {
            log.error("Failed to cleanup keycloak user {}", keycloakUserId, cleanupException);
        }
    }

    private void publishUserSyncForFirstMile(Long organizationId, UserEntity user, List<RoleEntity> roles) {
        if (organizationId == null || user == null || user.getId() == null || CollectionUtils.isEmpty(roles)) {
            return;
        }

        List<String> matchedRoleNames = roles.stream()
                .map(RoleEntity::getName)
                .filter(Objects::nonNull)
                .map(roleName -> roleName.toUpperCase(Locale.ROOT))
                .filter(FIRST_MILE_SYNC_ROLES::contains)
                .distinct()
                .toList();

        if (matchedRoleNames.isEmpty()) {
            return;
        }

        String topic = kafkaTopicProperties.getSyncUserFirstMile();
        for (String roleName : matchedRoleNames) {
            SyncUserFirstMileEvent event = SyncUserFirstMileEvent.builder()
                    .userId(user.getId())
                    .organizationId(organizationId)
                    .tenantId(organizationId)
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .fullName(user.getFullName())
                    .roleName(roleName)
                    .build();

            String partitionKey = user.getId() + ":" + roleName;
            kafkaProducer.sendMessageAsync(partitionKey, event, topic, (success, sentTopic, payload, ex) -> {
                if (success) {
                    log.info(
                            "Published sync-user-first-mile event: organizationId={}, userId={}, roleName={}, topic={}",
                            organizationId,
                            user.getId(),
                            roleName,
                            sentTopic
                    );
                    return;
                }

                log.error(
                        "Failed to publish sync-user-first-mile event: organizationId={}, userId={}, roleName={}, topic={}",
                        organizationId,
                        user.getId(),
                        roleName,
                        topic,
                        ex
                );
            });
        }
    }
}
