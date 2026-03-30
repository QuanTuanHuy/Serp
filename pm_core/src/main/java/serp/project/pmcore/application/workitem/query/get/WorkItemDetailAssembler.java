/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.port.IPriorityPort;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.IStatusPort;

@Component
@RequiredArgsConstructor
public class WorkItemDetailAssembler {

    private final IIssueTypePort issueTypePort;
    private final IStatusPort statusPort;
    private final IPriorityPort priorityPort;
    private final IWorkflowStepPort workflowStepPort;

    public WorkItemDetailView toView(WorkItemEntity workItem) {
        Long tenantId = workItem.getTenantId();

        IssueTypeEntity issueType = issueTypePort.getIssueTypeById(workItem.getIssueTypeId(), tenantId)
                .orElseThrow(() -> ResourceNotFoundException.issueType(workItem.getIssueTypeId()));
        StatusEntity status = statusPort.getStatusById(workItem.getStatusId(), tenantId)
                .orElseThrow(() -> ResourceNotFoundException.status(workItem.getStatusId()));
        PriorityEntity priority = priorityPort.getPriorityById(workItem.getPriorityId(), tenantId)
                .orElseThrow(() -> ResourceNotFoundException.priority(workItem.getPriorityId()));
        WorkflowStepEntity workflowStep = workflowStepPort.getWorkflowStepById(workItem.getWorkflowStepId(), tenantId)
                .orElseThrow(() -> ResourceNotFoundException.workflowStep(workItem.getWorkflowStepId()));

        return WorkItemDetailView.from(
                workItem,
                issueType,
                status,
                priority,
                workflowStep
        );
    }
}
