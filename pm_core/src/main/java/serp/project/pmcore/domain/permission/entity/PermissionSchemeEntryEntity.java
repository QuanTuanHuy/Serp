/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.permission.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PermissionSchemeEntryEntity extends BaseEntity {
    private Long tenantId;
    private Long schemeId;
    private String permissionKey;
    private String granteeType;
    private String granteeRef;
    private Long customFieldId;
}
