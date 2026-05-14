/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkActivityRequest {

    @NotEmpty
    private Set<Long> activityIds;

    @NotNull
    private BulkActivityAction action;

    private Long assigneeId;

    public enum BulkActivityAction {
        COMPLETE,
        CANCEL,
        DELETE,
        ASSIGN
    }
}
