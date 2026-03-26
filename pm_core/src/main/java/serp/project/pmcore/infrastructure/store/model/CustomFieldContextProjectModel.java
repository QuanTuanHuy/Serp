package serp.project.pmcore.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "custom_field_context_projects")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class CustomFieldContextProjectModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "context_id", nullable = false)
    private Long contextId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;
}
