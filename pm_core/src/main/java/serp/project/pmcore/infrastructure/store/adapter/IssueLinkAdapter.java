/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkDetailEntity;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkEntity;
import serp.project.pmcore.domain.issuelink.port.IIssueLinkPort;
import serp.project.pmcore.infrastructure.store.mapper.IssueLinkMapper;
import serp.project.pmcore.infrastructure.store.model.IssueLinkModel;
import serp.project.pmcore.infrastructure.store.repository.IIssueLinkRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IssueLinkAdapter implements IIssueLinkPort {

    private final IIssueLinkRepository issueLinkRepository;
    private final IssueLinkMapper issueLinkMapper;

    @Override
    public IssueLinkEntity save(IssueLinkEntity issueLink) {
        IssueLinkModel saved = issueLinkRepository.save(issueLinkMapper.toModel(issueLink));
        return issueLinkMapper.toEntity(saved);
    }

    @Override
    public Optional<IssueLinkEntity> getById(Long id, Long tenantId) {
        return issueLinkRepository.findByIdAndTenantId(id, tenantId)
                .map(issueLinkMapper::toEntity);
    }

    @Override
    public Optional<IssueLinkEntity> getActiveDuplicate(Long tenantId, Long sourceId, Long targetId, Long linkTypeId) {
        return issueLinkRepository.findFirstByTenantIdAndSourceIdAndTargetIdAndLinkTypeIdOrderByIdAsc(
                        tenantId, sourceId, targetId, linkTypeId)
                .map(issueLinkMapper::toEntity);
    }

    @Override
    public void delete(Long id, Long tenantId) {
        issueLinkRepository.deleteByIdAndTenantId(id, tenantId);
    }

    @Override
    public List<IssueLinkDetailEntity> listByWorkItemId(Long tenantId, Long workItemId) {
        return issueLinkMapper.toDetailEntities(issueLinkRepository.findIssueLinkDetailsByWorkItemId(tenantId, workItemId));
    }
}
