/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.IssueSecurityLevelEntity;

import java.util.List;

public interface IIssueSecurityLevelPort {
    List<IssueSecurityLevelEntity> createIssueSecurityLevels(List<IssueSecurityLevelEntity> levels);

    List<IssueSecurityLevelEntity> getIssueSecurityLevelsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    List<IssueSecurityLevelEntity> getIssueSecurityLevelsBySchemeId(Long schemeId, Long tenantId);
}
