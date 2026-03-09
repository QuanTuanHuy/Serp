/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service;

public interface IIssueTypeSchemeService {
    void validateIssueTypeInScheme(Long schemeId, Long issueTypeId, Long tenantId);
}
