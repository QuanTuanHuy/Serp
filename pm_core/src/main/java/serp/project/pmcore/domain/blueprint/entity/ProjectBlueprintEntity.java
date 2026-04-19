/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.blueprint.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.constant.TenantConstants;
import serp.project.pmcore.domain.shared.entity.BaseEntity;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProjectBlueprintEntity extends BaseEntity {
    private Long tenantId;
    private String name;
    private String description;
    private String typeKey;
    private String avatarUrl;
    private Boolean isSystem;
    private Long deletedAt;

    public boolean isSystem() {
        return Objects.equals(tenantId, TenantConstants.SYSTEM_TENANT_ID) && Boolean.TRUE.equals(isSystem);
    }
}
