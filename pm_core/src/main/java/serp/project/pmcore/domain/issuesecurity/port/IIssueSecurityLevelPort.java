/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuesecurity.port;

import java.util.List;

import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelEntity;

public interface IIssueSecurityLevelPort {
    List<IssueSecurityLevelEntity> createIssueSecurityLevels(List<IssueSecurityLevelEntity> levels);

    List<IssueSecurityLevelEntity> getIssueSecurityLevelsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    List<IssueSecurityLevelEntity> getIssueSecurityLevelsBySchemeId(Long schemeId, Long tenantId);
}
