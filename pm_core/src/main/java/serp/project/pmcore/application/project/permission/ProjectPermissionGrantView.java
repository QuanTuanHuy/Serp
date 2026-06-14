/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.permission;

import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntryEntity;

public record ProjectPermissionGrantView(
        Long id,
        String permissionKey,
        String granteeType,
        String granteeRef,
        Long customFieldId
) {
    public static ProjectPermissionGrantView from(PermissionSchemeEntryEntity entity) {
        return new ProjectPermissionGrantView(
                entity.getId(),
                entity.getPermissionKey(),
                entity.getGranteeType(),
                entity.getGranteeRef(),
                entity.getCustomFieldId()
        );
    }
}
