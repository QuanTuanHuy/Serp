package serp.project.pmcore.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkflowSchemeItemEntity extends BaseEntity {
    private Long tenantId;
    private Long schemeId;
    private Long issueTypeId;
    private Long workflowId;
}
