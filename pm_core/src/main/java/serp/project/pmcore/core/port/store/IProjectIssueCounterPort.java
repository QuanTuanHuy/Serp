/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

public interface IProjectIssueCounterPort {
    Long getNextIssueNo(Long projectId, Long tenantId);
}
