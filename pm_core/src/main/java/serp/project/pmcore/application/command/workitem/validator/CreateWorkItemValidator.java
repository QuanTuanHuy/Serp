/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.command.workitem.validator;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.dto.request.CreateWorkItemRequest;

@Component
public class CreateWorkItemValidator {

    public void validate(CreateWorkItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create work item request is required");
        }

        if (request.getCustomFields() != null && !request.getCustomFields().isEmpty()) {
            throw new IllegalArgumentException("customFields are not supported yet in create work item slice 1+2");
        }
    }
}
