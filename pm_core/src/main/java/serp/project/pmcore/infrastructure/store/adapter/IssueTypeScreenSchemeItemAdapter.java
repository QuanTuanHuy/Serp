/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeScreenSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.infrastructure.store.mapper.IssueTypeScreenSchemeItemMapper;
import serp.project.pmcore.infrastructure.store.repository.IIssueTypeScreenSchemeItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IssueTypeScreenSchemeItemAdapter implements IIssueTypeScreenSchemeItemPort {

    private final IIssueTypeScreenSchemeItemRepository issueTypeScreenSchemeItemRepository;
    private final IssueTypeScreenSchemeItemMapper issueTypeScreenSchemeItemMapper;

    @Override
    public List<IssueTypeScreenSchemeItemEntity> createIssueTypeScreenSchemeItems(List<IssueTypeScreenSchemeItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return issueTypeScreenSchemeItemMapper.toEntities(
                issueTypeScreenSchemeItemRepository.saveAll(issueTypeScreenSchemeItemMapper.toModels(items))
        );
    }

    @Override
    public List<IssueTypeScreenSchemeItemEntity> getIssueTypeScreenSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId) {
        return issueTypeScreenSchemeItemMapper.toEntities(
                issueTypeScreenSchemeItemRepository.findAllBySchemeIdAndTenantIdOrSystemTenant(schemeId, tenantId)
        );
    }

    @Override
    public Optional<IssueTypeScreenSchemeItemEntity> getItemBySchemeIdAndIssueTypeId(Long schemeId, Long issueTypeId, Long tenantId) {
        return issueTypeScreenSchemeItemRepository.findBySchemeIdAndIssueTypeIdAndTenantId(schemeId, issueTypeId, tenantId)
                .map(issueTypeScreenSchemeItemMapper::toEntity);
    }

    @Override
    public List<IssueTypeScreenSchemeItemEntity> getIssueTypeScreenSchemeItemsBySchemeId(Long schemeId, Long tenantId) {
        return issueTypeScreenSchemeItemMapper.toEntities(
                issueTypeScreenSchemeItemRepository.findAllBySchemeIdAndTenantId(schemeId, tenantId)
        );
    }

    @Override
    public boolean existsByIssueTypeId(Long issueTypeId, Long tenantId) {
        return issueTypeScreenSchemeItemRepository.existsByIssueTypeIdAndTenantId(issueTypeId, tenantId);
    }
}
