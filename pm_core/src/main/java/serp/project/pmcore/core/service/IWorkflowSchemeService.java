/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service;

public interface IWorkflowSchemeService {
    Long resolveWorkflowId(Long workflowSchemeId, Long issueTypeId, Long tenantId);
}
