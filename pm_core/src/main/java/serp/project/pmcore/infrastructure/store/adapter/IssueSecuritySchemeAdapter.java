/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.issuesecurity.entity.IssueSecuritySchemeEntity;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecuritySchemePort;
import serp.project.pmcore.infrastructure.store.mapper.IssueSecuritySchemeMapper;
import serp.project.pmcore.infrastructure.store.repository.IIssueSecuritySchemeRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IssueSecuritySchemeAdapter implements IIssueSecuritySchemePort {

    private final IIssueSecuritySchemeRepository issueSecuritySchemeRepository;
    private final IssueSecuritySchemeMapper issueSecuritySchemeMapper;

    @Override
    public IssueSecuritySchemeEntity createIssueSecurityScheme(IssueSecuritySchemeEntity scheme) {
        return issueSecuritySchemeMapper.toEntity(
                issueSecuritySchemeRepository.save(issueSecuritySchemeMapper.toModel(scheme))
        );
    }

    @Override
    public void updateIssueSecurityScheme(IssueSecuritySchemeEntity scheme) {
        issueSecuritySchemeRepository.save(issueSecuritySchemeMapper.toModel(scheme));
    }

    @Override
    public Optional<IssueSecuritySchemeEntity> getIssueSecuritySchemeById(Long schemeId, Long tenantId) {
        return issueSecuritySchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(issueSecuritySchemeMapper::toEntity);
    }

    @Override
    public Optional<IssueSecuritySchemeEntity> getIssueSecuritySchemeByIdIncludingSystem(Long schemeId, Long tenantId) {
        return issueSecuritySchemeRepository.findByIdAndTenantIdOrSystemTenant(schemeId, tenantId)
                .map(issueSecuritySchemeMapper::toEntity);
    }
}
