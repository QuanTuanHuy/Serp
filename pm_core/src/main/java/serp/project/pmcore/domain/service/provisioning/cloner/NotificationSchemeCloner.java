package serp.project.pmcore.domain.service.provisioning.cloner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.notification.entity.NotificationSchemeEntity;
import serp.project.pmcore.domain.notification.entity.NotificationSchemeEntryEntity;
import serp.project.pmcore.domain.notification.port.INotificationSchemeEntryPort;
import serp.project.pmcore.domain.notification.port.INotificationSchemePort;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.service.provisioning.support.CloneNamingHelper;
import serp.project.pmcore.domain.shared.enums.CloneMode;
import serp.project.pmcore.domain.shared.enums.SchemeType;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSchemeCloner {

    private final INotificationSchemePort notificationSchemePort;
    private final INotificationSchemeEntryPort notificationSchemeEntryPort;
    private final CloneNamingHelper cloneNamingHelper;

    public Long cloneNotificationScheme(NotificationSchemeEntity source,
                                        Long tenantId,
                                        Long userId,
                                        CloneMode cloneMode,
                                        ProvisioningExecutionContext context) {
        validateRequired(source, "source");
        validateRequired(tenantId, "tenantId");
        validateRequired(userId, "userId");

        List<NotificationSchemeEntryEntity> sourceEntries = notificationSchemeEntryPort
                .getNotificationSchemeEntriesBySchemeIdIncludingSystem(source.getId(), tenantId);

        long now = System.currentTimeMillis();

        NotificationSchemeEntity cloned = NotificationSchemeEntity.builder()
                .tenantId(tenantId)
                .name(cloneNamingHelper.buildSchemeCloneName(context.getProjectKey(), source.getName(), SchemeType.NOTIFICATION, cloneMode))
                .description(source.getDescription())
                .build();
        cloned.applyCreate(userId, now);

        NotificationSchemeEntity saved = notificationSchemePort.createNotificationScheme(cloned);

        if (!sourceEntries.isEmpty()) {
            List<NotificationSchemeEntryEntity> clonedEntries = new ArrayList<>();
            for (NotificationSchemeEntryEntity entry : sourceEntries) {
                clonedEntries.add(NotificationSchemeEntryEntity.builder()
                        .tenantId(tenantId)
                        .schemeId(saved.getId())
                        .eventId(entry.getEventId())
                        .recipientType(entry.getRecipientType())
                        .recipientRef(entry.getRecipientRef())
                        .customFieldId(entry.getCustomFieldId())
                        .channel(entry.getChannel())
                        .templateId(entry.getTemplateId())
                        .isEnabled(entry.getIsEnabled())
                        .conditionsJson(entry.getConditionsJson())
                        .createdAt(now)
                        .createdBy(userId)
                        .build());
            }

            notificationSchemeEntryPort.createNotificationSchemeEntries(clonedEntries);
        }

        log.info("Created {} NOTIFICATION scheme clone: source={} -> cloned={} (tenantId={})",
                cloneMode, source.getId(), saved.getId(), tenantId);

        return saved.getId();
    }

    private void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new DomainValidationException(
                    DomainErrorCode.CLONE_NOTIFICATION_SCHEME_FAILED,
                    fieldName + " is required"
            );
        }
    }
}