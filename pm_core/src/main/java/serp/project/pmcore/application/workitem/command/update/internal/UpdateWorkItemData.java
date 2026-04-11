/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.update.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record UpdateWorkItemData(
        Map<String, Object> systemFields,
        Map<String, Object> customFields
) {

    public UpdateWorkItemData {
        systemFields = systemFields == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(systemFields));
        customFields = customFields == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(customFields));
    }

    public boolean hasSystemField(String fieldRef) {
        return systemFields.containsKey(fieldRef);
    }

    public Object getSystemField(String fieldRef) {
        return systemFields.get(fieldRef);
    }

    public boolean isEmpty() {
        return systemFields.isEmpty() && customFields.isEmpty();
    }
}
