/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.workflow.WorkflowView;
import serp.project.pmcore.domain.workflow.query.WorkflowListCriteria;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

@Service
@RequiredArgsConstructor
public class ListWorkflowsQueryHandler implements IQueryHandler<ListWorkflowsQuery, PageView<WorkflowView>> {

    private final IWorkflowService workflowService;

    @Override
    @Transactional(readOnly = true)
    public PageView<WorkflowView> handle(ListWorkflowsQuery query) {
        WorkflowListCriteria criteria = query.toCriteria();
        return PageViews.from(
                workflowService.listVisibleWorkflows(query.tenantId(), criteria),
                criteria,
                workflow -> WorkflowView.from(workflow, Boolean.TRUE.equals(workflow.getIsSystem()))
        );
    }
}
