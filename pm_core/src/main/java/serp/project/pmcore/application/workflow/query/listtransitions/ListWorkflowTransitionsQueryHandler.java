/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.query.listtransitions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.workflow.WorkflowTransitionView;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListWorkflowTransitionsQueryHandler
        implements IQueryHandler<ListWorkflowTransitionsQuery, List<WorkflowTransitionView>> {

    private final IWorkflowService workflowService;

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowTransitionView> handle(ListWorkflowTransitionsQuery query) {
        return workflowService.listWorkflowTransitions(
                        query.workflowId(),
                        query.fromStepId(),
                        query.tenantId()
                ).stream()
                .map(WorkflowTransitionView::from)
                .toList();
    }
}
