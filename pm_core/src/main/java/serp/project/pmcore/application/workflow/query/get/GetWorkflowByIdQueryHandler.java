/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.workflow.WorkflowView;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

@Service
@RequiredArgsConstructor
public class GetWorkflowByIdQueryHandler implements IQueryHandler<GetWorkflowByIdQuery, WorkflowView> {

    private final IWorkflowService workflowService;

    @Override
    @Transactional(readOnly = true)
    public WorkflowView handle(GetWorkflowByIdQuery query) {
        WorkflowEntity workflow = workflowService.getVisibleWorkflowById(query.workflowId(), query.tenantId());
        return WorkflowView.from(workflow, Boolean.TRUE.equals(workflow.getIsSystem()));
    }
}
