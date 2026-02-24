/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkflowEntity extends BaseEntity {
    private Long tenantId;
    private String name;
    private String description;
    private Integer versionNo;
    private Boolean isActive;
    private Boolean isSystem;

    private List<WorkflowStepEntity> steps;
    private List<WorkflowTransitionEntity> transitions;
}
