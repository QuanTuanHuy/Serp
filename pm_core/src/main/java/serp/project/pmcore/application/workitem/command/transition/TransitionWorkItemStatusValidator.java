/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.transition;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;

import java.util.Map;

@Component
public class TransitionWorkItemStatusValidator {
    public void validate(TransitionWorkItemStatusCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Transition work item command is required");
        }
        if (command.projectId() == null || command.projectId() <= 0) {
            throw new IllegalArgumentException("projectId must be a positive number");
        }
        if (command.workItemId() == null || command.workItemId() <= 0) {
            throw new IllegalArgumentException("workItemId must be a positive number");
        }
        if (command.transitionId() == null || command.transitionId() <= 0) {
            throw new IllegalArgumentException("transitionId must be a positive number");
        }
        if (command.resolutionId() != null && command.resolutionId() <= 0) {
            throw new IllegalArgumentException("resolutionId must be a positive number when provided");
        }
        if (command.tenantId() == null || command.userId() == null) {
            throw new IllegalArgumentException("tenantId and userId are required");
        }
        if (command.fields() == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : command.fields().entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                throw new IllegalArgumentException("fields keys must be non-blank");
            }
            if (WorkItemFieldConstants.RESOLUTION_ID.equalsIgnoreCase(entry.getKey().trim())) {
                throw new IllegalArgumentException("resolution_id must be provided by top-level resolutionId, not inside fields");
            }
        }
    }
}
