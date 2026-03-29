/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkItemData {
    private Long issueTypeId;
    private String summary;
    private String description;
    private Long priorityId;
    private Long assigneeId;
    private Long parentId;
    private Long dueDate;
    private Long timeOriginalEstimate;
    private Long securityLevelId;
    private Map<String, Object> customFields;
}
