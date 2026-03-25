/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.NotificationSchemeEntity;
import serp.project.pmcore.domain.port.store.INotificationSchemePort;
import serp.project.pmcore.infrastructure.store.mapper.NotificationSchemeMapper;
import serp.project.pmcore.infrastructure.store.repository.INotificationSchemeRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationSchemeAdapter implements INotificationSchemePort {

    private final INotificationSchemeRepository notificationSchemeRepository;
    private final NotificationSchemeMapper notificationSchemeMapper;

    @Override
    public NotificationSchemeEntity createNotificationScheme(NotificationSchemeEntity scheme) {
        return notificationSchemeMapper.toEntity(
                notificationSchemeRepository.save(notificationSchemeMapper.toModel(scheme))
        );
    }

    @Override
    public Optional<NotificationSchemeEntity> getNotificationSchemeById(Long schemeId, Long tenantId) {
        return notificationSchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(notificationSchemeMapper::toEntity);
    }

    @Override
    public Optional<NotificationSchemeEntity> getNotificationSchemeByIdIncludingSystem(Long schemeId, Long tenantId) {
        return notificationSchemeRepository.findByIdAndTenantIdOrSystemTenant(schemeId, tenantId)
                .map(notificationSchemeMapper::toEntity);
    }
}
