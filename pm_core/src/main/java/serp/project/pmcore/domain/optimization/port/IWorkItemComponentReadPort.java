/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.port;

import serp.project.pmcore.domain.optimization.model.WorkItemComponentLink;

import java.util.List;

public interface IWorkItemComponentReadPort {
    List<WorkItemComponentLink> listActiveByWorkItemIds(Long tenantId, List<Long> workItemIds);
}
