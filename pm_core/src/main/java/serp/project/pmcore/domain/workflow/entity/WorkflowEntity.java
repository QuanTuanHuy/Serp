/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.entity.BaseEntity;
import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkflowEntity extends BaseEntity {
    private Long tenantId;
    private String workflowKey;
    private String name;
    private String description;
    private Long currentPublishedVersionId;
    private Long draftVersionId;
    private WorkflowLifecycleState lifecycleState;
    private Boolean isSystem;

    private List<WorkflowVersionEntity> versions;

    public void publish(Long publishedVersionId) {
        this.currentPublishedVersionId = publishedVersionId;
        this.draftVersionId = null;
        this.lifecycleState = WorkflowLifecycleState.ACTIVE;
    }
}
