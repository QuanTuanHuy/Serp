/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuesecurity.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelEntity;

public interface IIssueSecurityLevelPort {
    List<IssueSecurityLevelEntity> createIssueSecurityLevels(List<IssueSecurityLevelEntity> levels);

    List<IssueSecurityLevelEntity> getIssueSecurityLevelsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    List<IssueSecurityLevelEntity> getIssueSecurityLevelsBySchemeId(Long schemeId, Long tenantId);

    Optional<IssueSecurityLevelEntity> getIssueSecurityLevelByIdAndSchemeId(Long levelId, Long schemeId, Long tenantId);
}
