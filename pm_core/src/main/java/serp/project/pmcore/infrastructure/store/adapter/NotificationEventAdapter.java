/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.notification.entity.NotificationEventEntity;
import serp.project.pmcore.domain.notification.port.INotificationEventPort;
import serp.project.pmcore.infrastructure.store.mapper.NotificationEventMapper;
import serp.project.pmcore.infrastructure.store.repository.INotificationEventRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationEventAdapter implements INotificationEventPort {

    private final INotificationEventRepository notificationEventRepository;
    private final NotificationEventMapper notificationEventMapper;

    @Override
    public Optional<NotificationEventEntity> getNotificationEventById(Long eventId, Long tenantId) {
        return notificationEventRepository.findByIdAndTenantId(eventId, tenantId)
                .map(notificationEventMapper::toEntity);
    }

    @Override
    public Optional<NotificationEventEntity> getNotificationEventByIdIncludingSystem(Long eventId, Long tenantId) {
        return notificationEventRepository.findByIdAndTenantIdOrSystemTenant(eventId, tenantId)
                .map(notificationEventMapper::toEntity);
    }
}
