/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.core.port.store.IIssueTypeSchemeItemPort;
import serp.project.pmcore.infrastructure.store.mapper.IssueTypeSchemeItemMapper;
import serp.project.pmcore.infrastructure.store.repository.IIssueTypeSchemeItemRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class IssueTypeSchemeItemAdapter implements IIssueTypeSchemeItemPort {

    private final IIssueTypeSchemeItemRepository issueTypeSchemeItemRepository;
    private final IssueTypeSchemeItemMapper issueTypeSchemeItemMapper;

    @Override
    public IssueTypeSchemeItemEntity createIssueTypeSchemeItem(IssueTypeSchemeItemEntity item) {
        return issueTypeSchemeItemMapper.toEntity(issueTypeSchemeItemRepository.save(issueTypeSchemeItemMapper.toModel(item)));
    }

    @Override
    public List<IssueTypeSchemeItemEntity> createIssueTypeSchemeItems(List<IssueTypeSchemeItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return issueTypeSchemeItemMapper.toEntities(
                issueTypeSchemeItemRepository.saveAll(issueTypeSchemeItemMapper.toModels(items))
        );
    }

    @Override
    public List<IssueTypeSchemeItemEntity> getIssueTypeSchemeItemsBySchemeId(Long schemeId, Long tenantId) {
        return issueTypeSchemeItemMapper.toEntities(
                issueTypeSchemeItemRepository.findAllByTenantIdAndSchemeIdOrderBySequenceAsc(tenantId, schemeId)
        );
    }

    @Override
    public void deleteIssueTypeSchemeItemsBySchemeId(Long schemeId, Long tenantId) {
        issueTypeSchemeItemRepository.deleteBySchemeIdAndTenantId(schemeId, tenantId);
    }

    @Override
    public boolean existsIssueTypeInScheme(Long schemeId, Long issueTypeId, Long tenantId) {
        return issueTypeSchemeItemRepository.existsByTenantIdAndSchemeIdAndIssueTypeId(tenantId, schemeId, issueTypeId);
    }
}
