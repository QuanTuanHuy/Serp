/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.service;

public interface IIssueTypeSchemeService {
    void validateIssueTypeInScheme(Long schemeId, Long issueTypeId, Long tenantId);
}
