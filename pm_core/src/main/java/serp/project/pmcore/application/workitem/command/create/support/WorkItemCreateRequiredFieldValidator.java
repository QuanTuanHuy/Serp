/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support;

import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.application.workitem.command.create.model.CreateWorkItemData;
import serp.project.pmcore.application.workitem.command.create.model.CreateFieldRules;
import serp.project.pmcore.application.workitem.command.create.model.FieldPolicy;
import serp.project.pmcore.application.workitem.command.create.model.ResolvedCustomFields;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class WorkItemCreateRequiredFieldValidator {

    public void validate(CreateWorkItemData request,
                         Long priorityId,
                         Long assigneeId,
                         Long securityLevelId,
                         CreateFieldRules createFieldRules,
                         ResolvedCustomFields resolvedCustomFields) {
        List<String> missingFields = new ArrayList<>();

        Map<String, Object> effectiveSystemValues = new LinkedHashMap<>();
        effectiveSystemValues.put(WorkItemFieldConstants.ISSUE_TYPE_ID, request.getIssueTypeId());
        effectiveSystemValues.put(WorkItemFieldConstants.SUMMARY, request.getSummary());
        effectiveSystemValues.put(WorkItemFieldConstants.DESCRIPTION, request.getDescription());
        effectiveSystemValues.put(WorkItemFieldConstants.PRIORITY_ID, priorityId);
        effectiveSystemValues.put(WorkItemFieldConstants.ASSIGNEE_ID, assigneeId);
        effectiveSystemValues.put(WorkItemFieldConstants.PARENT_ID, request.getParentId());
        effectiveSystemValues.put(WorkItemFieldConstants.DUE_DATE, request.getDueDate());
        effectiveSystemValues.put(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE, request.getTimeOriginalEstimate());
        effectiveSystemValues.put(WorkItemFieldConstants.SECURITY_LEVEL_ID, securityLevelId);

        for (FieldPolicy systemPolicy : createFieldRules.systemPolicies().values()) {
            if (!systemPolicy.required()
                    || !WorkItemFieldConstants.SUPPORTED_CREATE_SYSTEM_FIELDS.contains(systemPolicy.fieldRef())) {
                continue;
            }

            if (isMissingValue(effectiveSystemValues.get(systemPolicy.fieldRef()))) {
                missingFields.add(systemPolicy.fieldRef());
            }
        }

        missingFields.addAll(resolvedCustomFields.missingFields());

        if (!missingFields.isEmpty()) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.REQUIRED_FIELDS_MISSING,
                    "Required fields are missing: " + String.join(", ", missingFields)
            );
        }
    }

    private boolean isMissingValue(Object value) {
        if (value == null) {
            return true;
        }

        return value instanceof String text && text.isBlank();
    }
}
