/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.assign;

import org.springframework.stereotype.Component;

@Component
public class AssignWorkItemValidator {

    public void validate(AssignWorkItemCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Assign work item command is required");
        }
        if (command.projectId() == null || command.projectId() <= 0) {
            throw new IllegalArgumentException("projectId must be a positive number");
        }
        if (command.workItemId() == null || command.workItemId() <= 0) {
            throw new IllegalArgumentException("workItemId must be a positive number");
        }
        if (command.assigneeId() != null && command.assigneeId() <= 0) {
            throw new IllegalArgumentException("assigneeId must be a positive number when provided");
        }
        if (command.tenantId() == null || command.tenantId() <= 0) {
            throw new IllegalArgumentException("tenantId must be a positive number");
        }
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be a positive number");
        }
    }
}
