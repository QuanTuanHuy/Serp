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
import serp.project.pmcore.domain.shared.enums.WorkflowVersionState;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkflowVersionEntity extends BaseEntity {
    private Long tenantId;
    private Long workflowId;
    private Integer versionNo;
    private WorkflowVersionState versionState;
    private Long baseVersionId;
    private Long publishedAt;
    private Long publishedBy;

    public boolean isActive() {
        return WorkflowVersionState.PUBLISHED.equals(versionState);
    }

    public void archive() {
        this.versionState = WorkflowVersionState.ARCHIVED;
    }

    public void publish(Long publishedBy, Long publishedAt) {
        this.versionState = WorkflowVersionState.PUBLISHED;
        this.publishedBy = publishedBy;
        this.publishedAt = publishedAt;
    }
}
