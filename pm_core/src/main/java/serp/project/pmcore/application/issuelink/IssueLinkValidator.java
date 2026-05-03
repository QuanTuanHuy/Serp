/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink;

import org.springframework.stereotype.Component;

@Component
public class IssueLinkValidator {

    public void validateProjectScopedRequest(Long projectId, Long workItemId, Long tenantId, Long userId) {
        requirePositive(projectId, "projectId");
        requirePositive(workItemId, "workItemId");
        requirePositive(tenantId, "tenantId");
        requirePositive(userId, "userId");
    }

    public void validateCreateRequest(Long projectId,
                                      Long workItemId,
                                      Long targetId,
                                      Long linkTypeId,
                                      Long tenantId,
                                      Long userId) {
        validateProjectScopedRequest(projectId, workItemId, tenantId, userId);
        requirePositive(targetId, "targetId");
        requirePositive(linkTypeId, "linkTypeId");
    }

    public void validateDeleteRequest(Long projectId,
                                      Long workItemId,
                                      Long linkId,
                                      Long tenantId,
                                      Long userId) {
        validateProjectScopedRequest(projectId, workItemId, tenantId, userId);
        requirePositive(linkId, "linkId");
    }

    private void requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive number");
        }
    }
}
