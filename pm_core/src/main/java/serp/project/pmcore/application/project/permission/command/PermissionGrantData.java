/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.permission.command;

public record PermissionGrantData(
        String permissionKey,
        String granteeType,
        String granteeRef,
        Long customFieldId
) {
}
