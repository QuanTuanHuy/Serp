/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelEntity;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelPort;
import serp.project.pmcore.infrastructure.store.mapper.IssueSecurityLevelMapper;
import serp.project.pmcore.infrastructure.store.repository.IIssueSecurityLevelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IssueSecurityLevelAdapter implements IIssueSecurityLevelPort {

    private final IIssueSecurityLevelRepository issueSecurityLevelRepository;
    private final IssueSecurityLevelMapper issueSecurityLevelMapper;

    @Override
    public List<IssueSecurityLevelEntity> createIssueSecurityLevels(List<IssueSecurityLevelEntity> levels) {
        if (levels == null || levels.isEmpty()) {
            return new ArrayList<>();
        }
        return issueSecurityLevelMapper.toEntities(
                issueSecurityLevelRepository.saveAll(issueSecurityLevelMapper.toModels(levels))
        );
    }

    @Override
    public List<IssueSecurityLevelEntity> getIssueSecurityLevelsBySchemeIdIncludingSystem(Long schemeId, Long tenantId) {
        return issueSecurityLevelMapper.toEntities(
                issueSecurityLevelRepository.findAllBySchemeIdAndTenantIdOrSystemTenant(schemeId, tenantId)
        );
    }

    @Override
    public List<IssueSecurityLevelEntity> getIssueSecurityLevelsBySchemeId(Long schemeId, Long tenantId) {
        return issueSecurityLevelMapper.toEntities(
                issueSecurityLevelRepository.findAllBySchemeIdAndTenantId(schemeId, tenantId)
        );
    }

    @Override
    public Optional<IssueSecurityLevelEntity> getIssueSecurityLevelByIdAndSchemeId(Long levelId, Long schemeId, Long tenantId) {
        return issueSecurityLevelRepository.findByIdAndSchemeIdAndTenantId(levelId, schemeId, tenantId)
                .map(issueSecurityLevelMapper::toEntity);
    }
}
