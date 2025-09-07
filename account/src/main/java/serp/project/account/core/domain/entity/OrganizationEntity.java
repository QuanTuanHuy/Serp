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
public class OrganizationEntity extends BaseEntity {
    private String name;
    private String code;
    private String description;
    private String address;
}
