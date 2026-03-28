/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.command.workitem.validator;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.dto.workitem.create.CreateWorkItemData;

import java.util.Map;

@Component
public class CreateWorkItemValidator {

    public void validate(CreateWorkItemData request) {
        if (request == null) {
            throw new IllegalArgumentException("Create work item request is required");
        }

        if (request.getCustomFields() == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : request.getCustomFields().entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                throw new IllegalArgumentException("customFields keys must be non-blank");
            }
        }
    }
}
