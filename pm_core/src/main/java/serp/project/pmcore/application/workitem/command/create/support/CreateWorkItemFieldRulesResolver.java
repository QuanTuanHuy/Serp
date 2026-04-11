/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.screen.service.IScreenService;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;
import serp.project.pmcore.domain.workitem.service.IWorkItemFieldResolver;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreateWorkItemFieldRulesResolver {

    private final IScreenService screenService;
    private final IWorkItemFieldResolver workItemFieldResolver;

    public WorkItemFieldRules resolveCreateFieldRules(ProjectEntity project, Long issueTypeId, Long tenantId) {
        Long createScreenId = screenService.resolveScreenIdForOperation(
                project.getId(),
                project.getIssueTypeScreenSchemeId(),
                issueTypeId,
                WorkItemFieldConstants.CREATE_OPERATION_KEY,
                tenantId
        );

        WorkItemFieldRules baseRules = workItemFieldResolver.resolveFieldRules(
                project.getId(),
                project.getFieldConfigSchemeId(),
                issueTypeId,
                createScreenId,
                tenantId
        );

        return applyCreateOverrides(baseRules);
    }

    private WorkItemFieldRules applyCreateOverrides(WorkItemFieldRules baseRules) {
        Map<String, WorkItemFieldPolicy> systemPolicies = new LinkedHashMap<>(baseRules.systemPolicies());
        Map<String, WorkItemFieldPolicy> customPolicies = new LinkedHashMap<>(baseRules.customPolicies());

        upsertAlwaysWritableSystemField(systemPolicies, WorkItemFieldConstants.ISSUE_TYPE_ID, false);
        upsertAlwaysWritableSystemField(systemPolicies, WorkItemFieldConstants.SUMMARY, true);

        return new WorkItemFieldRules(systemPolicies, customPolicies);
    }

    private void upsertAlwaysWritableSystemField(Map<String, WorkItemFieldPolicy> systemPolicies,
                                                 String fieldRef,
                                                 boolean forceRequired) {
        WorkItemFieldPolicy existingPolicy = systemPolicies.get(fieldRef);
        boolean required = forceRequired || (existingPolicy != null && existingPolicy.required());

        systemPolicies.put(fieldRef, new WorkItemFieldPolicy(
                WorkItemFieldConstants.FIELD_REF_TYPE_SYSTEM,
                fieldRef,
                required,
                false,
                true
        ));
    }
}
