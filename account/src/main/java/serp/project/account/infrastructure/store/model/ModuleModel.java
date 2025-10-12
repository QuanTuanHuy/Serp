package serp.project.account.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.domain.enums.ModuleType;
import serp.project.account.core.domain.enums.PricingModel;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "keycloak_client_id", nullable = false, unique = true, length = 100)
    private String keycloakClientId;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "module_type")
    @Enumerated(EnumType.STRING)
    private ModuleType moduleType;

    @Column(name = "is_global", nullable = false)
    private Boolean isGlobal;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "is_free")
    private Boolean isFree;

    @Column(name = "pricing_model")
    @Enumerated(EnumType.STRING)
    private PricingModel pricingModel;

    /**
     * List of module IDs that this module depends on
     * Stored as JSON array in database
     */
    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "depends_on_module_ids", columnDefinition = "jsonb")
    private List<Long> dependsOnModuleIds = new ArrayList<>();

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ModuleStatus status;

    @Column(name = "version", length = 20)
    private String version;
}
