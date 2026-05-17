/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.model.WorkItemComponentLink;
import serp.project.pmcore.domain.optimization.port.IWorkItemComponentReadPort;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemComponentRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkItemComponentReadAdapter implements IWorkItemComponentReadPort {

    private final IWorkItemComponentRepository workItemComponentRepository;

    @Override
    public List<WorkItemComponentLink> listActiveByWorkItemIds(Long tenantId, List<Long> workItemIds) {
        if (workItemIds == null || workItemIds.isEmpty()) {
            return List.of();
        }
        return workItemComponentRepository.findActiveLinksByTenantIdAndWorkItemIdIn(tenantId, workItemIds)
                .stream()
                .map(link -> new WorkItemComponentLink(link.getWorkItemId(), link.getComponentId()))
                .toList();
    }
}
