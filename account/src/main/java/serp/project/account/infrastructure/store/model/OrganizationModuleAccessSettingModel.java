/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "organization_module_access_settings", uniqueConstraints = {
        @UniqueConstraint(name = "uq_org_module_access_settings", columnNames = { "organization_id", "module_id" })
}, indexes = {
        @Index(name = "idx_org_module_access_settings_org", columnList = "organization_id"),
        @Index(name = "idx_org_module_access_settings_org_auto_grant",
                columnList = "organization_id, auto_grant_to_new_users")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class OrganizationModuleAccessSettingModel extends BaseModel {

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    @Column(name = "auto_grant_to_new_users", nullable = false)
    private Boolean autoGrantToNewUsers;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
