/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.update.support;

import org.springframework.stereotype.Component;
import serp.project.pmcore.application.workitem.command.update.internal.UpdateWorkItemData;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.util.WorkItemFieldValueUtils;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;

import java.util.Map;

@Component
public class UpdateWorkItemFieldWriteValidator {

    public void validateClientSuppliedWritableFields(UpdateWorkItemData data, WorkItemFieldRules fieldRules) {
        for (Map.Entry<String, Object> entry : data.systemFields().entrySet()) {
            WorkItemFieldPolicy policy = fieldRules.getSystemFieldPolicy(entry.getKey());
            if (!isSystemFieldClientWritable(entry.getKey(), policy)) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.FIELD_NOT_WRITABLE_ON_UPDATE,
                        "Field is not writable on update: field=" + entry.getKey()
                );
            }
            validateSystemFieldValue(entry.getKey(), entry.getValue());
        }

        for (String customFieldKey : data.customFields().keySet()) {
            WorkItemFieldPolicy policy = fieldRules.getCustomFieldPolicy(customFieldKey);
            if (policy == null || !policy.isClientWritable()) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.FIELD_NOT_WRITABLE_ON_UPDATE,
                        "Field is not writable on update: field=" + customFieldKey
                );
            }
        }
    }

    private boolean isSystemFieldClientWritable(String fieldRef, WorkItemFieldPolicy fieldPolicy) {
        if (WorkItemFieldConstants.START_DATE.equals(fieldRef)) {
            return true;
        }
        return WorkItemFieldConstants.SUPPORTED_UPDATE_SYSTEM_FIELDS.contains(fieldRef)
                && fieldPolicy != null
                && fieldPolicy.isClientWritable();
    }

    private void validateSystemFieldValue(String fieldRef, Object rawValue) {
        try {
            switch (fieldRef) {
                case WorkItemFieldConstants.SUMMARY, WorkItemFieldConstants.DESCRIPTION -> WorkItemFieldValueUtils.asNullableString(rawValue);
                case WorkItemFieldConstants.PRIORITY_ID,
                     WorkItemFieldConstants.ASSIGNEE_ID,
                     WorkItemFieldConstants.SECURITY_LEVEL_ID -> WorkItemFieldValueUtils.asNullablePositiveLong(rawValue);
                case WorkItemFieldConstants.START_DATE,
                     WorkItemFieldConstants.DUE_DATE,
                     WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE -> WorkItemFieldValueUtils.asNullableNonNegativeLong(rawValue);
                default -> throw new BusinessRuleViolationException(
                        DomainErrorCode.FIELD_NOT_WRITABLE_ON_UPDATE,
                        "Unsupported update system field: field=" + fieldRef
                );
            }
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.FIELD_NOT_WRITABLE_ON_UPDATE,
                    "Invalid update field value: field=" + fieldRef + ", message=" + ex.getMessage()
            );
        }
    }

}
