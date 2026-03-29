/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class CreateWorkItemValidator {

    public void validate(CreateWorkItemCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Create work item request is required");
        }

        if (command.customFields() == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : command.customFields().entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                throw new IllegalArgumentException("customFields keys must be non-blank");
            }
        }
    }
}
