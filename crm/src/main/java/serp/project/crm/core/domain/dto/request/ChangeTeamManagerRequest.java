/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChangeTeamManagerRequest {
    @NotNull(message = "New manager user ID is required")
    private Long newManagerUserId;

    @NotBlank(message = "Previous manager role is required")
    private String previousManagerRole;
}
