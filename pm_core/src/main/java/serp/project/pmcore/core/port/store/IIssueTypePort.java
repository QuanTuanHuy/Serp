/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.IssueTypeEntity;

import java.util.List;
import java.util.Optional;

public interface IIssueTypePort {
    IssueTypeEntity createIssueType(IssueTypeEntity issueType);

    Optional<IssueTypeEntity> getIssueTypeById(Long issueTypeId, Long tenantId);

    List<IssueTypeEntity> listIssueTypes(Long tenantId);

    void updateIssueType(IssueTypeEntity issueType);

    void deleteIssueType(Long issueTypeId, Long tenantId);

    boolean existsByTypeKey(Long tenantId, String typeKey);
}
