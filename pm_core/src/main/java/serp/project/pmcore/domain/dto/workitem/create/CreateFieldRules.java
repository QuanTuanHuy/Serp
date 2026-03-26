/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.dto.workitem.create;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record CreateFieldRules(Map<String, FieldPolicy> systemPolicies,
                               Map<String, FieldPolicy> customPolicies) {

    public CreateFieldRules {
        systemPolicies = Collections.unmodifiableMap(new LinkedHashMap<>(systemPolicies));
        customPolicies = Collections.unmodifiableMap(new LinkedHashMap<>(customPolicies));
    }

    public static CreateFieldRules empty() {
        return new CreateFieldRules(Map.of(), Map.of());
    }

    public FieldPolicy getSystemFieldPolicy(String fieldRef) {
        return systemPolicies.get(fieldRef);
    }

    public FieldPolicy getCustomFieldPolicy(String fieldRef) {
        return customPolicies.get(fieldRef);
    }
}
