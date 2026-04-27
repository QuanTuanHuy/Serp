/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.query.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.workflow.WorkflowValidationView;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

@Service
@RequiredArgsConstructor
public class ValidateWorkflowQueryHandler implements IQueryHandler<ValidateWorkflowQuery, WorkflowValidationView> {

    private final IWorkflowService workflowService;

    @Override
    @Transactional(readOnly = true)
    public WorkflowValidationView handle(ValidateWorkflowQuery query) {
        return WorkflowValidationView.from(workflowService.validateWorkflow(query.workflowId(), query.tenantId()));
    }
}
