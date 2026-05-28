/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.query.editor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.workflow.WorkflowStepView;
import serp.project.pmcore.application.workflow.WorkflowTransitionView;
import serp.project.pmcore.application.workflow.WorkflowView;
import serp.project.pmcore.domain.shared.enums.WorkflowVersionState;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionPort;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetWorkflowEditorQueryHandler implements IQueryHandler<GetWorkflowEditorQuery, WorkflowEditorView> {

    private final IWorkflowService workflowService;
    private final IWorkflowStepPort workflowStepPort;
    private final IWorkflowTransitionPort workflowTransitionPort;

    @Override
    @Transactional(readOnly = true)
    public WorkflowEditorView handle(GetWorkflowEditorQuery query) {
        WorkflowEntity workflow = workflowService.getVisibleWorkflowById(query.workflowId(), query.tenantId());
        Long versionId = resolveVersionId(workflow);
        WorkflowVersionState versionState = workflow.getDraftVersionId() != null
                ? WorkflowVersionState.DRAFT
                : WorkflowVersionState.PUBLISHED;
        boolean editable = workflow.getDraftVersionId() != null && !Boolean.TRUE.equals(workflow.getIsSystem());

        List<WorkflowStepView> steps = versionId == null
                ? List.of()
                : workflowStepPort.getWorkflowStepsByWorkflowVersionId(versionId, query.tenantId())
                        .stream()
                        .sorted(Comparator
                                .comparing((WorkflowStepEntity step) -> step.getStepOrder() == null
                                        ? Integer.MAX_VALUE
                                        : step.getStepOrder())
                                .thenComparing(step -> step.getId() == null ? Long.MAX_VALUE : step.getId()))
                        .map(WorkflowStepView::from)
                        .toList();

        List<WorkflowTransitionView> transitions = versionId == null
                ? List.of()
                : workflowTransitionPort.getWorkflowTransitionsByWorkflowVersionId(versionId, query.tenantId())
                        .stream()
                        .sorted(Comparator
                                .comparing((WorkflowTransitionEntity transition) -> transition.getSequence() == null
                                        ? Integer.MAX_VALUE
                                        : transition.getSequence())
                                .thenComparing(transition -> transition.getId() == null ? Long.MAX_VALUE : transition.getId()))
                        .map(WorkflowTransitionView::from)
                        .toList();

        return new WorkflowEditorView(
                WorkflowView.from(workflow, Boolean.TRUE.equals(workflow.getIsSystem())),
                versionId,
                versionState,
                editable,
                steps,
                transitions
        );
    }

    private Long resolveVersionId(WorkflowEntity workflow) {
        return workflow.getDraftVersionId() != null
                ? workflow.getDraftVersionId()
                : workflow.getCurrentPublishedVersionId();
    }
}
