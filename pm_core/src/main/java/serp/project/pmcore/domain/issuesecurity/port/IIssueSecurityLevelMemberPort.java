/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuesecurity.port;

import java.util.List;

import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelMemberEntity;

public interface IIssueSecurityLevelMemberPort {
    List<IssueSecurityLevelMemberEntity> createIssueSecurityLevelMembers(List<IssueSecurityLevelMemberEntity> members);

    List<IssueSecurityLevelMemberEntity> getIssueSecurityLevelMembersByLevelIdIncludingSystem(Long levelId, Long tenantId);

    List<IssueSecurityLevelMemberEntity> getIssueSecurityLevelMembersByLevelId(Long levelId, Long tenantId);
}
