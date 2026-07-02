/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubPostOfficeMapping;
import serp.project.second_mile.kafka.HubPostOfficeSyncEventPublisher;
import serp.project.second_mile.kafka.event.HubPostOfficeSyncEvent;
import serp.project.second_mile.kafka.event.HubPostOfficeSyncEventType;
import serp.project.second_mile.kafka.event.HubPostOfficeSyncOrigin;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.AssignHubPostOfficeRequest;
import serp.project.second_mile.dto.response.HubPostOfficeMappingResponse;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.mapper.HubPostOfficeMappingMapper;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.service.HubPostOfficeService;

@Service
@RequiredArgsConstructor
public class HubPostOfficeServiceImpl implements HubPostOfficeService {

    private final HubRepository hubRepository;
    private final HubPostOfficeMappingRepository hubPostOfficeMappingRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;
    private final HubPostOfficeSyncEventPublisher hubPostOfficeSyncEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HubPostOfficeMappingResponse> listPostOfficesForHub(long hubId, int page, int size) {
        Hub hub = getHubOrThrow(hubId);
        validateTenantAccess(hub);
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "postOfficeCode"));

        Page<HubPostOfficeMapping> pageResult =
                hubPostOfficeMappingRepository.findByHub_IdAndTenantId(hub.getId(), tenantId, pageable);
        Page<HubPostOfficeMappingResponse> mapped = pageResult.map(HubPostOfficeMappingMapper::toResponse);

        return PageResponse.<HubPostOfficeMappingResponse>builder()
                .items(mapped.getContent())
                .page(mapped.getNumber())
                .size(mapped.getSize())
                .totalElements(mapped.getTotalElements())
                .totalPages(mapped.getTotalPages())
                .hasNext(mapped.hasNext())
                .hasPrevious(mapped.hasPrevious())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HubPostOfficeMappingResponse assignPostOfficeToHub(long hubId, AssignHubPostOfficeRequest request) {
        Hub hub = getHubOrThrow(hubId);
        validateTenantAccess(hub);
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();

        String code = normalizePostOfficeCode(request.getPostOfficeCode());
        if (code.isEmpty()) {
            throw new AppException(ErrorCode.HUB_POST_OFFICE_CODE_INVALID);
        }

        hubPostOfficeMappingRepository.deleteByTenantIdAndPostOfficeCode(tenantId, code);

        HubPostOfficeMapping mapping = HubPostOfficeMapping.builder()
                .hub(hub)
                .postOfficeCode(code)
                .tenantId(tenantId)
                .build();
        HubPostOfficeMapping saved = hubPostOfficeMappingRepository.save(mapping);

        Long hubIdForSync = hub.getId();
        Long tenantIdForSync = tenantId;
        String codeForSync = code;
        hubPostOfficeSyncEventPublisher.publish(HubPostOfficeSyncEvent.builder()
                .eventType(HubPostOfficeSyncEventType.ASSIGNED)
                .origin(HubPostOfficeSyncOrigin.SECOND_MILE)
                .tenantId(tenantIdForSync)
                .hubId(hubIdForSync)
                .postOfficeCode(codeForSync)
                .build());

        return HubPostOfficeMappingMapper.toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePostOfficeFromHub(long hubId, String postOfficeCode) {
        Hub hub = getHubOrThrow(hubId);
        validateTenantAccess(hub);
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        String code = normalizePostOfficeCode(postOfficeCode);
        if (code.isEmpty()) {
            throw new AppException(ErrorCode.HUB_POST_OFFICE_CODE_INVALID);
        }
        hubPostOfficeMappingRepository.deleteByHub_IdAndPostOfficeCodeAndTenantId(hub.getId(), code, tenantId);

        Long hubIdForSync = hub.getId();
        Long tenantIdForSync = tenantId;
        String codeForSync = code;
        hubPostOfficeSyncEventPublisher.publish(HubPostOfficeSyncEvent.builder()
                .eventType(HubPostOfficeSyncEventType.REMOVED)
                .origin(HubPostOfficeSyncOrigin.SECOND_MILE)
                .tenantId(tenantIdForSync)
                .hubId(hubIdForSync)
                .postOfficeCode(codeForSync)
                .build());
    }

    private Hub getHubOrThrow(Long id) {
        return hubRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HUB_NOT_FOUND));
    }

    private void validateTenantAccess(Hub hub) {
        Long currentTenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        if (hub.getTenantId() == null || !hub.getTenantId().equals(currentTenantId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private static String normalizePostOfficeCode(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim();
    }
}
