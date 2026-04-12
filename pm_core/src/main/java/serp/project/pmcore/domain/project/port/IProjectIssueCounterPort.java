/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.port;

public interface IProjectIssueCounterPort {
    Long getNextIssueNo(Long projectId, Long tenantId);
}
