/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.issyetype.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.domain.issyetype.port.IIssueTypeScreenSchemePort;
import serp.project.pmcore.infrastructure.store.mapper.IssueTypeScreenSchemeMapper;
import serp.project.pmcore.infrastructure.store.repository.IIssueTypeScreenSchemeRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IssueTypeScreenSchemeAdapter implements IIssueTypeScreenSchemePort {

    private final IIssueTypeScreenSchemeRepository issueTypeScreenSchemeRepository;
    private final IssueTypeScreenSchemeMapper issueTypeScreenSchemeMapper;

    @Override
    public IssueTypeScreenSchemeEntity createIssueTypeScreenScheme(IssueTypeScreenSchemeEntity scheme) {
        return issueTypeScreenSchemeMapper.toEntity(
                issueTypeScreenSchemeRepository.save(issueTypeScreenSchemeMapper.toModel(scheme))
        );
    }

    @Override
    public Optional<IssueTypeScreenSchemeEntity> getIssueTypeScreenSchemeById(Long schemeId, Long tenantId) {
        return issueTypeScreenSchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(issueTypeScreenSchemeMapper::toEntity);
    }

    @Override
    public Optional<IssueTypeScreenSchemeEntity> getIssueTypeScreenSchemeByIdIncludingSystem(Long schemeId, Long tenantId) {
        return issueTypeScreenSchemeRepository.findByIdAndTenantIdOrSystemTenant(schemeId, tenantId)
                .map(issueTypeScreenSchemeMapper::toEntity);
    }
}
