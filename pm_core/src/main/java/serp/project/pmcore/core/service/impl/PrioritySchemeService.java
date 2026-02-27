/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.core.domain.entity.PrioritySchemeEntity;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.core.port.store.IPrioritySchemePort;
import serp.project.pmcore.core.service.IPrioritySchemeService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrioritySchemeService implements IPrioritySchemeService {

    private final IPrioritySchemePort prioritySchemePort;

    @Override
    public Long resolvePriorityId(Long prioritySchemeId, Long tenantId) {
        return prioritySchemePort.getPrioritySchemeById(prioritySchemeId, tenantId)
                .map(PrioritySchemeEntity::getDefaultPriorityId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));
    }

    @Override
    public PrioritySchemeEntity getPrioritySchemeById(Long prioritySchemeId, Long tenantId) {
        return prioritySchemePort.getPrioritySchemeById(prioritySchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));
    }

}
