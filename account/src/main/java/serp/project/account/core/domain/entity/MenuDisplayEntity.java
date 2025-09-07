package serp.project.account.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class MenuDisplayEntity extends BaseEntity {
    private String name;
    private String path;
    private String icon;
    private Integer order;
    private Long parentId;
    private Long moduleId;
}
