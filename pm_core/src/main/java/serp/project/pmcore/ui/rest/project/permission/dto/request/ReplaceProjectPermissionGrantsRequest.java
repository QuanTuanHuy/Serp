/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project.permission.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import serp.project.pmcore.application.project.permission.command.PermissionGrantData;

import java.util.List;

@Getter
@Setter
public class ReplaceProjectPermissionGrantsRequest {

    @NotNull
    @Valid
    private List<PermissionGrantRequest> grants;

    public List<PermissionGrantData> toGrantData() {
        if (grants == null) {
            return List.of();
        }
        return grants.stream().map(PermissionGrantRequest::toData).toList();
    }
}
