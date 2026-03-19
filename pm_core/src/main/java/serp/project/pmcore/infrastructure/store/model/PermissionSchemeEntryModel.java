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
@Table(name = "permission_scheme_entries")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class PermissionSchemeEntryModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "scheme_id", nullable = false)
    private Long schemeId;

    @Column(name = "permission_key", nullable = false)
    private String permissionKey;

    @Column(name = "grantee_type", nullable = false)
    private String granteeType;

    @Column(name = "grantee_ref")
    private String granteeRef;

    @Column(name = "custom_field_id")
    private Long customFieldId;
}
