package serp.project.pmcore.domain.service.provisioning.cloner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.NotificationSchemeEntity;
import serp.project.pmcore.domain.entity.NotificationSchemeEntryEntity;
import serp.project.pmcore.domain.enums.CloneMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainValidationException;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.INotificationSchemeEntryPort;
import serp.project.pmcore.domain.port.store.INotificationSchemePort;
import serp.project.pmcore.domain.service.provisioning.support.CloneNamingHelper;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSchemeCloner {

    private final INotificationSchemePort notificationSchemePort;
    private final INotificationSchemeEntryPort notificationSchemeEntryPort;
    private final CloneNamingHelper cloneNamingHelper;

    public Long cloneNotificationSchemeBySourceId(Long sourceNotificationSchemeId,
                                                  Long tenantId,
                                                  Long userId,
                                                  CloneMode cloneMode) {
        validateRequired(sourceNotificationSchemeId, "sourceNotificationSchemeId");
        validateRequired(tenantId, "tenantId");
        validateRequired(userId, "userId");

        NotificationSchemeEntity source = notificationSchemePort
                .getNotificationSchemeByIdIncludingSystem(sourceNotificationSchemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.NOTIFICATION_SCHEME_NOT_FOUND,
                        "Notification scheme not found for source id=" + sourceNotificationSchemeId
                ));

        return cloneNotificationScheme(source, tenantId, userId, cloneMode);
    }

    public Long cloneNotificationScheme(NotificationSchemeEntity source,
                                        Long tenantId,
                                        Long userId,
                                        CloneMode cloneMode) {
        validateRequired(source, "source");
        validateRequired(tenantId, "tenantId");
        validateRequired(userId, "userId");

        List<NotificationSchemeEntryEntity> sourceEntries = notificationSchemeEntryPort
                .getNotificationSchemeEntriesBySchemeIdIncludingSystem(source.getId(), tenantId);

        long now = System.currentTimeMillis();

        NotificationSchemeEntity cloned = NotificationSchemeEntity.builder()
                .tenantId(tenantId)
                .name(cloneNamingHelper.buildSchemeCloneName("", source.getName(), SchemeType.NOTIFICATION, cloneMode))
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