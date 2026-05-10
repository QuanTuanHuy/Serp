/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubPostOfficeMapping;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kafka.event.HubPostOfficeSyncEvent;
import serp.project.second_mile.kafka.event.HubPostOfficeSyncEventType;
import serp.project.second_mile.kafka.event.HubPostOfficeSyncOrigin;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.service.HubPostOfficeInboundSyncService;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubPostOfficeInboundSyncServiceImpl implements HubPostOfficeInboundSyncService {

    private final HubRepository hubRepository;
    private final HubPostOfficeMappingRepository hubPostOfficeMappingRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyFirstMileKafkaEvent(HubPostOfficeSyncEvent event) {
        if (event == null || !HubPostOfficeSyncOrigin.FIRST_MILE.equals(event.getOrigin())) {
            return;
        }
        if (event.getTenantId() == null || event.getPostOfficeCode() == null
                || event.getPostOfficeCode().isBlank() || event.getEventType() == null) {
            log.warn("Ignoring invalid HubPostOffice sync event: {}", event);
            return;
        }

        String code = event.getPostOfficeCode().trim();
        Long tenantId = event.getTenantId();

        if (HubPostOfficeSyncEventType.REMOVED.equals(event.getEventType())) {
            if (event.getHubId() == null) {
                hubPostOfficeMappingRepository.deleteByTenantIdAndPostOfficeCode(tenantId, code);
            } else {
                hubPostOfficeMappingRepository.deleteByHub_IdAndPostOfficeCodeAndTenantId(
                        event.getHubId(), code, tenantId);
            }
            log.info("Inbound FIRST_MILE REMOVED applied: tenantId={}, code={}, hubId={}",
                    tenantId, code, event.getHubId());
            return;
        }

        if (HubPostOfficeSyncEventType.ASSIGNED.equals(event.getEventType())) {
            if (event.getHubId() == null || event.getHubId() < 1) {
                log.warn("ASSIGNED from first-mile missing hub_id: tenantId={}, code={}", tenantId, code);
                return;
            }
            Hub hub = hubRepository.findById(event.getHubId())
                    .orElseThrow(() -> new AppException(ErrorCode.HUB_NOT_FOUND));
            if (hub.getTenantId() == null || !hub.getTenantId().equals(tenantId)) {
                log.warn("Hub tenant mismatch on inbound sync: hubId={}, eventTenant={}", hub.getId(), tenantId);
                return;
            }
            hubPostOfficeMappingRepository.deleteByTenantIdAndPostOfficeCode(tenantId, code);
            HubPostOfficeMapping mapping = HubPostOfficeMapping.builder()
                    .hub(hub)
                    .postOfficeCode(code)
                    .tenantId(tenantId)
                    .build();
            hubPostOfficeMappingRepository.save(mapping);
            log.info("Inbound FIRST_MILE ASSIGNED applied: tenantId={}, code={}, hubId={}",
                    tenantId, code, event.getHubId());
        }
    }
}
