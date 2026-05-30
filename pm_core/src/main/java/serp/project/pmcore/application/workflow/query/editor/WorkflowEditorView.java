/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.query.editor;

import serp.project.pmcore.application.workflow.WorkflowStepView;
import serp.project.pmcore.application.workflow.WorkflowTransitionView;
import serp.project.pmcore.application.workflow.WorkflowView;
import serp.project.pmcore.domain.shared.enums.WorkflowVersionState;

import java.util.List;

public record WorkflowEditorView(
        WorkflowView workflow,
        Long versionId,
        WorkflowVersionState versionState,
        boolean editable,
        List<WorkflowStepView> steps,
        List<WorkflowTransitionView> transitions
) {
}
