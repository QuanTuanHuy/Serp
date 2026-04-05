/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support;

import org.springframework.stereotype.Component;

import serp.project.pmcore.application.workitem.command.create.internal.CreateWorkItemData;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;

import java.util.Map;

@Component
public class WorkItemFieldWriteValidator {

    public void validateClientSuppliedWritableFields(CreateWorkItemData request, WorkItemFieldRules fieldRules) {
        validateSystemFieldWritable(WorkItemFieldConstants.DESCRIPTION, request.getDescription(), fieldRules);
        validateSystemFieldWritable(WorkItemFieldConstants.PRIORITY_ID, request.getPriorityId(), fieldRules);
        validateSystemFieldWritable(WorkItemFieldConstants.ASSIGNEE_ID, request.getAssigneeId(), fieldRules);
        validateSystemFieldWritable(WorkItemFieldConstants.PARENT_ID, request.getParentId(), fieldRules);
        validateSystemFieldWritable(WorkItemFieldConstants.DUE_DATE, request.getDueDate(), fieldRules);
        validateSystemFieldWritable(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE, request.getTimeOriginalEstimate(), fieldRules);
        validateSystemFieldWritable(WorkItemFieldConstants.SECURITY_LEVEL_ID, request.getSecurityLevelId(), fieldRules);

        if (request.getCustomFields() == null || request.getCustomFields().isEmpty()) {
            return;
        }

        validateCustomFiledWritable(request.getCustomFields(), fieldRules);
    }

    private void validateSystemFieldWritable(String fieldRef, Object value, WorkItemFieldRules fieldRules) {
        if (value == null) {
            return;
        }

        WorkItemFieldPolicy fieldPolicy = fieldRules.getSystemFieldPolicy(fieldRef);
        if (!isSystemFieldClientWritable(fieldRef, fieldPolicy)) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.FIELD_NOT_WRITABLE_ON_CREATE,
                    "Field is not writable on create: field=" + fieldRef
            );
        }
    }

    private boolean isSystemFieldClientWritable(String fieldRef, WorkItemFieldPolicy fieldPolicy) {
        if (WorkItemFieldConstants.ALWAYS_WRITABLE_ON_CREATE_SYSTEM_FIELDS.contains(fieldRef)) {
            return true;
        }

        return WorkItemFieldConstants.SUPPORTED_CREATE_SYSTEM_FIELDS.contains(fieldRef)
                && fieldPolicy != null
                && fieldPolicy.isClientWritable();
    }

    private void validateCustomFiledWritable(Map<String, Object> customFields, WorkItemFieldRules fieldRules) {
        if (customFields == null || customFields.isEmpty()) {
            return;
        }

        for (String customFieldKey : customFields.keySet()) {
            WorkItemFieldPolicy fieldPolicy = fieldRules.getCustomFieldPolicy(customFieldKey);
            if (!isCustomFieldClientWritable(fieldPolicy)) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.FIELD_NOT_WRITABLE_ON_CREATE,
                        "Field is not writable on create: field=" + customFieldKey
                );
            }
        }
    }

    private boolean isCustomFieldClientWritable(WorkItemFieldPolicy fieldPolicy) {
        return fieldPolicy != null && fieldPolicy.isClientWritable();
    }
}
