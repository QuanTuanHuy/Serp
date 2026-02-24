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
public class WorkItemLinkTypeEntity extends BaseEntity {
    private Long tenantId;
    private String name;
    private String outwardDesc;
    private String inwardDesc;
    private Boolean isSystem;
}
