/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;

public interface IIssueTypePort {
    IssueTypeEntity createIssueType(IssueTypeEntity issueType);

    Optional<IssueTypeEntity> getIssueTypeById(Long issueTypeId, Long tenantId);

    Optional<IssueTypeEntity> getIssueTypeByIdIncludingSystem(Long issueTypeId, Long tenantId);

    Optional<IssueTypeEntity> getIssueTypeByTypeKey(Long tenantId, String typeKey);

    List<IssueTypeEntity> listIssueTypes(Long tenantId);

    void updateIssueType(IssueTypeEntity issueType);

    void deleteIssueType(Long issueTypeId, Long tenantId);

    boolean existsByTypeKey(Long tenantId, String typeKey);
}
