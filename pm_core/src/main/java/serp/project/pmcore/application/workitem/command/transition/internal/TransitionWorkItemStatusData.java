/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.transition.internal;

import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.util.WorkItemFieldUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record TransitionWorkItemStatusData(
        Long transitionId,
        Long resolutionId,
        Map<String, Object> fields
) {
    public TransitionWorkItemStatusData {
        fields = fields == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(fields));
    }

    public Map<String, Object> systemFields() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String fieldRef = canonicalizeFieldRef(entry.getKey());
            if (fieldRef != null && WorkItemFieldConstants.SUPPORTED_TRANSITION_SYSTEM_FIELDS.contains(fieldRef)) {
                result.put(fieldRef, entry.getValue());
            }
        }
        return result;
    }

    public Map<String, Object> customFields() {
        return fields.entrySet().stream()
                .filter(entry -> {
                    String canonical = canonicalizeFieldRef(entry.getKey());
                    return canonical == null
                            || !WorkItemFieldConstants.SUPPORTED_TRANSITION_SYSTEM_FIELDS.contains(canonical);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
    }

    public boolean hasSystemField(String fieldRef) {
        return systemFields().containsKey(fieldRef);
    }

    public Object getSystemField(String fieldRef) {
        return systemFields().get(fieldRef);
    }

    private String canonicalizeFieldRef(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        return WorkItemFieldUtils.normalizeFieldRef(rawValue);
    }
}
