/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuelink.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkDetailEntity;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkEntity;
import serp.project.pmcore.domain.issuelink.port.IIssueLinkPort;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueLinkService implements IIssueLinkService {

    private final IIssueLinkPort issueLinkPort;

    @Override
    public IssueLinkEntity create(IssueLinkEntity draft, Long tenantId, Long userId) {
        if (Objects.equals(draft.getSourceId(), draft.getTargetId())) {
            throw new BusinessRuleViolationException(DomainErrorCode.SELF_LINK_NOT_ALLOWED);
        }
        issueLinkPort.getActiveDuplicate(tenantId, draft.getSourceId(), draft.getTargetId(), draft.getLinkTypeId())
                .ifPresent(existing -> {
                    throw new BusinessRuleViolationException(DomainErrorCode.DUPLICATE_ISSUE_LINK);
                });

        draft.setTenantId(tenantId);
        draft.setDeletedAt(null);
        draft.applyCreate(userId, System.currentTimeMillis());
        return issueLinkPort.save(draft);
    }

    @Override
    public IssueLinkEntity getById(Long id, Long tenantId) {
        return issueLinkPort.getById(id, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.issueLink(id));
    }

    @Override
    public IssueLinkEntity softDelete(IssueLinkEntity issueLink, Long userId, Long deletedAt) {
        issueLink.setDeletedAt(deletedAt);
        issueLink.applyUpdate(userId, deletedAt);
        return issueLinkPort.save(issueLink);
    }

    @Override
    public List<IssueLinkDetailEntity> listByWorkItemId(Long tenantId, Long workItemId) {
        return issueLinkPort.listByWorkItemId(tenantId, workItemId);
    }
}
