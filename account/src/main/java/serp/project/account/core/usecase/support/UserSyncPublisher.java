/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.dto.message.SyncUserEvent;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.port.client.IKafkaProducer;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.property.KafkaTopicProperties;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSyncPublisher {

    private final IUserService userService;
    private final IKafkaProducer kafkaProducer;
    private final KafkaTopicProperties kafkaTopicProperties;

    public void publishUserSync(Long organizationId, Long userId) {
        if (userId == null) {
            return;
        }

        try {
            UserEntity user = userService.getUserById(userId);
            if (user == null) {
                return;
            }

            Long resolvedOrganizationId = resolveOrganizationId(organizationId, user);
            if (resolvedOrganizationId == null) {
                log.debug("Skip sync-user event because organization is missing for userId={}", userId);
                return;
            }

            List<String> matchedRoleNames = user.getRoleNames().stream()
                    .filter(Objects::nonNull)
                    .map(roleName -> roleName.trim().toUpperCase(Locale.ROOT))
                    .filter(roleName -> !roleName.isEmpty())
                    .distinct()
                    .toList();

            if (matchedRoleNames.isEmpty()) {
                return;
            }

            String topic = kafkaTopicProperties.getSyncUser();
            SyncUserEvent event = SyncUserEvent.builder()
                    .userId(user.getId())
                    .organizationId(resolvedOrganizationId)
                    .tenantId(resolvedOrganizationId)
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .fullName(user.getFullName())
                    .roleNames(matchedRoleNames)
                    .build();

            String partitionKey = String.valueOf(userId);
            kafkaProducer.sendMessageAsync(partitionKey, event, topic, (success, sentTopic, payload, ex) -> {
                if (success) {
                    log.info("Published sync-user event: organizationId={}, userId={}, roleNames={}, topic={}",
                            resolvedOrganizationId, userId, matchedRoleNames, sentTopic);
                    return;
                }

                log.error("Failed to publish sync-user event: organizationId={}, userId={}, roleNames={}, topic={}",
                        resolvedOrganizationId, userId, matchedRoleNames, topic, ex);
            });
        } catch (Exception e) {
            log.error("Unexpected error while publishing sync-user event for userId={}", userId, e);
        }
    }

    private Long resolveOrganizationId(Long organizationId, UserEntity user) {
        if (organizationId != null) {
            return organizationId;
        }
        return user.getPrimaryOrganizationId();
    }
}
