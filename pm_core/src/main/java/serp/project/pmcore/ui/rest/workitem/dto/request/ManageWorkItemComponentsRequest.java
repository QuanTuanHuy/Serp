/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workitem.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Getter
@NoArgsConstructor
public class ManageWorkItemComponentsRequest {

    @NotEmpty
    private List<@NotNull Long> componentIds = List.of();

    public void setComponentIds(List<Long> componentIds) {
        if (componentIds == null) {
            this.componentIds = List.of();
            return;
        }
        this.componentIds = new ArrayList<>(new LinkedHashSet<>(componentIds));
    }
}
