/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record WorkItemFieldRules(Map<String, WorkItemFieldPolicy> systemPolicies,
                                 Map<String, WorkItemFieldPolicy> customPolicies) {

    public WorkItemFieldRules {
        systemPolicies = Collections.unmodifiableMap(new LinkedHashMap<>(systemPolicies));
        customPolicies = Collections.unmodifiableMap(new LinkedHashMap<>(customPolicies));
    }

    public static WorkItemFieldRules empty() {
        return new WorkItemFieldRules(Map.of(), Map.of());
    }

    public WorkItemFieldPolicy getSystemFieldPolicy(String fieldRef) {
        return systemPolicies.get(fieldRef);
    }

    public WorkItemFieldPolicy getCustomFieldPolicy(String fieldRef) {
        return customPolicies.get(fieldRef);
    }
}