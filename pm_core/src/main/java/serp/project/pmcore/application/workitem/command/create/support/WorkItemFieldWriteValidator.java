/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.constant.WorkItemFieldConstants;
import serp.project.pmcore.application.workitem.command.create.model.CreateWorkItemData;
import serp.project.pmcore.application.workitem.command.create.model.CreateFieldRules;
import serp.project.pmcore.application.workitem.command.create.model.FieldPolicy;
import serp.project.pmcore.domain.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.exception.DomainErrorCode;

import java.util.Map;

@Component
public class WorkItemFieldWriteValidator {

    public void validateClientSuppliedWritableFields(CreateWorkItemData request, CreateFieldRules createFieldRules) {
        validateSystemFieldWritable(WorkItemFieldConstants.DESCRIPTION, request.getDescription(), createFieldRules);
        validateSystemFieldWritable(WorkItemFieldConstants.PRIORITY_ID, request.getPriorityId(), createFieldRules);
        validateSystemFieldWritable(WorkItemFieldConstants.ASSIGNEE_ID, request.getAssigneeId(), createFieldRules);
        validateSystemFieldWritable(WorkItemFieldConstants.PARENT_ID, request.getParentId(), createFieldRules);
        validateSystemFieldWritable(WorkItemFieldConstants.DUE_DATE, request.getDueDate(), createFieldRules);
        validateSystemFieldWritable(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE, request.getTimeOriginalEstimate(), createFieldRules);
        validateSystemFieldWritable(WorkItemFieldConstants.SECURITY_LEVEL_ID, request.getSecurityLevelId(), createFieldRules);

        if (request.getCustomFields() == null || request.getCustomFields().isEmpty()) {
            return;
        }

        validateCustomFiledWritable(request.getCustomFields(), createFieldRules);
    }

    private void validateSystemFieldWritable(String fieldRef, Object value, CreateFieldRules createFieldRules) {
        if (value == null) {
            return;
        }

        FieldPolicy fieldPolicy = createFieldRules.getSystemFieldPolicy(fieldRef);
        if (!isSystemFieldClientWritable(fieldRef, fieldPolicy)) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.FIELD_NOT_WRITABLE_ON_CREATE,
                    "Field is not writable on create: field=" + fieldRef
            );
        }
    }

    private boolean isSystemFieldClientWritable(String fieldRef, FieldPolicy fieldPolicy) {
        if (WorkItemFieldConstants.ALWAYS_WRITABLE_ON_CREATE_SYSTEM_FIELDS.contains(fieldRef)) {
            return true;
        }

        return WorkItemFieldConstants.SUPPORTED_CREATE_SYSTEM_FIELDS.contains(fieldRef)
                && fieldPolicy != null
                && fieldPolicy.isClientWritableOnCreate();
    }

    private void validateCustomFiledWritable(Map<String, Object> customFields, CreateFieldRules createFieldRules) {
        if (customFields == null || customFields.isEmpty()) {
            return;
        }

        for (String customFieldKey : customFields.keySet()) {
            FieldPolicy fieldPolicy = createFieldRules.getCustomFieldPolicy(customFieldKey);
            if (!isCustomFieldClientWritable(fieldPolicy)) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.FIELD_NOT_WRITABLE_ON_CREATE,
                        "Field is not writable on create: field=" + customFieldKey
                );
            }
        }
    }

    private boolean isCustomFieldClientWritable(FieldPolicy fieldPolicy) {
        return fieldPolicy != null && fieldPolicy.isClientWritableOnCreate();
    }
}
