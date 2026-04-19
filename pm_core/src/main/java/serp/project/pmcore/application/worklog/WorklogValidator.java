/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog;

import org.springframework.stereotype.Component;

@Component
public class WorklogValidator {

    public void validateProjectScopedRequest(Long projectId, Long workItemId, Long tenantId, Long userId) {
        requirePositive(projectId, "projectId");
        requirePositive(workItemId, "workItemId");
        requirePositive(tenantId, "tenantId");
        requirePositive(userId, "userId");
    }

    public void validateWorklogId(Long worklogId) {
        requirePositive(worklogId, "worklogId");
    }

    public void validateListRequest(Long projectId, Long workItemId, Long tenantId, Long userId) {
        validateProjectScopedRequest(projectId, workItemId, tenantId, userId);
    }

    public void validateTimeEntry(Long timeSpent, Long startDate, String comment) {
        if (timeSpent == null || timeSpent < 60L) {
            throw new IllegalArgumentException("timeSpent must be at least 60 seconds");
        }
        if (startDate == null || startDate <= 0) {
            throw new IllegalArgumentException("startDate must be positive");
        }
        if (startDate > System.currentTimeMillis()) {
            throw new IllegalArgumentException("startDate cannot be in the future");
        }
        if (comment != null && comment.length() > 5000) {
            throw new IllegalArgumentException("comment must not exceed 5000 characters");
        }
    }

    private void requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
}
