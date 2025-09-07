package serp.project.account.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_organizations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "user_id", "role_id"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class UserOrganizationModel extends BaseModel {
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "description")
    private String description;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;
}
