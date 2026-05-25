/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.update;

import org.springframework.stereotype.Component;
import serp.project.pmcore.application.workitem.command.update.internal.UpdateWorkItemData;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;

import java.util.Map;

@Component
public class UpdateWorkItemValidator {

    public void validate(UpdateWorkItemCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Update work item command is required");
        }
        if (command.projectId() == null || command.projectId() <= 0) {
            throw new IllegalArgumentException("projectId must be a positive number");
        }
        if (command.workItemId() == null || command.workItemId() <= 0) {
            throw new IllegalArgumentException("workItemId must be a positive number");
        }
        if (command.tenantId() == null || command.tenantId() <= 0) {
            throw new IllegalArgumentException("tenantId must be a positive number");
        }
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be a positive number");
        }

        UpdateWorkItemData data = command.data();
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("At least one field update is required");
        }

        validateNullablePositiveLong(data, WorkItemFieldConstants.PRIORITY_ID);
        validateNullablePositiveLong(data, WorkItemFieldConstants.ASSIGNEE_ID);
        validateNullablePositiveLong(data, WorkItemFieldConstants.SECURITY_LEVEL_ID);
        validateNullableNonNegativeLong(data, WorkItemFieldConstants.START_DATE);
        validateNullableNonNegativeLong(data, WorkItemFieldConstants.DUE_DATE);
        validateNullableNonNegativeLong(data, WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE);
        validateCustomFieldKeys(data.customFields());
    }

    private void validateNullablePositiveLong(UpdateWorkItemData data, String fieldRef) {
        if (!data.hasSystemField(fieldRef)) {
            return;
        }
        Object rawValue = data.getSystemField(fieldRef);
        if (rawValue == null) {
            return;
        }
        if (!(rawValue instanceof Number number) || number.longValue() <= 0) {
            throw new IllegalArgumentException(fieldRef + " must be a positive number when provided");
        }
    }

    private void validateNullableNonNegativeLong(UpdateWorkItemData data, String fieldRef) {
        if (!data.hasSystemField(fieldRef)) {
            return;
        }
        Object rawValue = data.getSystemField(fieldRef);
        if (rawValue == null) {
            return;
        }
        if (!(rawValue instanceof Number number) || number.longValue() < 0) {
            throw new IllegalArgumentException(fieldRef + " must be greater than or equal to 0 when provided");
        }
    }

    private void validateCustomFieldKeys(Map<String, Object> customFields) {
        if (customFields == null || customFields.isEmpty()) {
            return;
        }

        for (String fieldKey : customFields.keySet()) {
            if (fieldKey == null || fieldKey.isBlank()) {
                throw new IllegalArgumentException("customFields keys must be non-blank");
            }
        }
    }
}
