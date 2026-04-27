/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelMemberEntity;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelMemberPort;
import serp.project.pmcore.infrastructure.store.mapper.IssueSecurityLevelMemberMapper;
import serp.project.pmcore.infrastructure.store.repository.IIssueSecurityLevelMemberRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class IssueSecurityLevelMemberAdapter implements IIssueSecurityLevelMemberPort {

    private final IIssueSecurityLevelMemberRepository issueSecurityLevelMemberRepository;
    private final IssueSecurityLevelMemberMapper issueSecurityLevelMemberMapper;

    @Override
    public List<IssueSecurityLevelMemberEntity> createIssueSecurityLevelMembers(List<IssueSecurityLevelMemberEntity> members) {
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }
        return issueSecurityLevelMemberMapper.toEntities(
                issueSecurityLevelMemberRepository.saveAll(issueSecurityLevelMemberMapper.toModels(members))
        );
    }

    @Override
    public List<IssueSecurityLevelMemberEntity> getIssueSecurityLevelMembersByLevelIdIncludingSystem(Long levelId, Long tenantId) {
        return issueSecurityLevelMemberMapper.toEntities(
                issueSecurityLevelMemberRepository.findAllByLevelIdAndTenantIdOrSystemTenant(levelId, tenantId)
        );
    }

    @Override
    public List<IssueSecurityLevelMemberEntity> getIssueSecurityLevelMembersByLevelId(Long levelId, Long tenantId) {
        return issueSecurityLevelMemberMapper.toEntities(
                issueSecurityLevelMemberRepository.findAllByLevelIdAndTenantId(levelId, tenantId)
        );
    }
}
