/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workitem.command.create.model.CreateWorkItemData;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public record CreateWorkItemCommand(
        Long projectId,
        Long issueTypeId,
        String summary,
        String description,
        Long priorityId,
        Long assigneeId,
        Long parentId,
        Long dueDate,
        Long timeOriginalEstimate,
        Long securityLevelId,
        Map<String, Object> customFields,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<CreateWorkItemResult> {

    public CreateWorkItemCommand {
        customFields = customFields == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(customFields));
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }

    public CreateWorkItemData toCreateWorkItemData() {
        return CreateWorkItemData.builder()
                .issueTypeId(issueTypeId)
                .summary(summary)
                .description(description)
                .priorityId(priorityId)
                .assigneeId(assigneeId)
                .parentId(parentId)
                .dueDate(dueDate)
                .timeOriginalEstimate(timeOriginalEstimate)
                .securityLevelId(securityLevelId)
                .customFields(customFields)
                .build();
    }
}
