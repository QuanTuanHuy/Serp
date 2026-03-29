package serp.project.pmcore.domain.project.provisioning.provisioner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.notification.entity.NotificationSchemeEntity;
import serp.project.pmcore.domain.notification.port.INotificationSchemePort;
import serp.project.pmcore.domain.project.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.project.provisioning.cloner.NotificationSchemeCloner;
import serp.project.pmcore.domain.project.provisioning.provisioner.base.AbstractMappedSharedProvisioner;
import serp.project.pmcore.domain.shared.enums.CloneMode;
import serp.project.pmcore.domain.shared.enums.SchemeType;
import serp.project.pmcore.domain.shared.port.store.ITenantSchemeMappingPort;

import java.util.Optional;

@Component
@Slf4j
public class NotificationSchemeProvisioner extends AbstractMappedSharedProvisioner<NotificationSchemeEntity> {

    private final INotificationSchemePort notificationSchemePort;
    private final NotificationSchemeCloner notificationSchemeCloner;

    public NotificationSchemeProvisioner(ITenantSchemeMappingPort tenantSchemeMappingPort,
                                         INotificationSchemePort notificationSchemePort,
                                         NotificationSchemeCloner notificationSchemeCloner) {
        super(tenantSchemeMappingPort);
        this.notificationSchemePort = notificationSchemePort;
        this.notificationSchemeCloner = notificationSchemeCloner;
    }

    @Override
    public SchemeType supports() {
        return SchemeType.NOTIFICATION;
    }

    @Override
    protected Optional<NotificationSchemeEntity> loadSourceByIdIncludingSystem(Long sourceSchemeId, Long tenantId) {
        return notificationSchemePort.getNotificationSchemeByIdIncludingSystem(sourceSchemeId, tenantId);
    }

    @Override
    protected Long getSourceId(NotificationSchemeEntity source) {
        return source.getId();
    }

    @Override
    protected Long getSourceTenantId(NotificationSchemeEntity source) {
        return source.getTenantId();
    }

    @Override
    protected boolean tenantSchemeExists(Long tenantSchemeId, Long tenantId) {
        return notificationSchemePort.getNotificationSchemeById(tenantSchemeId, tenantId).isPresent();
    }

    @Override
    protected String sourceEntityLabel() {
        return "notification scheme";
    }

    @Override
    protected Long cloneForTenant(NotificationSchemeEntity source,
                                  Long tenantId,
                                  Long userId,
                                  CloneMode cloneMode,
                                  ProvisioningExecutionContext context) {
        return notificationSchemeCloner.cloneNotificationScheme(source, tenantId, userId, cloneMode, context);
    }
}
