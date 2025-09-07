package serp.project.account.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "modules")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ModuleModel extends BaseModel {
    @Column(name = "module_name", nullable = false, unique = true, length = 100)
    private String moduleName;

    @Column(name = "description")
    private String description;

    @Column(name = "keycloak_client_id", nullable = false, unique = true, length = 100)
    private String keycloakClientId;
}
