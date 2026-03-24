/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "field_configuration_items")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class FieldConfigItemModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "field_configuration_id", nullable = false)
    private Long fieldConfigId;

    @Column(name = "field_ref_type", nullable = false)
    private String fieldRefType;

    @Column(name = "field_ref", nullable = false)
    private String fieldRef;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "is_hidden")
    private Boolean isHidden;

    @Column(name = "renderer_key")
    private String rendererKey;

    @Column(name = "sequence")
    private Integer sequence;
}
