/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project.permission.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import serp.project.pmcore.application.project.permission.command.PermissionGrantData;

@Getter
@Setter
public class PermissionGrantRequest {

    @NotBlank
    private String permissionKey;

    @NotBlank
    private String granteeType;

    private String granteeRef;
    private Long customFieldId;

    public PermissionGrantData toData() {
        return new PermissionGrantData(permissionKey, granteeType, granteeRef, customFieldId);
    }
}
