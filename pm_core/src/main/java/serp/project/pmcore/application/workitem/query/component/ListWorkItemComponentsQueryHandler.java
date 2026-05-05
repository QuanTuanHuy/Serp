/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.workitem.WorkItemComponentView;
import serp.project.pmcore.application.workitem.support.WorkItemComponentAccessHelper;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListWorkItemComponentsQueryHandler
        implements IQueryHandler<ListWorkItemComponentsQuery, List<WorkItemComponentView>> {

    private final WorkItemComponentAccessHelper accessHelper;
    private final IWorkItemReadPort workItemReadPort;

    @Override
    @Transactional(readOnly = true)
    public List<WorkItemComponentView> handle(ListWorkItemComponentsQuery query) {
        accessHelper.requireReadableWorkItem(
                query.projectId(),
                query.workItemId(),
                query.tenantId(),
                query.userId(),
                query.groupKeys()
        );
        return workItemReadPort.getActiveComponentsByWorkItemId(query.workItemId(), query.tenantId())
                .stream()
                .map(WorkItemComponentView::from)
                .toList();
    }
}
