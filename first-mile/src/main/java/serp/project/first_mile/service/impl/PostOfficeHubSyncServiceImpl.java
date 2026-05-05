/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.dto.message.HubPostOfficeSyncEvent;
import serp.project.first_mile.dto.message.HubPostOfficeSyncEventType;
import serp.project.first_mile.dto.message.HubPostOfficeSyncOrigin;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.service.PostOfficeHubSyncService;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostOfficeHubSyncServiceImpl implements PostOfficeHubSyncService {

    private final PostOfficeRepository postOfficeRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyInboundHubPostOfficeEvent(HubPostOfficeSyncEvent event) {
        if (event == null || !HubPostOfficeSyncOrigin.SECOND_MILE.equals(event.getOrigin())) {
            return;
        }
        if (event.getTenantId() == null || event.getPostOfficeCode() == null
                || event.getPostOfficeCode().isBlank() || event.getEventType() == null) {
            log.warn("Ignoring invalid HubPostOffice sync event: {}", event);
            return;
        }

        String code = event.getPostOfficeCode().trim();
        Optional<PostOffice> postOfficeOpt =
                postOfficeRepository.findByCodeIgnoreCaseAndTenantId(code, event.getTenantId());
        if (postOfficeOpt.isEmpty()) {
            log.warn("No post office for inbound hub sync: tenantId={}, code={}", event.getTenantId(), code);
            return;
        }

        PostOffice postOffice = postOfficeOpt.get();
        if (HubPostOfficeSyncEventType.ASSIGNED.equals(event.getEventType())) {
            if (event.getHubId() == null || event.getHubId() < 1) {
                log.warn("ASSIGNED hub sync missing hub_id: tenantId={}, code={}", event.getTenantId(), code);
                return;
            }
            postOffice.setHubId(event.getHubId());
            postOfficeRepository.save(postOffice);
            log.info("Inbound hub ASSIGNED synced to post office: tenantId={}, code={}, hubId={}",
                    event.getTenantId(), code, event.getHubId());
            return;
        }

        if (HubPostOfficeSyncEventType.REMOVED.equals(event.getEventType())) {
            Long currentHubId = postOffice.getHubId();
            if (currentHubId == null) {
                return;
            }
            if (event.getHubId() == null || Objects.equals(currentHubId, event.getHubId())) {
                postOffice.setHubId(null);
                postOfficeRepository.save(postOffice);
                log.info("Inbound hub REMOVED synced to post office: tenantId={}, code={}, clearedHubId={}",
                        event.getTenantId(), code, currentHubId);
            }
        }
    }
}
