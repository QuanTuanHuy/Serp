/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service;

public interface IWorkflowSchemeService {
    Long resolveWorkflowId(Long workflowSchemeId, Long issueTypeId, Long tenantId);
}
