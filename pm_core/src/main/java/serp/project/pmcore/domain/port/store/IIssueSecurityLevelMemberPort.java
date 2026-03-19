/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.IssueSecurityLevelMemberEntity;

import java.util.List;

public interface IIssueSecurityLevelMemberPort {
    List<IssueSecurityLevelMemberEntity> createIssueSecurityLevelMembers(List<IssueSecurityLevelMemberEntity> members);

    List<IssueSecurityLevelMemberEntity> getIssueSecurityLevelMembersByLevelIdIncludingSystem(Long levelId, Long tenantId);
}
