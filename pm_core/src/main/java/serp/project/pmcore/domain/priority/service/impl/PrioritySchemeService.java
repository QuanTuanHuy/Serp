/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.port.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.port.IPrioritySchemePort;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrioritySchemeService implements IPrioritySchemeService {

    private final IPrioritySchemePort prioritySchemePort;
    private final IPrioritySchemeItemPort prioritySchemeItemPort;

    @Override
    public Long resolveDefaultPriorityId(Long prioritySchemeId, Long tenantId) {
        PrioritySchemeEntity priorityScheme = getPrioritySchemeById(prioritySchemeId, tenantId);
        if (priorityScheme.getDefaultPriorityId() == null) {
            throw new DomainValidationException(
                    DomainErrorCode.DEFAULT_PRIORITY_NOT_CONFIGURED,
                    "Priority scheme has no default priority: schemeId=" + prioritySchemeId
            );
        }
        return priorityScheme.getDefaultPriorityId();
    }

    @Override
    public Long validatePriorityIdInScheme(Long prioritySchemeId, Long requestedPriorityId, Long tenantId) {
        if (requestedPriorityId == null) {
            return null;
        }

        getPrioritySchemeById(prioritySchemeId, tenantId);
        List<PrioritySchemeItemEntity> priorityItems = prioritySchemeItemPort
                .getPrioritySchemeItemsBySchemeId(prioritySchemeId, tenantId);
        boolean inScheme = priorityItems.stream()
                .map(PrioritySchemeItemEntity::getPriorityId)
                .anyMatch(requestedPriorityId::equals);
        if (!inScheme) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PRIORITY_NOT_IN_SCHEME,
                    "Priority is not allowed in priority scheme: schemeId=" + prioritySchemeId + ", priorityId=" + requestedPriorityId
            );
        }

        return requestedPriorityId;
    }

    @Override
    public PrioritySchemeEntity getPrioritySchemeById(Long prioritySchemeId, Long tenantId) {
        return prioritySchemePort.getPrioritySchemeById(prioritySchemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND,
                        "Priority scheme not found: id=" + prioritySchemeId
                ));
    }

}
