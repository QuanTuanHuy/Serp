/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.NotificationSchemeEntryEntity;
import serp.project.pmcore.domain.port.store.INotificationSchemeEntryPort;
import serp.project.pmcore.infrastructure.store.mapper.NotificationSchemeEntryMapper;
import serp.project.pmcore.infrastructure.store.repository.INotificationSchemeEntryRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationSchemeEntryAdapter implements INotificationSchemeEntryPort {

    private final INotificationSchemeEntryRepository notificationSchemeEntryRepository;
    private final NotificationSchemeEntryMapper notificationSchemeEntryMapper;

    @Override
    public List<NotificationSchemeEntryEntity> createNotificationSchemeEntries(List<NotificationSchemeEntryEntity> entries) {
        if (entries == null || entries.isEmpty()) {
            return new ArrayList<>();
        }
        return notificationSchemeEntryMapper.toEntities(
                notificationSchemeEntryRepository.saveAll(notificationSchemeEntryMapper.toModels(entries))
        );
    }

    @Override
    public List<NotificationSchemeEntryEntity> getNotificationSchemeEntriesBySchemeIdIncludingSystem(Long schemeId, Long tenantId) {
        return notificationSchemeEntryMapper.toEntities(
                notificationSchemeEntryRepository.findAllBySchemeIdAndTenantIdOrSystemTenant(schemeId, tenantId)
        );
    }
}
