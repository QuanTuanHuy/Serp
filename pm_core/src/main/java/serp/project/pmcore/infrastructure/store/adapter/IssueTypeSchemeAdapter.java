/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.core.port.store.IIssueTypeSchemePort;
import serp.project.pmcore.infrastructure.store.mapper.IssueTypeSchemeItemMapper;
import serp.project.pmcore.infrastructure.store.mapper.IssueTypeSchemeMapper;
import serp.project.pmcore.infrastructure.store.repository.IIssueTypeSchemeItemRepository;
import serp.project.pmcore.infrastructure.store.repository.IIssueTypeSchemeRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IssueTypeSchemeAdapter implements IIssueTypeSchemePort {

    private final IIssueTypeSchemeRepository issueTypeSchemeRepository;
    private final IIssueTypeSchemeItemRepository issueTypeSchemeItemRepository;
    private final IssueTypeSchemeMapper issueTypeSchemeMapper;
    private final IssueTypeSchemeItemMapper issueTypeSchemeItemMapper;

    @Override
    public IssueTypeSchemeEntity createIssueTypeScheme(IssueTypeSchemeEntity scheme) {
        return issueTypeSchemeMapper.toEntity(issueTypeSchemeRepository.save(issueTypeSchemeMapper.toModel(scheme)));
    }

    @Override
    public Optional<IssueTypeSchemeEntity> getIssueTypeSchemeById(Long schemeId, Long tenantId) {
        return issueTypeSchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(issueTypeSchemeMapper::toEntity);
    }

    @Override
    public Optional<IssueTypeSchemeEntity> getIssueTypeSchemeWithItems(Long schemeId, Long tenantId) {
        return issueTypeSchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(model -> {
                    IssueTypeSchemeEntity scheme = issueTypeSchemeMapper.toEntity(model);
                    scheme.setItems(issueTypeSchemeItemMapper.toEntities(
                            issueTypeSchemeItemRepository.findAllByTenantIdAndSchemeIdOrderBySequenceAsc(tenantId, schemeId)
                    ));
                    return scheme;
                });
    }

    @Override
    public List<IssueTypeSchemeEntity> listIssueTypeSchemes(Long tenantId) {
        return issueTypeSchemeMapper.toEntities(issueTypeSchemeRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId));
    }

    @Override
    public void updateIssueTypeScheme(IssueTypeSchemeEntity scheme) {
        issueTypeSchemeRepository.save(issueTypeSchemeMapper.toModel(scheme));
    }

    @Override
    public void deleteIssueTypeScheme(Long schemeId, Long tenantId) {
        issueTypeSchemeRepository.deleteByIdAndTenantId(schemeId, tenantId);
    }

    @Override
    public boolean existsByName(Long tenantId, String name) {
        return issueTypeSchemeRepository.existsByTenantIdAndName(tenantId, name);
    }
}
